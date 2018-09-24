package hu.mktiti.cirkus.runtime.handler.control

import hu.mktiti.cirkus.runtime.common.CallTarget
import hu.mktiti.cirkus.runtime.common.MessageException
import hu.mktiti.cirkus.runtime.handler.message.BotMessageHandler
import hu.mktiti.cirkus.runtime.handler.message.EngineMessageHandler

class ControlHandler(
        private val engineHandler: EngineMessageHandler,
        private val botAHandler: BotMessageHandler,
        private val botBHandler: BotMessageHandler,
        private val controlQueue: ControlQueue
) : Runnable {

    private var controlState: ControlState = ConnectionAwait()

    override fun run() {
        try {
            while (controlState !is Crash && controlState !is MatchEnded) {
                val controlMessage = controlQueue.getMessage()
                routeMessage(controlMessage)
            }
        } catch (ise: IllegalStateException) {
            println("Illegal state exception in control state $controlState")
        }

        sendGameOverToAll()

        controlState.let {
            if (it is Crash) {
                println("Game crashed: ${it.message}")
            }
        }
    }

    private fun atState(code: (ControlState) -> Unit) = controlState.let(code)

    private fun modState(code: (ControlState) -> ControlState) { controlState = controlState.let(code) }

    private fun crashable(nextState: ControlState, sender: () -> Boolean): ControlState =
            if (sender()) nextState else Crash("Unable to send message")

    private fun routeMessage(message: ControlMessage) {
        when (message) {
            is ActorBinaryRequest -> actorBinaryRequest(message)
            is ProxyCallMessage   -> proxyCall(message)
            is CallResultMessage  -> callResult(message)
            is GameResultMessage  -> gameResult(message)
            is ErrorResultMessage -> error(message)
        }
    }

    private fun actorBinaryRequest(request: ActorBinaryRequest) = atState { state ->
        if (state !is ConnectionAwait || !state.connect(request.actor)) {
            controlState = Crash("Not waiting for ${request.actor} connection")

        } else if (state.allConnected) {
            controlState = crashable(WaitingForEngine) {
                engineHandler.sendMatchStartNotice()
            }
        }
    }

    private fun proxyCall(proxyCallMessage: ProxyCallMessage) = modState { state ->
        if (state !is WaitingForEngine) {
            Crash("Not waiting for engine!")
        } else if (proxyCallMessage.actor != Actor.ENGINE) {
           Crash("Only engine can proxy call!")
        } else {
            val targetHandler = if (proxyCallMessage.proxyCall.target == CallTarget.BOT_A) botAHandler else botBHandler

            crashable(WaitingForBot(proxyCallMessage.proxyCall.target)) {
                targetHandler.proxyCall(proxyCallMessage.proxyCall, proxyCallMessage.callData)
            }
        }
    }

    private fun callResult(callResultMessage: CallResultMessage) = modState { state ->
        if (state !is WaitingForBot || state.bot != callResultMessage.actor.callTarget) {
            Crash("Not waiting for ${callResultMessage.actor}!")
        } else {
            crashable(WaitingForEngine) {
                engineHandler.sendCallResult(callResultMessage.result, callResultMessage.responseData)
            }
        }
    }

    private fun gameResult(gameResultMessage: GameResultMessage) = modState { state ->
        when {
            state !is WaitingForEngine ->              Crash("Not waiting for engine!")
            gameResultMessage.actor != Actor.ENGINE -> Crash("Only engine can proxy call!")
            else -> MatchEnded()
        }
    }

    private fun error(error: ErrorResultMessage) = modState { FatalError("error") }

    private fun sendGameOverToAll() {
        listOf(engineHandler, botAHandler, botBHandler).forEach {
            it.sendGameOverNotice()
        }
    }

}