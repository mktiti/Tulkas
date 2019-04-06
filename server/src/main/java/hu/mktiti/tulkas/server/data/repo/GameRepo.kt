package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.dao.JarData

@InjectableType
interface GameRepo : NamedEntityRepo<Game> {

    fun createGame(ownerId: Long, name: String, isMatch: Boolean, apiJar: ByteArray, engineJar: ByteArray): Long?

    fun gamesOf(ownerId: Long): List<Game>

    fun listWithOwnerName(): List<Pair<Game, String>>

    fun searchWithOwnerName(namePart: String): List<Pair<Game, String>>

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class GameDbRepo(
        private val jarDataRepo: JarDataRepo = inject(),
        connectionSource: ConnectionSource = inject()
) : NamedEntityDbRepo<Game>(tableName, listOf("ownerId", "name", "isMatch", "apiJarId", "engineJarId"), connectionSource), GameRepo {

    companion object {
        const val tableName = "Game"

        private const val selectWithUsernameQuery = """
            select g.*, u.name as owner
            from $tableName g
            join ${UserDbRepo.tableName} u on g.ownerId = u.id
            order by g.id
        """

        private const val searchWithUsernameQuery = """
            select g.*, u.name as owner
            from $tableName g
            join ${UserDbRepo.tableName} u on g.ownerId = u.id
            where upper(g.name) like ?
            order by g.id
        """
    }

    override fun PrefixedResultSet.mapRow() = Game(
            id = long("id"),
            ownerId = long("ownerId"),
            name = string("name"),
            isMatch = boolean("isMatch"),
            apiJarId = long("apiJarId"),
            engineJarId = long("engineJarId")
    )

    override fun insertMap(entity: Game) = with(entity) {
        listOf(ownerId, name, isMatch, apiJarId, engineJarId)
    }

    override fun createGame(
            ownerId: Long,
            name: String,
            isMatch: Boolean,
            apiJar: ByteArray,
            engineJar: ByteArray
    ) = guardedTransaction<Long> {
        save(Game(
                ownerId = ownerId,
                name = name,
                isMatch = isMatch,
                apiJarId = jarDataRepo.save(JarData(data = apiJar)),
                engineJarId = jarDataRepo.save(JarData(data = engineJar))
        ))
    }

    override fun gamesOf(ownerId: Long): List<Game> =
            selectMulti("select * from $tableName where ownerId = ?", ownerId)

    override fun listWithOwnerName(): List<Pair<Game, String>> =
            selectMultiTo(selectWithUsernameQuery) {
                mapRow() to string("owner")
            }

    override fun searchWithOwnerName(namePart: String): List<Pair<Game, String>> =
            selectMultiTo(searchWithUsernameQuery, "%$namePart%".toUpperCase()) {
                mapRow() to string("owner")
            }

}