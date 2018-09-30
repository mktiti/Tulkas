package hu.mktiti.cirkus.runtime.handler.control

import hu.mktiti.cirkus.runtime.common.CallResult
import hu.mktiti.cirkus.runtime.common.CallTarget
import hu.mktiti.cirkus.runtime.common.ProxyCall

enum class Actor(val callTarget: CallTarget?) {
    ENGINE(null), BOT_A(CallTarget.BOT_A), BOT_B(CallTarget.BOT_B)
}

fun callTargetToActor(target: CallTarget): Actor = when (target) {
    CallTarget.BOT_A -> Actor.BOT_A
    CallTarget.BOT_B -> Actor.BOT_B
}

sealed class ControlMessage(val actor: Actor)

class ActorBinaryRequest(actor: Actor) : ControlMessage(actor)

class ProxyCallMessage(actor: Actor, val proxyCall: ProxyCall, val callData: String?) : ControlMessage(actor)

class CallResultMessage(actor: Actor, val result: CallResult, val responseData: String?) : ControlMessage(actor)

class GameResultMessage(actor: Actor, val gameResultData: String?) : ControlMessage(actor)

class ErrorResultMessage(actor: Actor, val errorData: String?) : ControlMessage(actor)