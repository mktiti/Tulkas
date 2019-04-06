package hu.mktiti.tulkas.server.data

import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.property.intProperty
import hu.mktiti.tulkas.server.data.service.GameManager
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.glassfish.jersey.servlet.ServletContainer

fun main(args: Array<String>) {
    DbUtil.createDb()

    val context = ServletContextHandler(ServletContextHandler.NO_SESSIONS)
    context.contextPath = "/"

    val jettyServer = Server(intProperty("Server.Port", 8000))
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

    try {
        jettyServer.start()

        inject<GameManager>().rankAllUnranked()

        jettyServer.join()
    } finally {
        jettyServer.destroy()
    }
}