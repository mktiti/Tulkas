package hu.mktiti.tulkas.server.data.dto

data class SimpleUserDto(
        val name: String
)

data class DetailedUserDto(
        val name:  String,
        val games: List<SimpleGameDto>,
        val bots:  List<SimpleBotDto>
)

data class UserUploadData(
        val name: String,
        val password: String
)