package hu.mktiti.tulkas.server.data.security

import hu.mktiti.tulkas.server.data.util.ExtendedJerseyTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CorsFilterTest : ExtendedJerseyTest() {

    @Test
    fun `test CORS headers`() {
        val headers: Map<String, Any?> = target("/").request().options().headers

        assertEquals("*", firstHeaderValue(headers, "Access-Control-Allow-Origin"))
        assertEquals("true", firstHeaderValue(headers, "Access-Control-Allow-Credentials"))

        val allowedHeaders = firstHeaderValueList(headers, "Access-Control-Allow-Headers")
        assertEquals(listOf("origin", "content-type", "authorization", "accept").sorted(), allowedHeaders)

        val allowedMethods = firstHeaderValueList(headers, "Access-Control-Allow-Methods")
        assertEquals(listOf("GET", "PUT", "OPTIONS", "HEAD", "DELETE", "POST", "PATCH").sorted(), allowedMethods)
    }

    private fun firstHeaderValue(headers: Map<String, Any?>, header: String): String?
            = (headers[header] as? List<Any?>)?.first() as? String

    private fun firstHeaderValueList(headers: Map<String, Any?>, header: String): List<String>
            = firstHeaderValue(headers, header)?.split(",")?.map(String::trim)?.sorted() ?: emptyList()

}