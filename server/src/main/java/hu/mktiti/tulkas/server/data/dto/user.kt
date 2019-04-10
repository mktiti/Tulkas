package hu.mktiti.tulkas.server.data.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SimpleUserDto(
        val name: String
)

data class DetailedUserDto(
        val name:  String,
        val games: List<SimpleGameDto>,
        val bots:  List<SimpleBotDto>
)

data class UserUploadData(
        @JsonProperty val name: String,
        @JsonProperty val password: String
)