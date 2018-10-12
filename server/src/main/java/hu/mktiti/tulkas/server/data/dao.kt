package hu.mktiti.tulkas.server.data

import java.time.LocalDateTime

interface Entity {
    val id: Long
}

data class User(
        override val id: Long,
        val username: String,
        val passHash: String
) : Entity {

    override fun equals(other: Any?) = other is User && other.id == id

    override fun hashCode() = id.hashCode()

}

data class Game(
        override val id: Long,
        val ownerId: Long,
        val name: String,
        val isMatch: Boolean,
        val engineJar: ByteArray,
        val apiJar: ByteArray
) : Entity {

    override fun equals(other: Any?) = other is Game && other.id == id

    override fun hashCode() = id.hashCode()

}

class Bot(
        override val id: Long,
        val ownerId: Long,
        val gameId: Long,
        val name: String,
        val jar: ByteArray
) : Entity {

    override fun equals(other: Any?) = other is Bot && other.id == id

    override fun hashCode() = id.hashCode()

}

class GameLog(
        override val id: Long,
        val gameId: Long,
        val botAId: Long,
        val botBId: Long?,
        val time: LocalDateTime,
        val result: String
) : Entity {

    override fun equals(other: Any?) = other is GameLog && other.id == id

    override fun hashCode() = id.hashCode()

}

class ActorLog(
        override val id: Long,
        val actor: String,
        val time: LocalDateTime,
        val relativeIndex: Int,
        val message: String
) : Entity {

    override fun equals(other: Any?) = other is ActorLog && other.id == id

    override fun hashCode() = id.hashCode()

}