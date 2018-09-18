package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.api.LogTarget
import hu.mktiti.cirkus.runtime.common.*
import hu.mktiti.kreator.api.inject
import java.lang.Exception

class DefaultMessageHandler(
        private val inQueue: InQueue,
        private val outQueue: OutQueue,
        private val messageConverter: MessageConverter = inject()
) : MessageHandler {

    private fun sendMessage(message: Message) {
        outQueue.addMessage(messageConverter.toDto(message))
    }

    override fun sendActorBinaryRequest() {
        sendMessage(Message(ActorJar))
        val message = inQueue.getMessage()
        println("message response to actor jar: $message")
    }

    override fun callFunction(target: CallTarget, methodName: String, params: List<Any?>): Any? {
        sendMessage(Message(ProxyCall(target), Call(methodName, params)))

        val message = messageConverter.fromDto(inQueue.getMessage())
        val header = message.header
        if (header is CallResult && header.method == methodName) {
            return message.data
        } else {
            throw BotException("Not result is returned")
        }
    }

    override fun log(target: LogTarget, message: String) =
        sendMessage(Message(LogEntry(target, message)))

    override fun sendResult(result: GameResult) =
        sendMessage(Message(MatchResult(result)))

    override fun reportError(exception: Exception) =
        sendMessage(Message(ErrorResult("${exception.javaClass.name} thrown, message: ${exception.message}")))

}