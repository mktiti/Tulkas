package hu.mktiti.tulkas.runtime.handler

import hu.mktiti.kreator.property.property
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedSinglePlayerData
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedTwoPlayerData
import hu.mktiti.tulkas.runtime.handler.client.ActorInitData
import hu.mktiti.tulkas.runtime.handler.client.GameInitData
import hu.mktiti.tulkas.runtime.handler.client.runGame
import hu.mktiti.tulkas.runtime.handler.control.Actor
import java.nio.file.Files
import java.nio.file.Paths

fun loadJarBinary(path: String): ByteArray = Files.readAllBytes(Paths.get(path))

fun singlePlayerInitData(
        apiBinary: ByteArray,
        engineBinary: ByteArray,
        botBinary: ByteArray
): GameInitData = GameInitData(apiBinary,
    UnifiedSinglePlayerData(
        ActorInitData("engine", Actor.ENGINE, engineBinary),
        ActorInitData("bot", Actor.BOT_A, botBinary)
    )
)

fun twoPlayerInitData(
        apiBinary: ByteArray,
        engineBinary: ByteArray,
        botABinary: ByteArray,
        botBBinary: ByteArray
): GameInitData = GameInitData(apiBinary,
    UnifiedTwoPlayerData(
        ActorInitData("engine", Actor.ENGINE, engineBinary),
        ActorInitData("bot-A", Actor.BOT_A, botABinary),
        ActorInitData("bot-B", Actor.BOT_B, botBBinary)
    )
)

fun testChallenge(goodBot: Boolean): GameInitData {
    val apiJar = loadJarBinary(property("Handler.TestChallenge.api-path"))
    val jars: UnifiedSinglePlayerData<ByteArray> =
        UnifiedSinglePlayerData("Handler.TestChallenge.engine-path", "Handler.TestChallenge.${if (goodBot) "good" else "error"}-bot-path")
                .umap { property(it) }
                .umap(::loadJarBinary)


    return singlePlayerInitData(apiJar, jars.engine, jars.bot)
}

fun testMatch(): GameInitData {
    val apiJar = loadJarBinary(property("Handler.TestMatch.api-path"))
    val jars: UnifiedTwoPlayerData<ByteArray> =
            UnifiedTwoPlayerData("Handler.TestMatch.engine-path", "Handler.TestMatch.random-bot-path", "Handler.TestMatch.smart-bot-path")
                    .umap { property(it) }
                    .umap(::loadJarBinary)


    return twoPlayerInitData(apiJar, jars.engine, jars.botA, jars.botB)
}

fun main(args: Array<String>) {
    //val initData = testChallenge(false)
    val initData = testMatch()

    for (i in 1..1) {
        println("result: " + runGame(initData))
    }

}