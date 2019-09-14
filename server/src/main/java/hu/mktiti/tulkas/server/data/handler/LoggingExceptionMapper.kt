package hu.mktiti.tulkas.server.data.handler

import hu.mktiti.tulkas.runtime.common.logger
import javax.inject.Singleton
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
@Singleton
class LoggingExceptionMapper : ExceptionMapper<java.lang.Exception> {

    private val log by logger()

    override fun toResponse(exception: Exception): Response {
        log.error("Internal server error", exception)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build()
    }

}