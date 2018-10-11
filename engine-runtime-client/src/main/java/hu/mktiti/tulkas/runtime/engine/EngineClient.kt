package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.kreator.property.boolProperty
import hu.mktiti.tulkas.api.log.EngineLoggerFactory
import hu.mktiti.tulkas.runtime.base.*
import hu.mktiti.tulkas.runtime.common.InQueue
import hu.mktiti.tulkas.runtime.common.OutQueue
import hu.mktiti.tulkas.runtime.common.ProxyCallTimeoutException
import hu.mktiti.tulkas.runtime.common.logger

class EngineClient(
        private val isMatch: Boolean = boolProperty("IS_MATCH")
) : Client {

    private val log by logger()

    override fun runClient(
            inQueue: InQueue,
            outQueue: OutQueue,
            messageConverter: MessageConverter,
            binaryClassLoader: BinaryClassLoader,
            clientHelper: RuntimeClientHelper
    ) {
        val engineClientHelper: EngineClientHelper = DefaultEngineClientHelper(binaryClassLoader)
        val actorHelper: ActorHelper = DefaultActorHelper(clientHelper, engineClientHelper)

        val messageHandler: EngineMessageHandler = DefaultEngineMessageHandler(inQueue, outQueue, messageConverter)
        EngineLoggerFactory.setDefaultLogger(MessageHandlerEngineLogger(messageHandler))

        val actorBinary = messageHandler.loadActorBinary()
        if (actorBinary == null) {
            log.error("BotActor binary is null")
            log.error("Shutting down")
            return
        }

        binaryClassLoader.loadFromBinary(actorBinary)

        println("Channel created")

        val engine = actorHelper.createActors(messageHandler, isMatch)
            ?: throw RuntimeException("Failed to create actors")

        if (!messageHandler.waitForStart()) {
            log.error("Error - Was waiting for start notice, shutting down")
            return
        }

        log.info("Starting game")
        try {
            val result = engine.playGame()
            log.info("Game done")
            messageHandler.sendResult(result)

        } catch (te: ProxyCallTimeoutException) {
            log.info("Unexpected bot timeout (not caught by game engine)", te)
            messageHandler.log("Uncaught bot timeout occured, for automatic timeout handling use an abstract game engine base")
            messageHandler.reportError(te)
        }
    }

}

fun main(args: Array<String>) {
    ClientRuntime(EngineClient()).run()
}