package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.Bot
import hu.mktiti.tulkas.server.data.ConnectionSource
import java.sql.ResultSet

@Injectable
class BotRepo(
        connectionSource: ConnectionSource = inject()
) : Repo<Bot>("Bot", connectionSource) {

    override fun mapRow(resultSet: ResultSet, prefix: String) = Bot(
            id =      resultSet.getLong(prefix and "id"),
            gameId =  resultSet.getLong(prefix and "gameId"),
            ownerId = resultSet.getLong(prefix and "ownerId"),
            name =    resultSet.getString(prefix and "name"),
            jar =     resultSet.getBytes(prefix and "jar")
    )

    fun botsOf(ownerId: Long): List<Bot> =
            selectMulti("select * from $tableName where ownerId = ?", ownerId)

    fun botsByGame(gameId: Long): List<Bot> =
            selectMulti("select * from $tableName where gameId = ?", gameId)

}