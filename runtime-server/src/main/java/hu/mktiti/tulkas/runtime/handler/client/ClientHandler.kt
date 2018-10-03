package hu.mktiti.tulkas.runtime.handler.client

import hu.mktiti.kreator.property.boolProperty
import hu.mktiti.kreator.property.property
import hu.mktiti.tulkas.runtime.common.SocketChannel
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedActorsData
import hu.mktiti.tulkas.runtime.handler.control.Actor
import hu.mktiti.tulkas.runtime.handler.control.ControlQueue
import hu.mktiti.tulkas.runtime.handler.log.EngineLogRouter
import hu.mktiti.tulkas.runtime.handler.log.LogQueue
import hu.mktiti.tulkas.runtime.handler.log.botLogRouter
import hu.mktiti.tulkas.runtime.handler.message.*
import java.io.File
import java.net.ServerSocket
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import kotlin.concurrent.thread

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
        scriptPath: String = property("Client.Bot.script-path"),
        redirectOut: Boolean = boolProperty("Client.Bot.redirect-out", true)
) = startClientProcess(scriptPath, port, logPath, redirectOut)

fun startEngineProcess(
        port: Int,
        logPath: Path,
        scriptPath: String = property("Client.Engine.script-path"),
        redirectOut: Boolean = boolProperty("Client.Engine.redirect-out", true)
) = startClientProcess(scriptPath, port, logPath, redirectOut)

private fun startClientProcess(
        scriptPath: String,
        port: Int,
        logPath: Path,
        redirectOut: Boolean = true
): Process {
    val path = File(scriptPath).absolutePath
    return with(ProcessBuilder(path, HOST, port.toString(), logPath.toAbsolutePath().toString())) {
        if (redirectOut) {
            redirectOutput(ProcessBuilder.Redirect.INHERIT)
            redirectError(ProcessBuilder.Redirect.INHERIT)
        }
        start()
    }
}
