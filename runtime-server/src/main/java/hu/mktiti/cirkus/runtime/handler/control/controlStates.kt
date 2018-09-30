package hu.mktiti.cirkus.runtime.handler.control

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.runtime.common.CallTarget

sealed class ControlState(val final: Boolean)

class ConnectionAwait(
        waitingFor: Set<Actor>
) : ControlState(false) {

    private val waitingFor = HashSet(waitingFor)

    val allConnected
        get() = waitingFor.isEmpty()

    fun connect(actor: Actor): Boolean = waitingFor.remove(actor)
}

object WaitingForEngine : ControlState(false) {
    override fun toString() = "Waiting for engine"
}

data class WaitingForBot(
        val bot: CallTarget
) : ControlState(false)

data class FatalError(val errorMessage: String) : ControlState(true)

data class MatchEnded(val result: GameResult? = null) : ControlState(true)

data class Crash(val message: String) : ControlState(true)