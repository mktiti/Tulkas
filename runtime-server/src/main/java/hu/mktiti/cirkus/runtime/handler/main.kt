package hu.mktiti.cirkus.runtime.handler

import hu.mktiti.cirkus.runtime.handler.actordata.UnifiedSinglePlayerData
import hu.mktiti.cirkus.runtime.handler.actordata.UnifiedTwoPlayerData
import hu.mktiti.cirkus.runtime.handler.actordata.twoPlayer
import hu.mktiti.cirkus.runtime.handler.client.ActorInitData
import hu.mktiti.cirkus.runtime.handler.client.GameInitData
import hu.mktiti.cirkus.runtime.handler.client.startGame
import hu.mktiti.cirkus.runtime.handler.client.stopGame
import hu.mktiti.cirkus.runtime.handler.control.Actor
import hu.mktiti.kreator.property.property
import java.nio.file.Files
import java.nio.file.Paths

fun loadJarBinary(path: String): ByteArray = Files.readAllBytes(Paths.get(path))

fun singlePlayerInitData(
        engineBinary: ByteArray,
        botBinary: ByteArray
): GameInitData = GameInitData(
    UnifiedSinglePlayerData(
        ActorInitData("engine", Actor.ENGINE, engineBinary),
        ActorInitData("bot", Actor.BOT_A, botBinary)
    )
)

fun twoPlayerInitData(
        engineBinary: ByteArray,
        botABinary: ByteArray,
        botBBinary: ByteArray
): GameInitData = GameInitData(
    UnifiedTwoPlayerData(
        ActorInitData("engine", Actor.ENGINE, engineBinary),
        ActorInitData("bot-A", Actor.BOT_A, botABinary),
        ActorInitData("bot-B", Actor.BOT_B, botBBinary)
    )
)

fun main(args: Array<String>) {
    val names = twoPlayer("engine", "botA", "botB")

    val jars: UnifiedTwoPlayerData<ByteArray> = try {
        UnifiedTwoPlayerData("ENGINE_JAR_PATH", "BOT_JAR_PATH")
                .umap { property(it) }
                .umap(::loadJarBinary)
    } catch (e: Exception) {
        println("Exception while reading jar file: ${e.message}")
        System.exit(1)
        return
    }

    val initData = twoPlayerInitData(jars.engine, jars.botA, jars.botB)

    for (i in 1..10) {
        val handle = startGame(initData)
        handle.controlThread.join()
        stopGame(handle)
    }

}