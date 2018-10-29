package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.GameLog

@InjectableType
interface GameLogRepo : Repo<GameLog>

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class GameLogDbRepo(
        connectionSource: ConnectionSource = inject()
) : DbRepo<GameLog>(tableName, listOf("gameId", "botAId", "botBId", "time", "result"), connectionSource), GameLogRepo {

    companion object {
        const val tableName = "GameLog"
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

}
