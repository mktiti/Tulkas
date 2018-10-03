package hu.mktiti.tulkas.runtime.handler.actordata

import hu.mktiti.tulkas.runtime.handler.control.Actor

fun <T> twoPlayer(engine: T, botA: T, botB: T) =
        UnifiedTwoPlayerData(engine, botA, botB)

class UnifiedSinglePlayerData<out T>(
        engine: T,
        bot: T
) : SinglePlayerData<T, T>(
        engine, bot
), UnifiedActorsData<T> {

    override val actorData: List<T> by lazy { listOf(engine, bot) }

    constructor(initial: T) : this(initial, initial)

    constructor(init: () -> T) : this(init, init)

    constructor(engineInit: () -> T, botInit: () -> T) : this(engineInit(), botInit())

    override fun <R> umap(mapper: (T) -> R): UnifiedSinglePlayerData<R> = umap(mapper, mapper)

    override fun <R> umap(
            engineMapper: (T) -> R,
            botMapper: (T) -> R
    ): UnifiedSinglePlayerData<R> = UnifiedSinglePlayerData(
            engineMapper(engine),
            botMapper(bot)
    )

    infix fun <O> uzip(other: UnifiedSinglePlayerData<O>): UnifiedSinglePlayerData<Pair<T, O>> =
            uzipWith(other, ::Pair)

    fun <O, R> uzipWith(
            other: UnifiedSinglePlayerData<O>,
            mapper: (T, O) -> R
    ): UnifiedSinglePlayerData<R> = uzipWith(other, mapper, mapper)

    fun <O, R> uzipWith(
            other: UnifiedSinglePlayerData<O>,
            engineMapper: (T, O) -> R,
            botMapper: (T, O) -> R
    ): UnifiedSinglePlayerData<R> = UnifiedSinglePlayerData(
            engineMapper(engine, other.engine),
            botMapper(bot, other.bot)
    )

    override fun safeGet(actor: Actor): T? = when (actor) {
        Actor.ENGINE -> engine
        Actor.BOT_A  -> bot
        else -> null
    }

}