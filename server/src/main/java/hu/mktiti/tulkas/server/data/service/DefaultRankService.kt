package hu.mktiti.tulkas.server.data.service

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.repo.BotRepo
import hu.mktiti.tulkas.server.data.repo.GameLogRepo

@InjectableType
interface RankService {

    fun updateRanking(game: Game) {
        if (game.isMatch) {
            updateMatchRanking(game.id)
        } else {
            updateChallengeRanking(game.id)
        }
    }

    fun updateChallengeRanking(gameId: Long)

    fun updateMatchRanking(gameId: Long)

}

@Injectable(arity = InjectableArity.SINGLETON)
class DefaultRankService(
        private val gameLogRepo: GameLogRepo = inject(),
        private val botRepo: BotRepo = inject()
) : RankService {

    override fun updateChallengeRanking(gameId: Long) {
        val rankings = gameLogRepo.getChallengeRanking(gameId)
        botRepo.updateRanking(rankings)
    }

    override fun updateMatchRanking(gameId: Long) {
        val rankings = gameLogRepo.getMatchRanking(gameId)
        botRepo.updateRanking(rankings)
    }

}