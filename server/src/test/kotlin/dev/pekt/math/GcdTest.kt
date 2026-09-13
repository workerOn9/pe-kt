package dev.pekt.math

import kotlin.test.Test
import kotlin.test.assertEquals

class GcdTest {

    @Test
    fun `gcd known anchor`() {
        assertEquals(21L, gcd(1071, 462))
    }

    @Test
    fun `gcd coprime and non-coprime`() {
        assertEquals(1L, gcd(17, 31)) // 互质
        assertEquals(1L, gcd(8, 9))
        assertEquals(6L, gcd(12, 18)) // 非互质
        assertEquals(17L, gcd(17, 0))
        assertEquals(17L, gcd(0, 17))
        assertEquals(0L, gcd(0, 0))
        assertEquals(5L, gcd(5, 5))
    }

    @Test
    fun `gcd handles negatives and commutes`() {
        assertEquals(21L, gcd(-1071, 462))
        assertEquals(21L, gcd(1071, -462))
        assertEquals(21L, gcd(-1071, -462))
        assertEquals(gcd(1071, 462), gcd(462, 1071))
    }

    @Test
    fun `lcm known values`() {
        assertEquals(12L, lcm(4, 6))
        assertEquals(17L, lcm(17, 1))
        assertEquals(0L, lcm(0, 5))
        assertEquals(0L, lcm(5, 0))
        assertEquals(35L, lcm(-5, 7)) // 结果非负
    }

    @Test
    fun `lcm of coprime numbers is their product`() {
        assertEquals(17L * 31, lcm(17, 31))
    }

    @Test
    fun `lcm with large coprime pair does not overflow intermediate`() {
        // 若先乘后除，a*b = ~4e18 仍安全，但验证先除后乘路径与正确性
        val a = 2_000_000_000L
        val b = 1_999_999_999L // 与 a 互质
        assertEquals(a * b / gcd(a, b), lcm(a, b))
    }

    @Test
    fun `extendedGcd satisfies bezout identity`() {
        val (g, x, y) = extendedGcd(1071, 462)
        assertEquals(21L, g)
        assertEquals(21L, 1071 * x + 462 * y)
    }

    @Test
    fun `extendedGcd coprime gives gcd 1`() {
        val (g, x, y) = extendedGcd(17, 31)
        assertEquals(1L, g)
        assertEquals(1L, 17 * x + 31 * y)
    }

    @Test
    fun `extendedGcd edge cases`() {
        assertEquals(Triple(0L, 0L, 0L), extendedGcd(0, 0))
        val (g1, x1, y1) = extendedGcd(0, 5)
        assertEquals(5L, g1)
        assertEquals(5L, 0 * x1 + 5 * y1)
        val (g2, x2, y2) = extendedGcd(-12, 18)
        assertEquals(6L, g2)
        assertEquals(6L, -12 * x2 + 18 * y2)
    }
}
