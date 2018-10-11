package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.tulkas.api.GameResult
import hu.mktiti.tulkas.api.exception.BotTimeoutException
import hu.mktiti.tulkas.runtime.base.MessageConverter
import hu.mktiti.tulkas.runtime.base.MessageHandlerBase
import hu.mktiti.tulkas.runtime.common.*

class DefaultEngineMessageHandler(
        inQueue: InQueue,
        outQueue: OutQueue,
        messageConverter: MessageConverter
) : MessageHandlerBase(
        inQueue, outQueue, messageConverter
), EngineMessageHandler {

    override fun waitForStart(): Boolean  = inQueue.getMessage().header is StartNotice

    override fun callFunction(target: CallTarget, methodName: String, params: List<Any?>): Any? {
        sendMessage(Message(ProxyCall(target), Call(methodName, params)))

        val message = messageConverter.fromDto(inQueue.getMessage())
        val header = message.header
        if (header is CallResult && header.method == methodName) {
            return message.data
        } else if (header is BotTimeout) {
            throw BotTimeoutException()
        } else {
            throw BotException("Not result is returned")
        }
    }

    override fun sendResult(result: GameResult) {
        val header = when (result) {
            is hu.mktiti.tulkas.api.challenge.ChallengeResult -> ChallengeResultH(result.points, result.points)
            is hu.mktiti.tulkas.api.match.MatchResult -> MatchResultH(result.type)
            else -> throw IllegalArgumentException("Result type should be either MatchResultH or ChallengeResultH")
        }
        sendMessage(Message(header))
    }
}