package hu.mktiti.tulkas.server.data.repo

import java.sql.ResultSet
import java.time.LocalDateTime

class PrefixedResultSet(
        val resultSet: ResultSet,
        val prefix: String = ""
) {

    fun <R : Any> mapAll(mapper: PrefixedResultSet.() -> R): List<R> = resultSet.use {
        generateSequence {
            if (resultSet.next()) mapper() else null
        }.toList()
    }

    private fun String.prefixed() = if (prefix == "") this else "${prefix}_$this"

    fun longOpt(column: String): Long? = resultSet.getLong(column.prefixed())

    fun long(column: String): Long = longOpt(column)!!

    fun intOpt(column: String): Int? = resultSet.getInt(column.prefixed())

    fun int(column: String): Int = intOpt(column)!!

    fun doubleOpt(column: String): Double? = resultSet.getDouble(column.prefixed())

    fun double(column: String): Double = doubleOpt(column)!!

    fun stringOpt(column: String): String? = resultSet.getString(column.prefixed())

    fun string(column: String): String = stringOpt(column)!!

    fun byteArrayOpt(column: String): ByteArray? = resultSet.getBytes(column.prefixed())

    fun byteArray(column: String): ByteArray = byteArrayOpt(column)!!

    fun dateTimeOpt(column: String): LocalDateTime? = resultSet.getTimestamp(column.prefixed()).toLocalDateTime()

    fun dateTime(column: String): LocalDateTime = dateTimeOpt(column)!!

    fun booleanOpt(column: String): Boolean? = resultSet.getBoolean(column.prefixed())

    fun boolean(column: String): Boolean = booleanOpt(column)!!

}