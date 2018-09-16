package hu.mktiti.cirkus.runtime.handler

import hu.mktiti.cirkus.runtime.common.SocketChannel
import hu.mktiti.cirkus.runtime.handler.control.Actor
import hu.mktiti.cirkus.runtime.handler.control.ControlHandler
import hu.mktiti.cirkus.runtime.handler.control.ControlQueue
import hu.mktiti.cirkus.runtime.handler.log.EngineLogRouter
import hu.mktiti.cirkus.runtime.handler.log.LogQueue
import hu.mktiti.cirkus.runtime.handler.log.LogRouter
import hu.mktiti.cirkus.runtime.handler.log.botLogRouter
import hu.mktiti.cirkus.runtime.handler.message.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.concurrent.thread

fun createSocket(port: Int): Socket = ServerSocket(port).accept()

fun main(args: Array<String>) {

    val enginePort = 12345
    val botAPort   = 12346
    val botBPort   = 12347

    val controlQueue = ControlQueue()

    val engineLogQueue = LogQueue()
    val botALogQueue   = LogQueue()
    val botBLogQueue   = LogQueue()

    val executorService    = Executors.newFixedThreadPool(3)
    val engineSocketFuture = executorService.submit(Callable { createSocket(enginePort) })
    val botASocketFuture   = executorService.submit(Callable { createSocket(botAPort) })
    val botBSocketFuture   = executorService.submit(Callable { createSocket(botBPort) })

    println("Start clients! Ports: engine=$enginePort, botA=$botAPort, botB=$botBPort")

    val engineChannel = SocketChannel(engineSocketFuture.get())
    val botAChannel   = SocketChannel(botASocketFuture.get())
    val botBChannel   = SocketChannel(botBSocketFuture.get())

    val engineHandler: EngineMessageHandler = DefaultEngineMessageHandler(engineChannel)
    val botAHandler: BotMessageHandler      = DefaultBotMessageHandler(botAChannel)
    val botBHandler: BotMessageHandler      = DefaultBotMessageHandler(botBChannel)

    val engineLogRouter: LogRouter = EngineLogRouter(engineLogQueue, botALogQueue, botBLogQueue)
    val engineReceiver = MessageRouterReceiver(engineHandler, Actor.ENGINE, engineLogRouter, controlQueue)
    val botAReceiver   = MessageRouterReceiver(botAHandler, Actor.BOT_A, botLogRouter(botALogQueue), controlQueue)
    val botBReceiver   = MessageRouterReceiver(botBHandler, Actor.BOT_B, botLogRouter(botBLogQueue), controlQueue)

    val controlHandler = ControlHandler(engineHandler, botAHandler, botBHandler, controlQueue)

    thread(name = "Engine Receiver", start = true) { engineReceiver.run() }
    thread(name = "Bot A Receiver", start = true) { botAReceiver.run() }
    thread(name = "Bot B Receiver", start = true) { botBReceiver.run() }
    thread(name = "Control handler", start = true) { controlHandler.run() }

}