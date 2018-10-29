package hu.mktiti.tulkas.server.data.dto

import hu.mktiti.tulkas.server.data.dao.Bot

data class SimpleBotDto(
        val name: String,
        val ownerUsername: String,
        val game: String
)

fun Bot.toSimpleDto(ownerName: String, gameName: String) = SimpleBotDto(name, ownerName, gameName)

fun List<Pair<Bot, String>>.toSimpleDtos(ownerName: String) = map { it.first.toSimpleDto(ownerName, it.second) }

data class BotUploadData(
        val name: String,
        val game: String,
        val jarString: String
)