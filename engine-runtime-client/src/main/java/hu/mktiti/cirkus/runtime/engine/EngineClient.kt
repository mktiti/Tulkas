package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.EngineLoggerFactory
import hu.mktiti.cirkus.runtime.base.BinaryClassLoader
import hu.mktiti.cirkus.runtime.base.Client
import hu.mktiti.cirkus.runtime.base.ClientRuntime
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject

@Injectable
class EngineClient(
        private val actorHelper: ActorHelper = inject(),
        private val binaryClassLoader: BinaryClassLoader = inject()
) : Client {

    override fun runClient(inQueue: InQueue, outQueue: OutQueue) {

        val messageHandler: MessageHandler = DefaultMessageHandler(inQueue, outQueue)
        EngineLoggerFactory.setDefaultLogger(MessageHandlerEngineLogger(messageHandler))

        val actorBinary = messageHandler.loadActorBinary()
        if (actorBinary == null) {
            println("Actor binary is null")
            println("Shutting down")
            return
        }

        binaryClassLoader.loadFromBinary(actorBinary)

        println("Channel created")

        val (engine, _, _) = actorHelper.createActors(messageHandler)
                ?: throw RuntimeException("Failed to create actors")

        if (!messageHandler.waitForStart()) {
            println("Error - Was waiting for start notice, shutting down")
            return
        }

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