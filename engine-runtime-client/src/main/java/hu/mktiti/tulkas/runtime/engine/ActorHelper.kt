package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.api.BotInterface
import hu.mktiti.tulkas.api.GameEngine
import hu.mktiti.tulkas.runtime.base.RuntimeClientHelper
import hu.mktiti.tulkas.runtime.common.CallTarget

@InjectableType
interface ActorHelper {

    fun createActors(messageHandler: EngineMessageHandler, isMatch: Boolean): GameEngine<*>?

}

@Injectable
class DefaultActorHelper(
        private val clientHelper: RuntimeClientHelper,
        private val engineHelper: EngineClientHelper
) : ActorHelper {

    private fun proxy(botInterface: Class<out BotInterface>, messageHandler: EngineMessageHandler, target: CallTarget) =
            engineHelper.createProxyForBot(botInterface) { method, args ->
                messageHandler.callFunction(target, method, args)
            }

    override fun createActors(messageHandler: EngineMessageHandler, isMatch: Boolean): GameEngine<*>? {

        return try {
            val botInterface: Class<out BotInterface> = clientHelper.searchForBotInterface() ?: return null

            val bots = mutableListOf(
                    proxy(botInterface, messageHandler, CallTarget.BOT_A)
            )

            if (isMatch) {
                bots += proxy(botInterface, messageHandler, CallTarget.BOT_B)
            }

            return engineHelper.searchAndCreateEngine(botInterface, bots)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}