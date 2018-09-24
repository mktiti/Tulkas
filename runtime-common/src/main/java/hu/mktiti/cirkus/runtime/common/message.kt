package hu.mktiti.cirkus.runtime.common

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.api.LogTarget
import java.io.Serializable

enum class CallTarget {
    BOT_A, BOT_B
}

data class Message(val header: Header, val data: Any? = null)

data class MessageDto(val header: Header, val dataMessage: String? = null)

// Headers
sealed class Header

data class ProxyCall(val target: CallTarget) : Header()

data class LogEntry(val target: LogTarget, val message: String) : Header()

data class CallResult(val method: String) : Header()

data class MatchResult(val result: GameResult) : Header()

data class ErrorResult(val message: String) : Header()

object ActorJar : Header()

object ShutdownNotice : Header()

object StartNotice : Header()


// Data
data class Call(val method: String, val params: List<Any?>) : Serializable