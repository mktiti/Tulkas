package hu.mktiti.cirkus.runtime.handler.control

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class ControlQueue {

    private val queue: BlockingQueue<ControlMessage> = LinkedBlockingQueue()

    fun addMessage(message: ControlMessage) {
        queue.put(message)
    }

    fun getMessage(): ControlMessage = queue.take()

}