package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.runtime.base.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class BotClientSocketChannel(
        socket: Socket
) : BotClientChannel {

    private val objectOutStream = ObjectOutputStream(socket.getOutputStream())
    private val objectInStream = ObjectInputStream(socket.getInputStream())

    private fun sendMessage(message: Message) {
        objectOutStream.writeObject(message)
    }

    override fun sendResponse(data: Any?) = sendMessage(Message(MessageType.RESULT, data))

    override fun log(message: String) = sendMessage(Message(MessageType.LOG, LogEntry(LogTarget.SELF, message)))

    override fun waitForCall(): Call? = objectInStream.readObject() as? Call

}