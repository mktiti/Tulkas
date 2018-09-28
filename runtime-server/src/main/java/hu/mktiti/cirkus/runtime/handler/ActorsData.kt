package hu.mktiti.cirkus.runtime.handler

import hu.mktiti.cirkus.runtime.handler.control.Actor

class ActorsData<out T>(
        val engine: T,
        val botA: T,
        val botB: T
) : Iterable<T> {

    constructor(init: () -> T) : this(init(), init(), init())

    constructor(initial: T) : this(initial, initial, initial)

    constructor(engine: T, bots: T) : this(engine, bots, bots)

    fun <R> map(mapper: (T) -> R): ActorsData<R>
           = ActorsData(mapper(engine), mapper(botA), mapper(botB))

    fun <R> map(
            engineMapper: (T) -> R,
            botAMapper: (T) -> R,
            botBMapper: (T) -> R): ActorsData<R>
            = ActorsData(engineMapper(engine), botAMapper(botA), botBMapper(botB))

    fun <R> map(engineMapper: (T) -> R, botMapper: (T) -> R): ActorsData<R>
            = ActorsData(engineMapper(engine), botMapper(botA), botMapper(botB))

    fun <R> unify(mapper: (T, T, T) -> R) = mapper(engine, botA, botB)

    operator fun get(actor: Actor): T = when (actor) {
        Actor.ENGINE -> engine
        Actor.BOT_A -> botA
        Actor.BOT_B -> botB
    }

    override fun iterator(): Iterator<T> = listOf(engine, botA, botB).iterator()

    override fun toString() = "ActorsData [engine=$engine, botA=$botA, botB=$botB]"

}