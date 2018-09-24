package hu.mktiti.cirkus.runtime.handler.message

import hu.mktiti.cirkus.runtime.common.ActorJar
import hu.mktiti.cirkus.runtime.common.Channel
import hu.mktiti.cirkus.runtime.common.MessageDto
import hu.mktiti.cirkus.runtime.common.ShutdownNotice

open class AbstractClientMessageHandler(
        private val channel: Channel
) : ClientMessageHandler {

    protected fun sendMessage(message: MessageDto): Boolean = channel.sendMessage(message)

    override fun sendActorBinary(actor: ByteArray): Boolean = sendMessage(MessageDto(ActorJar))

    override fun sendGameOverNotice() {
        sendMessage(MessageDto(ShutdownNotice))
        channel.shutdown()
    }

    override fun waitForMessage(): MessageDto? = channel.waitForMessage()

}