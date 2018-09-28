package hu.mktiti.cirkus.runtime.base

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.cirkus.runtime.common.BotDefinitionException
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject

@InjectableType
interface RuntimeClientHelper {

    fun searchForBotInterface(): Class<out BotInterface>?

}

@Injectable(default = true)
class DefaultRuntimeClientHelper(
        private val binaryClassLoader: BinaryClassLoader = inject()
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