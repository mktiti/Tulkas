package hu.mktiti.tulkas.server.data.security

import hu.mktiti.tulkas.server.data.handler.ExtendedJerseyTest
import hu.mktiti.tulkas.server.data.util.fetchTest
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.Test
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType.APPLICATION_JSON
import kotlin.test.assertEquals

@Path("/jwt-test")
class JwtTestHandler {

    @GET
    @Path("unsecured-greet")
    @Produces(APPLICATION_JSON)
    fun unsecuredGreet() = "Hello there!"

    @GET
    @Path("secured-greet")
    @LoginRequired
    @Produces(APPLICATION_JSON)
    fun securedGreet() = "Hello there! You are authorized!"

    @GET
    @Path("users/{user}/secret-greet-fail")
    @LoginRequired(usernameParam = "username")
    @Produces(APPLICATION_JSON)
    fun secretGreetFail(@PathParam("user") user: String) = "Hello there $user! This is your secret page!"

    @GET
    @Path("users/{user}/secret-greet")
    @LoginRequired(usernameParam = "user")
    @Produces(APPLICATION_JSON)
    fun secretGreet(@PathParam("user") user: String) = "Hello there $user! This is your secret page!"

}

class LoginFilterTest : ExtendedJerseyTest() {

    @Test
    fun `test unsecured access`() {
        val result: String = fetchTest("/jwt-test/unsecured-greet")

        assertEquals("Hello there!", result)
    }

    @Test
    fun `test secured without jwt fail`() {
        val status = target("/jwt-test/secured-greet").request().get().status

        assertEquals(HttpStatus.UNAUTHORIZED_401, status)
    }

    @Test
    fun `test secured bad auth method fail`() {
        val status = target("/jwt-test/secured-greet").request()
                        .header(HttpHeaders.AUTHORIZATION, "Basic user|USER|2100-01-01T01:01")
                        .get().status

        assertEquals(HttpStatus.UNAUTHORIZED_401, status)
    }

    @Test
    fun `test secured bad jwt format fail`() {
        val status = target("/jwt-test/secured-greet").request()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user|2100-01-01T01:01")
                        .get().status

        assertEquals(HttpStatus.UNAUTHORIZED_401, status)
    }


    @Test
    fun `test secured success`() {
        val message = target("/jwt-test/secured-greet").request()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer user|USER|2100-01-01T01:01")
                        .get(String::class.java)

        assertEquals("Hello there! You are authorized!", message)
    }

    @Test
    fun `test owner required field not in path fail`() {
        val status = target("/jwt-test/users/General Kenobi/secret-greet-fail").request()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer General Kenobi|USER|2100-01-01T01:01")
                        .get().status

        assertEquals(HttpStatus.FORBIDDEN_403, status)
    }

    @Test
    fun `test owner required not owner fail`() {
        val status = target("/jwt-test/users/General Kenobi/secret-greet").request()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer Grievous|USER|2100-01-01T01:01")
                        .get().status

        assertEquals(HttpStatus.FORBIDDEN_403, status)
    }

    @Test
    fun `test owner required success`() {
        val message = target("/jwt-test/users/General Kenobi/secret-greet").request()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer General Kenobi|USER|2100-01-01T01:01")
                        .get(String::class.java)

        assertEquals("Hello there General Kenobi! This is your secret page!", message)
    }
}