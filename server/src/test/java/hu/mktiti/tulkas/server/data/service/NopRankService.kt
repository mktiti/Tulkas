package hu.mktiti.tulkas.server.data.service

import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.TestInjectable

@TestInjectable(arity = InjectableArity.SINGLETON, tags = ["nop"])
class NopRankService : RankService {

    override fun updateChallengeRanking(gameId: Long) {}

    override fun updateMatchRanking(gameId: Long) {}

}