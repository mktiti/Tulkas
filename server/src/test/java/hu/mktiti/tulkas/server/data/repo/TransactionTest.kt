package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.PreparedStatement
import java.util.*
import kotlin.test.assertEquals

internal class TransactionTest {

    private class TestTable(
            tableName: String
    ) {
        val createStatement = "create table $tableName (value int primary key);"
        val dropStatement = "drop table if exists $tableName;"
        val insertStatement = "insert into $tableName values (?);"
        val selectQuery = "select value from $tableName;"
    }

    private val testTables = (0 until 10).map { TestTable(tableName = "TestTable_$it") }

    @BeforeEach
    fun resetDb() {
        // Create empty table for transaction testing
        with (inject<ConnectionSource>().invoke()) {
            testTables.forEach { prepareStatement(it.dropStatement).use(PreparedStatement::execute) }
            testTables.forEach { prepareStatement(it.createStatement).use(PreparedStatement::execute) }
        }
    }

    @Test
    fun `test simple connection commit`() {
        transaction {
            for (i in 1..10) {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, i)
                    prepared.executeUpdate()
                }
            }
        }

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals((1..10).toList(), queryResult)
                }
            }

        }
    }

    @Test
    fun `test transaction rollback atomicity`() {
        val success = guardedTransaction {
            for (i in 1..10) {
                if (i == 5) {
                    rollback()
                }

                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, i)
                    prepared.executeUpdate()
                }
            }
        }

        assertEquals(false, success)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(), queryResult)
                }
            }

        }
    }

    @Test
    fun `test flattened transaction rollback atomicity`() {
        val success = guardedTransaction {
            transaction {
                for (i in 1..10) {
                    prepare(testTables.first().insertStatement).use { prepared ->
                        prepared.setInt(1, i)
                        prepared.executeUpdate()
                    }
                }
            }

            transaction {
                for (i in 11..20) {
                    if (i == 15) {
                        rollback()
                    }

                    prepare(testTables.first().insertStatement).use { prepared ->
                        prepared.setInt(1, i)
                        prepared.executeUpdate()
                    }
                }
            }

            transaction {
                for (i in 21..30) {
                    prepare(testTables.first().insertStatement).use { prepared ->
                        prepared.setInt(1, i)
                        prepared.executeUpdate()
                    }
                }
            }

        }

        assertEquals(false, success)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(), queryResult)
                }
            }

        }
    }

    @Test
    fun `test deep flattened transaction success`() {
        val success = guardedTransaction {
            fun insertAll(values: Stack<Int>) {
                if (values.isEmpty()) return

                transaction {
                    prepare(testTables.first().insertStatement).use { prepared ->
                        prepared.setInt(1, values.pop())
                        prepared.executeUpdate()
                    }
                    insertAll(values)
                }
            }

            insertAll(Stack<Int>().apply { addAll((1..50)) })
        }

        assertEquals(true, success)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals((1..50).toList(), queryResult)
                }
            }

        }
    }

    @Test
    fun `test deep flattened transaction rollback atomicity`() {
        val success = guardedTransaction {
            fun insertAll(values: Stack<Int>) {
                if (values.isEmpty()) rollback()

                transaction {
                    prepare(testTables.first().insertStatement).use { prepared ->
                        prepared.setInt(1, values.pop())
                        prepared.executeUpdate()
                    }
                    insertAll(values)
                }
            }

            insertAll(Stack<Int>().apply { addAll((1..50)) })
        }

        assertEquals(false, success)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(emptyList(), queryResult)
                }
            }

        }
    }

    @Test
    fun `test nested guarded transaction rollback`() {
        transaction {
            transaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, 1)
                    prepared.executeUpdate()
                }
            }

            val result = guardedTransaction {
                rollback()
            }

            transaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, if (result) 2 else 3)
                    prepared.executeUpdate()
                }
            }
        }

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(1, 3), queryResult)
                }
            }

        }
    }

    @Test
    fun `test nested guarded transaction sibling rollback`() {
        val parentResult = guardedTransaction {
            transaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, 1)
                    prepared.executeUpdate()
                }
            }

            guardedTransaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, 2)
                    prepared.executeUpdate()
                }
            }

            transaction {
                rollback()
            }
        }

        assertEquals(false, parentResult)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(emptyList(), queryResult)
                }
            }

        }
    }

    @Test
    fun `test nested new transaction rollback`() {
        transaction {
            transaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, 1)
                    prepared.executeUpdate()
                }
            }

            val result = newTransaction {
                rollback()
            }

            transaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, if (result) 2 else 3)
                    prepared.executeUpdate()
                }
            }
        }

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(1, 3), queryResult)
                }
            }

        }
    }

    @Test
    fun `test nested new transaction sibling rollback`() {
        val parentResult = guardedTransaction {
            transaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, 1)
                    prepared.executeUpdate()
                }
            }

            // Use different table to avoid deadlock
            newTransaction {
                prepare(testTables[1].insertStatement).use { prepared ->
                    prepared.setInt(1, 2)
                    prepared.executeUpdate()
                }
            }

            transaction {
                rollback()
            }
        }

        assertEquals(false, parentResult)

        transaction {
            prepare(testTables[0].selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(emptyList(), queryResult)
                }
            }

            prepare(testTables[1].selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(2), queryResult)
                }
            }
        }
    }

    @Test
    fun `test deep new nested transaction`() {
        val success = guardedTransaction {
            fun insertAll(values: Stack<Int>) {
                if (values.isEmpty()) return

                newTransaction {
                    val i = values.pop()
                    prepare(testTables[i].insertStatement).use { prepared ->
                        prepared.setInt(1, i)
                        prepared.executeUpdate()
                    }
                    insertAll(values)
                }
            }

            insertAll(Stack<Int>().apply { addAll((0 until testTables.size)) })
        }

        assertEquals(true, success)

        (0 until testTables.size).forEach { i ->
            transaction {
                prepare(testTables[i].selectQuery).use { prepared ->
                    prepared.executeQuery().use { result ->
                        val queryResult = result.mapAll { getInt(1) }.sorted()

                        assertEquals(listOf(i), queryResult)
                    }
                }

            }
        }
    }

    @Test
    fun `test deep new nested transaction rollback`() {
        val success = guardedTransaction {
            fun insertAll(values: Stack<Int>) {
                if (values.isEmpty()) return

                newTransaction {
                    val i = values.pop()

                    if (i == testTables.size / 2) {
                        rollback()
                    }

                    prepare(testTables[i].insertStatement).use { prepared ->
                        prepared.setInt(1, i)
                        prepared.executeUpdate()
                    }
                    insertAll(values)
                }
            }

            insertAll(Stack<Int>().apply { addAll((0 until testTables.size).reversed()) })
        }

        assertEquals(true, success)

        (0 until testTables.size).forEach { i ->
            transaction {
                prepare(testTables[i].selectQuery).use { prepared ->
                    prepared.executeQuery().use { result ->
                        val queryResult = result.mapAll { getInt(1) }.sorted()

                        assertEquals(if (i < testTables.size / 2) listOf(i) else emptyList(), queryResult)
                    }
                }

            }
        }
    }

    @Test
    fun `test consecutive transactions`() {
        val results: List<Boolean> = (0..10).map { i ->
            guardedTransaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, i)
                    prepared.executeUpdate()
                }
                if (i % 2 == 0) {
                    rollback()
                }
            }
        }

        assertEquals((0..10).map { it % 2 != 0 }.toList(), results)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(1, 3, 5, 7, 9), queryResult)
                }
            }

        }
    }

    @Test
    fun `test sql constraint exception in guarded nested`() {
        val root = guardedTransaction {
            prepare(testTables.first().insertStatement).use { prepared ->
                prepared.setInt(1, 1)
                prepared.executeUpdate()
            }

            val inner = guardedTransaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    prepared.setInt(1, 1)
                    prepared.executeUpdate()
                }
            }

            assertEquals(false, inner)

            prepare(testTables.first().insertStatement).use { prepared ->
                prepared.setInt(1, 2)
                prepared.executeUpdate()
            }
        }

        assertEquals(true, root)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(1, 2), queryResult)
                }
            }

        }
    }

    @Test
    fun `test sql exception in guarded nested`() {
        val root = guardedTransaction {
            prepare(testTables.first().insertStatement).use { prepared ->
                prepared.setInt(1, 1)
                prepared.executeUpdate()
            }

            val inner = guardedTransaction {
                prepare(testTables.first().insertStatement).use { prepared ->
                    // Missing parameter
                    prepared.executeUpdate()
                }
            }

            assertEquals(false, inner)

            prepare(testTables.first().insertStatement).use { prepared ->
                prepared.setInt(1, 2)
                prepared.executeUpdate()
            }
        }

        assertEquals(true, root)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(1, 2), queryResult)
                }
            }

        }
    }

    @Test
    fun `test without transaction rollback`() {
        val root = guardedTransaction {
            prepare(testTables.first().insertStatement).use { prepared ->
                prepared.setInt(1, 1)
                prepared.executeUpdate()
            }

            withoutTransaction {
                prepareStatement(testTables[1].insertStatement).use { prepared ->
                    prepared.setInt(1, 2)
                    prepared.executeUpdate()
                }

                prepareStatement(testTables[1].insertStatement).use { prepared ->
                    prepared.setInt(1, 2) // Error
                    prepared.executeUpdate()
                }
            }

            prepare(testTables.first().insertStatement).use { prepared ->
                prepared.setInt(1, 3)
                prepared.executeUpdate()
            }
        }

        assertEquals(false, root)

        transaction {
            prepare(testTables.first().selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(emptyList(), queryResult)
                }
            }
        }

        transaction {
            prepare(testTables[1].selectQuery).use { prepared ->
                prepared.executeQuery().use { result ->
                    val queryResult = result.mapAll { getInt(1) }.sorted()

                    assertEquals(listOf(2), queryResult)
                }
            }
        }
    }
}