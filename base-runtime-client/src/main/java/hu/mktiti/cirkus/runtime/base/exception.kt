package hu.mktiti.cirkus.runtime.base

open class BotException(message: String) : RuntimeException(message)

class BotDefinitionException(message: String) : BotException(message)