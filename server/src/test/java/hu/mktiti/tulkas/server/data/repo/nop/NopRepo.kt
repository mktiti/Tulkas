package hu.mktiti.tulkas.server.data.repo.nop

import hu.mktiti.tulkas.server.data.dao.Entity
import hu.mktiti.tulkas.server.data.repo.Repo

abstract class NopRepo<T : Entity> : Repo<T> {

    override fun find(id: Long): T? = null

    override fun listAll(): List<T> = emptyList()

    override fun delete(id: Long) {}

    override fun deleteAll() {}

    override fun save(entity: T): Long = 0

    override fun saveAll(entities: Iterable<T>): List<Long> = emptyList()
}