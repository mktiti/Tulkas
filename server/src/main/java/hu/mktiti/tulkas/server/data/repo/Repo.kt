package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.tulkas.server.data.ConnectionSource
import hu.mktiti.tulkas.server.data.Entity
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

abstract class Repo<T : Entity>(
        protected val tableName: String,
        private val connectionSource: ConnectionSource
) {

    abstract fun mapRow(resultSet: ResultSet, prefix: String = ""): T

    infix fun String.and(field: String) = if (this == "") field else "${this}_$field"

    fun mapSingle(resultSet: ResultSet, prefix: String = ""): T?
        = if (resultSet.next()) mapRow(resultSet, prefix) else null

    fun mapAll(resultSet: ResultSet, prefix: String = ""): List<T>
        = generateSequence {
            if (resultSet.next()) mapRow(resultSet, prefix) else null
        }.toList()

    fun find(id: Long): T? = selectSingle("select * from $tableName where id = ?", id)

    fun listAll(): List<T> = selectMulti("select * from $tableName")

    fun <R> select(
            query: String,
            setter: PreparedStatement.() -> Unit,
            transformer: (ResultSet) -> R): R = withConn {

        prepareStatement(query).use { prepared ->
            prepared.setter()
            prepared.executeQuery().use(transformer)
        }
    }

    fun <R> select(
            query: String,
            params: List<Any?>,
            transformer: (ResultSet) -> R): R = withConn {

        prepareStatement(query).use { prepared ->
            with(prepared) {
                params.forEachIndexed { i, p ->
                    setObject(i, p)
                }
                executeQuery().use(transformer)
            }
        }
    }

    fun selectSingle(query: String, prefix: String = "", setter: PreparedStatement.() -> Unit): T? =
            select(query, setter) { rs -> mapSingle(rs, prefix) }

    fun selectSingle(query: String, params: List<Any?> = emptyList(), prefix: String = ""): T? =
            select(query, params) { rs -> mapSingle(rs, prefix) }

    fun selectSingle(query: String, vararg params: Any?, prefix: String = ""): T? =
            selectSingle(query, params.asList(), prefix)

    fun selectMulti(query: String, prefix: String = "", setter: PreparedStatement.() -> Unit): List<T> =
            select(query, setter) { rs -> mapAll(rs, prefix) }

    fun selectMulti(query: String, params: List<Any?> = emptyList(), prefix: String = ""): List<T> =
            select(query, params) { rs -> mapAll(rs, prefix) }

    fun selectMulti(query: String, vararg params: Any?, prefix: String = ""): List<T> =
            selectMulti(query, params.asList(), prefix)

    private inline fun <T> withConn(block: Connection.() -> T): T = connectionSource().use(block)

}