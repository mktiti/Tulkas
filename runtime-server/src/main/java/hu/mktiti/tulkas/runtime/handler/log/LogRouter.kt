package hu.mktiti.tulkas.runtime.handler.log

import hu.mktiti.tulkas.api.log.LogTarget.*
import hu.mktiti.tulkas.runtime.common.LogEntry

interface LogRouter {

    fun onLogEntry(logEntry: LogEntry)

}

private val validBotLogTargets = listOf(SELF, BOTS, ALL)

fun botLogRouter(
        selfQueue: LogQueue
): LogRouter = object : LogRouter {

    override fun onLogEntry(logEntry: LogEntry) {
        if (logEntry.target in validBotLogTargets) {
            selfQueue.addEntry(ActorLogEntry(LogSender.SELF, logEntry.message))
        }
    }

}