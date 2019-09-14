package hu.mktiti.tulkas.server.data

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.property.intProperty
import hu.mktiti.kreator.property.propertyOpt
import hu.mktiti.tulkas.server.data.service.GameManager
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.glassfish.jersey.servlet.ServletContainer
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    setupLogging()

    DbUtil.createDb()

    val jettyServer = Server(intProperty("Server.Port", 8000))
    val context = ServletContextHandler(ServletContextHandler.NO_SESSIONS)

    context.contextPath = "/"
    jettyServer.handler = context

    with(context.addServlet(ServletContainer::class.java, "/api/*")) {
        initOrder = 0
        setInitParameter(
                "jersey.config.server.provider.packages",
                "hu.mktiti.tulkas.server.data.handler, hu.mktiti.tulkas.server.data.security, org.codehaus.jackson.jaxrs, org.glassfish.jersey.examples.multipart"
        )
        setInitParameter(
                "jersey.config.server.provider.classnames",
                "org.glassfish.jersey.jackson.JacksonFeature, org.glassfish.jersey.media.multipart.MultiPartFeature"
        )

    }

    jettyServer.start()

    inject<GameManager>().rankAllUnranked()

    jettyServer.join()
}

private fun setupLogging() {
    val logLevel = Level.toLevel(propertyOpt("Server.LogLevel"), Level.INFO)
    (LoggerFactory.getILoggerFactory().getLogger(Logger.ROOT_LOGGER_NAME) as? Logger)?.level = logLevel
}