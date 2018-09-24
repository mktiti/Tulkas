package hu.mktiti.cirkus.runtime.common

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

abstract class MessageQueue {

    protected val queue: BlockingQueue<MessageDto> = LinkedBlockingQueue<MessageDto>()

    abstract fun addMessage(message: MessageDto)

    fun getMessage(): MessageDto = queue.take()

}

class InQueue : MessageQueue() {

    override fun addMessage(message: MessageDto) {
        // Non-blocking
        queue.add(message)
    }

}

class OutQueue : MessageQueue() {

    override fun addMessage(message: MessageDto) {
        // Blocking
        queue.put(message)
    }

}