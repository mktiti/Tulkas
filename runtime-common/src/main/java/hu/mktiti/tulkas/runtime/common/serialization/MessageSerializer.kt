package hu.mktiti.tulkas.runtime.common.serialization

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.common.*
import java.nio.charset.StandardCharsets
import java.util.*

internal const val MESSAGE_SEPARATOR = '-'
internal const val MESSAGE_HEADER = "Message"

@InjectableType
interface MessageSerializer {

    fun serializeMessageDto(messageDto: MessageDto): String

}

@Injectable(tags = ["safe"])
class SafeMessageSerializer : MessageSerializer {

    private fun encode(param: String): String {
        return Base64.getEncoder().encodeToString(param.toByteArray(StandardCharsets.UTF_8))
    }

    private fun createMessage(name: String, params: Iterable<String>, dataString: String?): String {
        val headerList = mutableListOf(MESSAGE_HEADER, name)
        headerList.addAll(params.map(this::encode))
        headerList.add((dataString?.length ?: 0).toString())

        val header = headerList.joinToString(MESSAGE_SEPARATOR.toString(), postfix = MESSAGE_SEPARATOR.toString())
        return if (dataString == null) {
            header
        } else {
            header + dataString
        }
    }

    override fun serializeMessageDto(messageDto: MessageDto): String {
        val name: String = messageDto.header::class.simpleName ?: throw RuntimeException("")
        val params: List<String> = with(messageDto.header) {
            when (this) {
                is ProxyCall -> listOf(target.name)
                is LogEntry -> listOf(target.name, message)
                is CallResult -> listOf(method)
                is MatchResult -> listOf(result.type.name, result.actor?.name ?: "null")
                is ErrorResult -> listOf(message)
                ActorJar, ShutdownNotice, StartNotice -> emptyList()
            }
        }

        return createMessage(name, params, messageDto.dataMessage)
    }

}