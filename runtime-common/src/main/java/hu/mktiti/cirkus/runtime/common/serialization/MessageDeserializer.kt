package hu.mktiti.cirkus.runtime.common.serialization

import hu.mktiti.cirkus.api.GameResult
import hu.mktiti.cirkus.api.LogTarget
import hu.mktiti.cirkus.runtime.common.*
import hu.mktiti.cirkus.runtime.common.util.liftNulls
import hu.mktiti.cirkus.runtime.common.util.safeValueOf
import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.property.intProperty
import java.io.IOException
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.util.*

private fun Reader.readParameter(maxParamSize: Int = intProperty("HEADER_PARAM_LIMIT", 1000)): String? {
    val readChars = LinkedList<Char>()
    var count: Long = 0

    while (true) {
        val read = read()
        if (read == -1) {
            return null
        }

        val c = read.toChar()
        if (c == MESSAGE_SEPARATOR) {
            break
        } else if (++count > maxParamSize) {
            throw MessageException("Parameter exceeds maximum size [max=$maxParamSize]")
        } else {
            readChars.addFirst(c)
        }
    }

    return String(readChars.reversed().toCharArray())
}

private fun Reader.readPartOneOf(possibilities: Collection<String>): String? {
    val charArrays: MutableList<CharArray> = ArrayList(possibilities.map(String::toCharArray))
    var position = 0

    while (true) {
        val read = read()
        if (read == -1) {
            return null
        }

        val char = read.toChar()
        if (char == MESSAGE_SEPARATOR) {
            charArrays.removeIf {
                it.size != position
            }
            return charArrays.singleOrNull()?.let { String(it) }
        }

        charArrays.removeIf {
            it.size <= position || char != it[position]
        }

        if (charArrays.isEmpty()) {
            return null
        }

        position++
    }

}

private fun Reader.readBinaryParam(sizeLimit: Int = intProperty("BINARY_PARAM_LIMIT", 1_000_000)): String? {
    val sizeParam = readParameter()?.toIntOrNull() ?:
        throw MessageException("Illegal size param")

    if (sizeParam > sizeLimit) {
        throw MessageException("Illegal size param")
    }

    if (sizeParam == 0) {
        return null
    }

    val inArray = CharArray(sizeParam)
    return if (read(inArray) == sizeParam) {
        String(inArray)
    } else {
        null
    }
}

@InjectableType
interface MessageDeserializer {

    @Throws(MessageException::class)
    fun readMessageDto(reader: Reader): MessageDto

}

class HeaderTypeData(val name: String, val paramCount: Int, val creator: (List<String>) -> Header?)

@Injectable(tags = ["safe"])
class SafeMessageDeserializer : MessageDeserializer {

    private val headerData = listOf(
            HeaderTypeData("LogEntry", 2) { params ->
                val target = safeValueOf<LogTarget>(params[0])
                if (target != null) LogEntry(target, params[1]) else null
            },
            HeaderTypeData("MatchResult", 2) { params ->
                val result = safeValueOf<GameResult.ResultType>(params[0])
                val actor = safeValueOf<GameResult.Actor>(params[1])

                if (result != null) {
                    MatchResult(GameResult(result, actor))
                } else {
                    null
                }
            },
            HeaderTypeData("ProxyCall", 1) { params ->
                safeValueOf<CallTarget>(params[0])?.let(::ProxyCall)
            },
            HeaderTypeData("CallResult", 1) { params -> CallResult(params[0]) },
            HeaderTypeData("ErrorResult", 1) { params -> ErrorResult(params[0]) },
            HeaderTypeData("ActorJar", 0) { ActorJar },
            HeaderTypeData("ShutdownNotice", 0) { ShutdownNotice },
            HeaderTypeData("StartNotice", 0) { StartNotice }
    )

    private val headerNames = headerData.map(HeaderTypeData::name)

    private fun decode(param: String): String {
        return String(Base64.getDecoder().decode(param), StandardCharsets.UTF_8)
    }

    @Throws(MessageException::class)
    override fun readMessageDto(reader: Reader): MessageDto = try {
        if (reader.readPartOneOf(listOf(MESSAGE_HEADER)) == null) {
            throw MessageException("Invalid message format (should start with $MESSAGE_HEADER)")
        }

        val headerType = reader.readPartOneOf(headerNames) ?: throw MessageException("Invalid message header type")

        val headerData = headerData.find { it.name == headerType }
                ?: throw MessageException("No data about header type, this should not happen")

        val params: List<String> = (0 until headerData.paramCount)
                .map { reader.readParameter() }
                .liftNulls() ?: throw MessageException("Header parameter cannot be null")

        try {
            val header = headerData.creator(params.map(this::decode))
                    ?: throw MessageException("Invalid header parameter for $headerType")

            MessageDto(header, reader.readBinaryParam())
        } catch (_: IndexOutOfBoundsException) {
            throw MessageException("Header parameter missing for $headerType")
        }
    } catch (_: IOException) {
        throw MessageException("IOException while trying to parse message")
    }

}