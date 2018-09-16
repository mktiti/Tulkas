package hu.mktiti.cirkus.runtime.handler.log

import hu.mktiti.cirkus.runtime.common.LogEntry
import hu.mktiti.cirkus.runtime.common.LogTarget

interface LogRouter {

    fun onLogEntry(logEntry: LogEntry)

}

private val validBotLogTargets = listOf(LogTarget.SELF, LogTarget.BOTS, LogTarget.ALL)

fun botLogRouter(
        selfQueue: LogQueue
): LogRouter = object : LogRouter {

    override fun onLogEntry(logEntry: LogEntry) {
        if (logEntry.target in validBotLogTargets) {
            selfQueue.addEntry(ActorLogEntry(LogSender.SELF, logEntry.message))
        }
    }

}