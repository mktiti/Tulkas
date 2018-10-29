package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.ActorLog
import hu.mktiti.tulkas.server.data.dao.ConnectionSource

@InjectableType
interface ActorLogRepo : Repo<ActorLog> {

    fun saveLog(gameId: Long, target: String, messages: List<Pair<String, String>>): List<Long>

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class ActorLogDbRepo(
        connectionSource: ConnectionSource = inject()
) : DbRepo<ActorLog>(tableName, listOf("gameId", "sender", "target", "relativeIndex", "message"), connectionSource), ActorLogRepo {

    companion object {
        const val tableName = "ActorLog"
    }

    override fun PrefixedResultSet.mapRow() = ActorLog(
            id = long("id"),
            gameId = long("gameId"),
            sender = string("sender"),
            target = string("target"),
            relativeIndex = int("relativeIndex"),
            message = string("message")
    )

    override fun insertMap(entity: ActorLog) = with(entity) {
        listOf(gameId, sender, target, relativeIndex, message)
    }

    override fun saveLog(gameId: Long, target: String, messages: List<Pair<String, String>>): List<Long> =
        saveAll(messages.mapIndexed { i, (sender, message) ->
            ActorLog(
                    id = -1,
                    gameId = gameId,
                    sender = sender,
                    target = target,
                    message = message,
                    relativeIndex = i
            )
        })

}