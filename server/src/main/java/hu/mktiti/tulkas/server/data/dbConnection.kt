package hu.mktiti.tulkas.server.data

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.property.property
import java.sql.Connection
import java.sql.DriverManager

@InjectableType
interface ConnectionSource {

    operator fun invoke(): Connection

}

@Injectable(arity = InjectableArity.SINGLETON_AUTOSTART, default = true, tags = ["jdbc"])
class JdbcConnectionSource(
        driverClass: String = property("Server.Database.DriverClass"),
        private val connectionString: String = property("Server.Database.ConnectionString"),
        private val username: String = property("Server.Database.Username"),
        private val password: String = property("Server.Database.Password")
) : ConnectionSource {

    init {
        Class.forName(driverClass)
    }

    override operator fun invoke(): Connection = DriverManager.getConnection(connectionString, username, password)

}