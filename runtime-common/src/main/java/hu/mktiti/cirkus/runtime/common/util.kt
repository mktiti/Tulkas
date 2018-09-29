package hu.mktiti.cirkus.runtime.common

inline fun <reified T : Enum<T>> safeValueOf(name: String): T? = enumValues<T>().find { it.name == name }

fun <T : Any> List<T?>.liftNulls(): List<T>? {
    val filtered: List<T> = filterNotNull()
    return if (filtered.size == size) filtered else null
}

inline fun forever(block: () -> Unit): Nothing { while (true) { block() } }