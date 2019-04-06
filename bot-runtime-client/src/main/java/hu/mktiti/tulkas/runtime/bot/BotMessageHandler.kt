package hu.mktiti.tulkas.runtime.bot

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.base.MessageHandler
import hu.mktiti.tulkas.runtime.common.Call
import java.lang.Exception

@InjectableType
interface BotMessageHandler : MessageHandler {

    fun reportBotError(exception: Exception)

    fun sendResponse(method: String, data: Any?)

    fun waitForCall(): Call?

    fun waitForBot(): ByteArray?

}