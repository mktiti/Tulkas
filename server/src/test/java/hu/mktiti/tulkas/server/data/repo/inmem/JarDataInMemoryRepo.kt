package hu.mktiti.tulkas.server.data.repo.inmem

import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.dao.JarData
import hu.mktiti.tulkas.server.data.repo.JarDataRepo

@TestInjectable(environment = "unit", tags = ["mem"])
class JarDataInMemoryRepo(
        jars: List<JarData> = emptyList()
) : InMemoryRepo<JarData>(jars), JarDataRepo {

    override fun JarData.newId(newId: Long): JarData = copy(id = newId)

    override fun loadBot(bot: Bot): ByteArray?
            = find(bot.jarId)?.data

    override fun loadGameApi(game: Game): ByteArray?
            = find(game.apiJarId)?.data

    override fun loadGameEngine(game: Game): ByteArray?
            = find(game.engineJarId)?.data
}