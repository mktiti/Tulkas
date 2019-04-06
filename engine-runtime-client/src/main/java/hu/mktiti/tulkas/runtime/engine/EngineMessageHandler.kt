package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.api.GameResult
import hu.mktiti.tulkas.runtime.base.MessageHandler
import hu.mktiti.tulkas.runtime.common.CallTarget

@InjectableType
interface EngineMessageHandler : MessageHandler {

    fun callFunction(target: CallTarget, methodName: String, params: List<Any?>): Any?

    fun sendResult(result: GameResult)

    fun waitForStart(): Boolean
}