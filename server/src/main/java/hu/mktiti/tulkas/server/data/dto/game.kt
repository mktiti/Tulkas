package hu.mktiti.tulkas.server.data.dto

import hu.mktiti.tulkas.server.data.dao.Game
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
open class SimpleGameDto(
        val name: String,
        val owner: String,
        val isMatch: Boolean
)

fun Game.toSimpleDto(ownerName: String) = SimpleGameDto(name, ownerName, isMatch)

fun List<Game>.toSimpleDtos(ownerName: String) = map { it.toSimpleDto(ownerName) }

fun List<Pair<Game, String>>.toSimpleDtos() = map { it.first.toSimpleDto(it.second) }

data class DetailedGameDto(
        val name: String,
        val isMatch: Boolean,
        val owner: String,
        val bots: List<SimpleBotDto>
)

data class GameUploadData(
        val name: String,
        val isMatch: Boolean,
        val apiJarString: String,
        val engineJarString: String
)