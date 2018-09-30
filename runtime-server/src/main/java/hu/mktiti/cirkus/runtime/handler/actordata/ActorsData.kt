package hu.mktiti.cirkus.runtime.handler.actordata

import hu.mktiti.cirkus.runtime.handler.control.Actor

class ActorArityException(message: String) : RuntimeException(message)

interface ActorsData<out E, out B> {

    val engine: E

    val bots: List<B>

    val actors: Set<Actor>

    fun <ER, BR> map(
            engineMapper: (E) -> ER,
            botMapper: (B) -> BR
    ): ActorsData<ER, BR>

    fun <R> unify(
            engineMapper: (E) -> R,
            botMapper: (B) -> R
    ): UnifiedActorsData<R>

    fun <R> unify(
            init: () -> R
    ): UnifiedActorsData<R>

    fun safeGetBot(actor: Actor): B?

    fun getBot(actor: Actor): B =
            safeGetBot(actor) ?: throw ActorArityException("Actor '$actor' is not playing or is not a bot")

}

interface UnifiedActorsData<out T> : ActorsData<T, T> {

    val actorData: List<T>

    fun safeGet(actor: Actor): T?

    fun <R> umap(mapper: (T) -> R): UnifiedActorsData<R> = umap(mapper, mapper)

    fun <R> umap(
            engineMapper: (T) -> R,
            botMapper: (T) -> R
    ): UnifiedActorsData<R>

    operator fun get(actor: Actor): T =
            safeGet(actor) ?: throw ActorArityException("Actor '$actor' is not playing currently")

}

fun <T, E : T, B : T> ActorsData<E, B>.unified(): UnifiedActorsData<T> = unify({ it }, { it })