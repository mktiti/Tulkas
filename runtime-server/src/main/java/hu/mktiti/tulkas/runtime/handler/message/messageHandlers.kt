package hu.mktiti.tulkas.runtime.handler.message

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.common.CallResult
import hu.mktiti.tulkas.runtime.common.MessageDto
import hu.mktiti.tulkas.runtime.common.ProxyCall

@InjectableType
interface ClientMessageHandler {

    fun sendActorBinary(actor: ByteArray): Boolean

    fun sendGameOverNotice()

    fun waitForMessage(): MessageDto?

}

@InjectableType
interface BotMessageHandler : ClientMessageHandler {

    fun proxyCall(proxyCall: ProxyCall, callData: String?): Boolean

}

@InjectableType
interface EngineMessageHandler : ClientMessageHandler {

    fun sendMatchStartNotice(): Boolean

    fun sendCallResult(callResult: CallResult, resultData: String?): Boolean

    fun sendCallTimeout(): Boolean

}