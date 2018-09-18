package hu.mktiti.cirkus.runtime.base

import hu.mktiti.cirkus.runtime.common.Channel
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.cirkus.runtime.common.SocketChannel
import hu.mktiti.kreator.api.inject
import java.net.Socket
import kotlin.concurrent.thread

class ClientRuntime(
        private val client: Client = inject()
) {

    private val defaultPort = 12345
    private val defaultAddress = "localhost"

    private val clientPrefix = "client."

    fun run(args: Array<String>) {
        if (args.size % 2 != 0) {
            println("Run with arguments: [{property_key} {property_value}]")
            return
        }

        val paramMap = args.toList().chunked(2).map { l -> l[0] to l[1] }.toMap()

        val port = paramMap["port"]?.toIntOrNull() ?: defaultPort
        val address = paramMap["address"] ?: defaultAddress

        val socket = Socket(address, port)
        val channel: Channel = SocketChannel(socket)

        val inQueue = InQueue()
        val outQueue = OutQueue()

        val receiver = Receiver(channel, inQueue)
        val sender = Sender(channel, outQueue)

        thread(start = true) { receiver.run() }
        thread(start = true) { sender.run() }

        val clientParams = paramMap.filterKeys { it.startsWith(clientPrefix) }.mapKeys { it.key.substring(clientPrefix.length) }
        thread(start = true) { client.runClient(clientParams, inQueue, outQueue) }

    }

}

fun main(args: Array<String>) {
    ClientRuntime().run(args)
}