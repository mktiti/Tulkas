package hu.mktiti.tulkas.server.data.security

import hu.mktiti.kreator.property.intPropertyOpt
import hu.mktiti.tulkas.server.data.dao.User
import hu.mktiti.tulkas.server.data.repo.inmem.UserInMemoryRepo
import hu.mktiti.tulkas.server.data.safeValueOf
import hu.mktiti.tulkas.server.data.util.ExtendedJerseyTest
import hu.mktiti.tulkas.server.data.util.withinRange
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import kotlin.test.assertEquals

class JwtIssuerTest : ExtendedJerseyTest() {

    private val userRepo = UserInMemoryRepo(listOf(User(name = "alice", passHash = "hunter2")))
    private val issuer = JwtIssuer(userRepo)

    @Test
    fun `test jwt issue missing credentials`() {
        val status = target("/login").request().get().status

        assertEquals(HttpStatus.BAD_REQUEST_400, status)
    }

    @Test
    fun `test jwt issue missing password`() {
        val status = target("/login")
                .queryParam("alice", "nonexistent")
                .request().get().status

        assertEquals(HttpStatus.BAD_REQUEST_400, status)
    }

    @Test
    fun `test jwt issue unknown user`() {
        val status = issuer.login("nonexistent", "hunter2").status

        assertEquals(HttpStatus.BAD_REQUEST_400, status)
    }

    @Test
    fun `test jwt issue bad password`() {
        val status = issuer.login("alice", "bad-password").status

        assertEquals(HttpStatus.BAD_REQUEST_400, status)
    }

    @Test
    fun `test jwt issue success`() {
        val token = issuer.login("alice", "hunter2").entity as? String ?: ""

        val split = token.split("|")
        assertEquals("alice", split[0])
        assertEquals(UserRole.USER, safeValueOf<UserRole>(split[1]))

        val expiry = LocalDateTime.parse(split[2])
        val expectedExp = LocalDateTime.now().plusDays(intPropertyOpt("Server.Security.JwtExpiryDays")?.toLong() ?: 7)
        assert(expiry.withinRange(expectedExp, Duration.ofHours(1)))
    }

    @Test
    fun `test jwt refresh invalid token`() {
        val status = target("/login/refresh")
                        .queryParam("token", "invalid")
                        .request().get().status

        assertEquals(HttpStatus.BAD_REQUEST_400, status)
    }

    @Test
    fun `test jwt refresh success`() {
        val token = issuer.refreshToken("alice|USER|${LocalDateTime.now().plusHours(1)}").entity as String

        val split = token.split("|")
        assertEquals("alice", split[0])
        assertEquals(UserRole.USER, safeValueOf<UserRole>(split[1]))

        val expiry = LocalDateTime.parse(split[2])
        val expectedExp = LocalDateTime.now().plusDays(intPropertyOpt("Server.Security.JwtExpiryDays")?.toLong() ?: 7)
        assert(expiry.withinRange(expectedExp, Duration.ofHours(1)))
    }

}