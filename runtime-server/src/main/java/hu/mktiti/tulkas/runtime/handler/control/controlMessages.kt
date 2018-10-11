package hu.mktiti.tulkas.runtime.handler.control

import hu.mktiti.tulkas.api.challenge.ChallengeResult
import hu.mktiti.tulkas.api.match.MatchResult
import hu.mktiti.tulkas.runtime.common.CallResult
import hu.mktiti.tulkas.runtime.common.CallTarget
import hu.mktiti.tulkas.runtime.common.ProxyCall

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

class ChallengeResultMessage(actor: Actor, val result: ChallengeResult) : ControlMessage(actor)

class MatchResultMessage(actor: Actor, val result: MatchResult) : ControlMessage(actor)

class BotTimeoutMessage(actor: Actor) : ControlMessage(actor)

class ErrorResultMessage(actor: Actor, val errorData: String?) : ControlMessage(actor)