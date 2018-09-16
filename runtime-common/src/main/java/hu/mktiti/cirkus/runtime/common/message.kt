package hu.mktiti.cirkus.runtime.common

import com.fasterxml.jackson.annotation.JsonTypeInfo
import hu.mktiti.cirkus.api.GameResult
import java.io.Serializable

enum class LogTarget : Serializable {
    SELF, BOT_A, BOT_B, BOTS, ALL
}

enum class CallTarget : Serializable {
    BOT_A, BOT_B
}

data class Message(val header: Header, val data: Any? = null) : Serializable

class MessageDto(val header: Header, val dataMessage: String?) : Serializable

// Headers

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY
)
sealed class Header : Serializable

data class ProxyCall(val target: CallTarget) : Header(), Serializable

data class LogEntry(val target: LogTarget, val message: String) : Header(), Serializable

data class CallResult(val method: String) : Header(), Serializable

data class MatchResult(val result: GameResult) : Header(), Serializable

data class ErrorResult(val message: String) : Header(), Serializable

object ActorJar : Header(), Serializable

object ShutdownNotice : Header(), Serializable

object StartNotice : Header(), Serializable

// Data

data class Call(val method: String, val params: List<Any?>) : Serializable