package hu.mktiti.tulkas.server.data.dao

data class User(
        override val id: Long = -1,
        override val name: String,
        val passHash: String
) : NamedEntity {

    override fun equals(other: Any?) = other is User && other.id == id

    override fun hashCode() = id.hashCode()

    infix fun contentEq(other: User): Boolean = name == other.name && passHash == other.passHash

    infix fun sameAs(other: User): Boolean = id == other.id && contentEq(other)

}