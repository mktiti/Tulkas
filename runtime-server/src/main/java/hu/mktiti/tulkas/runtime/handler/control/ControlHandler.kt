package hu.mktiti.tulkas.runtime.handler.control

import hu.mktiti.tulkas.runtime.common.logger
import hu.mktiti.tulkas.runtime.handler.actordata.ActorArityException
import hu.mktiti.tulkas.runtime.handler.actordata.ActorsData
import hu.mktiti.tulkas.runtime.handler.actordata.unified

class ControlHandler(
        private val handlers: ActorsData<EngineControlHandle, BotControlHandle>,
        private val controlQueue: ControlQueue
) : Runnable {

    private val log by logger()

    private val unifiedHandlers = handlers.unified()

    private var controlState: ControlState = ConnectionAwait(handlers.actors)

    override fun run() {
        try {
            while (!controlState.final) {
                val controlMessage = controlQueue.getMessage()
                routeMessage(controlMessage)
                log.info("Control state: {}", controlState)
            }
        } catch (ise: IllegalStateException) {
            log.error("Illegal state exception in control state {}", controlState)
        } catch (aae: ActorArityException) {
            log.error("BotActor arity mixup occured", aae)
        }

        sendGameOverToAll()

        controlState.let {
            if (it is Crash) {
                log.error("Game crashed: {}", it.message)
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
            is ChallengeResultMessage  -> challengeResult(message)
            is MatchResultMessage  -> matchResult(message)
            is BotTimeoutMessage   -> botTimeout(message)
            is ErrorResultMessage  -> error(message)
        }
    }

    private fun handle(actor: Actor): ControlClientHandle = unifiedHandlers[actor]

    private fun actorBinaryRequest(request: ActorBinaryRequest) = atState { state ->
        if (state !is ConnectionAwait || !state.connect(request.actor)) {
            controlState = Crash("Not waiting for ${request.actor} connection")

        } else {
            request.actor.let {
                handle(it).apply {
                    messageHandler.sendActorBinary(binary)
                }
            }

            if (state.allConnected) {
                controlState = crashable(WaitingForEngine) {
                    handlers.engine.messageHandler.sendMatchStartNotice()
                }
            }
        }
    }

    private fun proxyCall(proxyCallMessage: ProxyCallMessage) = modState { state ->
        if (state !is WaitingForEngine) {
            Crash("Not waiting for engine!")
        } else if (proxyCallMessage.actor != Actor.ENGINE) {
           Crash("Only engine can proxy call!")
        } else {
            val targetHandler = handlers.getBot(callTargetToActor(proxyCallMessage.proxyCall.target))

            crashable(WaitingForBot(proxyCallMessage.proxyCall.target)) {
                targetHandler.messageHandler.proxyCall(proxyCallMessage.proxyCall, proxyCallMessage.callData)
            }
        }
    }

    private fun callResult(callResultMessage: CallResultMessage) = modState { state ->
        if (state !is WaitingForBot || state.bot != callResultMessage.actor.callTarget) {
            Crash("Not waiting for ${callResultMessage.actor}!")
        } else {
            crashable(WaitingForEngine) {
                handlers.engine.messageHandler.sendCallResult(callResultMessage.result, callResultMessage.responseData)
            }
        }
    }

    private fun challengeResult(challengeResultMessage: ChallengeResultMessage) = modState { state ->
        when {
            state !is WaitingForEngine -> Crash("Not waiting for engine!")
            handlers.isMatch -> Crash("Match must not return challenge result!")
            challengeResultMessage.actor != Actor.ENGINE -> Crash("Only engine can proxy call!")
            else -> MatchEnded(challengeResultMessage.result)
        }
    }

    private fun matchResult(matchResultMessage: MatchResultMessage) = modState { state ->
        when {
            state !is WaitingForEngine -> Crash("Not waiting for engine!")
            !handlers.isMatch -> Crash("Challenge must not return match result!")
            matchResultMessage.actor != Actor.ENGINE -> Crash("Only engine can proxy call!")
            else -> MatchEnded(matchResultMessage.result)
        }
    }

    private fun botTimeout(timeoutMessage: BotTimeoutMessage) = modState { state ->
        if (state !is WaitingForBot || state.bot != timeoutMessage.actor.callTarget) {
            Crash("Not waiting for ${timeoutMessage.actor}!")
        } else {
            crashable(WaitingForEngine) {
                handlers.engine.messageHandler.sendCallTimeout()
            }
        }
    }

    private fun error(error: ErrorResultMessage) = modState {
        FatalError("Error from ${error.actor}: ${error.errorData}")
    }

    private fun sendGameOverToAll() {
        unifiedHandlers.actorData.forEach {
            it.messageHandler.sendGameOverNotice()
        }
    }

}