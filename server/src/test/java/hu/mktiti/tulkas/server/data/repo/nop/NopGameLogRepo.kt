package hu.mktiti.tulkas.server.data.repo.nop

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.tulkas.server.data.dao.GameLog
import hu.mktiti.tulkas.server.data.repo.ChallengeBotRank
import hu.mktiti.tulkas.server.data.repo.GameLogRepo
import hu.mktiti.tulkas.server.data.repo.MatchBotRank
import hu.mktiti.tulkas.server.data.repo.MatchLogWithActors

@Injectable(arity = InjectableArity.SINGLETON, tags = ["nop"])
class NopGameLogRepo : NopRepo<GameLog>(), GameLogRepo {

    override fun matchesOfBot(id: Long): List<MatchLogWithActors> = emptyList()

    override fun challengesOfBot(id: Long): List<GameLog> = emptyList()

    override fun getChallengeRanking(id: Long): List<ChallengeBotRank> = emptyList()

    override fun getMatchRanking(id: Long): List<MatchBotRank> = emptyList()

}