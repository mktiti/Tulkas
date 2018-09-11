package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.runtime.base.RuntimeClient
import hu.mktiti.kreator.Injectable
import hu.mktiti.kreator.inject
import java.net.Socket

@Injectable
class EngineClientRuntime(
        private val actorHelper: ActorHelper = inject()
) : RuntimeClient {

    private val defaultPort = 12345

    override fun runClient(arguments: Map<String, String>) {
        val port = arguments["port"]?.toIntOrNull() ?: defaultPort

        val socket = Socket("localhost", port)
        val channel: EngineClientChannel = EngineClientSocketChannel(socket)
        val (engine, _, _) = actorHelper.createActors(channel) ?: throw RuntimeException("Failed to create actors")

        println("Starting game")
        val result = engine.playGame()
        println("Game done")
        println("Did A win? " + result.doAWins())
        println("Did B win? " + result.doBWins())

        channel.sendResult(result)
    }

}