package hu.mktiti.cirkus.runtime.handler.message

import hu.mktiti.cirkus.runtime.common.CallResult
import hu.mktiti.cirkus.runtime.common.MessageDto
import hu.mktiti.cirkus.runtime.common.ProxyCall
import hu.mktiti.kreator.InjectableType

@InjectableType
interface ClientMessageHandler {

    fun sendActorBinary(actor: ByteArray)

    fun sendGameOverNotice()

    fun waitForMessage(): MessageDto

}

@InjectableType
interface BotMessageHandler : ClientMessageHandler {

    fun proxyCall(proxyCall: ProxyCall, callData: String?)

}

@InjectableType
interface EngineMessageHandler : ClientMessageHandler {

    fun sendMatchStartNotice()

    fun sendCallResult(callResult: CallResult, resultData: String?)

}