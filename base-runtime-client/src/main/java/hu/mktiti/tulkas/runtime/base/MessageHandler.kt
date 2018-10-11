package hu.mktiti.tulkas.runtime.base

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.api.log.LogTarget
import java.lang.Exception

@InjectableType
interface MessageHandler {

    fun loadActorBinary(): ByteArray?

    fun log(target: LogTarget, message: String)

    fun log(message: String)

    fun reportError(exception: Exception)
}