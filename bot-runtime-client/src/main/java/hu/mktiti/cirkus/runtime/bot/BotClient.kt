package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.api.GameBotLogger
import hu.mktiti.cirkus.runtime.base.Client
import hu.mktiti.cirkus.runtime.base.ClientRuntime
import hu.mktiti.cirkus.runtime.base.RuntimeClientHelper
import hu.mktiti.cirkus.runtime.common.Call
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject

@Injectable
class BotClient(
        private val clientHelper: RuntimeClientHelper = inject(),
        private val botClientHelper: BotClientHelper = inject()
) : Client {

    override fun runClient(inQueue: InQueue, outQueue: OutQueue) {
        val messageHandler: MessageHandler = DefaultMessageHandler(inQueue, outQueue)

        messageHandler.sendActorBinaryRequest()
        val botLogger: GameBotLogger = MessageHandlerBotLogger(messageHandler)

        try {
            val botInterface: Class<out BotInterface> = clientHelper.searchForBotInterface() ?: return
            val bot: BotInterface = botClientHelper.searchAndCreateBotImplementation(botInterface, botLogger) ?: return
            val proxy: BotProxy = botClientHelper.createProxyForBot(botInterface, bot)

            while (true) {
                val call: Call = messageHandler.waitForCall() ?: break
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