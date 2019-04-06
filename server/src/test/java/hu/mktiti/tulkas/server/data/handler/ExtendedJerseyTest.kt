package hu.mktiti.tulkas.server.data.handler

import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import javax.ws.rs.core.Application

open class ExtendedJerseyTest : JerseyTest() {

    // Needed for target()
    @BeforeEach
    @Throws(Exception::class)
    fun before() {
        super.setUp()
    }

    // Needed for target()
    @AfterEach
    @Throws(Exception::class)
    fun after() {
        super.tearDown()
    }

    override fun configure(): Application
            = ResourceConfig().packages("hu.mktiti.tulkas.server.data.handler", "hu.mktiti.tulkas.server.data.security")

}