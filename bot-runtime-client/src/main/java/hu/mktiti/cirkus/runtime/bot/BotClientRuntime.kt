package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.runtime.base.Call
import hu.mktiti.cirkus.runtime.base.RuntimeClient
import hu.mktiti.cirkus.runtime.base.RuntimeClientHelper
import hu.mktiti.kreator.Injectable
import hu.mktiti.kreator.inject
import java.net.Socket

@Injectable
class BotClientRuntime(
        private val clientHelper: RuntimeClientHelper = inject(),
        private val botClientHelper: BotClientHelper = inject()
) : RuntimeClient {

    private val defaultPort = 12345

    override fun runClient(arguments: Map<String, String>) {
        val port = arguments["port"]?.toIntOrNull() ?: defaultPort

        val socket = Socket("localhost", port)
        val channel: BotClientChannel = BotClientSocketChannel(socket)

        try {
            val botInterface: Class<out BotInterface> = clientHelper.searchForBotInterface() ?: return
            val bot: BotInterface = botClientHelper.searchAndCreateBotImplementation(botInterface) ?: return
            val proxy: BotProxy = botClientHelper.createProxyForBot(botInterface, bot)

            while (true) {
                val call: Call = channel.waitForCall() ?: break
                val response = proxy.callMethod(call.method, call.params)
                channel.sendResponse(call.method, response)
            }
        } catch (e: Exception) {
            channel.reportBotError(e)
        }

    }

}

fun main(args: Array<String>) {
    BotClientRuntime().runClient(mapOf())
}