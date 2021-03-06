package hu.mktiti.tulkas.runtime.handler.message

import hu.mktiti.tulkas.runtime.common.*

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

    override fun sendMatchStartNotice() = sendMessage(MessageDto(StartNotice))

    override fun sendCallResult(callResult: CallResult, resultData: String?) = sendMessage(MessageDto(callResult, resultData))

    override fun sendCallTimeout(): Boolean = sendMessage(MessageDto(BotTimeout))

}