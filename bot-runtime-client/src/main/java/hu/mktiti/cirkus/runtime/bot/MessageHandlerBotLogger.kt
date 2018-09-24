package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.api.GameBotLogger

class MessageHandlerBotLogger(
        private val messageHandler: MessageHandler
) : GameBotLogger {

    override fun log(message: String?) {
        messageHandler.log(message ?: return)
    }

}