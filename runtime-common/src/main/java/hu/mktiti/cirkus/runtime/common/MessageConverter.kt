package hu.mktiti.cirkus.runtime.common

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

@InjectableType
interface MessageConverter {

    fun toDto(message: Message): MessageDto

    fun fromDto(message: MessageDto): Message

}

@Injectable(tags = ["serialize"], default = true)
class SerializationMessageConverter : MessageConverter {

    override fun toDto(message: Message): MessageDto {
        val data = message.data?.let { data ->
            ByteArrayOutputStream().use { baos ->
                ObjectOutputStream(baos).use { oos ->
                    oos.writeObject(data)
                    oos.flush()
                }
                Base64.getEncoder().encodeToString(baos.toByteArray())
            }
        }
        return MessageDto(message.header, data)
    }

    override fun fromDto(message: MessageDto): Message {
        val data = message.dataMessage?.let { data ->
            val bytes = Base64.getDecoder().decode(data)
            ObjectInputStream(ByteArrayInputStream(bytes)).use {
                it.readObject()
            }
        }
        return Message(message.header, data)
    }

}