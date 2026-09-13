package dev.pekt.math

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CombinatoricsTest {

    @Test
    fun `binomial known anchor C(40,20)`() {
        assertEquals(BigInteger("137846528820"), binomial(40, 20))
    }

    @Test
    fun `binomial boundary values`() {
        assertEquals(BigInteger.ONE, binomial(0, 0))
        assertEquals(BigInteger.ONE, binomial(5, 0))
        assertEquals(BigInteger.ONE, binomial(5, 5))
        assertEquals(BigInteger.valueOf(5), binomial(5, 1))
        assertEquals(BigInteger.valueOf(5), binomial(5, 4))
    }

    @Test
    fun `binomial symmetry`() {
        assertEquals(binomial(40, 20), binomial(40, 20))
        assertEquals(binomial(30, 7), binomial(30, 23))
    }

    @Test
    fun `binomial Pascal identity on small range`() {
        for (n in 1..15) {
            for (k in 0..n) {
                val left = if (k == 0) BigInteger.ZERO else binomial(n - 1, k - 1)
                val right = if (k == n) BigInteger.ZERO else binomial(n - 1, k)
                assertEquals(left + right, binomial(n, k), "C($n,$k)")
            }
        }
    }

    @Test
    fun `binomial large value exceeds Long range`() {
        // C(100, 50) = 100891344545564193334812497256 > Long.MAX_VALUE
        assertEquals(BigInteger("100891344545564193334812497256"), binomial(100, 50))
    }

    @Test
    fun `binomial rejects invalid arguments`() {
        assertFailsWith<IllegalArgumentException> { binomial(-1, 0) }
        assertFailsWith<IllegalArgumentException> { binomial(5, -1) }
        assertFailsWith<IllegalArgumentException> { binomial(5, 6) }
    }
}
