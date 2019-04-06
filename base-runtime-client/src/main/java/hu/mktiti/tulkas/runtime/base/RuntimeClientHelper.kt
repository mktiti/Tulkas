package hu.mktiti.tulkas.runtime.base

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.api.BotInterface
import hu.mktiti.tulkas.runtime.common.BotDefinitionException

@InjectableType
interface RuntimeClientHelper {

    fun searchForBotInterface(): Class<out BotInterface>?

}

class DefaultRuntimeClientHelper(
        private val binaryClassLoader: BinaryClassLoader
) : RuntimeClientHelper {

    override fun searchForBotInterface(): Class<out BotInterface>? {
        val botInterfaces = binaryClassLoader.allClasses()
                .filter { BotInterface::class.java.isAssignableFrom(it) && it.isInterface }
                .map { it.asSubclass(BotInterface::class.java) }

        return when (botInterfaces.size) {
            0, 1 -> botInterfaces.firstOrNull()
            else -> throw BotDefinitionException("Multiple bot interfaces found!")
        }
    }

}