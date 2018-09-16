package hu.mktiti.cirkus.runtime.handler.control

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.runtime.common.CallTarget

sealed class ControlState(val final: Boolean)

data class ConnectionAwait(
        private val connectedActors: MutableSet<Actor> = mutableSetOf()
) : ControlState(false) {

    val allConnected
        get() = connectedActors.size == 3

    fun connect(actor: Actor): Boolean = connectedActors.add(actor)
}

object WaitingForEngine : ControlState(false) {
    override fun toString() = "Waiting for engine"
}

data class WaitingForBot(
        val bot: CallTarget
) : ControlState(false)

data class FatalError(val errorMessage: String) : ControlState(true)

data class MatchEnded(val result: GameResult? = null) : ControlState(true)