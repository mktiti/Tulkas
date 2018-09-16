package hu.mktiti.cirkus.runtime.common

import hu.mktiti.kreator.InjectableType
import hu.mktiti.kreator.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

@InjectableType
interface Channel {

    fun sendMessage(message: MessageDto)

    fun waitForMessage(): MessageDto

    fun shutdown()

}

class SocketChannel(
        private val socket: Socket,
        private val messageSerializer: MessageSerializer = inject()
) : Channel {

    private val outputWriter: PrintWriter = PrintWriter(socket.getOutputStream())
    private val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    override fun sendMessage(message: MessageDto) {
        val messageString = messageSerializer.serializeMessageDto(message)

        println("Message sent:\n-------------")
        println(messageString)
        println("-------------")

        outputWriter.println(messageString)
        outputWriter.flush()
    }

    override fun waitForMessage(): MessageDto {
        val line = bufferedReader.readLine() ?: throw RuntimeException("Cannot read from socket")

        println("Message received:\n-------------")
        println(line)
        println("-------------")

        return messageSerializer.deserializeMessageDto(line)
    }

    override fun shutdown() {
        socket.close()
    }

}