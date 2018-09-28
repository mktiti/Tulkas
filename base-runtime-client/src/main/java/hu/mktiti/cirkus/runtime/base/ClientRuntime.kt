package hu.mktiti.cirkus.runtime.base

import hu.mktiti.cirkus.runtime.common.*
import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.property.property
import kotlin.concurrent.thread

class ClientRuntime(
        private val client: Client = inject(),
        private val threadPrefix: String = property("THREAD_PREFIX", "")
) {

    fun run(channel: Channel = inject()) {
        val inQueue = InQueue()
        val outQueue = OutQueue()

        thread(name = "$threadPrefix Client Receiver", start = true) {
            Receiver(channel, inQueue).run()
            System.exit(0)
        }

        thread(name = "$threadPrefix Client Sender", start = true, isDaemon = true) {
            Sender(channel, outQueue).run()
            inQueue.addMessage(MessageDto(ShutdownNotice))
            System.exit(0)
        }

        thread(name = "$threadPrefix Client", start = true, isDaemon = true) {
            try {
                client.runClient(inQueue, outQueue)
            } catch (t: Throwable) {
                println("Throwable while running client: $t")
                outQueue.addMessage(MessageDto(ErrorResult(t.message ?: "")))
            }
        }
    }

}

fun main(args: Array<String>) {
    ClientRuntime().run()
}