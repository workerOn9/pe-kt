#!/usr/bin/env kotlin
/**
 * Project Euler 266 — Pseudo Square Root：直接暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 完全按「枚举子集」的定义做：
 *   · 题面锚点先对齐——PSR(12) = 3、PSR(3102) = 47 按定义从 ⌊√n⌋ 向下试除；
 *   · 对素数集合 {2,3,5,...} 的全部 2^k 个子集做 DFS（乘积单调递增，一旦超过 S = ⌊√p⌋ 就剪枝，
 *     只影响速度不影响结果），记录不超过 S 的最大子集乘积；
 *   · 在 5/6/10/15/20/22/25 个素数的规模上输出结果（与 solution.kt 的方法 A、方法 B 对照）；
 *   · 25 个素数（2^25 = 33 554 432 个子集）跑 JIT 预热后 3 轮计时，作为 meta.bruteForceBaselineMs
 *     的「缩小规模 + 外推」口径。
 *
 * 规模上限：42 个素数时 DFS 要访问 2^42 ≈ 4.4×10^12 个结点，按本机 2^25 的速率外推约需十几小时，
 * 不可行；折半枚举（solution.kt）才是可行路径。本文件只负责小规模正确性对照与耗时基准。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

import java.math.BigInteger

private fun primesBelow(limit: Int): IntArray {
    val composite = BooleanArray(limit)
    val out = ArrayList<Int>()
    for (i in 2 until limit) {
        if (!composite[i]) {
            out.add(i)
            var j = i.toLong() * i
            while (j < limit) {
                composite[j.toInt()] = true
                j += i
            }
        }
    }
    return out.toIntArray()
}

private fun isqrt(n: BigInteger): BigInteger {
    if (n.signum() == 0) return BigInteger.ZERO
    var x = BigInteger.ONE.shiftLeft((n.bitLength() + 1) / 2)
    while (true) {
        val y = x.add(n.divide(x)).shiftRight(1)
        if (y >= x) return x
        x = y
    }
}

private fun productOf(primes: IntArray): BigInteger {
    var p = BigInteger.ONE
    for (q in primes) p = p.multiply(BigInteger.valueOf(q.toLong()))
    return p
}

/** 题面锚点：按定义从 ⌊√n⌋ 向下试除找最大因数。 */
private fun psrByScan(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    while (n % r != 0L) r--
    return r
}

/** 暴力枚举结果：最优乘积、访问结点数（DFS 调用次数）、进入过的可行子集数。 */
private class Brute(val best: BigInteger, val nodes: Long)

/**
 * 对 primes 的全部 2^k 个子集做 DFS：结点携带当前子集乘积，遇到更优的就更新 best。
 * 乘积单调递增 ⇒ 某分支一旦 > S 就不用再往下（乘法只会更大），这是纯剪枝、不改结果。
 */
private fun bruteEnumeration(primes: IntArray, s: BigInteger): Brute {
    var best = BigInteger.ONE
    var nodes = 0L
    fun rec(i: Int, cur: BigInteger) {
        nodes++
        if (cur > best) best = cur
        if (i == primes.size) return
        val next = cur.multiply(BigInteger.valueOf(primes[i].toLong()))
        if (next <= s) rec(i + 1, next)   // 含第 i 个素数的分支
        rec(i + 1, cur)                   // 不含第 i 个素数的分支
    }
    rec(0, BigInteger.ONE)
    return Brute(best, nodes)
}

fun main() {
    // ---------- 题面锚点 ----------
    check(psrByScan(12) == 3L) { "PSR(12) 应为 3" }
    check(psrByScan(3102) == 47L) { "PSR(3102) 应为 47" }
    println("题面锚点：PSR(12) = 3，PSR(3102) = 47（按定义试除复现）")

    // ---------- 各规模暴力枚举，并与 solution.kt 的已知结果对照 ----------
    val primes = primesBelow(190)
    val expected = mapOf(
        5 to "42",
        6 to "165",
        10 to "79534",
        15 to "783152070",
        20 to "23619540863730",
        22 to "1793779293633437",
        // 25 个素数的值由本文件自己算出（solution.kt 侧不跑 2^25 规模）
        25 to null,
    )
    for ((k, exp) in expected.entries.sortedBy { it.key }) {
        val ps = primes.copyOfRange(0, k)
        val s = isqrt(productOf(ps))
        val r = bruteEnumeration(ps, s)
        if (exp != null) {
            check(r.best.toString() == exp) { "k=$k：暴力得 ${r.best}，与 solution.kt 的 $exp 不一致" }
        }
        println("k=$k（${ps.first()}…${ps.last()}）：PSR = ${r.best}，DFS 访问 ${r.nodes} 个结点" +
            if (exp != null) "（与 solution.kt 方法 A/B 一致）" else "")
    }

    // ---------- 耗时基准：25 个素数（2^25 = 33 554 432 个子集）----------
    val ps25 = primes.copyOfRange(0, 25)
    val s25 = isqrt(productOf(ps25))
    val base = bruteEnumeration(ps25, s25)
    bruteEnumeration(ps25, s25)
    var bestMs = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val r = bruteEnumeration(ps25, s25)
        val ms = (System.nanoTime() - t0) / 1e6
        check(r.best == base.best)
        if (ms < bestMs) bestMs = ms
        println("  暴力 k=25 第 ${round + 1} 轮：${"%.1f".format(ms)} ms（${r.nodes} 结点）")
    }
    println("暴力 k=25（2^25 子集）：${"%.1f".format(bestMs)} ms（3 轮最优，JIT 预热后）")
    println("外推：k=42 需 2^42 ≈ ${"%.1e".format(Math.pow(2.0, 42.0))} 个子集，约 ${"%.1f".format(bestMs * Math.pow(2.0, 17.0) / 1000.0 / 3600.0)} 小时，不可行")
    println("小规模暴力与折半枚举完全一致；check() 全部通过")
}
