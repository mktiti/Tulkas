package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.api.GameBotLogger
import hu.mktiti.cirkus.runtime.common.BotDefinitionException
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import org.reflections.Reflections
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

@InjectableType
interface BotClientHelper {

    fun <I : BotInterface> searchAndCreateBotImplementation(botInterface: Class<I>, logger: GameBotLogger): I?

    fun createProxyForBot(botInterface: Class<out BotInterface>, bot: BotInterface): BotProxy

}

@Injectable(default = true)
class DefaultBotClientHelper(
        private val reflections: Reflections = inject()
) : BotClientHelper {

    override fun createProxyForBot(botInterface: Class<out BotInterface>, bot: BotInterface): BotProxy {
        val methods = botInterface.declaredMethods
                .filter { Modifier.isPublic(it.modifiers) }
                .map { it.name to { args: List<Any?> -> it.invoke(bot, *(args.toTypedArray())) } }
                .toMap()
        return MappedBotProxy(methods)
    }

    override fun <I : BotInterface> searchAndCreateBotImplementation(botInterface: Class<I>, logger: GameBotLogger): I? {
        val classes: List<Class<out I>> = reflections.getSubTypesOf(botInterface).toList()
        val constructors: List<Constructor<*>> =
                classes
                        .filter { !Modifier.isAbstract(it.modifiers) && Modifier.isPublic(it.modifiers) }
                        .flatMap { it.constructors.toList() }
                        .filter { it.parameterCount == 0 ||
                                 (it.parameterCount == 1 && GameBotLogger::class.java.isAssignableFrom(it.parameterTypes[0])) }
                        .filterNotNull()

        val instance = when (constructors.size) {
            0 -> null
            1 -> {
                val constructor = constructors.first()
                if (constructor.parameterCount == 1) {
                    constructor.newInstance(logger)
                } else {
                    constructor.newInstance()
                }
            }
            else ->
                throw BotDefinitionException("Multiple valid bot found for bot interface (public non-abstract class with no-arg public constructor)!")
        }

        return instance as? I
    }
}