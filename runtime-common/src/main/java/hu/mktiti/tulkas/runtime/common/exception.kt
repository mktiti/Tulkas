package hu.mktiti.tulkas.runtime.common

open class BotException(message: String) : RuntimeException(message)

class BotDefinitionException(message: String) : BotException(message)

class MessageException(message: String) : RuntimeException(message)

class ProxyCallTimeoutException(method: String) : BotException("Bot call timeout for method '$method'")