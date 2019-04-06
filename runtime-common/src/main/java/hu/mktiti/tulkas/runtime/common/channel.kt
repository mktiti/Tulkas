package hu.mktiti.tulkas.runtime.common

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.property.intProperty
import hu.mktiti.kreator.property.property
import hu.mktiti.tulkas.runtime.common.serialization.MessageDeserializer
import hu.mktiti.tulkas.runtime.common.serialization.MessageSerializer
import java.io.*
import java.net.Socket
import java.nio.charset.StandardCharsets

@InjectableType
interface Channel {

    fun sendMessage(message: MessageDto): Boolean

    fun waitForMessage(): MessageDto?

    fun shutdown()

}

fun connect(host: String = property("SOCKET_HOST"), port: Int = intProperty("SOCKET_PORT")) = Socket(host, port)

@Injectable
class SocketChannel(
        private val socket: Socket = connect(),
        private val messageSerializer: MessageSerializer = inject(),
        private val messageDeserializer: MessageDeserializer = inject()
) : Channel {

    private val log by logger()

    private val outputWriter: PrintWriter = PrintWriter(OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
    private val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))

    @Throws(MessageException::class)
    override fun sendMessage(message: MessageDto): Boolean = try {
        val messageString = messageSerializer.serializeMessageDto(message)

        log.info("Sending message: {}", messageString)

        outputWriter.println(messageString)
        outputWriter.flush()
        true
    } catch (_: IOException) {
        false
    } catch (_: MessageException) {
        false
    }

    override fun waitForMessage(): MessageDto? = try {
        messageDeserializer.readMessageDto(bufferedReader)
    } catch (me: MessageException) {
        log.info("Error while reading message (maybe closed channel)")
        null
    }

    override fun shutdown() {
        if (!socket.isClosed) {
            try {
                socket.close()
            } catch (ioe: IOException) {}
        }
    }

}