package hu.mktiti.cirkus.runtime.handler.control

import hu.mktiti.cirkus.runtime.common.CallTarget
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
            while (true) {
                val controlMessage = controlQueue.getMessage()
                println("Routing message $controlMessage")
                routeMessage(controlMessage)
                println("Control state: $controlState")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun routeMessage(message: ControlMessage) {
        when (message) {
            is ActorBinaryRequest -> actorBinaryRequest(message)
            is ProxyCallMessage   -> proxyCall(message)
            is CallResultMessage  -> callResult(message)
            is GameResultMessage  -> gameResult(message)
            is ErrorResultMessage -> error(message)
        }
    }

    private fun actorBinaryRequest(request: ActorBinaryRequest) = controlState.let { state ->
        if (state !is ConnectionAwait) {
            sendGameOverToAll()
            throw IllegalStateException("Not waiting for connection!")
        } else {
            if (!state.connect(request.actor)) {
                sendGameOverToAll()
                throw IllegalStateException("Actor ${request.actor} already connected!")
            } else if (state.allConnected) {
                engineHandler.sendMatchStartNotice()
                controlState = WaitingForEngine
            }
        }
    }

    private fun proxyCall(proxyCallMessage: ProxyCallMessage) = controlState.let { state ->
        if (state !is WaitingForEngine) {
            sendGameOverToAll()
            throw IllegalStateException("Not waiting for engine!")
        } else if (proxyCallMessage.actor != Actor.ENGINE) {
            sendGameOverToAll()
            throw IllegalStateException("Only engine can proxy call!")
        } else {
            val targetHandler = if (proxyCallMessage.proxyCall.target == CallTarget.BOT_A) botAHandler else botBHandler
            targetHandler.proxyCall(proxyCallMessage.proxyCall, proxyCallMessage.callData)
            controlState = WaitingForBot(proxyCallMessage.proxyCall.target)
        }
    }

    private fun callResult(callResultMessage: CallResultMessage) = controlState.let { state ->
        if (state !is WaitingForBot || state.bot != callResultMessage.actor.callTarget) {
            sendGameOverToAll()
            throw IllegalStateException("Not waiting for ${callResultMessage.actor}!")
        } else {
            engineHandler.sendCallResult(callResultMessage.result, callResultMessage.responseData)
            controlState = WaitingForEngine
        }
    }

    private fun gameResult(gameResultMessage: GameResultMessage) = controlState.let { state ->
        sendGameOverToAll()
        if (state !is WaitingForEngine) {
            throw IllegalStateException("Not waiting for engine!")
        } else if (gameResultMessage.actor != Actor.ENGINE) {
            throw IllegalStateException("Only engine can proxy call!")
        } else {
            controlState = MatchEnded()
        }
    }

    private fun error(error: ErrorResultMessage) = controlState.let { state ->
        sendGameOverToAll()
        controlState = FatalError("error")
    }

    private fun sendGameOverToAll() {
        listOf(engineHandler, botAHandler, botBHandler).forEach {
            it.sendGameOverNotice()
        }
    }

}