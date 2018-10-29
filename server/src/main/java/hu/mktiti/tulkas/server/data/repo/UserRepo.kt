package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.User
import hu.mktiti.tulkas.server.data.security.PasswordHasher

@InjectableType
interface UserRepo : NamedEntityRepo<User> {

    fun createUser(username: String, password: String): Long

    fun authenticate(username: String, password: String): User?

}

@Injectable(arity = InjectableArity.SINGLETON, tags = ["hsqldb"], default = true)
class UserDbRepo(
        connectionSource: ConnectionSource = inject(),
        private val hasher: PasswordHasher = inject()
) : NamedEntityDbRepo<User>(tableName, listOf("name", "password"), connectionSource), UserRepo {

    companion object {
        const val tableName = "User"
    }

    override fun PrefixedResultSet.mapRow() = User(
            id       = long("id"),
            name     = string("name"),
            passHash = string("password")
    )

    override fun insertMap(entity: User) = listOf(entity.name, entity.passHash)

    override fun createUser(username: String, password: String) = save(User(
            name = username,
            passHash = hasher.hash(password)
    ))

    override fun authenticate(username: String, password: String): User? = findByName(username)?.let { user ->
        if (hasher.validate(password, user.passHash)) user else null
    }

}

fun main(args: Array<String>) {
    with(inject<UserRepo>()) {
        println("Users:")
        listAll().forEach { println("\t$it") }
        createUser(username = "mktiti", password = "password")
        println("Users:")
        listAll().forEach { println("\t$it") }
    }
}