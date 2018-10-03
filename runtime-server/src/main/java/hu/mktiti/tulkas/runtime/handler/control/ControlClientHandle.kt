package hu.mktiti.tulkas.runtime.handler.control

import hu.mktiti.tulkas.runtime.handler.message.BotMessageHandler
import hu.mktiti.tulkas.runtime.handler.message.ClientMessageHandler
import hu.mktiti.tulkas.runtime.handler.message.EngineMessageHandler

sealed class ControlClientHandle(
        val binary: ByteArray
) {
    abstract val messageHandler: ClientMessageHandler
}

class EngineControlHandle(
        binary: ByteArray,
        override val messageHandler: EngineMessageHandler
) : ControlClientHandle(binary)

class BotControlHandle(
        binary: ByteArray,
        override val messageHandler: BotMessageHandler
) : ControlClientHandle(binary)