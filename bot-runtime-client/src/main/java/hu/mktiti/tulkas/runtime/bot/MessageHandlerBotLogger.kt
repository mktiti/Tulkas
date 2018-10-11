package hu.mktiti.tulkas.runtime.bot

import hu.mktiti.tulkas.api.GameBotLogger

class MessageHandlerBotLogger(
        private val messageHandler: BotMessageHandler
) : GameBotLogger {

    override fun log(message: String?) {
        messageHandler.log(message ?: return)
    }

}