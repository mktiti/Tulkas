package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.ConnectionSource
import hu.mktiti.tulkas.server.data.User
import java.sql.ResultSet

@Injectable
class UserRepo(
        connectionSource: ConnectionSource = inject()
) : Repo<User>("User", connectionSource) {

    override fun mapRow(resultSet: ResultSet, prefix: String) = User(
            id =       resultSet.getLong(prefix and "id"),
            username = resultSet.getString(prefix and "username"),
            passHash = resultSet.getString(prefix and "passHash")
    )

    fun findByUserName(username: String): User? =
            selectSingle("select * from $tableName where username = ?", username)

}