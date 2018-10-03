package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.tulkas.api.GameEngineLogger
import hu.mktiti.tulkas.api.LogTarget

class MessageHandlerEngineLogger(
        private val messageHandler: MessageHandler
) : GameEngineLogger {

    override fun logFor(logTarget: LogTarget?, message: String?) {
        messageHandler.log(logTarget ?: LogTarget.SELF, message ?: return)
    }

}