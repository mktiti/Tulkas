package hu.mktiti.tulkas.server.data.handler

import java.net.URI
import java.util.*
import javax.ws.rs.core.Response

fun <T> entity(producer: () -> T?): Response =
        producer()?.let { Response.ok(it).build() } ?: notFound()

fun <T> safeEntity(producer: () -> T?): Response = entity {
    try {
        producer()
    } catch (_: Exception) {
        null
    }
}

fun notFound(): Response = Response.status(404).build()

fun badRequest(message: String = ""): Response = Response.status(400).entity(message).build()

fun pathCreated(path: String, producer: () -> Long?): Response {
    producer() ?: return badRequest()
    return Response.created(URI.create(path)).build()
}

fun fromBase64(dataString: String): ByteArray = Base64.getDecoder().decode(dataString)