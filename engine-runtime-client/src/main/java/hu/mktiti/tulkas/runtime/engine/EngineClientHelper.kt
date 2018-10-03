package hu.mktiti.tulkas.runtime.engine

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject
import hu.mktiti.tulkas.api.BotInterface
import hu.mktiti.tulkas.api.GameEngine
import hu.mktiti.tulkas.runtime.base.BinaryClassLoader
import hu.mktiti.tulkas.runtime.common.BotDefinitionException
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy

@InjectableType
interface EngineClientHelper {

    fun <T : BotInterface> createProxyForBot(botClass: Class<T>, invokeLogic: (String, List<Any?>) -> Any?): T

    fun <T : BotInterface> searchAndCreateEngine(botClass: Class<T>, botA: BotInterface, botB: BotInterface): GameEngine<*>?

}

@Injectable
class DefaultEngineClientHelper(
        private val binaryClassLoader: BinaryClassLoader = inject()
) : EngineClientHelper {

    override fun <T : BotInterface> createProxyForBot(botClass: Class<T>, invokeLogic: (String, List<Any?>) -> Any?): T {
        return botClass.cast(Proxy.newProxyInstance(binaryClassLoader, arrayOf(botClass)) { _, method, arguments ->
            invokeLogic(method.name, arguments.asList())
        })
    }

    override fun <T : BotInterface> searchAndCreateEngine(botClass: Class<T>, botA: BotInterface, botB: BotInterface): GameEngine<*>? {

        val candidateClasses: List<Class<*>> = binaryClassLoader.allClasses().filter { GameEngine::class.java.isAssignableFrom(it) }
        val constructors: List<Constructor<*>> =
                candidateClasses
                        .filter { !Modifier.isAbstract(it.modifiers) && Modifier.isPublic(it.modifiers) }
                        .flatMap { it.constructors.toList() }
                        .filter { it.parameterCount == 2 && it.parameterTypes.all { p ->  p.isAssignableFrom(botClass) } }

        val instance = when (constructors.size) {
            0 -> null
            1 -> constructors.first().newInstance(botA, botB)
            else ->
                throw BotDefinitionException("Multiple valid bot found for bot interface (public non-abstract class with no-arg public constructor)!")
        }

        return instance as? GameEngine<*>
    }

}