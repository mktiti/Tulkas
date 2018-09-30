package hu.mktiti.cirkus.runtime.handler.client

import hu.mktiti.cirkus.runtime.common.SocketChannel
import hu.mktiti.cirkus.runtime.handler.actordata.UnifiedActorsData
import hu.mktiti.cirkus.runtime.handler.control.Actor
import hu.mktiti.cirkus.runtime.handler.control.ControlQueue
import hu.mktiti.cirkus.runtime.handler.log.EngineLogRouter
import hu.mktiti.cirkus.runtime.handler.log.LogQueue
import hu.mktiti.cirkus.runtime.handler.log.botLogRouter
import hu.mktiti.cirkus.runtime.handler.message.*
import hu.mktiti.kreator.property.boolProperty
import hu.mktiti.kreator.property.property
import java.io.File
import java.net.ServerSocket
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.concurrent.thread

private const val CLIENT_SCRIPT_POST = "-runtime-client/start.sh"
private const val HOST = "localhost"

sealed class ActorHandle(
        val process: Process,
        val logQueue: LogQueue
) {
    abstract val messageHandler: ClientMessageHandler
}

class EngineHandle(
        process: Process,
        logQueue: LogQueue,
        override val messageHandler: EngineMessageHandler
) : ActorHandle(process, logQueue)

class BotHandle(
        process: Process,
        logQueue: LogQueue,
        override val messageHandler: BotMessageHandler
) : ActorHandle(process, logQueue)

fun startEngine(
        logBasePath: Path,
        logQueue: LogQueue,
        logQueues: UnifiedActorsData<LogQueue>,
        executor: ExecutorService,
        controlQueue: ControlQueue
): Future<EngineHandle> {
    val server = ServerSocket(0, 1)
    val logPath = logBasePath.resolve("engine.log")

    val port = server.localPort
    println("Listening for engine on port $port")

    val process = startEngineProcess(port, logPath)

    return executor.submit(Callable {
        val socket = server.accept()

        val channel = SocketChannel(socket)

        val messageHandler = DefaultEngineMessageHandler(channel)

        val logRouter = EngineLogRouter(logQueues)

        val receiver = MessageRouterReceiver(messageHandler, Actor.ENGINE, logRouter, controlQueue)

        thread(name = "Engine Receiver", start = true) { receiver.run() }

        EngineHandle(process, logQueue, messageHandler)
    })
}

fun startBot(
        name: String,
        actor: Actor,
        logBasePath: Path,
        logQueue: LogQueue,
        executor: ExecutorService,
        controlQueue: ControlQueue
): Future<BotHandle> {
    val server = ServerSocket(0, 1)
    val logPath = logBasePath.resolve("$name.log")

    val port = server.localPort
    println("Listening for $name on port $port")

    val process = startBotProcess(port, logPath)

    return executor.submit(Callable {
        val socket = server.accept()

        val channel = SocketChannel(socket)

        val messageHandler = DefaultBotMessageHandler(channel)

        val logRouter = botLogRouter(logQueue)

        val receiver = MessageRouterReceiver(messageHandler, actor, logRouter, controlQueue)

        thread(name = "${name.capitalize()} Receiver", start = true) { receiver.run() }

        BotHandle(process, logQueue, messageHandler)
    })
}

fun startBotProcess(
        port: Int,
        logPath: Path,
        scriptPathPrefix: String = property("CLIENT_START_SCRIPT_PRE", ""),
        log: Boolean = boolProperty("LOG_BOT_OUT", true)
) = startClientProcess(scriptPathPrefix, "bot", port, logPath, log)

fun startEngineProcess(
        port: Int,
        logPath: Path,
        scriptPathPrefix: String = property("CLIENT_START_SCRIPT_PRE", ""),
        log: Boolean = boolProperty("LOG_BOT_OUT", true)
) = startClientProcess(scriptPathPrefix, "engine", port, logPath, log)

private fun startClientProcess(
        scriptPathPrefix: String,
        clientName: String,
        port: Int,
        logPath: Path,
        log: Boolean = true
): Process {
    println("Pre: $scriptPathPrefix")
    val path = File(scriptPathPrefix + File.separator + clientName + CLIENT_SCRIPT_POST).absolutePath
    println("Path: $path")
    return with(ProcessBuilder(path, HOST, port.toString(), logPath.toAbsolutePath().toString())) {
        if (log) {
            redirectOutput(ProcessBuilder.Redirect.INHERIT)
            redirectError(ProcessBuilder.Redirect.INHERIT)
        }
        start()
    }
}
