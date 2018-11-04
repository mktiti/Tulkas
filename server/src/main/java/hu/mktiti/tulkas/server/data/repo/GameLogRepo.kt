package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.GameLog

data class MatchLogWithActors(
        val gameLog: GameLog,
        val botA: Pair<Bot, String>,
        val botB: Pair<Bot, String>
)

@InjectableType
interface GameLogRepo : Repo<GameLog> {

    fun matchesOfBot(id: Long): List<MatchLogWithActors>

    fun challengesOfBot(id: Long): List<GameLog>

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class GameLogDbRepo(
        connectionSource: ConnectionSource = inject()
) : DbRepo<GameLog>(tableName, listOf("gameId", "botAId", "botBId", "time", "result"), connectionSource), GameLogRepo {

    companion object {
        const val tableName = "GameLog"

        private val selectMatchesOfBot = """
            select gl.*,
                ${BotDbRepo.prefixedCols("ba")}, ua.name as aOwner,
                ${BotDbRepo.prefixedCols("bb")}, ub.name as bOwner
            from $tableName gl
                join ${BotDbRepo.tableName} ba on ba.id = gl.botAId
                    join ${UserDbRepo.tableName} ua on ua.id = ba.ownerId
                join ${BotDbRepo.tableName} bb on bb.id = gl.botBId
                    join ${UserDbRepo.tableName} ub on ub.id = bb.ownerId
            where ba.id = ? or bb.id = ?
            order by gl.time desc
        """

        private val selectChallengesOfBot = """
            select gl.*
            from $tableName gl
            where gl.botAId = ?
            order by gl.time desc
        """
    }

    override fun PrefixedResultSet.mapRow() = GameLog(
            id = long("id"),
            gameId = long("gameId"),
            botAId = long("botAId"),
            botBId = longOpt("botBId"),
            time = dateTime("time"),
            result = string("result")
    )

    override fun insertMap(entity: GameLog) = with(entity) {
        listOf(gameId, botAId, botBId, time, result)
    }

    override fun matchesOfBot(id: Long): List<MatchLogWithActors> =
            selectMultiTo(
                    query = selectMatchesOfBot,
                    prefixedCreator = { listOf("ba", "bb") },
                    params = *arrayOf(id, id))
            { rs, views ->
                MatchLogWithActors(
                        gameLog = rs.mapRow(),
                        botA = BotDbRepo.staticMapRow(views[0]) to rs.string("aOwner"),
                        botB = BotDbRepo.staticMapRow(views[1]) to rs.string("bOwner")
                )
            }


    override fun challengesOfBot(id: Long): List<GameLog> = selectMulti(selectChallengesOfBot, id)

}
