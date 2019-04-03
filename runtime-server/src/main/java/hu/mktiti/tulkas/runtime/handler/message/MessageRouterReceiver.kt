package hu.mktiti.tulkas.runtime.handler.message

import hu.mktiti.tulkas.api.challenge.ChallengeResult
import hu.mktiti.tulkas.api.match.MatchResult
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
            is LogEntry    -> logRouter.onLogEntry(header)

            is ProxyCall   -> controlQueue.addMessage(ProxyCallMessage(actor, header, messageDto.dataMessage))
            is CallResult  -> controlQueue.addMessage(CallResultMessage(actor, header, messageDto.dataMessage))
            is ChallengeResultH -> controlQueue.addMessage(ChallengeResultMessage(actor, ChallengeResult(header.points, header.maxPoints)))
            is MatchResultH -> controlQueue.addMessage(MatchResultMessage(actor, MatchResult(header.resultType)))
            is BotTimeout  -> controlQueue.addMessage(BotTimeoutMessage(actor))
            is ErrorResult -> controlQueue.addMessage(ErrorResultMessage(actor, messageDto.dataMessage))
            is ActorJar    -> controlQueue.addMessage(ActorBinaryRequest(actor, header.type))

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