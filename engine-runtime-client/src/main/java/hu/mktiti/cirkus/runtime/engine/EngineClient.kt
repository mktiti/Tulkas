package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.runtime.base.Client
import hu.mktiti.cirkus.runtime.base.ClientRuntime
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.kreator.Injectable
import hu.mktiti.kreator.inject

@Injectable
class EngineClient(
        private val actorHelper: ActorHelper = inject()
) : Client {

    override fun runClient(arguments: Map<String, String>, inQueue: InQueue, outQueue: OutQueue) {

        val messageHandler: MessageHandler = DefaultMessageHandler(inQueue, outQueue)

        messageHandler.sendActorBinaryRequest()

        println("Channel created")

        val (engine, _, _) = actorHelper.createActors(messageHandler) ?: throw RuntimeException("Failed to create actors")

        println("Starting game")
        val result = engine.playGame()
        println("Game done")
        println("Did A win? " + result.doAWins())
        println("Did B win? " + result.doBWins())

        messageHandler.sendResult(result)
    }

}

fun main(args: Array<String>) {
    ClientRuntime().run(arrayOf("port", "12345"))
}