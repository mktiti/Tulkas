package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.runtime.base.*
import hu.mktiti.kreator.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class BotClientSocketChannel(
        socket: Socket,
        private val messageHelper: MessageHelper = inject()
) : BotClientChannel {

    private val outputWriter: PrintWriter = PrintWriter(socket.getOutputStream())
    private val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private fun sendMessage(message: Message) {
        val messageString = messageHelper.serializeMessage(message)
        outputWriter.print(messageString)
        outputWriter.flush()
    }

    override fun sendResponse(method: String, data: Any?) = sendMessage(Message(CallResult(method), data))

    override fun log(message: String) = sendMessage(Message(LogEntry(LogTarget.SELF), message))

    override fun waitForCall(): Call? {
        val line = bufferedReader.readLine() ?: throw RuntimeException("Cannot read from socket")
        val message = messageHelper.deserializeMessage(line)
        if (message.header !is ProxyCall) throw BotException("Call expected")
        return message.data as? Call ?: throw BotException("Call expected")
    }

    override fun reportBotError(exception: Exception) =
        sendMessage(Message(ErrorResult("${exception.javaClass.name} thrown, message: ${exception.message}")))

}