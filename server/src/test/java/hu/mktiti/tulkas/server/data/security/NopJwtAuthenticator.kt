package hu.mktiti.tulkas.server.data.security

import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.tulkas.server.data.safeValueOf
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

@TestInjectable(environment = "unit", tags = ["noop"])
class NopJwtAuthenticator : JwtAuthenticator {

    override fun verify(token: String): JwtTokenData? {
        val split = token.split("|")

        if (split.size != 3) return null

        val username = if (split[0] != "") split[0] else return null
        val role = safeValueOf<UserRole>(split[1]) ?: return null
        val expiration = try {
            LocalDateTime.parse(split[2])
        } catch (_: DateTimeParseException) {
            return null
        }

        return JwtTokenData(username, role, expiration)
    }

    override fun encode(username: String, role: UserRole, expiration: LocalDateTime): String? {
        return "$username|${role.name}|$expiration"
    }

}