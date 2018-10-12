package hu.mktiti.tulkas.runtime.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UtilTest {

    private enum class TestEnum {
        VALUE_ONE, VALUE_TWO
    }

    @Test
    fun `test safeValueOf success`() {
        assertEquals(TestEnum.VALUE_ONE, safeValueOf<TestEnum>("VALUE_ONE"))
    }

    @Test
    fun `test safeValueOf invalid`() {
        assertEquals(null, safeValueOf<TestEnum>("VALUE_THREE"))
    }

    @Test
    fun `test safeValueOf empty`() {
        assertEquals(null, safeValueOf<TestEnum>(""))
    }

    @Test
    fun `test liftNulls empty`() {
        assertEquals(emptyList<Int>(), emptySet<Int>().liftNulls())
    }

    @Test
    fun `test liftNulls no nulls`() {
        assertEquals(listOf(1, 2, 3), listOf(1, 2, 3).liftNulls())
    }

    @Test
    fun `test liftNulls with nulls`() {
        assertEquals(null, listOf(1, 2, null, 3).liftNulls())
    }

    @Test
    fun `test first change`() {
        assertEquals(1 to "asd", ("one" to "asd").fst(1))
    }

    @Test
    fun `test first change mapper`() {
        assertEquals(1 to "asd", ("1" to "asd").fst(String::toInt))
    }

    @Test
    fun `test second change`() {
        assertEquals("asd" to 2, ("asd" to "two").snd(2))
    }

    @Test
    fun `test second change mapper`() {
        assertEquals("asd" to 2, ("asd" to "2").snd(String::toInt))
    }

}