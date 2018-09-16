package hu.mktiti.cirkus.runtime.base

import hu.mktiti.cirkus.runtime.common.Channel
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.cirkus.runtime.common.ShutdownNotice
import java.io.IOException

class Receiver(
        private val channel: Channel,
        private val inQueue: InQueue
) : Runnable {

    override fun run() {
        try {
            while (true) {
                val messageDto = channel.waitForMessage()

                if (messageDto.header is ShutdownNotice) {
                    System.exit(0)
                    return
                } else {
                    inQueue.addMessage(messageDto)
                }
            }
        } catch (ise: IllegalStateException) {
            ise.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

}

class Sender(
        private val channel: Channel,
        private val outQueue: OutQueue
) : Runnable {

    override fun run() {
        try {
            while (true) {
                channel.sendMessage(outQueue.getMessage())
            }
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

}