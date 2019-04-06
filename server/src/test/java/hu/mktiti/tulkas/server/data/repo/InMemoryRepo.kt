package hu.mktiti.tulkas.server.data.repo

import hu.mktiti.tulkas.server.data.dao.Entity
import hu.mktiti.tulkas.server.data.dao.NamedEntity

abstract class InMemoryRepo<T : Entity>(
        initialData: List<T>
) : Repo<T> {

    protected val data: MutableMap<Long, T> =
            HashMap(initialData.mapIndexed { i, e -> i.toLong() to e.withId(i.toLong()) }.toMap())

    protected var idCounter: Long = initialData.size.toLong()

    fun T.withId(newId: Long): T = if (id == newId) this else newId(newId)

    abstract fun T.newId(newId: Long): T

    override fun find(id: Long): T? = data[id]

    override fun listAll(): List<T> = data.values.sortedBy { it.id }

    override fun delete(id: Long) {
        data.remove(id)
    }

    override fun deleteAll() {
        data.clear()
    }

    override fun save(entity: T): Long {
        data[entity.id] = entity.newId(idCounter)
        return idCounter++
    }

    override fun saveAll(entities: Iterable<T>): List<Long> {
        val startId = idCounter
        data.putAll(entities.map { e -> idCounter to e.withId(idCounter++) })
        return (startId until idCounter).toList()
    }

}

abstract class NamedEntityInMemoryRepo<T : NamedEntity>(
        initialData: List<T>
) : InMemoryRepo<T>(initialData), NamedEntityRepo<T> {

    override fun findByName(name: String): T? = data.values.singleOrNull { it.name == name }

    override fun searchNameContaining(namePart: String): List<T> = data.values.filter { it.name.contains(namePart) }

}