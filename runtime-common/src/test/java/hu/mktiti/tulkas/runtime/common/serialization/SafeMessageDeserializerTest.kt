package hu.mktiti.tulkas.runtime.common.serialization

import hu.mktiti.tulkas.api.log.LogTarget
import hu.mktiti.tulkas.api.match.MatchResult
import hu.mktiti.tulkas.runtime.common.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.Reader
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.test.assertEquals

internal class SafeMessageDeserializerTest {

    private val paramLimit: Int = 100
    private val binaryParamLimit: Int = 1000

    private val testMessage = "message"

    private val deserializer: MessageDeserializer = SafeMessageDeserializer(
            maxParamSize = paramLimit,
            maxBinaryParamSize = binaryParamLimit
    )

    private fun String.reader(): Reader = StringReader(this)

    private fun base64(value: String): String =
            Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    @Test
    fun `test proxy call no param deserialize success`() {
        val messageString = "Message-ProxyCall-${base64("BOT_B")}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ProxyCall(CallTarget.BOT_B))
        assertEquals(expected, result)
    }

    @Test
    fun `test proxy call deserialize success`() {
        val callData = base64(testMessage)
        val messageString = "Message-ProxyCall-${base64("BOT_A")}-${callData.length}-$callData\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ProxyCall(CallTarget.BOT_A), callData)
        assertEquals(expected, result)
    }

    @Test
    fun `test log entry deserialize success`() {
        val messageString = "Message-LogEntry-${base64("BOTS")}-${base64(testMessage)}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(LogEntry(LogTarget.BOTS, testMessage))
        assertEquals(expected, result)
    }

    @Test
    fun `test call result deserialize success`() {
        val messageString = "Message-CallResult-${base64(testMessage)}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(CallResult(testMessage))
        assertEquals(expected, result)
    }

    @Test
    fun `test challenge result no points no max deserialize success`() {
        val messageString = "Message-ChallengeResultH-${base64("null")}-${base64("null")}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ChallengeResultH(null, null))
        assertEquals(expected, result)
    }

    @Test
    fun `test challenge result crash deserialize success`() {
        val messageString = "Message-ChallengeResultH-${base64("null")}-${base64("10")}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ChallengeResultH(null, 10L))
        assertEquals(expected, result)
    }

    @Test
    fun `test challenge result no max deserialize success`() {
        val messageString = "Message-ChallengeResultH-${base64("10")}-${base64("null")}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ChallengeResultH(10L, null))
        assertEquals(expected, result)
    }

    @Test
    fun `test challenge result with max deserialize success`() {
        val messageString = "Message-ChallengeResultH-${base64("10")}-${base64("20")}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ChallengeResultH(10L, 20L))
        assertEquals(expected, result)
    }

    @Test
    fun `test match result deserialize success`() {
        val messageString = "Message-MatchResultH-${base64("BOT_A_ERROR")}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(MatchResultH(MatchResult.ResultType.BOT_A_ERROR))
        assertEquals(expected, result)
    }

    @Test
    fun `test bot timeout deserialize success`() {
        val messageString = "Message-BotTimeout-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(BotTimeout)
        assertEquals(expected, result)
    }

    @Test
    fun `test error result deserialize success`() {
        val messageString = "Message-ErrorResult-${base64(testMessage)}-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ErrorResult(testMessage))
        assertEquals(expected, result)
    }

    @Test
    fun `test actor jar deserialize success`() {
        val jarData = base64(testMessage)
        val messageString = "Message-ActorJar-${base64("ACTOR")}-${jarData.length}-$jarData\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ActorJar(ActorBinType.ACTOR), jarData)
        assertEquals(expected, result)
    }

    @Test
    fun `test shutdown notice deserialize success`() {
        val messageString = "Message-ShutdownNotice-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(ShutdownNotice)
        assertEquals(expected, result)
    }

    @Test
    fun `test start notice deserialize success`() {
        val messageString = "Message-StartNotice-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        val expected = MessageDto(StartNotice)
        assertEquals(expected, result)
    }

    @Test
    fun `test plus parameter deserialize failure`() {
        val messageString = "Message-StartNotice-whatever-0-\n"

        assertThrows<MessageException> {
            deserializer.readMessageDto(messageString.reader())
        }
    }

    @Test
    fun `test missing parameter deserialize failure`() {
        val messageString = "Message-ErrorResult-0-\n"

        assertThrows<MessageException> {
            deserializer.readMessageDto(messageString.reader())
        }
    }

    @Test
    fun `test long parameter deserialize success`() {
        val longParam = "a".repeat(paramLimit)
        val messageString = "Message-ErrorResult-$longParam-0-\n"

        val result = deserializer.readMessageDto(messageString.reader())

        assert(result.header is ErrorResult)
    }

    @Test
    fun `test too long parameter deserialize failure`() {
        val longParam = "a".repeat(paramLimit + 1)
        val messageString = "Message-ErrorResult-$longParam-0-\n"

        assertThrows<MessageException> {
            deserializer.readMessageDto(messageString.reader())
        }
    }

    @Test
    fun `test long binary parameter deserialize success`() {
        val longParam = "a".repeat(binaryParamLimit)
        val messageString = "Message-ProxyCall-${base64("BOT_A")}-${longParam.length}-$longParam\n"

        val result = deserializer.readMessageDto(messageString.reader())

        assertEquals(ProxyCall(CallTarget.BOT_A), result.header)
    }

    @Test
    fun `test too long binary parameter deserialize failure`() {
        val longParam = "a".repeat(binaryParamLimit + 1)
        val messageString = "Message-ProxyCall-${base64("BOT_A")}-${longParam.length}-$longParam\n"

        assertThrows<MessageException> {
            deserializer.readMessageDto(messageString.reader())
        }
    }

    @Test
    fun `test incorrect binary parameter size too big deserialize failure`() {
        val binaryParam = "whatever"
        val messageString = "Message-ActorJar-${base64("API")}-${binaryParam.length + 1}-$binaryParam\n"

        assertThrows<MessageException> {
            deserializer.readMessageDto(messageString.reader())
        }
    }

    @Test
    fun `test incorrect binary parameter size too small deserialize failure`() {
        val binaryParam = "whatever"
        val messageString = "Message-ActorJar-${base64("API")}-${binaryParam.length - 1}-$binaryParam\n"

        assertThrows<MessageException> {
            deserializer.readMessageDto(messageString.reader())
        }
    }

}