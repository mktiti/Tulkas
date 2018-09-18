package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.GameEngineLogger
import hu.mktiti.cirkus.api.LogTarget

class MessageHandlerEngineLogger(
        private val messageHandler: MessageHandler
) : GameEngineLogger {

    override fun logFor(logTarget: LogTarget?, message: String?) {
        messageHandler.log(logTarget ?: LogTarget.SELF, message ?: return)
    }

}