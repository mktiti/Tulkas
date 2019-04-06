package hu.mktiti.tulkas.server.data

import javax.ws.rs.core.Response

fun ByteArray.hexString(): String = joinToString(separator = "") {
    (it.toInt() + 128).toString(16).toUpperCase()
}

fun charHexValue(char: Char): Int = when (char) {
    in '0'..'9' -> char - '0'
    in 'A'..'F' -> char - 'A' + 10
    in 'a'..'f' -> char - 'a' + 10
    else -> throw IllegalArgumentException("Char must be valid hex value (actual: $char)")
}

fun String.hexBytes(): ByteArray = toCharArray().toList().chunked(2) { values ->
    (16 * charHexValue(values[0]) + charHexValue(values[1])).toByte()
}.toByteArray()

fun <T : AutoCloseable, R> T.useWith(code: T.() -> R): R = use { with(this) { code() } }

inline fun forever(block: () -> Unit): Nothing { while (true) { block() } }

fun response(status: Response.Status, headers: Map<String, String> = emptyMap()): Response = Response.status(status).apply {
    for ((header, value) in headers) {
        header(header, value)
    }
}.build()

fun response(status: Response.Status, header: Pair<String, String>): Response =
        Response.status(status).header(header.first, header.second).build()

inline fun <reified T : Enum<T>> safeValueOf(name: String?): T? =
        if (name == null) null else enumValues<T>().find { it.name == name }