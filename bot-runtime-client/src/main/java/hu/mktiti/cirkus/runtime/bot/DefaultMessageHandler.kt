package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.runtime.base.MessageConverter
import hu.mktiti.cirkus.runtime.common.*
import hu.mktiti.kreator.api.inject
import java.util.*

class DefaultMessageHandler(
        private val inQueue: InQueue,
        private val outQueue: OutQueue,
        private val messageConverter: MessageConverter = inject()
) : MessageHandler {

    private val log by logger()

    private fun sendMessage(message: Message) {
        outQueue.addMessage(messageConverter.toDto(message))
    }

    override fun loadActorBinary(): ByteArray? {
        sendMessage(Message(ActorJar))
        return with(inQueue.getMessage()) {
            when {
                header !is ActorJar -> {
                    log.error("Actor Jar Expected, received {}", header::class.simpleName)
                    sendMessage(Message(ErrorResult("Actor Jar Expected, received ${header::class.simpleName}")))
                    null
                }
                dataMessage == null -> {
                    log.error("Actor Jar contained no data part")
                    sendMessage(Message(ErrorResult("Actor Jar contained no data part")))
                    null
                }
                else -> Base64.getDecoder().decode(dataMessage)
            }
        }
    }

    override fun sendResponse(method: String, data: Any?) = sendMessage(Message(CallResult(method), data))

    override fun log(message: String) = sendMessage(selfLogMessage(message))

    override fun waitForCall(): Call? {
        val message = messageConverter.fromDto(inQueue.getMessage())
        if (message.header !is ProxyCall) throw BotException("Call expected")
        return message.data as? Call
                ?: throw BotException("Call expected")
    }

    override fun waitForBot(): ByteArray? {
        val message = messageConverter.fromDto(inQueue.getMessage())
        if (message.header !is ActorJar) throw BotException("Bot jar expected")
        val base64String = message.data as? String ?: throw BotException("Base 64 binary data expected")
        return Base64.getDecoder().decode(base64String)
    }

    override fun reportBotError(exception: Exception) =
        sendMessage(Message(ErrorResult("${exception.javaClass.name} thrown, message: ${exception.message}")))

}