package hu.mktiti.tulkas.runtime.common.serialization

import hu.mktiti.tulkas.api.log.LogTarget
import hu.mktiti.tulkas.api.match.MatchResult
import hu.mktiti.tulkas.runtime.common.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.util.*

internal class SafeMessageSerializerTest {

    private val serializer = SafeMessageSerializer()

    private val testMessage = "MessageData"
    private val testMethod = "void myMethod(String)"

    private fun base64(value: String): String =
            Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))

    @Test
    fun `test proxy call no param serialization success`() {
        val message = MessageDto(ProxyCall(CallTarget.BOT_A), null)

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ProxyCall-${base64("BOT_A")}-0-", result)
    }

    @Test
    fun `test proxy call serialization success`() {
        val message = MessageDto(ProxyCall(CallTarget.BOT_B), testMessage)

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ProxyCall-${base64("BOT_B")}-${testMessage.length}-$testMessage", result)
    }

    @Test
    fun `test log entry serialization success`() {
        val message = MessageDto(LogEntry(LogTarget.BOTS, testMessage))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-LogEntry-${base64("BOTS")}-${base64(testMessage)}-0-", result)
    }

    @Test
    fun `test call result no data serialization success`() {
        val message = MessageDto(CallResult(testMethod))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-CallResult-${base64(testMethod)}-0-", result)
    }

    @Test
    fun `test call result serialization success`() {
        val message = MessageDto(CallResult(testMethod), testMessage)

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-CallResult-${base64(testMethod)}-${testMessage.length}-$testMessage", result)
    }

    @Test
    fun `test challenge result no points no max serialization success`() {
        val message = MessageDto(ChallengeResultH(null, null))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ChallengeResultH-${base64("null")}-${base64("null")}-0-", result)
    }

    @Test
    fun `test challenge result crash serialization success`() {
        val message = MessageDto(ChallengeResultH(null, 10L))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ChallengeResultH-${base64("null")}-${base64("10")}-0-", result)
    }

    @Test
    fun `test challenge result no max serialization success`() {
        val message = MessageDto(ChallengeResultH(10L, null))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ChallengeResultH-${base64("10")}-${base64("null")}-0-", result)
    }

    @Test
    fun `test challenge result with max serialization success`() {
        val message = MessageDto(ChallengeResultH(10L, 20L))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ChallengeResultH-${base64("10")}-${base64("20")}-0-", result)
    }

    @Test
    fun `test match result with max serialization success`() {
        val message = MessageDto(MatchResultH(MatchResult.ResultType.BOT_A_WIN))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-MatchResultH-${base64("BOT_A_WIN")}-0-", result)
    }

    @Test
    fun `test bot timeout serialization success`() {
        val message = MessageDto(BotTimeout)

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-BotTimeout-0-", result)
    }

    @Test
    fun `test error result serialization success`() {
        val message = MessageDto(ErrorResult(testMessage))

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ErrorResult-${base64(testMessage)}-0-", result)
    }

    @Test
    fun `test actor jar serialization success`() {
        val message = MessageDto(ActorJar(ActorBinType.API), testMessage)

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ActorJar-${base64("API")}-${testMessage.length}-$testMessage", result)
    }

    @Test
    fun `test shutdown notice serialization success`() {
        val message = MessageDto(ShutdownNotice)

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-ShutdownNotice-0-", result)
    }

    @Test
    fun `test start notice serialization success`() {
        val message = MessageDto(StartNotice)

        val result = serializer.serializeMessageDto(message)

        assertEquals("Message-StartNotice-0-", result)
    }
}