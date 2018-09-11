package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.runtime.base.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class EngineClientSocketChannel(
        socket: Socket
) : EngineClientChannel {

    private val objectOutStream = ObjectOutputStream(socket.getOutputStream())
    private val objectInStream = ObjectInputStream(socket.getInputStream())

    private fun sendMessage(message: Message) {
        objectOutStream.writeObject(message)
    }

    override fun callFunction(target: CallTarget, methodName: String, params: List<Any?>): Any? {
        sendMessage(Message(MessageType.CALL, ProxyCall(target, Call(methodName, params))))
        return objectInStream.readObject()
    }

    override fun log(target: LogTarget, message: String) =
        sendMessage(Message(MessageType.LOG, LogEntry(target, message)))

    override fun sendResult(result: GameResult) =
        sendMessage(Message(MessageType.RESULT, result))
}