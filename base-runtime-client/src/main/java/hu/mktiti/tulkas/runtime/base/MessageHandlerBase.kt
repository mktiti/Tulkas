package hu.mktiti.tulkas.runtime.base

import hu.mktiti.tulkas.api.log.LogTarget
import hu.mktiti.tulkas.runtime.common.*
import java.lang.Exception
import java.util.*

abstract class MessageHandlerBase(
        protected val inQueue: InQueue,
        private val outQueue: OutQueue,
        protected val messageConverter: MessageConverter
) : MessageHandler {

    protected val log by logger()

    protected fun sendMessage(message: Message) {
        outQueue.addMessage(messageConverter.toDto(message))
    }

    override fun loadActorBinary(): ByteArray? {
        sendMessage(Message(ActorJar))
        return with(inQueue.getMessage()) {
            when {
                header !is ActorJar -> {
                    log.error("BotActor Jar Expected, received {}", header::class.simpleName)
                    sendMessage(Message(ErrorResult("BotActor Jar Expected, received ${header::class.simpleName}")))
                    null
                }
                dataMessage == null -> {
                    log.error("BotActor Jar contained no actordata part")
                    sendMessage(Message(ErrorResult("BotActor Jar contained no actordata part")))
                    null
                }
                else -> Base64.getDecoder().decode(dataMessage)
            }
        }
    }

    override fun log(message: String) = log(LogTarget.SELF, message)

    override fun log(target: LogTarget, message: String) =
            sendMessage(Message(LogEntry(target, message)))

    override fun reportError(exception: Exception) =
            sendMessage(Message(ErrorResult("${exception.javaClass.name} thrown, message: ${exception.message}")))

}