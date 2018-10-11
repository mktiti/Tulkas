package hu.mktiti.tulkas.runtime.handler

import hu.mktiti.kreator.property.property
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedSinglePlayerData
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedTwoPlayerData
import hu.mktiti.tulkas.runtime.handler.client.ActorInitData
import hu.mktiti.tulkas.runtime.handler.client.GameInitData
import hu.mktiti.tulkas.runtime.handler.client.startGame
import hu.mktiti.tulkas.runtime.handler.client.stopGame
import hu.mktiti.tulkas.runtime.handler.control.Actor
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
    val jars: UnifiedTwoPlayerData<ByteArray> = try {
        UnifiedTwoPlayerData("Handler.TestGame.Jar.engine-path", "Handler.TestGame.Jar.bot-path")
                .umap { property(it) }
                .umap(::loadJarBinary)
    } catch (e: Exception) {
        println("Exception while reading jar file: ${e.message}")
        System.exit(1)
        return
    }

    val initData = twoPlayerInitData(jars.engine, jars.botA, jars.botB)

    for (i in 1..1) {
        val handle = startGame(initData)
        handle.controlThread.join()
        stopGame(handle)
    }

}