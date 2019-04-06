package hu.mktiti.tulkas.server.data.security

import hu.mktiti.tulkas.server.data.handler.ExtendedJerseyTest
import hu.mktiti.tulkas.server.data.util.fetchTest
import org.eclipse.jetty.http.HttpStatus
import org.junit.jupiter.api.Test
import javax.ws.rs.GET
import javax.ws.rs.Path
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

}