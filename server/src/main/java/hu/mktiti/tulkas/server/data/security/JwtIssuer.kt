package hu.mktiti.tulkas.server.data.security

import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.property.intProperty
import hu.mktiti.tulkas.server.data.handler.badRequest
import hu.mktiti.tulkas.server.data.handler.entity
import hu.mktiti.tulkas.server.data.repo.UserRepo
import java.time.LocalDateTime
import javax.inject.Singleton
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import javax.ws.rs.core.Response

@Path("/login")
@Singleton
class JwtIssuer(
        private val userRepo: UserRepo = inject(),
        private val authenticator: JwtAuthenticator = inject(),
        private val expirationDays: Int = intProperty("Server.Security.JwtExpiryDays", 7)
) {

    @GET
    @Produces(APPLICATION_JSON)
    fun login(@QueryParam("username") username: String, @QueryParam("password") password: String): Response {
        val user = userRepo.authenticate(username, password) ?: return badRequest("Bad user credentials")
        return issue(user.name, UserRole.USER)
    }

    @GET
    @Path("/refresh")
    @Produces(APPLICATION_JSON)
    fun refreshToken(@QueryParam("token") token: String): Response {
        val tokenData = authenticator.verify(token) ?: return badRequest("Invalid auth token")
        return issue(tokenData.username, tokenData.role)
    }

    private fun issue(username: String, role: UserRole): Response = entity {
        authenticator.encode(username, role, LocalDateTime.now().plusDays(expirationDays.toLong()))
    }

}