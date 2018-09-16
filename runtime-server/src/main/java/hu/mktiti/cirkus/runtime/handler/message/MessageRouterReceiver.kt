package hu.mktiti.cirkus.runtime.handler.message

import hu.mktiti.cirkus.runtime.common.*
import hu.mktiti.cirkus.runtime.handler.control.*
import hu.mktiti.cirkus.runtime.handler.log.LogRouter
import java.io.IOException

class MessageRouterReceiver(
        private val clientHandler: ClientMessageHandler,
        private val actor: Actor,
        private val logRouter: LogRouter,
        private val controlQueue: ControlQueue
) : Runnable {

    private fun onMessage(messageDto: MessageDto): Boolean {
        val header = messageDto.header
        when (header) {
            is LogEntry -> logRouter.onLogEntry(header)

            is ProxyCall   -> controlQueue.addMessage(ProxyCallMessage(actor, header, messageDto.dataMessage))
            is CallResult  -> controlQueue.addMessage(CallResultMessage(actor, header, messageDto.dataMessage))
            is MatchResult -> controlQueue.addMessage(GameResultMessage(actor, messageDto.dataMessage))
            is ErrorResult -> controlQueue.addMessage(ErrorResultMessage(actor, messageDto.dataMessage))
            is ActorJar    -> controlQueue.addMessage(ActorBinaryRequest(actor))

            else -> return true
        }
        return false
    }

    override fun run() {
        try {
            while (true) {
                val messageDto = clientHandler.waitForMessage()

                if (onMessage(messageDto)) {
                    System.exit(0)
                    return
                }
            }
        } catch (ise: IllegalStateException) {
            ise.printStackTrace()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        }
    }

}