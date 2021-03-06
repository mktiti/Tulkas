package hu.mktiti.tulkas.runtime.common.serialization

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.property.intProperty
import hu.mktiti.tulkas.api.log.LogTarget
import hu.mktiti.tulkas.runtime.common.*
import java.io.IOException
import java.io.Reader
import java.net.SocketException
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.collections.ArrayList

private fun Reader.readParameter(maxParamSize: Int): String? {
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

private fun Reader.readBinaryParam(maxBinaryParamSize: Int, maxParamSize: Int): String? {
    val sizeParam = readParameter(maxParamSize)?.toIntOrNull() ?:
        throw MessageException("Illegal size param")

    if (sizeParam > maxBinaryParamSize) {
        throw MessageException("Illegal size param")
    }

    val param: String? = if (sizeParam == 0) {
        null
    } else {
        val inArray = CharArray(sizeParam)
        var readPos = 0

        while (readPos != sizeParam) {
            val readCount = read(inArray, readPos, sizeParam - readPos)
            if (readCount == -1) {
                throw MessageException("Channel closed while reading message binary parameter")
            }

            readPos += readCount
        }

        String(inArray)
    }

    val next = read()
    return if (next != '\n'.toInt()) {
        throw MessageException("Message not terminated by newline ('\\n'), got '$next' (char '${next.toChar()}')")
    } else {
        param
    }
}

@InjectableType
interface MessageDeserializer {

    @Throws(MessageException::class)
    fun readMessageDto(reader: Reader): MessageDto

}

class HeaderTypeData(val name: String, val paramCount: Int, val creator: (List<String>) -> Header?)

@Injectable(tags = ["safe"])
class SafeMessageDeserializer(
        private val maxParamSize: Int = intProperty("HEADER_PARAM_LIMIT", 1000),
        private val maxBinaryParamSize: Int = intProperty("BINARY_PARAM_LIMIT", 1_000_000)
) : MessageDeserializer {

    private val log by logger()

    private val headerData = listOf(
            HeaderTypeData("LogEntry", 2) { params ->
                val target = safeValueOf<LogTarget>(params[0])
                if (target != null) LogEntry(target, params[1]) else null
            },
            HeaderTypeData("ChallengeResultH", 2) { params ->
                val points    = params[0].toLongOrNull()
                val maxPoints = params[1].toLongOrNull()
                ChallengeResultH(points, maxPoints)
            },
            HeaderTypeData("MatchResultH", 1) { params ->
                safeValueOf<hu.mktiti.tulkas.api.match.MatchResult.ResultType>(params[0])?.let(::MatchResultH)
            },
            HeaderTypeData("ProxyCall", 1) { params ->
                safeValueOf<CallTarget>(params[0])?.let(::ProxyCall)
            },
            HeaderTypeData("CallResult", 1) { params -> CallResult(params[0]) },
            HeaderTypeData("BotTimeout", 0) { _ -> BotTimeout },
            HeaderTypeData("ErrorResult", 1) { params -> ErrorResult(params[0]) },
            HeaderTypeData("ActorJar", 1) { params ->
                safeValueOf<ActorBinType>(params[0])?.let(::ActorJar)
            },
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

        val headerType = reader.readPartOneOf(headerNames)
                ?: throw MessageException("Invalid message header type")

        val headerData = headerData.find { it.name == headerType }
                ?: throw MessageException("No actordata about header type, this should not happen")

        val params: List<String> = (0 until headerData.paramCount)
                .map { reader.readParameter(maxParamSize) }
                .liftNulls() ?: throw MessageException("Header parameter cannot be null")

        try {
            val header = headerData.creator(params.map(this::decode))
                    ?: throw MessageException("Invalid header parameter for $headerType")

            MessageDto(header, reader.readBinaryParam(maxBinaryParamSize = maxBinaryParamSize, maxParamSize = maxParamSize))
        } catch (_: IndexOutOfBoundsException) {
            throw MessageException("Header parameter missing for $headerType")
        }
    } catch (se: SocketException) {
        log.error("SocketException while parsing message - possibly socket closed")
        throw MessageException("SocketException while trying to parse message - possibly socket closed")
    } catch (ioe: IOException) {
        log.error("IOException while parsing message", ioe)
        throw MessageException("IOException while trying to parse message")
    } catch (iae: IllegalArgumentException) {
        log.error("IllegalArgumentException while parsing message", iae)
        throw MessageException("IllegalArgumentException while trying to parse message")
    }
}