package hu.mktiti.tulkas.runtime.handler.control

import hu.mktiti.tulkas.api.GameResult
import hu.mktiti.tulkas.runtime.common.ActorBinType
import hu.mktiti.tulkas.runtime.common.CallTarget

sealed class ControlState(val final: Boolean)

class ConnectionAwait(
        waitingFor: Set<Actor>
) : ControlState(false) {

    private val waitingFor = HashSet(waitingFor.flatMap {
        actor -> ActorBinType.values().map { actor to it }
    })

    val allConnected
        get() = waitingFor.isEmpty()

    fun connect(actor: Actor, type: ActorBinType): Boolean = waitingFor.remove(actor to type)
}

object WaitingForEngine : ControlState(false) {
    override fun toString() = "Waiting for engine"
}

data class WaitingForBot(
        val bot: CallTarget
) : ControlState(false)

data class FatalError(val result: GameResult?, val errorMessage: String) : ControlState(true)

data class MatchEnded(val result: GameResult) : ControlState(true)

data class Crash(val message: String) : ControlState(true)