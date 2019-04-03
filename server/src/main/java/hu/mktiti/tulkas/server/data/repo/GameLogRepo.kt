package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.GameLog
import java.util.*

data class MatchLogWithActors(
        val gameLog: GameLog,
        val botA: Pair<Bot, String>,
        val botB: Pair<Bot, String>
)

sealed class GameBotRank(
        val botId: Long,
        val rank: Int
)

class ChallengeBotRank(
        botId: Long,
        val ratio: Double,
        rank: Int = -1
) : GameBotRank(botId, rank)

class MatchBotRank(
        botId: Long,
        val points: Int,
        rank: Int = -1
) : GameBotRank(botId, rank)

@InjectableType
interface GameLogRepo : Repo<GameLog> {

    fun matchesOfBot(id: Long): List<MatchLogWithActors>

    fun challengesOfBot(id: Long): List<GameLog>

    fun getChallengeRanking(id: Long): List<ChallengeBotRank>

    fun getMatchRanking(id: Long): List<MatchBotRank>

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class GameLogDbRepo(
        connectionSource: ConnectionSource = inject()
) : DbRepo<GameLog>(tableName, listOf("gameId", "botAId", "botBId", "time", "result", "points", "maxPoints"), connectionSource), GameLogRepo {

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

        private const val challengeRankQuery = """
            select
               b.id as botId,
               vgl.id as logId,
               gl.points as points,
               gl.maxPoints as maxPoints,
               nvl2(gl.points, nvl2(gl.maxPoints, convert(gl.points, double) / gl.maxPoints, gl.points), 0) as ratio
            from ${BotDbRepo.tableName} b
                left join (
                    select botAId as botId, max(id) as id
                    from $tableName
                    where gameId = ?
                    group by botAId
                ) vgl on b.id = vgl.botId
                left join $tableName gl on vgl.id = gl.id
            where b.gameId = ?
            order by ratio desc;
        """

        private const val matchRankQuery = """
            select b.id as botId, nvl(sum(u.points), 0) as points
            from ${BotDbRepo.tableName} b
            left join (
                select
                    gl.botAId as botId,
                    sum(decode(gl.result, 'BOT_A_WIN', 2, 'BOT_B_ERROR', 2, 'BOT_A_ERROR', 0, 'BOT_B_WIN', 0, 1)) as points
                from $tableName gl
                    join (
                        select max(id) as id
                        from $tableName
                        where gameId = ?
                        group by botAId, botBId
                    ) vgl on gl.id = vgl.id
                group by gl.botAId
            union
                select
                       gl.botBId as botId,
                       sum(decode(gl.result, 'BOT_B_WIN', 2, 'BOT_A_ERROR', 2, 'BOT_B_ERROR', 0, 'BOT_A_WIN', 0, 1)) as points
                from $tableName gl
                         join (
                              select max(id) as id
                              from $tableName
                              where gameId = ?
                              group by botAId, botBId
                              ) vgl on gl.id = vgl.id
                group by gl.botBId
            ) u on b.id = u.botId
            where b.gameId = ?
            group by b.id
            order by points desc;
        """
    }

    override fun PrefixedResultSet.mapRow() = GameLog(
            id = long("id"),
            gameId = long("gameId"),
            botAId = long("botAId"),
            botBId = longOpt("botBId"),
            time = dateTime("time"),
            result = string("result"),
            points = intOpt("points"),
            maxPoints = intOpt("maxPoints")
    )

    override fun insertMap(entity: GameLog) = with(entity) {
        listOf(gameId, botAId, botBId, time, result, points, maxPoints)
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

    override fun getChallengeRanking(id: Long): List<ChallengeBotRank> =
        selectMultiTo(challengeRankQuery, id, id) {
            long("botId") to double("ratio")
        }.fold(LinkedList()) { list, (bot, ratio) ->
            list.apply {
                val rank = when {
                    list.isEmpty() -> 1
                    list.first.ratio > ratio -> list.first.rank + 1
                    else -> list.first.rank
                }

                addFirst(ChallengeBotRank(bot, ratio, rank))
            }
        }

    override fun getMatchRanking(id: Long): List<MatchBotRank>  =
            selectMultiTo(matchRankQuery, id, id, id) {
                long("botId") to int("points")
            }.fold(LinkedList()) { list, (bot, points) ->
                list.apply {
                    val rank = when {
                        list.isEmpty() -> 1
                        list.first.points > points -> list.first.rank + 1
                        else -> list.first.rank
                    }

                    addFirst(MatchBotRank(bot, points, rank))
                }
            }

}
