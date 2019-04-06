package hu.mktiti.tulkas.server.data.dao

data class JarData(
        override val id: Long = -1,
        val data: ByteArray
) : Entity {

    override fun equals(other: Any?) = other is JarData && other.id == id

    override fun hashCode() = id.hashCode()

    override fun toString() = "JarData { id = $id, data = ${data.size} bytes }"

}