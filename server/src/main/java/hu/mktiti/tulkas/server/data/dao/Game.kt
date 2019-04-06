package hu.mktiti.tulkas.server.data.dao

data class Game(
        override val id: Long = -1,
        val ownerId: Long,
        override val name: String,
        val isMatch: Boolean,
        val engineJarId: Long,
        val apiJarId: Long
) : NamedEntity {

    override fun equals(other: Any?) = other is Game && other.id == id

    override fun hashCode() = id.hashCode()

}