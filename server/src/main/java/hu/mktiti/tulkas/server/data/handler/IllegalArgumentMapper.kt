package hu.mktiti.tulkas.server.data.handler

import javax.inject.Singleton
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
@Singleton
class IllegalArgumentMapper : ExceptionMapper<IllegalArgumentException> {

    override fun toResponse(exception: IllegalArgumentException): Response
            = Response.status(Response.Status.BAD_REQUEST)
                        .entity(exception.message)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .build()

}