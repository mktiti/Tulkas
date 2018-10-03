package hu.mktiti.tulkas.runtime.base

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.property.property
import hu.mktiti.kreator.property.propertyOpt
import hu.mktiti.tulkas.runtime.common.*
import kotlin.concurrent.thread

class ClientRuntime(
        private val client: Client = inject(),
        private val threadPrefix: String = property("THREAD_PREFIX", "")
) {

    private val log by logger()

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
                log.error("Error while running client", t)
                outQueue.addMessage(MessageDto(ErrorResult(t.message
                        ?: "")))
            }
        }
    }

}

fun setUpLogger(
        logFile: String = property("LOG_PATH"),
        clientPrefix: String? = propertyOpt("THREAD_PREFIX")
) {
    val fullPrefix = clientPrefix?.let { "$it - " } ?: ""

    with(FileAppender<ILoggingEvent>()) {
        context = UnifiedLogFactory.getContext()
        name = "Client log file appender"
        file = logFile

        encoder = PatternLayoutEncoder().apply {
            context = this@with.context
            pattern = "$fullPrefix[%date] %thread %logger %level %msg %n at: %method %ex{full}%n"
            start()
        }

        start()

        UnifiedLogFactory.addAppender(this)
    }
}

fun main(args: Array<String>) {
    setUpLogger()
    ClientRuntime().run()
}