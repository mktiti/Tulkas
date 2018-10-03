package hu.mktiti.tulkas.runtime.handler.message

import hu.mktiti.tulkas.runtime.common.*
import hu.mktiti.tulkas.runtime.handler.control.*
import hu.mktiti.tulkas.runtime.handler.log.LogRouter

class MessageRouterReceiver(
        private val clientHandler: ClientMessageHandler,
        private val actor: Actor,
        private val logRouter: LogRouter,
        private val controlQueue: ControlQueue
) : Runnable {

    private fun onMessage(messageDto: MessageDto): Boolean {
        val header = messageDto.header
        when (header) {
            is LogEntry -> logRouter.onLogEntry(header)

            is ProxyCall -> controlQueue.addMessage(ProxyCallMessage(actor, header, messageDto.dataMessage))
            is CallResult -> controlQueue.addMessage(CallResultMessage(actor, header, messageDto.dataMessage))
            is MatchResult -> controlQueue.addMessage(GameResultMessage(actor, messageDto.dataMessage))
            is ErrorResult -> controlQueue.addMessage(ErrorResultMessage(actor, messageDto.dataMessage))
            is ActorJar -> controlQueue.addMessage(ActorBinaryRequest(actor))

            is ShutdownNotice, is StartNotice -> return true
        }
        return false
    }

    override fun run() {
        forever {
            val messageDto = clientHandler.waitForMessage()

            if (messageDto == null || onMessage(messageDto)) {
                return
            }
        }
    }

}