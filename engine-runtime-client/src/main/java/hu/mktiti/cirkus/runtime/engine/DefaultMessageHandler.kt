package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.api.LogTarget
import hu.mktiti.cirkus.runtime.base.MessageConverter
import hu.mktiti.cirkus.runtime.common.*
import hu.mktiti.kreator.api.inject
import java.lang.Exception
import java.util.*

class DefaultMessageHandler(
        private val inQueue: InQueue,
        private val outQueue: OutQueue,
        private val messageConverter: MessageConverter = inject()
) : MessageHandler {

    private fun sendMessage(message: Message) {
        outQueue.addMessage(messageConverter.toDto(message))
    }

    override fun loadActorBinary(): ByteArray? {
        sendMessage(Message(ActorJar))
        return with(inQueue.getMessage()) {
            when {
                header !is ActorJar -> {
                    log(LogTarget.SELF, "Actor Jar Expected, received ${header::class.simpleName}")
                    sendMessage(Message(ErrorResult("Actor Jar Expected, received ${header::class.simpleName}")))
                    null
                }
                dataMessage == null -> {
                    log(LogTarget.SELF, "Actor Jar contained no data part")
                    sendMessage(Message(ErrorResult("Actor Jar contained no data part")))
                    null
                }
                else -> Base64.getDecoder().decode(dataMessage)
            }
        }
    }

    override fun waitForStart(): Boolean  = inQueue.getMessage().header is StartNotice

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