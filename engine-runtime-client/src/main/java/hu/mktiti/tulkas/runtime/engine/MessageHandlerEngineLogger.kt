package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.tulkas.api.log.GameEngineLogger
import hu.mktiti.tulkas.api.log.LogTarget

class MessageHandlerEngineLogger(
        private val messageHandler: EngineMessageHandler
) : GameEngineLogger {

    override fun logFor(logTarget: LogTarget?, message: String?) {
        messageHandler.log(logTarget ?: LogTarget.SELF, message ?: return)
    }

}