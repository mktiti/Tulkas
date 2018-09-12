package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.runtime.base.CallTarget
import hu.mktiti.cirkus.runtime.base.LogTarget
import hu.mktiti.kreator.InjectableType
import java.lang.Exception

@InjectableType
interface EngineClientChannel {

    fun callFunction(target: CallTarget, methodName: String, params: List<Any?>): Any?

    fun log(target: LogTarget, message: String)

    fun sendResult(result: GameResult)

    fun reportError(exception: Exception)

}