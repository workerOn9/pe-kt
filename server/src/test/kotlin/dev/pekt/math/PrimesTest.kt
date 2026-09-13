package dev.pekt.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PrimesTest {

    @Test
    fun `sieve marks primes correctly up to 30`() {
        val s = sieve(30)
        assertEquals(31, s.size)
        assertFalse(s[0])
        assertFalse(s[1])
        val expectedPrimes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
        for (i in 0..30) assertEquals(i in expectedPrimes, s[i], "sieve[$i]")
    }

    @Test
    fun `sieve boundary values 0 and 1`() {
        assertEquals(1, sieve(0).size)
        assertFalse(sieve(1)[1])
        assertTrue(sieve(2)[2])
    }

    @Test
    fun `sieve rejects negative limit`() {
        assertFailsWith<IllegalArgumentException> { sieve(-1) }
    }

    @Test
    fun `primesUpTo returns primes below 10 and their sum is 17`() {
        assertEquals(listOf(2L, 3L, 5L, 7L), primesUpTo(10))
        assertEquals(17L, primesUpTo(10).sum())
    }

    @Test
    fun `primesUpTo empty below 2`() {
        assertEquals(emptyList(), primesUpTo(0))
        assertEquals(emptyList(), primesUpTo(1))
    }

    @Test
    fun `primesUpTo rejects negative limit`() {
        assertFailsWith<IllegalArgumentException> { primesUpTo(-5) }
    }

    @Test
    fun `isPrime typical and boundary values`() {
        assertFalse(isPrime(-7))
        assertFalse(isPrime(0))
        assertFalse(isPrime(1))
        assertTrue(isPrime(2))
        assertTrue(isPrime(3))
        assertFalse(isPrime(4))
        assertTrue(isPrime(97))
        assertFalse(isPrime(91)) // 7 * 13
        assertTrue(isPrime(104729)) // 第 10000 个素数
        assertFalse(isPrime(104730))
        // 大合数：两个大素数的积
        assertFalse(isPrime(1000003L * 1000033L))
        assertTrue(isPrime(1000003L))
    }

    @Test
    fun `nthPrime known values`() {
        assertEquals(2L, nthPrime(1))
        assertEquals(3L, nthPrime(2))
        assertEquals(5L, nthPrime(3))
        assertEquals(13L, nthPrime(6))
        assertEquals(104729L, nthPrime(10000))
    }

    @Test
    fun `nthPrime rejects n less than 1`() {
        assertFailsWith<IllegalArgumentException> { nthPrime(0) }
        assertFailsWith<IllegalArgumentException> { nthPrime(-3) }
    }
}
