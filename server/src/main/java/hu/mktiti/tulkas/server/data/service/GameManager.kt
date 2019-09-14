package hu.mktiti.tulkas.server.data.service

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.api.challenge.ChallengeResult
import hu.mktiti.tulkas.api.match.MatchResult
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedSinglePlayerData
import hu.mktiti.tulkas.runtime.handler.actordata.UnifiedTwoPlayerData
import hu.mktiti.tulkas.runtime.handler.client.runGame
import hu.mktiti.tulkas.runtime.handler.log.ActorLogEntry
import hu.mktiti.tulkas.runtime.handler.singlePlayerInitData
import hu.mktiti.tulkas.runtime.handler.twoPlayerInitData
import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.dao.GameLog
import hu.mktiti.tulkas.server.data.forever
import hu.mktiti.tulkas.server.data.repo.*
import java.time.LocalDateTime
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

private class GameRunData(
        val botJar: ByteArray,
        val gameApiJar: ByteArray,
        val gameEngineJar: ByteArray
)

@InjectableType
interface GameManager {

    fun rankAllUnranked()

    fun onNewBot(botId: Long)

}

@Injectable(arity = InjectableArity.SINGLETON)
class DefaultGameManager(
        private val gameRepo: GameRepo = inject(),
        private val botRepo: BotRepo = inject(),
        private val jarDataRepo: JarDataRepo = inject(),
        private val gameLogRepo: GameLogRepo = inject(),
        private val actorLogRepo: ActorLogRepo = inject(),
        private val rankService: RankService = inject()
) : GameManager {

    private val botQueue: BlockingQueue<Long> = LinkedBlockingQueue()

    override fun rankAllUnranked() {
        botRepo.unrankedBots().map(Bot::id).forEach(botQueue::put)
    }

    override fun onNewBot(botId: Long) {
        botQueue.put(botId)
    }

    init {
        thread(isDaemon = true) {
            processBots()
        }
    }

    private fun processBots(): Nothing = forever {
        val bot = botRepo.find(botQueue.take())
        if (bot == null) {
            println("Bot not found")
            return@forever
        }

        val game = gameRepo.find(bot.gameId)

        if (game == null) {
            println("Bot [${bot.name}] have no valid game! (id: ${bot.gameId})")
            return@forever
        }

        val gameRunData = GameRunData(
                botJar = jarDataRepo.loadBot(bot) ?: return@forever,
                gameApiJar = jarDataRepo.loadGameApi(game) ?: return@forever,
                gameEngineJar = jarDataRepo.loadGameEngine(game) ?: return@forever
        )

        if (game.isMatch) {
            playMatches(bot, game, gameRunData)
        } else {
            playChallenge(bot, game, gameRunData)
        }
        rankService.updateRanking(game)
    }

    private fun playChallenge(bot: Bot, game: Game, gameRunData: GameRunData) {
        val (result, logs) = runGame(singlePlayerInitData(gameRunData.gameApiJar, gameRunData.gameEngineJar, gameRunData.botJar))
        if (result != null && result !is ChallengeResult) {
            println("Invalid game result")
            return
        }

        if (logs !is UnifiedSinglePlayerData<List<ActorLogEntry>>) {
            println("Invalid log format")
            return
        }

        val cr = result as? ChallengeResult
        val points: Long? = cr?.points
        val maxPoints: Long? = cr?.maxPoints

        guardedTransaction {
            val gameLog = gameLogRepo.save(GameLog(
                gameId = game.id,
                botAId = bot.id,
                botBId = null,
                time = LocalDateTime.now(),
                result = if (cr == null || points == null) "ERROR" else "OK",
                points = points?.toInt(),
                maxPoints = maxPoints?.toInt()
            ))

            actorLogRepo.saveLog(gameLog, "ENGINE", logs.engine.map { (sender, target) -> sender.name to target })
            actorLogRepo.saveLog(gameLog, "BOT_A", logs.bot.map { (sender, target) -> sender.name to target })
        }
    }

    private fun playMatches(bot: Bot, game: Game, gameRunData: GameRunData) {
        botRepo.olderOpponents(bot).forEach { opponent ->
            playMatch(bot, game, gameRunData, opponent)
        }
    }

    private fun playMatch(bot: Bot, game: Game, gameRunData: GameRunData, opponent: Bot) {
        val opponentJar = jarDataRepo.loadBot(opponent)
        if (opponentJar == null) {
            println("Cannot load binary for Bot [${opponent.id}]")
            return
        }

        val (result, logs) = runGame(twoPlayerInitData(gameRunData.gameApiJar, gameRunData.gameEngineJar, gameRunData.botJar, opponentJar))
        if (result != null && result !is MatchResult) {
            println("Invalid game result")
            return
        }

        if (logs !is UnifiedTwoPlayerData<List<ActorLogEntry>>) {
            println("Invalid log format")
            return
        }

        transaction {
            val gameLog = gameLogRepo.save(GameLog(
                    gameId = game.id,
                    botAId = bot.id,
                    botBId = opponent.id,
                    time = LocalDateTime.now(),
                    result = (result as? MatchResult)?.toString() ?: "ERROR",
                    points = null,
                    maxPoints = null
            ))

            logs.umap({ it to "ENGINE" }, { it to "BOT_A" }, { it to "BOT_B" }).umap { (log, target) ->
                actorLogRepo.saveLog(gameLog, target, log.map { (sender, target) -> sender.name to target })
            }
        }

    }

}