package hu.mktiti.tulkas.server.data.handler

import hu.mktiti.tulkas.server.data.dao.Bot
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.dao.JarData
import hu.mktiti.tulkas.server.data.dao.User
import hu.mktiti.tulkas.server.data.dto.SimpleUserDto
import hu.mktiti.tulkas.server.data.repo.BotRepo
import hu.mktiti.tulkas.server.data.repo.GameRepo
import hu.mktiti.tulkas.server.data.repo.JarDataRepo
import hu.mktiti.tulkas.server.data.repo.UserRepo
import hu.mktiti.tulkas.server.data.repo.inmem.BotInMemoryRepo
import hu.mktiti.tulkas.server.data.repo.inmem.GameInMemoryRepo
import hu.mktiti.tulkas.server.data.repo.inmem.JarDataInMemoryRepo
import hu.mktiti.tulkas.server.data.repo.inmem.UserInMemoryRepo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UserHandlerTest {

    companion object {
        private val testUsers = listOf(
                User(id = 0, name = "Alice",    passHash = "hunter2"),
                User(id = 1, name = "Bob",      passHash = "bob"),
                User(id = 2, name = "Charlie",  passHash = "paddyspub"),
                User(id = 3, name = "Daniel",   passHash = "asd"),
                User(id = 4, name = "username", passHash = "password")
        )

        private val testJarData = listOf(JarData(id = 0, data = ByteArray(0)))

        private val testGames = listOf(
                Game(id = 0, ownerId = 0, name = "Alice's challenge", isMatch = false, apiJarId = 0, engineJarId = 0),
                Game(id = 1, ownerId = 0, name = "Alice's match", isMatch = true, apiJarId = 0, engineJarId = 0),
                Game(id = 2, ownerId = 1, name = "Bob's challenge", isMatch = false, apiJarId = 0, engineJarId = 0),
                Game(id = 3, ownerId = 2, name = "Charlie's match", isMatch = true, apiJarId = 0, engineJarId = 0)
        )

        private val testBots = listOf(
                Bot(id = 0, gameId = 0, ownerId = 0, name = "Alice challenge reference", rank = null, jarId = 0),
                Bot(id = 1, gameId = 0, ownerId = 1, name = "Alice challenge - Bob's bot", rank = null, jarId = 0),
                Bot(id = 2, gameId = 1, ownerId = 0, name = "Alice match reference", rank = null, jarId = 0),
                Bot(id = 3, gameId = 1, ownerId = 1, name = "Alice match - Bob's boss", rank = 1, jarId = 0),
                Bot(id = 4, gameId = 1, ownerId = 2, name = "Alice match - Charlie's chief", rank = 2, jarId = 0),
                Bot(id = 5, gameId = 1, ownerId = 3, name = "Alice match - Daniel's distinguished", rank = 3, jarId = 0),
                Bot(id = 6, gameId = 2, ownerId = 1, name = "Bob challenge reference", rank = null, jarId = 0),
                Bot(id = 6, gameId = 2, ownerId = 3, name = "Bob challenge - Username's unbeaten", rank = null, jarId = 0)
        )
    }

    private lateinit var userHandler: UserHandler

    @BeforeEach
    fun setup() {
        val userRepo: UserRepo = UserInMemoryRepo(testUsers)
        val jarDataRepo: JarDataRepo = JarDataInMemoryRepo(testJarData)
        val gameRepo: GameRepo = GameInMemoryRepo(testGames, userRepo, jarDataRepo)
        val botRepo: BotRepo = BotInMemoryRepo(testBots, userRepo, gameRepo, jarDataRepo)

        userHandler = UserHandler(
                userRepo = userRepo,
                botRepo = botRepo,
                gameRepo = gameRepo
        )
    }

    @Test
    fun `test list all null search`() {
        val list = userHandler.listAll(null)
        assertEquals(testUsers.map { SimpleUserDto(it.name) }, list)
    }

    @Test
    fun `test list all empty search`() {
        val list = userHandler.listAll("")
        assertEquals(testUsers.map { SimpleUserDto(it.name) }, list)
    }

    @Test
    fun `test list all with search`() {
        val list = userHandler.listAll("a")
        assertEquals(listOf("Alice", "Charlie", "Daniel", "username").map(::SimpleUserDto), list)
    }

}