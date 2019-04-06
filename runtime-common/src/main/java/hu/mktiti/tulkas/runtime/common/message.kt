package hu.mktiti.tulkas.runtime.common

import hu.mktiti.tulkas.api.log.LogTarget
import hu.mktiti.tulkas.api.match.MatchResult
import java.io.Serializable

enum class CallTarget {
    BOT_A, BOT_B
}

enum class ActorBinType {
    API, ACTOR
}

data class Message(val header: Header, val data: Any? = null)

data class MessageDto(val header: Header, val dataMessage: String? = null)

// Headers
sealed class Header

data class ProxyCall(val target: CallTarget) : Header()

data class LogEntry(val target: LogTarget, val message: String) : Header()

data class CallResult(val method: String) : Header()

data class ChallengeResultH(val points: Long?, val maxPoints: Long?) : Header()

data class MatchResultH(val resultType: MatchResult.ResultType) : Header()

object BotTimeout : Header()

data class ErrorResult(val message: String) : Header()

data class ActorJar(val type: ActorBinType) : Header()

object ShutdownNotice : Header()

object StartNotice : Header()

// Data
data class Call(val method: String, val params: List<Any?>) : Serializable

// Helper
fun selfLogMessage(message: String) = Message(LogEntry(LogTarget.SELF, message))