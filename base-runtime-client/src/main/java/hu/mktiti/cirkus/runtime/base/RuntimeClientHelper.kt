package hu.mktiti.cirkus.runtime.base

import hu.mktiti.cirkus.api.BotInterface
import hu.mktiti.kreator.Injectable
import hu.mktiti.kreator.InjectableArity
import hu.mktiti.kreator.InjectableType
import hu.mktiti.kreator.inject
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Modifier

@InjectableType
interface RuntimeClientHelper {

    fun searchForBotInterface(): Class<out BotInterface>?

}

@Injectable(arity = InjectableArity.SINGLETON)
fun produceReflections(): Reflections {
    val config = ConfigurationBuilder().setScanners(SubTypesScanner()).setUrls(ClasspathHelper.forJavaClassPath())
    return Reflections(config)
}

@Injectable(default = true)
class DefaultRuntimeClientHelper(private val reflections: Reflections = inject()) : RuntimeClientHelper {

    override fun searchForBotInterface(): Class<out BotInterface>? {
        val botInterfaces = reflections.getSubTypesOf(BotInterface::class.java).filter { c -> Modifier.isInterface(c.modifiers) }
        return when (botInterfaces.size) {
            0, 1 -> botInterfaces.firstOrNull()
            else -> throw BotDefinitionException("Multiple bot interfaces found!")
        }
    }

}