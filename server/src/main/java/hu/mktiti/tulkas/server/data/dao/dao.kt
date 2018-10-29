package hu.mktiti.tulkas.server.data.dao

interface Entity {
    val id: Long
}

interface NamedEntity : Entity{
    val name: String
}