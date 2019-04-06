package hu.mktiti.tulkas.runtime.base

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.api.log.LogTarget
import hu.mktiti.tulkas.runtime.common.ActorBinType

@InjectableType
interface MessageHandler {

    fun loadActorBinary(type: ActorBinType): ByteArray?

    fun log(target: LogTarget, message: String)

    fun log(message: String)

    fun reportError(exception: Exception)
}