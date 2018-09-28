package hu.mktiti.cirkus.runtime.base

import hu.mktiti.cirkus.runtime.common.Channel
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.cirkus.runtime.common.ShutdownNotice
import hu.mktiti.cirkus.runtime.common.util.forever
import hu.mktiti.kreator.api.inject

class Receiver(
        private val channel: Channel = inject(),
        private val inQueue: InQueue = inject()
) : Runnable {

    override fun run() {
        forever {
            val messageDto = channel.waitForMessage()

            if (messageDto == null || messageDto.header is ShutdownNotice) {
                return
            } else {
                inQueue.addMessage(messageDto)
            }
        }
    }

}

class Sender(
        private val channel: Channel = inject(),
        private val outQueue: OutQueue = inject()
) : Runnable {

    override fun run() {
        while (true) {
            val message = outQueue.getMessage()
            //println("Out message: ${message.header}")

            if (!channel.sendMessage(message)) {
                return
            }
        }
    }

}