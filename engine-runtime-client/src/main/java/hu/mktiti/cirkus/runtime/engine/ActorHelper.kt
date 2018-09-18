package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.api.GameEngine
import hu.mktiti.cirkus.api.GameEngineLogger
import hu.mktiti.cirkus.runtime.base.RuntimeClientHelper
import hu.mktiti.cirkus.runtime.common.CallTarget
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject

data class Actors<T : BotInterface>(
        val engine: GameEngine<*>,
        val botA: T,
        val botB: T
)

@InjectableType
interface ActorHelper {

    fun createActors(messageHandler: MessageHandler, logger: GameEngineLogger): Actors<*>?

}

@Injectable
class DefaultActorHelper(
        private val clientHelper: RuntimeClientHelper = inject(),
        private val engineHelper: EngineClientHelper = inject()
) : ActorHelper {

    private fun proxy(botInterface: Class<out BotInterface>, messageHandler: MessageHandler, target: CallTarget) =
            engineHelper.createProxyForBot(botInterface) { method, args ->
                messageHandler.callFunction(target, method, args)
            }

    override fun createActors(messageHandler: MessageHandler, logger: GameEngineLogger): Actors<*>? {

        return try {
            val botInterface: Class<out BotInterface> = clientHelper.searchForBotInterface() ?: return null
            val proxyA: BotInterface = proxy(botInterface, messageHandler, CallTarget.BOT_A)
            val proxyB: BotInterface = proxy(botInterface, messageHandler, CallTarget.BOT_B)
            val engine: GameEngine<*> = engineHelper.searchAndCreateEngine(botInterface, proxyA, proxyB, logger) ?: return null
            Actors(engine, proxyA, proxyB)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}