#!/usr/bin/env kotlin
/**
 * Project Euler 258 — 暴力对照（与 solution.kt 不共享核心代码路径）
 *
 * 三条按定义逐步推进的低速路径，互相独立：
 *   1. exactBig：BigInteger 精确迭代，全程不做任何取模，最后一步才 mod 20092010，
 *      独立核对「递推定义 + 取模时机」；跑 k = 10^4 / 10^5 / 10^6。
 *   2. modFull：完整数组 IntArray(k+1)，逐项 g[n] = g[n−2000] + g[n−1999]；跑 k = 2×10^6。
 *   3. modBuffer：长度 2000 的循环缓冲（写指针滚动，原地覆盖），冲到 k = 10^8 并计时，
 *      作为 bruteForceBaselineMs 的数据来源。
 * 三者在各自规模上一致，且 g_{10^8} mod 20092010 = 1410018 与 solution.kt 的快速路径相同。
 *
 * 注：g 的增长率 ≈ ρ^n，ρ ≈ 1.00035（特征根略大于 1），所以 k = 10^6 时精确值才约
 * 150 个十进制位，BigInteger 路线完全可行；k = 10^8 起精确值超过万位，才必须换取模。
 *
 * 运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

private const val DIM = 2000
private const val MOD = 20092010L
private const val MODI = 20092010
private const val BASELINE_K = 100_000_000

/** 精确迭代（无取模）：返回 g_k 的真值。 */
private fun exactBig(k: Int): BigInteger {
    val buf = Array(DIM) { BigInteger.ONE }
    for (n in DIM..k) {
        val i = n % DIM
        val j = if (i + 1 == DIM) 0 else i + 1
        buf[i] = buf[i] + buf[j]
    }
    return buf[k % DIM]
}

/** 完整数组按定义递推（k ≤ 数百万时可用）。 */
private fun modFull(k: Int): Int {
    val g = IntArray(k + 1) { 1 }
    for (n in DIM..k) {
        var v = g[n - DIM] + g[n - DIM + 1]
        if (v >= MODI) v -= MODI
        g[n] = v
    }
    return g[k]
}

/** 循环缓冲：buf[pos] 恒为 g_{n−2000}，buf[pos+1] 恒为 g_{n−1999}，写完原地前移。 */
private fun modBuffer(k: Int): Int {
    val buf = IntArray(DIM) { 1 }
    var pos = 0                                   // 下一个待写槽 = n mod 2000（n = 2000 起）
    for (n in DIM..k) {
        val nxt = if (pos + 1 == DIM) 0 else pos + 1
        var v = buf[pos] + buf[nxt]
        if (v >= MODI) v -= MODI
        buf[pos] = v
        pos = nxt
    }
    return buf[k % DIM]
}

fun main() {
    println("== PE 258 暴力对照 ==")

    // 0) 定义早期值（手算核对）
    println("循环缓冲早期值：g_2000 = ${modBuffer(2000)}，g_3999 = ${modBuffer(3999)}，g_4000 = ${modBuffer(4000)}")

    // 1) 精确 BigInteger 迭代（无取模）vs 循环缓冲（取模）：三条路径交叉
    for (k in intArrayOf(10_000, 100_000, 1_000_000)) {
        val exact = exactBig(k)
        val exactMod = exact.mod(BigInteger.valueOf(MOD)).toLong()
        val buffered = modBuffer(k).toLong()
        check(exactMod == buffered) { "k = $k：BigInteger 精确 $exactMod ≠ 循环缓冲 $buffered" }
        println("k = $k：BigInteger 精确迭代（末步取模）$exactMod = 循环缓冲 $buffered")
    }

    // 2) 完整数组 vs 循环缓冲
    for (k in intArrayOf(100_000, 2_000_000)) {
        val full = modFull(k)
        val buffered = modBuffer(k)
        check(full == buffered) { "k = $k：完整数组 $full ≠ 循环缓冲 $buffered" }
        println("k = $k：完整数组 = 循环缓冲 = $full")
    }

    // 3) 暴力基准：k = 10^8 的循环缓冲，JIT 预热后 3 轮取最优
    val expected = 1_410_018                         // 与 solution.kt 快速路径一致
    check(modBuffer(BASELINE_K) == expected) { "k = $BASELINE_K 基线值不符" }
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val v = modBuffer(BASELINE_K)
        val ms = (System.nanoTime() - t0) / 1e6
        check(v == expected)
        if (ms < best) best = ms
        println("  第 ${round + 1} 轮：${"%.1f".format(ms)} ms（g_${BASELINE_K} = $v）")
    }
    println("暴力基准 O(k) 循环缓冲 k = $BASELINE_K：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    println("check() 全部通过")
}
