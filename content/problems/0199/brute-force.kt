package dev.pekt.problems

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * PE 199 暴力解：与优化解同一填充过程，但全程用 BigDecimal 50 位精度，
 * 显式存储全部圆的曲率、两遍处理（先填圆、再累加面积），
 * 用于复核 double 精度与最终 8 位小数的舍入是否可靠。
 */
private class BGap(val a: BigDecimal, val b: BigDecimal, val c: BigDecimal, val across: BigDecimal)

fun bruteForce199(): BigDecimal {
    val mc = MathContext(50)
    val one = BigDecimal.ONE
    val sqrt3 = BigDecimal(3).sqrt(mc)
    val kIn = one.divide(sqrt3.multiply(BigDecimal(2)).subtract(BigDecimal(3)), mc)
    val kOut = BigDecimal.ONE.negate()

    val bends = ArrayList<BigDecimal>(1 shl 17)
    var gaps = ArrayList<BGap>(64)
    repeat(3) { gaps.add(BGap(kOut, kIn, kIn, kIn)) }
    gaps.add(BGap(kIn, kIn, kIn, kOut))
    repeat(3) { bends.add(kIn) }

    for (iter in 0 until 10) {
        val next = ArrayList<BGap>(gaps.size * 3)
        for (g in gaps) {
            val s = g.a.add(g.b).add(g.c)
            val disc = g.a.multiply(g.b).add(g.b.multiply(g.c)).add(g.c.multiply(g.a))
            val sq = disc.sqrt(mc)
            val kp = s.add(sq.multiply(BigDecimal(2)))
            val km = s.subtract(sq.multiply(BigDecimal(2)))
            val n = if (kp.subtract(g.across).abs() > km.subtract(g.across).abs()) kp else km
            bends.add(n)
            next.add(BGap(g.a, g.b, n, g.c))
            next.add(BGap(g.b, g.c, n, g.a))
            next.add(BGap(g.c, g.a, n, g.b))
        }
        gaps = next
    }
    var area = BigDecimal.ZERO
    for (k in bends) {
        area = area.add(one.divide(k.multiply(k), mc))
    }
    val frac = one.subtract(area)
    return frac.setScale(8, RoundingMode.HALF_UP)
}

fun main() {
    val t = System.nanoTime()
    val frac = bruteForce199()
    val ms = (System.nanoTime() - t) / 1e6
    println("brute uncovered=$frac bestMs=${"%.1f".format(ms)}")
}
