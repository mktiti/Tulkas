package hu.mktiti.tulkas.runtime.handler.client

import hu.mktiti.kreator.property.property
import hu.mktiti.tulkas.api.GameResult
import hu.mktiti.tulkas.runtime.common.forever
import hu.mktiti.tulkas.runtime.common.snd
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedActorsData
import hu.mktiti.tulkas.runtime.handler.control.*
import hu.mktiti.tulkas.runtime.handler.log.ActorLogEntry
import hu.mktiti.tulkas.runtime.handler.log.LogQueue
import hu.mktiti.tulkas.runtime.handler.log.LogSender
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ActorInitData(
        val name: String,
        val actor: Actor,
        val binary: ByteArray
)

class GameInitData(val apiBinary: ByteArray, val actorInitData: UnifiedActorsData<ActorInitData>, val timeout: Int = 600)

fun createLogDir(logDirBase: String = property("Client.Log.base-dir")): Path {
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

fun runGame(initData: GameInitData): Pair<GameResult?, UnifiedActorsData<List<ActorLogEntry>>> {

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

    val controlHandler = ControlHandler(initData.apiBinary, controlHandlers, controlQueue)

    val controlExec = Executors.newSingleThreadExecutor()
    val futureResult = controlExec.submit(controlHandler)

    return try {
        futureResult.get(initData.timeout.toLong(), TimeUnit.SECONDS) to logQueues.umap(LogQueue::getAll)
    } catch (te: TimeoutException) {
        logQueues.umap {
            it.addEntry(ActorLogEntry(LogSender.RUNTIME, "[TIMEOUT] Game exceeded time limit"))
        }
        null to logQueues.umap(LogQueue::getAll)
    } finally {
        controlExec.shutdownNow()
        controlQueue.addMessage(ErrorResultMessage(Actor.ENGINE, "Shutting down"))
        initDataWithHandles.unify({ it.second }, { it.second })
            .actorData.forEach {
                it.messageHandler.sendGameOverNotice()
                it.process.destroyForcibly()
            }
    }
}