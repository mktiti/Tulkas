package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.JarData

@InjectableType
interface BotRepo : Repo<Bot> {

    fun createBot(ownerId: Long, gameId: Long, name: String, jar: ByteArray): Long?

    fun botsOf(ownerId: Long): List<Bot>

    fun botsOf(ownerUsername: String): List<Pair<Bot, String>>

    fun botsByGame(gameId: Long): List<Pair<Bot, String>>

    fun botByUserAndName(username: String, name: String): Bot?

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class BotDbRepo(
        private val jarDataRepo: JarDataRepo = inject(),
        connectionSource: ConnectionSource = inject()
) : DbRepo<Bot>(tableName, listOf("gameId", "ownerId", "name", "jarId"), connectionSource), BotRepo {

    companion object {
        const val tableName = "Bot"

        val columns = listOf("id", "gameId", "ownerId", "name", "jarId")

        fun prefixedCols(prefix: String) = columns.joinToString(separator = ", ") { "$prefix.$it as ${prefix}_$it" }

        fun staticMapRow(prefixed: PrefixedResultSet) = with(prefixed) {
            Bot(
                id = long("id"),
                gameId = long("gameId"),
                ownerId = long("ownerId"),
                name = string("name"),
                jarId = long("jarId")
            )
        }

        private const val selectByUsernameQuery = """
            select b.*, g.name as gameName
            from $tableName b
            join ${UserDbRepo.tableName} u on b.ownerId = u.id
            join ${GameDbRepo.tableName} g on b.gameId = g.id
            where u.name = ?
            order by b.id
        """

        private const val selectByGameWithOwner = """
            select b.*, o.name as ownerName
            from $tableName b
            join ${UserDbRepo.tableName} o on b.ownerId = o.id
            where b.gameId = ?
            order by b.id
        """

        private const val selectByOwnerAndName = """
            select b.*
            from $tableName b
            join ${UserDbRepo.tableName} o on b.ownerId = o.id
            where o.name = ?
                and b.name = ?
        """
    }

    override fun PrefixedResultSet.mapRow() = staticMapRow(this)

    override fun insertMap(entity: Bot) = with(entity) {
        listOf(gameId, ownerId, name, jarId)
    }

    override fun createBot(
            ownerId: Long,
            gameId: Long,
            name: String,
            jar: ByteArray
    ) = guardedTransaction<Long> {
        save(Bot(
            ownerId = ownerId,
            gameId = gameId,
            name = name,
            jarId = jarDataRepo.save(JarData(data = jar))
        ))
    }

    override fun botsOf(ownerId: Long): List<Bot> =
            selectMulti("select * from $tableName where ownerId = ?", ownerId)

    override fun botsOf(ownerUsername: String): List<Pair<Bot, String>> =
            selectMultiTo(selectByUsernameQuery, ownerUsername) {
                mapRow() to string("gameName")
            }

    override fun botsByGame(gameId: Long): List<Pair<Bot, String>> =
            selectMultiTo(selectByGameWithOwner, gameId) {
                mapRow() to string("ownerName")
            }

    override fun botByUserAndName(username: String, name: String): Bot? =
            selectSingle(selectByOwnerAndName, username, name)

}