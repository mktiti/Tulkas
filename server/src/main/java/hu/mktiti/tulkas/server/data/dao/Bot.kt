package hu.mktiti.tulkas.server.data.dao

data class Bot(
        override val id: Long = -1,
        val ownerId: Long,
        val gameId: Long,
        val name: String,
        val jarId: Long,
        val rank: Int?
) : Entity {

    override fun equals(other: Any?) = other is Bot && other.id == id

    override fun hashCode() = id.hashCode()

}