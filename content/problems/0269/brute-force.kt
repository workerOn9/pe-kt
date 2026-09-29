#!/usr/bin/env kotlin
/**
 * Project Euler 269 — Polynomials with at Least One Integer Root
 * （至少有一个整数根的多项式）：直接暴力对照
 *
 * 完全按题面定义逐个整数求值：
 *   对 n = 1, 2, 3, … 取十进制数位（自低位起），对每个候选整数根 r ∈ [-9, 9]
 *   用 Horner 法求 P_n(r)（自高位起 v ← v·r + d_i），只要某个 r 使 v = 0 就计数。
 *   不做任何数位 DP、剪枝或有理根定理推导，与 solution.kt 的两条路径无共享逻辑。
 *
 * 复杂度 O(N · 19 · 位数)：完整规模 n ≤ 10^16 需要约 3×10^18 次乘加，不可行；
 * 本文件只跑到 10^5（题面锚点 14696）、10^6、10^7（本机计时基准），
 * meta.bruteForceBaselineMs 取 10^7 的实测值，并在 analysis.md 里写明外推口径。
 *
 * 输出：各规模的计数值与 JIT 预热后 3 轮最优毫秒数。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

import kotlin.math.abs

/** 暴力计数：n ∈ [1, 10^L] 中有整数根的个数。 */
private fun bruteZ(L: Int): Long {
    var limit = 1L
    repeat(L) { limit *= 10L }
    val digits = IntArray(L + 1)
    var count = 0L
    var n = 1L
    while (n <= limit) {
        var len = 0
        var x = n
        while (x > 0) {
            digits[len++] = (x % 10L).toInt()
            x /= 10L
        }
        var has = false
        var r = -9
        while (r <= 9 && !has) {
            var v = 0L
            var i = len - 1
            while (i >= 0) {
                v = v * r + digits[i]
                i--
            }
            if (v == 0L) has = true
            r++
        }
        if (has) count++
        n++
    }
    return count
}

/** 顺带统计「除 0 与 ±1 以外的根是否可以出现」，作为根范围引理的旁证。 */
private fun maxAbsRootSeen(L: Int): Int {
    var limit = 1L
    repeat(L) { limit *= 10L }
    var best = 0
    val digits = IntArray(L + 1)
    var n = 1L
    while (n <= limit) {
        var len = 0
        var x = n
        while (x > 0) {
            digits[len++] = (x % 10L).toInt()
            x /= 10L
        }
        for (r in -9..9) {
            if (abs(r) <= best) continue
            var v = 0L
            for (i in len - 1 downTo 0) v = v * r + digits[i]
            if (v == 0L) best = abs(r)
        }
        n++
    }
    return best
}

/** JIT 预热后 3 轮取最优。 */
private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    val anchor = bruteZ(5)
    check(anchor == 14_696L) { "Z(10^5) 应得 14696，实得 $anchor" }
    println("n ≤ 10^5：$anchor，与题面锚点 14696 一致")

    val z6 = bruteZ(6)
    check(z6 == 152_960L) { "Z(10^6) 应得 152960，实得 $z6" }
    println("n ≤ 10^6：$z6")
    println("n ≤ 10^6 中出现过的最大 |根| = ${maxAbsRootSeen(6)}（引理：|r| ≤ 9）")

    bruteZ(7) // 预热

    val ms5 = bestOf3("直接暴力 n≤10^5", 14_696L) { bruteZ(5) }
    val ms6 = bestOf3("直接暴力 n≤10^6", z6) { bruteZ(6) }
    val ms7 = bestOf3("直接暴力 n≤10^7", bruteZ(7)) { bruteZ(7) }

    println()
    println("n ≤ 10^5：$anchor，最优 ${"%.1f".format(ms5)} ms")
    println("n ≤ 10^6：$z6，最优 ${"%.1f".format(ms6)} ms")
    println("n ≤ 10^7：${bruteZ(7)}，最优 ${"%.1f".format(ms7)} ms")
    println()
    println("bruteForceBaselineMs 取 n≤10^7 的 ${"%.1f".format(ms7)} ms；" +
        "外推 n≤10^16 需约 ${"%.2e".format(ms7 * 1e9)} ms 量级（规模 10^9 倍且位数更长）")
}
