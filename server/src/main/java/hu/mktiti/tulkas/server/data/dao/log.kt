package hu.mktiti.tulkas.server.data.dao

import java.time.LocalDateTime

data class GameLog(
        override val id: Long = -1,
        val gameId: Long,
        val botAId: Long,
        val botBId: Long?,
        val time: LocalDateTime,
        val result: String
) : Entity {

    override fun equals(other: Any?) = other is GameLog && other.id == id

    override fun hashCode() = id.hashCode()

}

data class ActorLog(
        override val id: Long = -1,
        val gameId: Long,
        val sender: String,
        val target: String,
        val relativeIndex: Int,
        val message: String
) : Entity {

    override fun equals(other: Any?) = other is ActorLog && other.id == id

    override fun hashCode() = id.hashCode()

    infix fun sameAs(other: ActorLog) = id == other.id && contentEquls(other)

    infix fun contentEquls(other: ActorLog) =
            gameId == other.gameId &&
                    sender == other.sender &&
                    target == other.target &&
                    relativeIndex == other.relativeIndex &&
                    message == other.message

}