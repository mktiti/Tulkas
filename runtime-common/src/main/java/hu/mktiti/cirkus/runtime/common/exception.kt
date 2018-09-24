package hu.mktiti.cirkus.runtime.common

open class BotException(message: String) : RuntimeException(message)

class BotDefinitionException(message: String) : BotException(message)

class MessageException(message: String) : RuntimeException(message)