package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.tulkas.server.data.dao.Bot

@TestInjectable(environment = "unit", tags = ["mem"])
class BotInMemoryRepo(
        bots: List<Bot> = listOf()
) : InMemoryRepo<Bot>(bots), BotRepo {

    override fun botByUserAndName(username: String, name: String): Bot? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun Bot.newId(newId: Long): Bot = copy(id = newId)

    override fun createBot(ownerId: Long, gameId: Long, name: String, jar: ByteArray): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun botsOf(ownerId: Long): List<Bot> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun botsOf(ownerUsername: String): List<Pair<Bot, String>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun botsByGame(gameId: Long): List<Pair<Bot, String>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}