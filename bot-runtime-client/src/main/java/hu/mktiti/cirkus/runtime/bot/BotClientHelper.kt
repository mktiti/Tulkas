package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.runtime.base.BinaryClassLoader
import hu.mktiti.cirkus.runtime.common.BotDefinitionException
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

@InjectableType
interface BotClientHelper {

    fun <I : BotInterface> searchAndCreateBotImplementation(botInterface: Class<I>): I?

    fun createProxyForBot(botInterface: Class<out BotInterface>, bot: BotInterface): BotProxy

}

@Injectable(default = true)
class DefaultBotClientHelper(
        private val binaryClassLoader: BinaryClassLoader = inject()
) : BotClientHelper {

    override fun createProxyForBot(botInterface: Class<out BotInterface>, bot: BotInterface): BotProxy {
        val methods = botInterface.declaredMethods
                .filter { Modifier.isPublic(it.modifiers) }
                .map { it.name to { args: List<Any?> -> it.invoke(bot, *(args.toTypedArray())) } }
                .toMap()
        return MappedBotProxy(methods)
    }

    override fun <I : BotInterface> searchAndCreateBotImplementation(botInterface: Class<I>): I? {
        val candidateClasses: List<Class<*>> = binaryClassLoader.allClasses().filter { botInterface.isAssignableFrom(it) }
        val constructors: List<Constructor<out I>> =
                candidateClasses
                        .filter { !Modifier.isAbstract(it.modifiers) && Modifier.isPublic(it.modifiers) }
                        .map { it.asSubclass(botInterface) }
                        .mapNotNull { it.getConstructor() }

        return when (constructors.size) {
            0 -> null
            1 -> constructors.first().newInstance()
            else ->
                throw BotDefinitionException("Multiple valid bot found for bot interface (public non-abstract class with no-arg public constructor)!")
        }
    }
}