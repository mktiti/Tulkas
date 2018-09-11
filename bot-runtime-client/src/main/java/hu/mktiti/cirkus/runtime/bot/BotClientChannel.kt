package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.runtime.base.Call
import hu.mktiti.kreator.InjectableType

@InjectableType
interface BotClientChannel {

    fun sendResponse(data: Any?)

    fun log(message: String)

    fun waitForCall(): Call?

}