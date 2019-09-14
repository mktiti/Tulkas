package hu.mktiti.tulkas.server.data.dto

import com.fasterxml.jackson.annotation.JsonProperty
import hu.mktiti.tulkas.server.data.dao.Game

data class SimpleGameDto(
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
        @JsonProperty("name") val name: String,
        @JsonProperty("isMatch") val isMatch: Boolean,
        @JsonProperty("apiJarString") val apiJarString: String,
        @JsonProperty("engineJarString") val engineJarString: String
)