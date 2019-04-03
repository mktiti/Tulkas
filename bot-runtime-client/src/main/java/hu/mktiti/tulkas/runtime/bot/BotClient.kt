package hu.mktiti.tulkas.runtime.bot

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.property.intPropertyOpt
import hu.mktiti.tulkas.api.BotInterface
import hu.mktiti.tulkas.api.BotLoggerFactory
import hu.mktiti.tulkas.runtime.base.*
import hu.mktiti.tulkas.runtime.common.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Injectable
class BotClient(
        private val proxyCallTimeout: Int = intPropertyOpt("PROXY_CALL_TIMEOUT") ?: 10 // Secs
) : Client {

    private val log by logger()

    override fun runClient(
            inQueue: InQueue,
            outQueue: OutQueue,
            messageConverter: MessageConverter,
            binaryClassLoader: BinaryClassLoader,
            clientHelper: RuntimeClientHelper
    ) {

        val botClientHelper: BotClientHelper = DefaultBotClientHelper(binaryClassLoader)

        val messageHandler: BotMessageHandler = DefaultBotMessageHandler(inQueue, outQueue, messageConverter)
        BotLoggerFactory.setDefaultLogger(MessageHandlerBotLogger(messageHandler))

        val apiBinary = messageHandler.loadActorBinary(ActorBinType.API)
        if (apiBinary == null) {
            log.error("Game api binary is null")
            log.error("Shutting down")
            return
        }

        val actorBinary = messageHandler.loadActorBinary(ActorBinType.ACTOR)
        if (actorBinary == null) {
            log.error("Bot actor binary is null")
            log.error("Shutting down")
            return
        }

        binaryClassLoader.loadFromBinary(apiBinary)
        binaryClassLoader.loadFromBinary(actorBinary)

        val proxyCallExecutor = Executors.newSingleThreadExecutor()

        try {
            val botInterface: Class<out BotInterface> = clientHelper.searchForBotInterface() ?: return
            val bot: BotInterface = botClientHelper.searchAndCreateBotImplementation(botInterface) ?: return
            val proxy: BotProxy = botClientHelper.createProxyForBot(botInterface, bot)

            while (true) {
                val call: Call = messageHandler.waitForCall() ?: break
                log.info("Proxy call received: method={}, params={}", call.method, call.params)

                val responseFuture = proxyCallExecutor.submit(Callable {
                    proxy.callMethod(call.method, call.params)
                })

                try {

                    val response = responseFuture.get(proxyCallTimeout.toLong(), TimeUnit.SECONDS)
                    messageHandler.sendResponse(call.method, response)

                } catch (te: TimeoutException) {
                    log.info("Bot proxy call timeout", te)
                    messageHandler.reportBotError(ProxyCallTimeoutException(call.method))
                }
            }
        } catch (e: Exception) {
            log.error("Error occured", e)
            messageHandler.reportBotError(e)
        }

        proxyCallExecutor.shutdownNow()

    }

}

fun main(args: Array<String>) {
    ClientRuntime(BotClient()).run()
}