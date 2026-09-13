package dev.pekt.math

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BigIntegerExtTest {

    @Test
    fun `digitSum of small values`() {
        assertEquals(0, BigInteger.ZERO.digitSum())
        assertEquals(5, BigInteger.valueOf(5).digitSum())
        assertEquals(10, BigInteger("1234").digitSum())
        assertEquals(6, BigInteger("-123").digitSum()) // 负号不计
    }

    @Test
    fun `digitSum of 2^1000 is 1366 - PE 16 anchor`() {
        assertEquals(1366, BigInteger.TWO.pow(1000).digitSum())
    }

    @Test
    fun `factorial boundary values`() {
        assertEquals(BigInteger.ONE, factorial(0))
        assertEquals(BigInteger.ONE, factorial(1))
        assertEquals(BigInteger.TWO, factorial(2))
        assertEquals(BigInteger.valueOf(120), factorial(5))
    }

    @Test
    fun `factorial of 20 matches Long range boundary`() {
        assertEquals(BigInteger.valueOf(2432902008176640000L), factorial(20))
    }

    @Test
    fun `factorial of 21 exceeds Long`() {
        assertEquals(BigInteger("51090942171709440000"), factorial(21))
    }

    @Test
    fun `digitSum of 100! is 648 - PE 20 anchor`() {
        assertEquals(648, factorial(100).digitSum())
    }

    @Test
    fun `factorial rejects negative n`() {
        assertFailsWith<IllegalArgumentException> { factorial(-1) }
    }
}
