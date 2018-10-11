package hu.mktiti.tulkas.runtime.base

import hu.mktiti.tulkas.runtime.common.*

class Receiver(
        private val channel: Channel,
        private val inQueue: InQueue
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
        private val channel: Channel,
        private val outQueue: OutQueue
) : Runnable {

    private val log by logger()

    override fun run() {
        forever {
            val message = outQueue.getMessage()
            log.info("Out message: {}", message.header)

            if (!channel.sendMessage(message)) {
                return
            }
        }
    }

}