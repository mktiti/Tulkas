package hu.mktiti.tulkas.runtime.base

import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.tulkas.runtime.common.Message
import hu.mktiti.tulkas.runtime.common.MessageDto
import java.io.*
import java.util.*

@InjectableType
interface MessageConverter {

    fun toDto(message: Message): MessageDto

    fun fromDto(message: MessageDto): Message

}

class CustomLoaderObjectInputStream(
        inputStream: InputStream,
        private val classLoader: ClassLoader
) : ObjectInputStream(inputStream) {

    override fun resolveClass(streamClass: ObjectStreamClass): Class<*> = try {
        Class.forName(streamClass.name, false, classLoader)
    } catch (_: ClassNotFoundException) {
        super.resolveClass(streamClass)
    }

}

class SerializationMessageConverter(
        private val binaryClassLoader: BinaryClassLoader
) : MessageConverter {

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
            CustomLoaderObjectInputStream(ByteArrayInputStream(bytes), binaryClassLoader).use {
                it.readObject()
            }
        }
        return Message(message.header, data)
    }

}