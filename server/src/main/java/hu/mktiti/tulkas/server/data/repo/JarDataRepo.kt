package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.dao.JarData

@InjectableType
interface JarDataRepo : Repo<JarData> {

    fun loadBot(bot: Bot): ByteArray?

    fun loadGameApi(game: Game): ByteArray?

    fun loadGameEngine(game: Game): ByteArray?

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class JarDataDbRepo(
        connectionSource: ConnectionSource = inject()
) : DbRepo<JarData>(tableName, listOf("data"), connectionSource), JarDataRepo {

    companion object {
        const val tableName = "JarData"
    }

    override fun PrefixedResultSet.mapRow() = JarData(
            id = long("id"),
            data = byteArray("data")
    )

    override fun insertMap(entity: JarData) = listOf(entity.data)

    override fun loadBot(bot: Bot): ByteArray? = find(bot.jarId)?.data

    override fun loadGameApi(game: Game): ByteArray? = find(game.apiJarId)?.data

    override fun loadGameEngine(game: Game): ByteArray? = find(game.engineJarId)?.data

}