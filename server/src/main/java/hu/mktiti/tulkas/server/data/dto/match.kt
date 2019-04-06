package hu.mktiti.tulkas.server.data.dto

import hu.mktiti.tulkas.server.data.dao.ActorLog
import hu.mktiti.tulkas.server.data.dao.GameLog
import hu.mktiti.tulkas.server.data.repo.MatchLogWithActors
import java.time.LocalDateTime

data class SimpleMatchDto(
        val id: Long,
        val botA: SimpleBotDto,
        val botB: SimpleBotDto?,
        val time: LocalDateTime,
        val result: String
)

fun fromChallenge(bot: SimpleBotDto, challenge: GameLog) = SimpleMatchDto(
        id = challenge.id,
        botA = bot,
        botB = null,
        time = challenge.time,
        result = challenge.result
)

fun fromMatch(bot: SimpleBotDto, match: MatchLogWithActors) = SimpleMatchDto(
        id = match.gameLog.id,
        botA = match.botA.first.toSimpleDto(match.botA.second, bot.game),
        botB = match.botB.first.toSimpleDto(match.botB.second, bot.game),
        time = match.gameLog.time,
        result = match.gameLog.result
)

data class SimpleActorLogDto(
        val sender: String,
        val target: String,
        val message: String
)

fun ActorLog.toSimpleDto() = SimpleActorLogDto(
        sender = sender,
        target = target,
        message = message
)