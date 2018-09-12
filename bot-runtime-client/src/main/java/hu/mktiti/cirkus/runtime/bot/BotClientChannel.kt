package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.runtime.base.Call
import hu.mktiti.kreator.InjectableType
import java.lang.Exception

@InjectableType
interface BotClientChannel {

    fun reportBotError(exception: Exception)

    fun sendResponse(method: String, data: Any?)

    fun log(message: String)

    fun waitForCall(): Call?

}