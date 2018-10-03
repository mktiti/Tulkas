package hu.mktiti.tulkas.runtime.handler.actordata

import hu.mktiti.tulkas.runtime.handler.control.Actor

open class TwoPlayerData<out E, out B>(
        override val engine: E,
        val botA: B,
        val botB: B
) : ActorsData<E, B> {

    override val bots by lazy { listOf(botA, botB) }

    override val actors: Set<Actor> by lazy { setOf(Actor.ENGINE, Actor.BOT_A, Actor.BOT_B) }

    constructor(engine: E, bot: B) : this(engine, bot, bot)

    constructor(engineInit: () -> E, botInit: () -> B) : this(engineInit(), botInit(), botInit())

    constructor(
            engineInit: () -> E,
            botAInit: () -> B,
            botBInit: () -> B
    ) : this(engineInit(), botAInit(), botBInit())

    override fun <ER, BR> map(
            engineMapper: (E) -> ER,
            botMapper: (B) -> BR
    ): TwoPlayerData<ER, BR> = map(engineMapper, botMapper, botMapper)

    fun <ER, BR> map(
            engineMapper: (E) -> ER,
            botAMapper: (B) -> BR,
            botBMapper: (B) -> BR
    ): TwoPlayerData<ER, BR> = TwoPlayerData(engineMapper(engine), botAMapper(botA), botBMapper(botB))

    override fun <R> unify(
            engineMapper: (E) -> R,
            botMapper: (B) -> R
    ): UnifiedTwoPlayerData<R> = unify(engineMapper, botMapper, botMapper)

    fun <R> unify(
            engineMapper: (E) -> R,
            botAMapper: (B) -> R,
            botBMapper: (B) -> R
    ): UnifiedTwoPlayerData<R> = UnifiedTwoPlayerData(engineMapper(engine), botAMapper(botA), botBMapper(botB))

    override fun <R> unify(init: () -> R) = UnifiedTwoPlayerData(init)

    fun <R> collect(collector: (E, B, B) -> R): R = collector(engine, botA, botB)

    infix fun <OE, OB> zip(other: TwoPlayerData<OE, OB>): TwoPlayerData<Pair<E, OE>, Pair<B, OB>> =
            zipWith(other, ::Pair, ::Pair)

    fun <OE, OB, ER, BR> zipWith(
            other: TwoPlayerData<OE, OB>,
            engineMapper: (E, OE) -> ER,
            botMapper: (B, OB) -> BR
    ): TwoPlayerData<ER, BR> = zipWith(other, engineMapper, botMapper, botMapper)

    fun <OE, OB, ER, BR> zipWith(
            other: TwoPlayerData<OE, OB>,
            engineMapper: (E, OE) -> ER,
            botAMapper: (B, OB) -> BR,
            botBMapper: (B, OB) -> BR
    ): TwoPlayerData<ER, BR> =
            TwoPlayerData(
                    engineMapper(engine, other.engine),
                    botAMapper(botA, other.botA),
                    botBMapper(botB, other.botB)
            )

    override fun safeGetBot(actor: Actor): B? = when(actor) {
        Actor.BOT_A -> botA
        Actor.BOT_B -> botB
        else -> null
    }

    override fun toString() = "ActorsData [engine=$engine, botA=$botA, botB=$botB]"

}

fun <T, E : T, B : T> TwoPlayerData<E, B>.unified(): UnifiedTwoPlayerData<T> =
        UnifiedTwoPlayerData(engine, botA, botB)