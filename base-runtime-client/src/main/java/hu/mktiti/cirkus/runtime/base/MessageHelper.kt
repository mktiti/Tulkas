package hu.mktiti.cirkus.runtime.base

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import hu.mktiti.kreator.Injectable
import hu.mktiti.kreator.InjectableType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

@InjectableType
interface MessageHelper {

    fun serializeMessage(message: Message): String

    fun deserializeMessage(message: String): Message

}

@Injectable(tags = ["json"], default = true)
class JsonMessageHelper() : MessageHelper {

    private val mapper = jacksonObjectMapper()

    override fun deserializeMessage(message: String): Message {
        val messageDto: MessageDto = mapper.readValue(message)
        return if (messageDto.dataMessage == null) {
            Message(messageDto.header)
        } else {
            val bytes = Base64.getDecoder().decode(messageDto.dataMessage)
            ObjectInputStream(ByteArrayInputStream(bytes)).use {
                Message(messageDto.header, it.readObject())
            }
        }
    }

    override fun serializeMessage(message: Message): String {
        val dataString = ByteArrayOutputStream().use { baos ->
            ObjectOutputStream(baos).use { oos ->
                oos.writeObject(message.data)
            }
            Base64.getEncoder().encodeToString(baos.toByteArray())
        }

        return mapper.writeValueAsString(MessageDto(message.header, dataString))
    }
}