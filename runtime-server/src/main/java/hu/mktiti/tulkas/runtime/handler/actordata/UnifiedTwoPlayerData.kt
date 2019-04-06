package hu.mktiti.tulkas.runtime.handler.actordata

import hu.mktiti.tulkas.runtime.handler.control.Actor

fun actorsData(): UnifiedTwoPlayerData<Actor> =
        UnifiedTwoPlayerData(Actor.ENGINE, Actor.BOT_A, Actor.BOT_B)

class UnifiedTwoPlayerData<out T>(
        engine: T,
        botA: T,
        botB: T
) : TwoPlayerData<T, T>(
        engine, botA, botB
), UnifiedActorsData<T> {

    override val actorData: List<T> by lazy { listOf(engine, botA, botB) }

    constructor(initial: T) : this(initial, initial)

    constructor(engine: T, bot: T) : this(engine, bot, bot)

    constructor(init: () -> T) : this(init, init)

    constructor(engineInit: () -> T, botInit: () -> T) : this(engineInit, botInit, botInit)

    constructor(
            engineInit: () -> T,
            botAInit: () -> T,
            botBInit: () -> T
    ) : this(engineInit(), botAInit(), botBInit())

    override fun <R> umap(mapper: (T) -> R): UnifiedTwoPlayerData<R> = umap(mapper, mapper)

    override fun <R> umap(
            engineMapper: (T) -> R,
            botMapper: (T) -> R
    ): UnifiedTwoPlayerData<R> = umap(engineMapper, botMapper, botMapper)

    fun <R> umap(
            engineMapper: (T) -> R,
            botAMapper: (T) -> R,
            botBMapper: (T) -> R
    ): UnifiedTwoPlayerData<R> = UnifiedTwoPlayerData(
            engineMapper(engine),
            botAMapper(botA),
            botBMapper(botB)
    )

    infix fun <O> uzip(other: UnifiedTwoPlayerData<O>): UnifiedTwoPlayerData<Pair<T, O>> =
            uzipWith(other, ::Pair)

    fun <O, R> uzipWith(
            other: UnifiedTwoPlayerData<O>,
            mapper: (T, O) -> R
    ): UnifiedTwoPlayerData<R> = uzipWith(other, mapper, mapper)

    fun <O, R> uzipWith(
            other: UnifiedTwoPlayerData<O>,
            engineMapper: (T, O) -> R,
            botMapper: (T, O) -> R
    ): UnifiedTwoPlayerData<R> = uzipWith(other, engineMapper, botMapper, botMapper)

    fun <O, R> uzipWith(
            other: UnifiedTwoPlayerData<O>,
            engineMapper: (T, O) -> R,
            botAMapper: (T, O) -> R,
            botBMapper: (T, O) -> R
    ): UnifiedTwoPlayerData<R> = UnifiedTwoPlayerData(
            engineMapper(engine, other.engine),
            botAMapper(botA, other.botA),
            botBMapper(botB, other.botB)
    )

    override fun safeGet(actor: Actor): T? = when (actor) {
        Actor.ENGINE -> engine
        Actor.BOT_A  -> botA
        Actor.BOT_B  -> botB
    }

}