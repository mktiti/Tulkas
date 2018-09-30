package hu.mktiti.cirkus.runtime.handler

import hu.mktiti.cirkus.runtime.common.SocketChannel
import hu.mktiti.cirkus.runtime.handler.client.createLogDir
import hu.mktiti.cirkus.runtime.handler.client.startBot
import hu.mktiti.cirkus.runtime.handler.client.startEngine
import hu.mktiti.cirkus.runtime.handler.control.Actor
import hu.mktiti.cirkus.runtime.handler.control.ControlHandler
import hu.mktiti.cirkus.runtime.handler.control.ControlQueue
import hu.mktiti.cirkus.runtime.handler.log.EngineLogRouter
import hu.mktiti.cirkus.runtime.handler.log.LogQueue
import hu.mktiti.cirkus.runtime.handler.log.botLogRouter
import hu.mktiti.cirkus.runtime.handler.message.*
import hu.mktiti.kreator.property.property
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.concurrent.thread

fun createSocket(port: Int): Socket = ServerSocket(port).accept()

fun loadJarBinary(path: String): ByteArray = Files.readAllBytes(Paths.get(path))

fun main(args: Array<String>) {
    val ports = ActorsData(12345, 12346, 12347)
    val names = ActorsData("engine", "botA", "botB")

    val logPaths = with(createLogDir()) {
        names.map { resolve("$it.log") }
    }

    val jars = try {
        ActorsData(
                property("ENGINE_JAR_PATH"),
                property("BOT_JAR_PATH")
        ).map(::loadJarBinary)
    } catch (e: Exception) {
        println("Exception while reading jar file: ${e.message}")
        System.exit(1)
        return
    }

    val controlQueue = ControlQueue()
    val logQueues = ActorsData(::LogQueue)

    val executorService = Executors.newFixedThreadPool(3)
    val socketFutures = ports.map { port ->
        executorService.submit(Callable { createSocket(port) })
    }

    println("Starting clients! Ports: $ports")

    val processes = ports.zipWith(logPaths, ::startEngine, ::startBot)
    val channels  = socketFutures.map { SocketChannel(it.get()) }

    val engineHandler: EngineMessageHandler = DefaultEngineMessageHandler(channels.engine)
    val botAHandler: BotMessageHandler      = DefaultBotMessageHandler(channels.botA)
    val botBHandler: BotMessageHandler      = DefaultBotMessageHandler(channels.botB)

    val engineReceiver = MessageRouterReceiver(engineHandler, Actor.ENGINE, EngineLogRouter(logQueues), controlQueue)
    val botAReceiver   = MessageRouterReceiver(botAHandler, Actor.BOT_A, botLogRouter(logQueues.botA), controlQueue)
    val botBReceiver   = MessageRouterReceiver(botBHandler, Actor.BOT_B, botLogRouter(logQueues.botB), controlQueue)

    val controlHandler = ControlHandler(engineHandler, botAHandler, botBHandler, controlQueue, jars)

    thread(name = "Engine Receiver", start = true) { engineReceiver.run() }
    thread(name = "Bot A Receiver", start = true) { botAReceiver.run() }
    thread(name = "Bot B Receiver", start = true) { botBReceiver.run() }

    thread(name = "Control handler", start = true) { controlHandler.run() }.join()

    processes.map(Process::destroyForcibly)

    println("Logs: ${
        logQueues.map(LogQueue::getAll)
    }")

    System.exit(0)

}