package hu.mktiti.cirkus.runtime.base

import java.io.Serializable

enum class LogTarget : Serializable {
    SELF, ENGINE, BOT_A, BOT_B, ALL
}

enum class MessageType : Serializable {
    CALL, LOG, RESULT, GAME_RESULT
}

enum class CallTarget : Serializable {
    BOT_A, BOT_B
}

data class Message(val type: MessageType, val data: Any?) : Serializable

data class LogEntry(val target: LogTarget, val message: String)

data class Call(val method: String, val params: List<Any?>) : Serializable

data class ProxyCall(val target: CallTarget, val call: Call) : Serializable