package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.tulkas.server.data.DbUtil
import hu.mktiti.tulkas.server.data.dao.ActorLog
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ActorLogDbRepoTest {

    private val repo = ActorLogDbRepo()

    @BeforeEach
    fun resetDb() {
        DbUtil.recreateDb()
        repo.deleteAll()
    }

    @Test
    fun `test insert`() {
        val toInsert = ActorLog(
                gameId = 0,
                sender = "ENGINE",
                target = "BOT_A",
                relativeIndex = 0,
                message = "Test message (Engine => BotA)"
        )

        repo.save(toInsert)

        val inserted = repo.listAll().single()

        assert(toInsert contentEquls inserted)
    }

    @Test
    fun `test save log`() {
        val senders = listOf("RUNTIME", "ENGINE", "SELF")
        val messages = (1..10).map {
            senders[it % senders.size] to "message $it"
        }

        repo.saveLog(0, "BOT_A", messages)

        val all = repo.listAll()

        assertEquals(messages.size, all.size)
        messages.zip(all).forEachIndexed { i, (expected, actual) ->
            val (sender, message) = expected
            assertEquals(0, actual.gameId)
            assertEquals(sender, actual.sender)
            assertEquals("BOT_A", actual.target)
            assertEquals(i, actual.relativeIndex)
            assertEquals(message, actual.message)
        }
    }

}