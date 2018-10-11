package hu.mktiti.tulkas.runtime.handler.actordata

import hu.mktiti.tulkas.runtime.handler.control.Actor

open class SinglePlayerData<out E, out B>(
        override val engine: E,
        val bot: B
) : ActorsData<E, B> {

    override val bots by lazy { listOf(bot) }

    override val actors: Set<Actor> by lazy { setOf(Actor.ENGINE, Actor.BOT_A) }

    override val isMatch = false

    constructor(engineInit: () -> E, botInit: () -> B) : this(engineInit(), botInit())

    override fun <ER, BR> map(
            engineMapper: (E) -> ER,
            botMapper: (B) -> BR
    ): SinglePlayerData<ER, BR> = SinglePlayerData(engineMapper(engine), botMapper(bot))

    override fun <R> unify(
            engineMapper: (E) -> R,
            botMapper: (B) -> R
    ): UnifiedSinglePlayerData<R> = UnifiedSinglePlayerData(engineMapper(engine), botMapper(bot))

    override fun <R> unify(init: () -> R) = UnifiedSinglePlayerData(init)

    fun <R> collect(collector: (E, B) -> R): R = collector(engine, bot)

    infix fun <OE, OB> zip(other: SinglePlayerData<OE, OB>): SinglePlayerData<Pair<E, OE>, Pair<B, OB>> =
            zipWith(other, ::Pair, ::Pair)

    fun <OE, OB, ER, BR> zipWith(
            other: SinglePlayerData<OE, OB>,
            engineMapper: (E, OE) -> ER,
            botMapper: (B, OB) -> BR
    ): SinglePlayerData<ER, BR> = SinglePlayerData(
            engineMapper(engine, other.engine),
            botMapper(bot, other.bot)
    )

    override fun safeGetBot(actor: Actor): B? = when(actor) {
        Actor.BOT_A -> bot
        else -> null
    }

    override fun toString() = "ActorsData [engine=$engine, bot=$bot]"

}

fun <T, E : T, B : T> SinglePlayerData<E, B>.unified(): UnifiedSinglePlayerData<T> =
        UnifiedSinglePlayerData(engine, bot)