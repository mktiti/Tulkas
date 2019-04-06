package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.User
import hu.mktiti.tulkas.server.data.security.PasswordHasher

@TestInjectable(environment = "unit", tags = ["mem"])
class UserInMemoryRepo(
        users: List<User> = listOf(),
        private val hasher: PasswordHasher = inject()
) : NamedEntityInMemoryRepo<User>(users), UserRepo {

    override fun User.newId(newId: Long): User = copy(id = newId)

    override fun createUser(username: String, password: String): Long = save(
            User(id = -1, name = username, passHash = hasher.hash(password))
    )

    override fun authenticate(username: String, password: String): User? {
        val user = findByName(username)
        return if (user != null && user.passHash == hasher.hash(password)) user else null
    }

}