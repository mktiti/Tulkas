package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.GameEngineLogger
import hu.mktiti.cirkus.runtime.base.Client
import hu.mktiti.cirkus.runtime.base.ClientRuntime
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject

@Injectable
class EngineClient(
        private val actorHelper: ActorHelper = inject()
) : Client {

    override fun runClient(inQueue: InQueue, outQueue: OutQueue) {

        val messageHandler: MessageHandler = DefaultMessageHandler(inQueue, outQueue)
        val logger: GameEngineLogger = MessageHandlerEngineLogger(messageHandler)

        messageHandler.sendActorBinaryRequest()

        println("Channel created")

        val (engine, _, _) = actorHelper.createActors(messageHandler, logger) ?: throw RuntimeException("Failed to create actors")

        println("Starting game")
        val result = engine.playGame()
        println("Game done")
        println("Did A win? " + result.doAWins())
        println("Did B win? " + result.doBWins())

        messageHandler.sendResult(result)
    }

}

fun main(args: Array<String>) {
    ClientRuntime().run()
}