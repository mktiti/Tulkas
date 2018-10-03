package hu.mktiti.tulkas.runtime.handler.message

import hu.mktiti.tulkas.runtime.common.ActorJar
import hu.mktiti.tulkas.runtime.common.Channel
import hu.mktiti.tulkas.runtime.common.MessageDto
import hu.mktiti.tulkas.runtime.common.ShutdownNotice
import java.util.*

open class AbstractClientMessageHandler(
        private val channel: Channel
) : ClientMessageHandler {

    protected fun sendMessage(message: MessageDto): Boolean = channel.sendMessage(message)

    override fun sendActorBinary(actor: ByteArray): Boolean
            = sendMessage(MessageDto(ActorJar, Base64.getEncoder().encodeToString(actor)))

    override fun sendGameOverNotice() {
        sendMessage(MessageDto(ShutdownNotice))
        channel.shutdown()
    }

    override fun waitForMessage(): MessageDto? = channel.waitForMessage()

}