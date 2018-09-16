package hu.mktiti.cirkus.runtime.bot

import hu.mktiti.cirkus.runtime.common.BotException

interface BotProxy {

    fun callMethod(methodName: String, arguments: List<Any?>): Any?

}

class MappedBotProxy(
        private val callMap: Map<String, (List<Any?>) -> Any?>
) : BotProxy {

    override fun callMethod(methodName: String, arguments: List<Any?>): Any? =
        (callMap[methodName] ?: throw BotException("No method '$methodName' mapped")).invoke(arguments)

}