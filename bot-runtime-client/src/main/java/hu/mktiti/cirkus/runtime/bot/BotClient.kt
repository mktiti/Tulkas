package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.api.BotLoggerFactory
import hu.mktiti.cirkus.runtime.base.BinaryClassLoader
import hu.mktiti.cirkus.runtime.base.Client
import hu.mktiti.cirkus.runtime.base.ClientRuntime
import hu.mktiti.cirkus.runtime.base.RuntimeClientHelper
import hu.mktiti.cirkus.runtime.common.Call
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.cirkus.runtime.common.logger
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject

@Injectable
class BotClient(
        private val clientHelper: RuntimeClientHelper = inject(),
        private val botClientHelper: BotClientHelper = inject(),
        private val binaryClassLoader: BinaryClassLoader = inject()
) : Client {

    private val log by logger()

    override fun runClient(inQueue: InQueue, outQueue: OutQueue) {

        val messageHandler: MessageHandler = DefaultMessageHandler(inQueue, outQueue)
        BotLoggerFactory.setDefaultLogger(MessageHandlerBotLogger(messageHandler))

        val actorBinary = messageHandler.loadActorBinary()
        if (actorBinary == null) {
            log.error("Actor binary is null")
            log.error("Shutting down")
            return
        }

        binaryClassLoader.loadFromBinary(actorBinary)

        try {
            val botInterface: Class<out BotInterface> = clientHelper.searchForBotInterface() ?: return
            val bot: BotInterface = botClientHelper.searchAndCreateBotImplementation(botInterface) ?: return
            val proxy: BotProxy = botClientHelper.createProxyForBot(botInterface, bot)

            while (true) {
                val call: Call = messageHandler.waitForCall() ?: break
                log.info("Proxy call received: method={}, params={}", call.method, call.params)
                val response = proxy.callMethod(call.method, call.params)
                messageHandler.sendResponse(call.method, response)
            }
        } catch (e: Exception) {
            messageHandler.reportBotError(e)
        }

    }

}

fun main(args: Array<String>) {
    ClientRuntime().run()
}