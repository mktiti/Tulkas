package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.tulkas.server.data.DbUtil
import hu.mktiti.tulkas.server.data.dao.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class UserDbRepoTest {

    private val repo = UserDbRepo()

    private val testUsers = listOf(
            User(id = 0, name = "alice",    passHash = "alice12345"),
            User(id = 1, name = "bob",      passHash = "bob"),
            User(id = 2, name = "charlie",  passHash = "paddyspub"),
            User(id = 3, name = "daniel",   passHash = "asd"),
            User(id = 4, name = "username", passHash = "password")
    )

    @BeforeEach
    fun resetDb() {
        DbUtil.recreateDb()
    }

    @Test
    fun `test list all`() {
        val users = repo.listAll()

        assertEquals(testUsers.size, users.size)
        testUsers.zip(users).forEachIndexed { i, (expected, actual) ->
            assert(expected sameAs actual) {
                "Expected user: $expected, actual: $actual (index: $i)"
            }
        }
    }

    @Test
    fun `test insert`() {
        val toInsert = User(name = "newuser", passHash = "password")
        repo.save(toInsert)

        val inserted = repo.listAll().last()

        assert(toInsert contentEq inserted)
    }

    @Test
    fun `test insert all`() {
        val ids = repo.saveAll(
                (0..10).map { i -> User(name = "user$i", passHash = "pass$i") }
        )

        val newUsers = repo.listAll().drop(testUsers.size)

        assert(
            newUsers.asSequence().mapIndexed { i, u ->
                u.id == ids[i] && u.name == "user$i" && u.passHash == "pass$i"
            }.all { it }
        )
    }

    @Test
    fun `test user create`() {
        val id = repo.createUser("jondoe", "password")

        val inserted = repo.listAll().last()

        assert(inserted sameAs User(id, "jondoe", "password"))
    }

    @Test
    fun `test find by name`() {
        val charlie = repo.findByName("charlie")

        assert(charlie != null && charlie sameAs testUsers[2])
    }

    @Test
    fun `test find by name no result`() {
        val noOne = repo.findByName("noOne")

        assertNull(noOne)
    }

    @Test
    fun `test search by name`() {
        val containsA = repo.searchNameContaining("a").sortedBy { it.name }
        val names = containsA.map(User::name)

        assertEquals(listOf("alice", "charlie", "daniel", "username"), names)
    }

    @Test
    fun `test authenticate success`() {
        val result = repo.authenticate("alice", "alice12345")

        assertEquals("alice", result?.name)
    }

    @Test
    fun `test authenticate invalid password`() {
        val result = repo.authenticate("alice", "invalidPassword")

        assertNull(result)
    }

    @Test
    fun `test authenticate non existing`() {
        val result = repo.authenticate("noOne", "asd")

        assertNull(result)
    }
}