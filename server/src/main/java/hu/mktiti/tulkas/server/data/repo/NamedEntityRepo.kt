package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import hu.mktiti.tulkas.server.data.dao.NamedEntity

interface NamedEntityRepo<T : NamedEntity> : Repo<T> {

    fun findByName(name: String): T?

    fun searchNameContaining(namePart: String): List<T>

}

abstract class NamedEntityDbRepo<T : NamedEntity>(
        private val tableName: String,
        insertCols: List<String>,
        connectionSource: ConnectionSource
) : NamedEntityRepo<T>, DbRepo<T>(
        tableName, insertCols, connectionSource
) {

    override fun findByName(name: String): T? = selectSingle("select * from $tableName where name = ?", name)

    override fun searchNameContaining(namePart: String): List<T> =
            selectMulti("select * from $tableName where name like ? order by id", "%$namePart%")

}