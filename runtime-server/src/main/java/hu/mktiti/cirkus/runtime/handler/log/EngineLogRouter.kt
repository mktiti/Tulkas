package hu.mktiti.cirkus.runtime.handler.log

import hu.mktiti.cirkus.api.LogTarget
import hu.mktiti.cirkus.api.LogTarget.*
import hu.mktiti.cirkus.runtime.common.LogEntry
import hu.mktiti.cirkus.runtime.handler.actordata.UnifiedActorsData
import hu.mktiti.cirkus.runtime.handler.actordata.UnifiedSinglePlayerData
import hu.mktiti.cirkus.runtime.handler.actordata.UnifiedTwoPlayerData

private data class LogMappingEntry(val selector: List<LogTarget>, val queue: LogQueue, val sender: LogSender)

class EngineLogRouter(
        logQueues: UnifiedActorsData<LogQueue>
) : LogRouter {

    private companion object {
        private val engineSelectors = listOf(SELF,  ALL)
        private val botASelectors =   listOf(BOT_A, BOTS, ALL)
        private val botBSelectors =   listOf(BOT_B, BOTS, ALL)
    }

    private val selectorMap: Collection<LogMappingEntry>

    init {
        selectorMap = mutableSetOf(LogMappingEntry(engineSelectors, logQueues.engine, LogSender.SELF)).apply {
            when (logQueues) {
                is UnifiedTwoPlayerData<LogQueue> -> {
                    add(LogMappingEntry(botASelectors, logQueues.botA, LogSender.ENGINE))
                    add(LogMappingEntry(botBSelectors, logQueues.botB, LogSender.ENGINE))
                }
                is UnifiedSinglePlayerData<LogQueue> -> {
                    add(LogMappingEntry(botASelectors, logQueues.bot, LogSender.ENGINE))
                }
            }
        }
    }

    override fun onLogEntry(logEntry: LogEntry) {
        selectorMap
                .filter { logEntry.target in it.selector }
                .forEach { it.queue.addEntry(ActorLogEntry(it.sender, logEntry.message)) }
    }

}