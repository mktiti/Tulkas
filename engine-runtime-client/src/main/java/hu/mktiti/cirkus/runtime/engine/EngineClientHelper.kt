package hu.mktiti.cirkus.runtime.engine

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.api.GameEngine
import hu.mktiti.cirkus.api.GameEngineLogger
import hu.mktiti.cirkus.runtime.common.BotDefinitionException
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import org.reflections.Reflections
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

@InjectableType
interface EngineClientHelper {

    fun <T : BotInterface> createProxyForBot(botClass: Class<T>, invokeLogic: (String, List<Any?>) -> Any?): T

    fun <T : BotInterface> searchAndCreateEngine(botClass: Class<T>, botA: BotInterface, botB: BotInterface, logger: GameEngineLogger): GameEngine<*>?

}

@Injectable(default = true)
class DefaultEngineClientHelper(
        private val reflections: Reflections = inject()
) : EngineClientHelper {

    override fun <T : BotInterface> createProxyForBot(botClass: Class<T>, invokeLogic: (String, List<Any?>) -> Any?): T {
        val proxy: Any = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), arrayOf(botClass)) { _, method, arguments ->
            invokeLogic(method.name, arguments.asList())
        }
        return if (botClass.isInstance(proxy)) {
            botClass.cast(proxy)
        } else {
            throw RuntimeException()
        }
    }

    private fun <T, B> filterConstructors(constructor: Constructor<T>, botInterface: Class<B>): Boolean {
        fun allBots(vararg types: Class<*>): Boolean = types.all { it.isAssignableFrom(botInterface) }

        val paramTypes = constructor.parameterTypes
        return when (paramTypes.size) {
            2 -> allBots(*paramTypes)
            3 -> (allBots(paramTypes[0], paramTypes[1]) && GameEngineLogger::class.java.isAssignableFrom(paramTypes[2]) ||
                    (allBots(paramTypes[1], paramTypes[2]) && GameEngineLogger::class.java.isAssignableFrom(paramTypes[0])))
            else -> false
        }
    }

    override fun <T : BotInterface> searchAndCreateEngine(botClass: Class<T>, botA: BotInterface, botB: BotInterface, logger: GameEngineLogger): GameEngine<*>? {
        val classes: List<Class<out GameEngine<*>>> = reflections.getSubTypesOf(GameEngine::class.java).toList()
        val constructors: List<Constructor<*>> =
                classes
                        .filter { !Modifier.isAbstract(it.modifiers) && Modifier.isPublic(it.modifiers) }
                        .flatMap { it.constructors.toList() }
                        .filter { filterConstructors(it, botClass) }

        val instance = when (constructors.size) {
            0 -> null
            1 -> {
                val constructor = constructors.first()
                when {
                    constructor.parameterCount == 2 -> constructor.newInstance(botClass, botClass)
                    GameEngineLogger::class.java.isAssignableFrom(constructor.parameterTypes[0]) ->
                        constructor.newInstance(logger, botA, botB)
                    else -> constructor.newInstance(botA, botB, logger)
                }
            }
            else ->
                throw BotDefinitionException("Multiple valid bot found for bot interface (public non-abstract class with no-arg public constructor)!")
        }

        return instance as? GameEngine<*>
    }

}