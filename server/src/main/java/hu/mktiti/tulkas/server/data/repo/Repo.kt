package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.Entity
import hu.mktiti.tulkas.server.data.useWith
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement

interface Repo<T : Entity> {

    fun find(id: Long): T?

    fun listAll(): List<T>

    fun delete(id: Long)

    fun delete(entity: T) = delete(entity.id)

    fun deleteAll()

    fun save(entity: T): Long

    fun saveAll(entities: Iterable<T>): List<Long>

}

abstract class DbRepo<T : Entity>(
        private val tableName: String,
        insertCols: List<String>,
        private val connectionSource: ConnectionSource
) : Repo<T> {

    private val insertCommand = "insert into $tableName (${insertCols.joinToString()}) values (${insertCols.joinToString { "?" }});"

    protected abstract fun PrefixedResultSet.mapRow(): T

    protected abstract fun insertMap(entity: T): List<Any?>

    protected fun mapSingle(resultSet: ResultSet, prefix: String = ""): T?
        = resultSet.use { if (resultSet.next()) PrefixedResultSet(resultSet, prefix).mapRow() else null }

    protected fun mapAll(resultSet: ResultSet, prefix: String = ""): List<T> {
        val prefixedRS = PrefixedResultSet(resultSet, prefix)
        return resultSet.mapAll {
            prefixedRS.mapRow()
        }
    }

    override fun find(id: Long): T? = selectSingle("select * from $tableName where id = ? order by id;", id)

    override fun listAll(): List<T> = selectMulti("select * from $tableName order by id;")

    protected fun <R> select(
            query: String,
            setter: PreparedStatement.() -> Unit,
            transformer: (ResultSet) -> R): R = withConnection {

        prepareStatement(query).useWith {
            setter()
            executeQuery().use(transformer)
        }
    }

    protected fun <R> select(
            query: String,
            params: List<Any?>,
            transformer: (ResultSet) -> R): R = select(query, transformer = transformer, setter = {
        params.forEachIndexed { i, p ->
            setObject(i + 1, p)
        }
    })

    protected fun selectSingle(query: String, prefix: String = "", setter: PreparedStatement.() -> Unit): T? =
            select(query, setter) { rs -> mapSingle(rs, prefix) }

    protected fun selectSingle(query: String, params: List<Any?> = emptyList(), prefix: String = ""): T? =
            select(query, params) { rs -> mapSingle(rs, prefix) }

    protected fun selectSingle(query: String, vararg params: Any?, prefix: String = ""): T? =
            selectSingle(query, params.asList(), prefix)

    protected fun selectMulti(query: String, prefix: String = "", setter: PreparedStatement.() -> Unit): List<T> =
            select(query, setter) { rs -> mapAll(rs, prefix) }

    protected fun selectMulti(query: String, params: List<Any?> = emptyList(), prefix: String = ""): List<T> =
            select(query, params) { rs -> mapAll(rs, prefix) }

    protected fun selectMulti(query: String, vararg params: Any?, prefix: String = ""): List<T> =
            selectMulti(query, params.asList(), prefix)

    protected fun <R : Any> selectMultiTo(
            query: String,
            vararg params: Any?,
            rowTransformer: PrefixedResultSet.() -> R
    ): List<R> =
            select(query, params = params.toList()) { rs ->
                PrefixedResultSet(rs, "").mapAll(rowTransformer)
            }

    protected fun <R : Any> selectMultiTo(
            query: String,
            vararg params: Any?,
            prefixedCreator: () -> List<String>,
            rowTransformer: (PrefixedResultSet, List<PrefixedResultSet>) -> R
    ): List<R> = select(query, params.toList()) { rs ->
        val prefixed = PrefixedResultSet(rs)
        val rsViews = prefixedCreator().map { PrefixedResultSet(rs, it) }
        rs.mapAll { rowTransformer(prefixed, rsViews) }
    }

    protected fun <R> runUpdate(
            query: String,
            setter: PreparedStatement.() -> Unit = {},
            transformer: PreparedStatement.() -> R): R = withConnection {
        prepareStatement(query, Statement.RETURN_GENERATED_KEYS).useWith {
            setter()
            executeUpdate()
            transformer()
        }
    }

    protected fun <R> runUpdate(query: String, params: List<Any?>, transformer: PreparedStatement.() -> R): R =
        runUpdate(query, transformer = transformer, setter = { setAllParams(params) })

    protected fun <R> runUpdate(
            query: String,
            vararg params: Any?,
            transformer: PreparedStatement.() -> R
    ): R = runUpdate(query, params.toList(), transformer)

    override fun delete(id: Long) = runUpdate("delete from $tableName where id = ?;", id) {}

    override fun deleteAll() = runUpdate("delete from $tableName;") {}

    override fun save(entity: T): Long = runUpdate(insertCommand, insertMap(entity)) {
        generatedKeys.useWith {
            next()
            getLong(1)
        }
    }

    override fun saveAll(entities: Iterable<T>): List<Long> =
        if (entities.firstOrNull() == null) {
            emptyList()
        } else {
            transaction {
                prepare(insertCommand).useWith {
                    for (entity in entities) {
                        setAllParams(insertMap(entity))
                        addBatch()
                    }
                    executeBatch()
                    generatedKeys.mapAll { getLong(1) }
                }
            }
        }

}