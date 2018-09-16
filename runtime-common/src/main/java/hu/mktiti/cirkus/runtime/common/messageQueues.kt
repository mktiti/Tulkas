package hu.mktiti.cirkus.runtime.common

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

abstract class MessageQueue {

    protected val queue: BlockingQueue<MessageDto> = LinkedBlockingQueue<MessageDto>()

    abstract fun addMessage(message: MessageDto)

    fun getMessage(): MessageDto = queue.take()

}

/**
 * Should never have more than 3 messages
 * Bot client => 1 Actor jar, 1 Proxy call, 1 Shutdown notice
 * Engine client => (1 Proxy call response / 1 Actor binary), 1 Match init, 1 Shutdown notice
 */
class InQueue : MessageQueue(){

    override fun addMessage(message: MessageDto) {
        // Non-blocking (Exception on limit reach)
        queue.add(message)
    }

}

class OutQueue : MessageQueue() {

    override fun addMessage(message: MessageDto) {
        // Blocking
        queue.put(message)
    }

}