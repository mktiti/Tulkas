package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.runtime.base.Client
import hu.mktiti.cirkus.runtime.base.ClientRuntime
import hu.mktiti.cirkus.runtime.base.RuntimeClientHelper
import hu.mktiti.cirkus.runtime.common.Call
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.kreator.Injectable
import hu.mktiti.kreator.inject

@Injectable
class BotClient(
        private val clientHelper: RuntimeClientHelper = inject(),
        private val botClientHelper: BotClientHelper = inject()
) : Client {

    override fun runClient(arguments: Map<String, String>, inQueue: InQueue, outQueue: OutQueue) {
        val messageHandler: MessageHandler = DefaultMessageHandler(inQueue, outQueue)

        messageHandler.sendActorBinaryRequest()

        try {
            val botInterface: Class<out BotInterface> = clientHelper.searchForBotInterface() ?: return
            val bot: BotInterface = botClientHelper.searchAndCreateBotImplementation(botInterface) ?: return
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
    ClientRuntime().run(arrayOf("port", "12346"))
}