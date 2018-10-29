package hu.mktiti.tulkas.server.data

import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.property.property
import hu.mktiti.tulkas.server.data.dao.ConnectionSource
import org.hsqldb.cmdline.SqlFile
import java.io.File
import java.io.InputStreamReader
import java.sql.Connection

object DbUtil {

    private val dbConnection: Connection = inject<ConnectionSource>().invoke()

    fun recreateDb() {
        dropTables()
        createDb()
        fillDb()
    }

    fun createDb() {
        runScript(property("Server.Database.Script.Init"), "database init")
    }

    fun dropTables() {
        runScript(property("Server.Database.Script.Drop"), "database tables drop")
    }

    fun fillDb() {
        runScript(property("Server.Database.Script.Fill"), "database fill")
    }

    fun fillDbIfEmpty() {
        val hasData = dbConnection.prepareStatement("""
            select 1 from User
            union
            select 1 from JarData
            union
            select 1 from Game
            union
            select 1 from Bot
            union
            select 1 from GameLog
            union
            select 1 from ActorLog
        """).useWith {
            executeQuery().useWith {
                next()
            }
        }

        if (!hasData) {
            println("Filling database")
            fillDb()
        }
    }

    fun clearTables() {
        runScript(property("Server.Database.Script.Clear"), "database tables clear")
    }

    fun resetDb() {
        clearTables()
        fillDb()
    }

    private fun runScript(
            script: String,
            taskName: String
    ) {
        javaClass.getResourceAsStream(script).use { stream ->
            with(SqlFile(InputStreamReader(stream), taskName, System.out, "UTF-8", false, File("."))) {
                connection = dbConnection
                execute()
            }
        }
    }

}