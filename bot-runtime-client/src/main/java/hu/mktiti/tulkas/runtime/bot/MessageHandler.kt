package hu.mktiti.tulkas.runtime.bot

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.common.Call
import java.lang.Exception

@InjectableType
interface MessageHandler {

    fun loadActorBinary(): ByteArray?

    fun reportBotError(exception: Exception)

    fun sendResponse(method: String, data: Any?)

    fun log(message: String)

    fun waitForCall(): Call?

    fun waitForBot(): ByteArray?

}