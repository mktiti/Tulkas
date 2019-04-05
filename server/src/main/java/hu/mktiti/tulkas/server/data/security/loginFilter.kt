package hu.mktiti.tulkas.server.data.security

import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.runtime.common.logger
import hu.mktiti.tulkas.server.data.response
import javax.annotation.Priority
import javax.ws.rs.NameBinding
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE
import javax.ws.rs.core.Response.Status.UNAUTHORIZED
import javax.ws.rs.ext.Provider

@NameBinding
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoginRequired

@LoginRequired
@Provider
@Priority(Priorities.AUTHENTICATION)
class LoginFilter(
        private val tokenAuthenticator: JwtAuthenticator = inject()
) : ContainerRequestFilter {

    companion object {
        private const val SCHEME_STRING = "Bearer"
    }

    private val log by logger()

    override fun filter(request: ContainerRequestContext) {

        val header = request.getHeaderString(HttpHeaders.AUTHORIZATION)?.trim() ?: ""
        if (!header.startsWith(SCHEME_STRING, ignoreCase = true)) {
            log.info("Unknown authentication scheme", header)

            request.abortUnauthorized()
        } else {

            val token = header.substring(SCHEME_STRING.length).trimStart()

            val tokenData = tokenAuthenticator.verify(token)
            if (tokenData == null || tokenData.username.isBlank()) {
                request.abortUnauthorized()
            }
        }

    }

    private fun ContainerRequestContext.abortUnauthorized() {
        abortWith(response(UNAUTHORIZED, header = WWW_AUTHENTICATE to SCHEME_STRING))
    }

}