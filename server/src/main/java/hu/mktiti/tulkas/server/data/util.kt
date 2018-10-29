package hu.mktiti.tulkas.server.data

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