package hu.mktiti.tulkas.runtime.handler.log

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.handler.log.LogSender.*

@InjectableType
interface LogConverter {

    fun convert(entry: ActorLogEntry): String

}

@Injectable(arity = InjectableArity.SINGLETON)
class DefaultLogConverter : LogConverter {

    private fun prefix(sender: LogSender): String = when (sender) {
        RUNTIME -> "[tulkas] "
        ENGINE -> "[game] "
        SELF -> ""
    }

    override fun convert(entry: ActorLogEntry): String = prefix(entry.sender) + entry.message

}