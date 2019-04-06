package hu.mktiti.tulkas.runtime.handler.message

import hu.mktiti.tulkas.runtime.common.*
import java.util.*

open class AbstractClientMessageHandler(
        private val channel: Channel
) : ClientMessageHandler {

    protected fun sendMessage(message: MessageDto): Boolean = channel.sendMessage(message)

    override fun sendActorBinary(type: ActorBinType, actor: ByteArray): Boolean
            = sendMessage(MessageDto(ActorJar(type), Base64.getEncoder().encodeToString(actor)))

    override fun sendGameOverNotice() {
        sendMessage(MessageDto(ShutdownNotice))
        channel.shutdown()
    }

    override fun waitForMessage(): MessageDto? = channel.waitForMessage()

}