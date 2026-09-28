#!/usr/bin/env kotlin
/**
 * Project Euler 251 — Cardano Triplets 暴力对照
 *
 * 思路：与 solution.kt 的参数化计数**完全不同**的两条朴素路线：
 *
 *   1) 整数判据暴力枚举：完全不碰 ∛ 与参数化，直接扫 (a, b)（a+b+1 ≤ N），
 *      由「27b²c = 27a²+(2a−1)³」解出 c = (27a²+(2a−1)³)/(27b²)，
 *      检查整除与 a+b+c ≤ N。复杂度 O(N²)，中间量在 N ≤ 2×10⁵ 时不越过 Long。
 *
 *   2) 高精度数值全枚举：把 (a,b,c) 按定义代入，用 50 位十进制精度的
 *      BigDecimal（√ 用内置 sqrt、∛ 用牛顿迭代）计算
 *      ∛(a+b√c) + ∛(a−b√c) 并与 1 比较。用来独立确认判据 1) 没有把方程变形错。
 *
 *   判据本身（见 solution.kt 头部）是三次根式方程的必要充分条件；
 *   两个方法只在小规模的交集里互相印证，任何一边写错都会在小范围内暴露。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar [N]（默认 20000，只用于计时）
 */

import java.math.BigDecimal
import java.math.MathContext

private val TWO = BigDecimal(2)
private val THREE = BigDecimal(3)
private val ONE = BigDecimal.ONE

/** BigDecimal 实立方根（牛顿迭代；定点小数下用「步长 < 容差」停机，避免末位振荡）。 */
private fun cbrt(v: BigDecimal, mc: MathContext): BigDecimal {
    if (v.signum() == 0) return BigDecimal.ZERO
    val neg = v.signum() < 0
    val a = if (neg) v.negate() else v
    var x = BigDecimal(Math.cbrt(a.toDouble()))
    if (x.signum() <= 0) x = ONE
    val tolerance = BigDecimal("1e-45")
    var iteration = 0
    while (iteration < 100) {
        val nx = x.multiply(TWO).add(a.divide(x.multiply(x, mc), mc)).divide(THREE, mc)
        val step = nx.subtract(x).abs()
        x = nx
        iteration++
        if (step.compareTo(tolerance) < 0) break
    }
    return if (neg) x.negate() else x
}

/** ∛(a+b√c) + ∛(a−b√c) 的高精度数值。 */
private fun cubeRootSum(a: Int, b: Int, c: Int, mc: MathContext): BigDecimal {
    val s = BigDecimal(c).sqrt(mc)
    val t = BigDecimal(b).multiply(s, mc)
    val x = cbrt(BigDecimal(a).add(t, mc), mc)
    val y = cbrt(BigDecimal(a).subtract(t, mc), mc)
    return x.add(y, mc)
}

/**
 * 整数判据暴力：O(N²) 扫 (a, b) 解出 c，检查整除与 a+b+c ≤ n。
 * 中间量上界（n ≤ 2×10⁵）：(2a−1)³ ≤ 8×10¹⁵、27a² ≤ 1.1×10¹²，均在 Long 内。
 */
private fun bruteForceCount(n: Long): Long {
    require(n <= 200_000L) { "暴力法只在小规模使用（n ≤ 2×10⁵）" }
    var count = 0L
    var a = 1L
    while (a + 2 <= n) {
        val m = 2 * a - 1
        val num = 27 * a * a + m * m * m
        var b = 1L
        while (a + b + 1 <= n) {
            val den = 27 * b * b
            if (num % den == 0L) {
                val c = num / den
                if (a + b + c <= n) count++
            }
            b++
        }
        a++
    }
    return count
}

/** 高精度数值全枚举：a+b+c ≤ bound，按定义判定 ∛(a+b√c)+∛(a−b√c) = 1。 */
private fun numericSweepCount(bound: Int): Int {
    val mc = MathContext(50)
    val eps = BigDecimal("1e-25")
    var count = 0
    for (a in 1..bound) for (b in 1..bound) for (c in 1..bound) {
        if (a + b + c > bound) continue
        val gap = cubeRootSum(a, b, c, mc).subtract(ONE).abs()
        if (gap.compareTo(eps) < 0) count++
    }
    return count
}

fun main(args: Array<String>) {
    val nMax = if (args.isNotEmpty()) args[0].toLong() else 20_000L

    // 1) 高精度数值全枚举（不依赖任何变形，直接代入原式）
    for (bound in intArrayOf(40, 60)) {
        println("高精度数值全枚举 a+b+c ≤ $bound：${numericSweepCount(bound)} 个三元组")
    }

    // 2) 整数判据暴力在各小上界下的计数（供与 solution.kt 逐项比对）
    println("整数判据暴力枚举（O(N²)）：")
    for (n in longArrayOf(200, 500, 1000, 2000, 5000, 10_000)) {
        println("  N = $n：${bruteForceCount(n)} 个")
    }
    check(bruteForceCount(1000) == 149L) { "题面样例（N=1000 → 149）不符" }
    println("  N = 1000 得 149，与题面样例一致")

    // 3) 计时：JIT 预热后 3 次取最优
    bruteForceCount(nMax)
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        bruteForceCount(nMax)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
    }
    println("brute-force：${"%.1f".format(best)} ms（N = $nMax，O(N²) 枚举，3 次最优）")
}
