package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.runtime.base.*
import hu.mktiti.kreator.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.Exception
import java.net.Socket

class EngineClientSocketChannel(
        socket: Socket,
        private val messageHelper: MessageHelper = inject()
) : EngineClientChannel {

    private val outputWriter: PrintWriter = PrintWriter(socket.getOutputStream())
    private val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private fun sendMessage(message: Message) {
        val messageString = messageHelper.serializeMessage(message)
        outputWriter.print(messageString)
        outputWriter.flush()
    }

    override fun callFunction(target: CallTarget, methodName: String, params: List<Any?>): Any? {
        sendMessage(Message(ProxyCall(target), Call(methodName, params)))

        val line = bufferedReader.readLine() ?: throw RuntimeException("Cannot read from socket")
        val message = messageHelper.deserializeMessage(line)
        val header = message.header
        if (header is CallResult && header.method == methodName) {
            return message.data
        } else {
            throw BotException("Not result is returned")
        }
    }

    override fun log(target: LogTarget, message: String) =
        sendMessage(Message(LogEntry(target), message))

    override fun sendResult(result: GameResult) =
        sendMessage(Message(MatchResult(result)))

    override fun reportError(exception: Exception) =
        sendMessage(Message(ErrorResult("${exception.javaClass.name} thrown, message: ${exception.message}")))

}