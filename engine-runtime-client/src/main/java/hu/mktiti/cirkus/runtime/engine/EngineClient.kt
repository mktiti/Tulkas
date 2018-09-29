package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.EngineLoggerFactory
import hu.mktiti.cirkus.runtime.base.BinaryClassLoader
import hu.mktiti.cirkus.runtime.base.Client
import hu.mktiti.cirkus.runtime.base.ClientRuntime
import hu.mktiti.cirkus.runtime.common.InQueue
import hu.mktiti.cirkus.runtime.common.OutQueue
import hu.mktiti.cirkus.runtime.common.logger
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject

@Injectable
class EngineClient(
        private val actorHelper: ActorHelper = inject(),
        private val binaryClassLoader: BinaryClassLoader = inject()
) : Client {

    private val log by logger()

    override fun runClient(inQueue: InQueue, outQueue: OutQueue) {

        val messageHandler: MessageHandler = DefaultMessageHandler(inQueue, outQueue)
        EngineLoggerFactory.setDefaultLogger(MessageHandlerEngineLogger(messageHandler))

        val actorBinary = messageHandler.loadActorBinary()
        if (actorBinary == null) {
            log.error("Actor binary is null")
            log.error("Shutting down")
            return
        }

        binaryClassLoader.loadFromBinary(actorBinary)

        println("Channel created")

        val (engine, _, _) = actorHelper.createActors(messageHandler)
                ?: throw RuntimeException("Failed to create actors")

        if (!messageHandler.waitForStart()) {
            log.error("Error - Was waiting for start notice, shutting down")
            return
        }

        log.info("Starting game")
        val result = engine.playGame()
        log.info("Game done")
        log.info("Did A win? {}", result.doAWins())
        log.info("Did B win? {}", result.doBWins())

        messageHandler.sendResult(result)
    }

}

fun main(args: Array<String>) {
    ClientRuntime().run()
}