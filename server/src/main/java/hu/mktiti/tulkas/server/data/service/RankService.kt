package hu.mktiti.tulkas.server.data.service

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.repo.BotRepo
import hu.mktiti.tulkas.server.data.repo.GameLogRepo

@Injectable(arity = InjectableArity.SINGLETON)
class RankService(
        private val gameLogRepo: GameLogRepo = inject(),
        private val botRepo: BotRepo = inject()
) {

    fun updateRanking(game: Game) {
        if (game.isMatch) {
            updateMatchRanking(game.id)
        } else {
            updateChallengeRanking(game.id)
        }
    }

    fun updateChallengeRanking(gameId: Long) {
        val rankings = gameLogRepo.getChallengeRanking(gameId)
        botRepo.updateRanking(rankings)
    }

    fun updateMatchRanking(gameId: Long) {
        val rankings = gameLogRepo.getMatchRanking(gameId)
        botRepo.updateRanking(rankings)
    }

}