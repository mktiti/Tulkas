package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.ConnectionSource
import hu.mktiti.tulkas.server.data.Game
import java.sql.ResultSet

@Injectable
class GameRepo(
        connectionSource: ConnectionSource = inject()
) : Repo<Game>("Game", connectionSource) {

    override fun mapRow(resultSet: ResultSet, prefix: String) = Game(
            id = resultSet.getLong(prefix and "id"),
            ownerId = resultSet.getLong(prefix and "ownerId"),
            name = resultSet.getString(prefix and "name"),
            isMatch = resultSet.getBoolean(prefix and "isMatch"),
            apiJar = resultSet.getBytes(prefix and "apiJar"),
            engineJar = resultSet.getBytes(prefix and "engineJar")
    )

    fun gamesOf(ownerId: Long): List<Game> =
            selectMulti("select * from $tableName where ownerId = ?", ownerId)

}