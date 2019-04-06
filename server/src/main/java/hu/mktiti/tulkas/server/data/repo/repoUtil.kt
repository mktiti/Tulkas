package hu.mktiti.tulkas.server.data.repo

import java.sql.PreparedStatement
import java.sql.ResultSet

fun <T : Any> ResultSet.mapAll(mapper: ResultSet.() -> T): List<T> = use {
    generateSequence {
        if (next()) mapper() else null
    }.toList()
}

fun PreparedStatement.setAllParams(params: List<Any?>) {
    params.forEachIndexed { i, p ->
        setObject(i + 1, p)
    }
}