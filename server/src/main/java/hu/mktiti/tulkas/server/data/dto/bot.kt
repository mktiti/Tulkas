package hu.mktiti.tulkas.server.data.dto

import hu.mktiti.tulkas.server.data.dao.Bot
import org.codehaus.jackson.annotate.JsonProperty

data class SimpleBotDto(
        val name: String,
        val ownerUsername: String,
        val game: String
)

fun Bot.toSimpleDto(ownerName: String, gameName: String) = SimpleBotDto(name, ownerName, gameName)

fun List<Pair<Bot, String>>.toSimpleDtos(ownerName: String) = map { it.first.toSimpleDto(ownerName, it.second) }

data class DetailedBotData(
        val name: String,
        val ownerUsername: String,
        val game: String,
        val played: List<SimpleMatchDto>
)

data class BotUploadData(
        @JsonProperty("name") val name: String,
        @JsonProperty("game") val game: String,
        @JsonProperty("jarString") val jarString: String
)