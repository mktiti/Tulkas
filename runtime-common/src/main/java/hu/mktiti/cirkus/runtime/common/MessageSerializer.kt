package hu.mktiti.cirkus.runtime.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.api.inject

@InjectableType
interface MessageSerializer {

    fun serializeMessage(message: Message): String

    fun serializeMessageDto(message: MessageDto): String

    fun deserializeMessage(message: String): Message

    fun deserializeMessageDto(message: String): MessageDto

}

@Injectable(tags = ["json"], default = true)
class JsonMessageSerializer(
        private val messageConverter: MessageConverter = inject()
) : MessageSerializer {

    private val mapper = jacksonObjectMapper()

    override fun deserializeMessage(message: String) = messageConverter.fromDto(deserializeMessageDto(message))

    override fun deserializeMessageDto(message: String): MessageDto = mapper.readValue(message)

    override fun serializeMessage(message: Message) = serializeMessageDto(messageConverter.toDto(message))

    override fun serializeMessageDto(message: MessageDto): String = mapper.writeValueAsString(message)
}