package hu.mktiti.cirkus.runtime.handler.client

import hu.mktiti.cirkus.runtime.common.forever
import hu.mktiti.cirkus.runtime.common.snd
import hu.mktiti.cirkus.runtime.handler.actordata.UnifiedActorsData
import hu.mktiti.cirkus.runtime.handler.control.*
import hu.mktiti.cirkus.runtime.handler.log.LogQueue
import hu.mktiti.kreator.property.property
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread

class ActorInitData(
        val name: String,
        val actor: Actor,
        val binary: ByteArray
)

class GameInitData(val actorInitData: UnifiedActorsData<ActorInitData>)

class GameHandle(
        val actorHandles: UnifiedActorsData<ActorHandle>,
        val controlQueue: ControlQueue,
        val controlThread: Thread
)

fun createLogDir(logDirBase: String = property("LOG_DIR_BASE")): Path {
    val basePath = Paths.get(logDirBase)
    if (!Files.exists(basePath)) {
        Files.createDirectory(basePath)
    }

    if (!Files.isWritable(basePath)) {
        throw RuntimeException("Cannot create log dir, cannot access base dir '$logDirBase'")
    }

    var counter = 0
    forever {
        val subPath = basePath.resolve("log-${counter++}")
        if (!Files.exists(subPath)) {
            Files.createDirectory(subPath)
            return subPath
        }
    }
}

fun startGame(initData: GameInitData): GameHandle {

    val logBasePath = createLogDir()

    val executor = Executors.newFixedThreadPool(initData.actorInitData.actorData.size)
    val controlQueue = ControlQueue()

    val initDataWithLogs = initData.actorInitData.umap {
        it to LogQueue()
    }
    val logQueues = initDataWithLogs.umap { it.second }

    val initDataWithHandleFutures = initDataWithLogs.map(
            { it.snd(startEngine(logBasePath, it.second, logQueues, executor, controlQueue)) },
            { it.snd(startBot(it.first.name, it.first.actor, logBasePath, it.second, executor, controlQueue)) }
    )

    val initDataWithHandles = initDataWithHandleFutures.map(
            { it.snd(Future<EngineHandle>::get) },
            { it.snd(Future<BotHandle>::get) }
    )

    executor.shutdownNow()

    val controlHandlers = initDataWithHandles.map(
            { EngineControlHandle(it.first.binary, it.second.messageHandler) },
            { BotControlHandle(it.first.binary, it.second.messageHandler) }
    )

    val controlHandler = ControlHandler(controlHandlers, controlQueue)

    val controlThread = thread(name = "Control handler", start = true) { controlHandler.run() }

    return GameHandle(initDataWithHandles.unify({ it.second }, { it.second }), controlQueue, controlThread)
}

fun stopGame(handle: GameHandle) = with(handle) {
    controlQueue.addMessage(ErrorResultMessage(Actor.ENGINE, "Shutting down"))
    actorHandles.actorData.forEach {
        it.process.destroyForcibly()
        it.messageHandler.sendGameOverNotice()
    }

    println("Logs: ${
        actorHandles.umap { it.logQueue }.actorData.map(LogQueue::getAll)
    }")
}