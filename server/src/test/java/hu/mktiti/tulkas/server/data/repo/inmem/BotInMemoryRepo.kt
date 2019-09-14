package hu.mktiti.tulkas.server.data.repo.inmem

import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.JarData
import hu.mktiti.tulkas.server.data.repo.*

@TestInjectable(environment = "unit", tags = ["mem"])
class BotInMemoryRepo(
        bots: List<Bot> = emptyList(),
        private val userRepo: UserRepo = inject(),
        private val gameRepo: GameRepo = inject(),
        private val jarDataRepo: JarDataRepo = inject()
) : InMemoryRepo<Bot>(bots), BotRepo {

    override fun Bot.newId(newId: Long): Bot = copy(id = newId)

    override fun createBot(ownerId: Long, gameId: Long, name: String, jar: ByteArray): Long?
            = save(Bot(ownerId = ownerId,
                        gameId = gameId,
                        name = name,
                        jarId = jarDataRepo.save(JarData(data = jar)),
                        rank = null))

    override fun botsOf(ownerId: Long): List<Bot> = entities.filter { it.ownerId == ownerId }

    override fun botsOf(ownerUsername: String): List<Pair<Bot, String>> {
        val ownerId = userRepo.findByName(ownerUsername)?.id ?: return emptyList()
        return entities.filter { it.ownerId == ownerId }.map { it to (gameRepo.find(it.gameId)?.name ?: "") }
    }

    override fun botsByGame(gameId: Long): List<Pair<Bot, String>>
            = entities.filter { it.gameId == gameId }.map { it to "" }

    override fun botByUserAndName(username: String, name: String): Bot? {
        val userId = userRepo.findByName(username)?.id ?: return null
        return entities.find { it.ownerId == userId && it.name == name }
    }

    override fun updateRanking(rankings: Collection<GameBotRank>) {
        for (ranking in rankings) {
            data[ranking.botId]?.let {
                switch(it.id, it.copy(rank = ranking.rank))
            }
        }
    }

    override fun unrankedBots(): List<Bot>
            = entities.filter { it.rank == null }

    override fun olderOpponents(bot: Bot): List<Bot>
        = entities.filter { it.id < bot.id && it.gameId == bot.gameId }

}