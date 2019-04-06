package hu.mktiti.tulkas.runtime.bot

import hu.mktiti.tulkas.runtime.base.MessageConverter
import hu.mktiti.tulkas.runtime.base.MessageHandlerBase
import hu.mktiti.tulkas.runtime.common.*
import java.util.*

class DefaultBotMessageHandler(
        inQueue: InQueue,
        outQueue: OutQueue,
        messageConverter: MessageConverter
) : MessageHandlerBase(
        inQueue, outQueue, messageConverter
), BotMessageHandler {

    override fun sendResponse(method: String, data: Any?) = sendMessage(Message(CallResult(method), data))

    override fun waitForCall(): Call? {
        val message = messageConverter.fromDto(inQueue.getMessage())
        if (message.header !is ProxyCall) throw BotException("Call expected")
        return message.data as? Call
                ?: throw BotException("Call expected")
    }

    override fun waitForBot(): ByteArray? {
        val message = messageConverter.fromDto(inQueue.getMessage())
        if (message.header !is ActorJar) throw BotException("Bot jar expected")
        val base64String = message.data as? String ?: throw BotException("Base 64 binary actordata expected")
        return Base64.getDecoder().decode(base64String)
    }

    override fun reportBotError(exception: Exception) =
        sendMessage(Message(ErrorResult("${exception.javaClass.name} thrown, message: ${exception.message}")))

}