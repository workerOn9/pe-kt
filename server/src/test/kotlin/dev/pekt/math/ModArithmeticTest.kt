package dev.pekt.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModArithmeticTest {

    @Test
    fun `modPow typical values`() {
        assertEquals(8L, modPow(2, 3, 100))
        assertEquals(1L, modPow(2, 10, 1023)) // 1024 mod 1023
        assertEquals(0L, modPow(7, 3, 7))
    }

    @Test
    fun `modPow boundary exp 0 and mod 1`() {
        assertEquals(1L, modPow(123, 0, 97))
        assertEquals(0L, modPow(123, 5, 1))
        assertEquals(1L, modPow(0, 0, 5)) // 约定 0^0 mod m = 1 mod m
    }

    @Test
    fun `modPow negative base is normalized`() {
        assertEquals(Math.floorMod(-1L, 11L), modPow(-1, 1, 11))
        assertEquals(1L, modPow(-1, 2, 11))
    }

    @Test
    fun `modPow large exponent matches BigInteger`() {
        val expected = java.math.BigInteger.valueOf(3)
            .modPow(java.math.BigInteger.valueOf(123456789), java.math.BigInteger.valueOf(1_000_000_007))
            .toLong()
        assertEquals(expected, modPow(3, 123456789, 1_000_000_007))
    }

    @Test
    fun `modPow rejects invalid arguments`() {
        assertFailsWith<IllegalArgumentException> { modPow(2, 3, 0) }
        assertFailsWith<IllegalArgumentException> { modPow(2, 3, -5) }
        assertFailsWith<IllegalArgumentException> { modPow(2, -1, 7) }
    }

    @Test
    fun `modInverse known anchor - inverse of 3 mod 11 is 4`() {
        assertEquals(4L, modInverse(3, 11))
    }

    @Test
    fun `modInverse verifies a times x congruent 1`() {
        val x = modInverse(17, 3120)
        assertEquals(1L, Math.floorMod(17 * x, 3120L))
        // 负的 a 也应工作
        val xNeg = modInverse(-3, 11)
        assertEquals(1L, Math.floorMod(-3 * xNeg, 11L))
    }

    @Test
    fun `modInverse throws when gcd is not 1`() {
        assertFailsWith<ArithmeticException> { modInverse(6, 10) } // gcd = 2
        assertFailsWith<ArithmeticException> { modInverse(0, 11) } // gcd = 11
        assertFailsWith<ArithmeticException> { modInverse(12, 8) }
    }

    @Test
    fun `modInverse rejects mod less than 2`() {
        assertFailsWith<IllegalArgumentException> { modInverse(1, 1) }
        assertFailsWith<IllegalArgumentException> { modInverse(1, 0) }
    }
}
