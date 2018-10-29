package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.tulkas.server.data.dao.ActorLog

@TestInjectable(environment = "unit", tags = ["mem"])
class ActorLogInMemoryRepo(
        logs: List<ActorLog> = listOf()
) : InMemoryRepo<ActorLog>(logs), ActorLogRepo {

    override fun ActorLog.newId(newId: Long) = copy(id = newId)

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