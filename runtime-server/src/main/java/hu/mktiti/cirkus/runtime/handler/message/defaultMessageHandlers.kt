package hu.mktiti.cirkus.runtime.handler.message

import hu.mktiti.cirkus.runtime.common.*

class DefaultBotMessageHandler(
        channel: Channel
) : AbstractClientMessageHandler(
        channel = channel
), BotMessageHandler {

    override fun proxyCall(proxyCall: ProxyCall, callData: String?) = sendMessage(MessageDto(proxyCall, callData))

}

class DefaultEngineMessageHandler(
        channel: Channel
) : AbstractClientMessageHandler(
        channel = channel
), EngineMessageHandler {

    override fun sendMatchStartNotice() = sendMessage(MessageDto(StartNotice, null))

    override fun sendCallResult(callResult: CallResult, resultData: String?) = sendMessage(MessageDto(callResult, resultData))

}