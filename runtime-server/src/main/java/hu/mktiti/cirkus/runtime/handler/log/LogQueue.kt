package hu.mktiti.cirkus.runtime.handler.log

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.ArrayList

enum class LogSender {
    RUNTIME, ENGINE, SELF
}

data class ActorLogEntry(val sender: LogSender, val message: String)

class LogQueue {

    private val queue: Queue<ActorLogEntry> = ConcurrentLinkedQueue()

    private val isClosed = AtomicBoolean(false)

    fun addEntry(logEntry: ActorLogEntry) {
        if (!isClosed.get()) {
            queue.add(logEntry)
        }
    }

    fun getAll(): List<ActorLogEntry> {
        close()
        return ArrayList(queue)
    }

    fun close() {
        isClosed.set(true)
    }

}