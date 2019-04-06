package hu.mktiti.tulkas.runtime.common

inline fun <reified T : Enum<T>> safeValueOf(name: String): T? = enumValues<T>().find { it.name == name }

fun <T : Any> Collection<T?>.liftNulls(): List<T>? {
    val filtered: List<T> = filterNotNull()
    return if (filtered.size == size) filtered else null
}

inline fun forever(block: () -> Unit): Nothing { while (true) { block() } }

fun <F, S, NF> Pair<F, S>.fst(first: NF): Pair<NF, S> = first to second

fun <F, S, NF> Pair<F, S>.fst(mapper: (F) -> NF): Pair<NF, S> = mapper(first) to second

fun <F, S, NS> Pair<F, S>.snd(second: NS): Pair<F, NS> = first to second

fun <F, S, NS> Pair<F, S>.snd(mapper: (S) -> NS): Pair<F, NS> = first to mapper(second)

fun langThread(
        start: Boolean = true,
        isDaemon: Boolean = false,
        contextClassLoader: ClassLoader? = null,
        name: String? = null,
        priority: Int = -1,
        block: () -> Unit
): Thread = Thread(Runnable(block)).apply {

    if (isDaemon) this.isDaemon = true

    if (priority > 0) this.priority = priority

    if (name != null) this.name = name

    if (contextClassLoader != null) this.contextClassLoader = contextClassLoader

    if (start) this.start()

}