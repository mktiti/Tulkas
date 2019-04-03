package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.JarData

@InjectableType
interface JarDataRepo : Repo<JarData> {

    fun loadBot(botId: Long): ByteArray?

    fun loadGameApi(engineId: Long): ByteArray?

    fun loadGameEngine(engineId: Long): ByteArray?

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class JarDataDbRepo(
        connectionSource: ConnectionSource = inject()
) : DbRepo<JarData>(tableName, listOf("data"), connectionSource), JarDataRepo {

    companion object {
        const val tableName = "JarData"

        private const val botSelectQuery = """
            select d.*
            from $tableName d
            join ${BotDbRepo.tableName} b on b.jarId = d.id
            where b.id = ?
        """

        private const val gameApiSelectQuery = """
            select d.*
            from $tableName d
            join ${GameDbRepo.tableName} g on g.apiJarId = d.id
            where g.id = ?
        """

        private const val gameEngineSelectQuery = """
            select d.*
            from $tableName d
            join ${GameDbRepo.tableName} g on g.engineJarId = d.id
            where g.id = ?
        """
    }

    override fun PrefixedResultSet.mapRow() = JarData(
            id = long("id"),
            data = byteArray("data")
    )

    override fun insertMap(entity: JarData) = listOf(entity.data)

    override fun loadBot(botId: Long): ByteArray? = selectSingle(botSelectQuery, listOf(botId))?.data

    override fun loadGameApi(engineId: Long): ByteArray? = selectSingle(gameApiSelectQuery, listOf(engineId))?.data

    override fun loadGameEngine(engineId: Long): ByteArray? = selectSingle(gameEngineSelectQuery, listOf(engineId))?.data

}