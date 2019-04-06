package hu.mktiti.tulkas.server.data.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.common.logger
import hu.mktiti.tulkas.runtime.common.safeValueOf
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

enum class UserRole {
    USER, ADMIN
}

data class JwtTokenData(
        val username: String,
        val role: UserRole,
        val expiration: LocalDateTime
)

@InjectableType
interface JwtAuthenticator {

    fun verify(token: String): JwtTokenData?

    fun encode(username: String, role: UserRole = UserRole.USER, expiration: LocalDateTime): String?

}

internal fun createRsaAlgorithm(): Algorithm {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(512)
    val keys = generator.generateKeyPair()

    return Algorithm.RSA512(keys.public as RSAPublicKey, keys.private as RSAPrivateKey)
}

@Injectable(arity = InjectableArity.SINGLETON_AUTOSTART)
class RsaJwtAuthenticator(
        private val algorithm: Algorithm = createRsaAlgorithm(),
        private val startedAt: LocalDateTime = LocalDateTime.now()
) : JwtAuthenticator {

    private val log by logger()

    override fun verify(token: String): JwtTokenData? {

        fun Date.toLocalDT(): LocalDateTime? = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

        try {
            val decoded = JWT.require(algorithm).build().verify(token) ?: return null

            if (decoded.issuedAt?.toLocalDT()?.isBefore(startedAt) != false) {
                return null
            }

            return JwtTokenData(
                    username = decoded.subject ?: return null,
                    role = safeValueOf<UserRole>(decoded.getClaim("role").asString()) ?: return null,
                    expiration = decoded.expiresAt?.toLocalDT() ?: return null
            )
        } catch (jve: JWTVerificationException) {
            log.error("Cannot verify JWT token", jve)
        }

        return null
    }

    override fun encode(username: String, role: UserRole, expiration: LocalDateTime): String? {

        fun LocalDateTime.toUtil(): Date =
                Date.from(atZone(ZoneId.systemDefault()).toInstant())

        try {
            return with(JWT.create()) {
                withIssuedAt(Date())
                withSubject(username)
                withNotBefore(startedAt.toUtil())
                withExpiresAt(expiration.toUtil())
                withClaim("role", role.name)
                sign(algorithm)
            }
        } catch (jce: JWTCreationException) {
            log.error("Cannot create JWT token", jce)
        }

        return null
    }

}