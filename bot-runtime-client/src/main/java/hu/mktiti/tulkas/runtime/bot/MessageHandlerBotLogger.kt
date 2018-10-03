package hu.mktiti.tulkas.runtime.bot

import hu.mktiti.tulkas.api.GameBotLogger

class MessageHandlerBotLogger(
        private val messageHandler: MessageHandler
) : GameBotLogger {

    override fun log(message: String?) {
        messageHandler.log(message ?: return)
    }

}