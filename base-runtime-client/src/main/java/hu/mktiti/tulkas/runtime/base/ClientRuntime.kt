package hu.mktiti.tulkas.runtime.base

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import hu.mktiti.kreator.property.property
import hu.mktiti.kreator.property.propertyOpt
import hu.mktiti.tulkas.runtime.common.*
import hu.mktiti.tulkas.runtime.common.serialization.MessageDeserializer
import hu.mktiti.tulkas.runtime.common.serialization.MessageSerializer
import hu.mktiti.tulkas.runtime.common.serialization.SafeMessageDeserializer
import hu.mktiti.tulkas.runtime.common.serialization.SafeMessageSerializer

class ClientRuntime(
        private val client: Client,
        private val threadPrefix: String = property("THREAD_PREFIX", "")
) {

    private val log by logger()

    fun run() {
        setUpLogger()

        val serializer: MessageSerializer = SafeMessageSerializer()
        val deserializer: MessageDeserializer = SafeMessageDeserializer()
        val channel: Channel = SocketChannel(connect(), serializer, deserializer)

        val inQueue = InQueue()
        val outQueue = OutQueue()

        val binaryClassLoader = BinaryClassLoader()

        //System.setSecurityManager(ClientSecurityManager())

        langThread(name = "$threadPrefix Client Receiver", start = true) {
            Receiver(channel, inQueue).run()
            System.exit(0)
        }

        langThread(name = "$threadPrefix Client Sender", start = true, isDaemon = true) {
            Sender(channel, outQueue).run()
            inQueue.addMessage(MessageDto(ShutdownNotice))
            System.exit(0)
        }

        langThread(name = "$threadPrefix Client", start = true, isDaemon = true) {
            try {
                client.runClient(
                            inQueue,
                            outQueue,
                            SerializationMessageConverter(binaryClassLoader),
                            binaryClassLoader,
                            DefaultRuntimeClientHelper(binaryClassLoader)
                        )
            } catch (t: Throwable) {
                log.error("Error while running client", t)
                outQueue.addMessage(MessageDto(ErrorResult(t.message ?: "")))
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