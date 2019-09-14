package hu.mktiti.tulkas.server.data.repo.inmem

import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.server.data.dao.Game
import hu.mktiti.tulkas.server.data.dao.JarData
import hu.mktiti.tulkas.server.data.repo.GameRepo
import hu.mktiti.tulkas.server.data.repo.JarDataRepo
import hu.mktiti.tulkas.server.data.repo.UserRepo

@TestInjectable(environment = "unit", tags = ["mem"])
class GameInMemoryRepo(
        games: List<Game> = emptyList(),
        private val userRepo: UserRepo = inject(),
        private val jarDataRepo: JarDataRepo = inject()
) : NamedEntityInMemoryRepo<Game>(games), GameRepo {

    override fun Game.newId(newId: Long): Game = copy(id = newId)

    override fun createGame(ownerId: Long, name: String, isMatch: Boolean, apiJar: ByteArray, engineJar: ByteArray): Long?
            = save(Game(ownerId = ownerId,
                        name = name,
                        isMatch = isMatch,
                        apiJarId = jarDataRepo.save(JarData(data = apiJar)),
                        engineJarId = jarDataRepo.save(JarData(data = engineJar))))

    override fun gamesOf(ownerId: Long): List<Game>
            = entities.filter { it.ownerId == ownerId }

    override fun listWithOwnerName(): List<Pair<Game, String>>
            = entities.map { it to (userRepo.find(it.ownerId)?.name ?: "") }

    override fun searchWithOwnerName(namePart: String): List<Pair<Game, String>>
            = entities.filter { it.name.contains(namePart, ignoreCase = true) }.map { it to (userRepo.find(it.ownerId)?.name ?: "") }

}