package hu.mktiti.cirkus.runtime.handler.log

import hu.mktiti.cirkus.api.LogTarget
import hu.mktiti.cirkus.api.LogTarget.*
import hu.mktiti.cirkus.runtime.common.LogEntry
import hu.mktiti.cirkus.runtime.handler.ActorsData

private data class LogMappingEntry(val selector: List<LogTarget>, val queue: LogQueue, val sender: LogSender)

class EngineLogRouter(
        engineQueue: LogQueue,
        botAQueue: LogQueue,
        botBQueue: LogQueue
) : LogRouter {

    constructor(data: ActorsData<LogQueue>) : this(data.engine, data.botA, data.botB)

    private companion object {
        private val engineSelectors = listOf(SELF, ALL)
        private val botASelectors = listOf(BOT_A, BOTS, ALL)
        private val botBSelectors = listOf(BOT_B, BOTS, ALL)
    }

    private val selectorMap = setOf(
            LogMappingEntry(engineSelectors, engineQueue, LogSender.SELF),
            LogMappingEntry(botASelectors, botAQueue, LogSender.ENGINE),
            LogMappingEntry(botBSelectors, botBQueue, LogSender.ENGINE)
    )

    override fun onLogEntry(logEntry: LogEntry) {
        selectorMap
                .filter { logEntry.target in it.selector }
                .forEach { it.queue.addEntry(ActorLogEntry(it.sender, logEntry.message)) }
    }

}