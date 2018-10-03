package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.api.GameResult
import hu.mktiti.tulkas.api.LogTarget
import hu.mktiti.tulkas.runtime.common.CallTarget
import java.lang.Exception

@InjectableType
interface MessageHandler {

    fun loadActorBinary(): ByteArray?

    fun callFunction(target: CallTarget, methodName: String, params: List<Any?>): Any?

    fun log(target: LogTarget, message: String)

    fun sendResult(result: GameResult)

    fun reportError(exception: Exception)

    fun waitForStart(): Boolean
}