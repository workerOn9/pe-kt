#!/usr/bin/env kotlin
/**
 * Project Euler 268 — At Least Four Distinct Prime Factors Less Than 100
 * （至少被 4 个小于 100 的相异素数整除）：直接暴力对照
 *
 * 完全按题面定义逐个整数分解：
 *   对 n = 1, 2, 3, … 把 25 个小于 100 的素数依次除尽，统计「整除 n 的相异素数个数」，
 *   ≥ 4 就计数。不做任何容斥、剪枝或解析推导，与 solution.kt 的枚举路径无任何共享逻辑。
 *
 * 复杂度 O(N · 25)：完整规模 10^16 需要约 2.5×10^17 次除法，不可行；
 * 本文件只跑到 10^3（题面锚点 23）、10^5、10^6、10^7（本机计时基准），
 * meta.bruteForceBaselineMs 取 10^7 的实测值，并在 analysis.md 里写明外推口径。
 *
 * 输出：各规模的计数值与 JIT 预热后 3 轮最优毫秒数。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private val PRIMES = intArrayOf(
    2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47,
    53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
)

/** 统计 n ∈ [1, limit−1] 中至少被 4 个上述素数整除的个数；顺带返回做过的除法次数。 */
private fun brute(limit: Long): Pair<Long, Long> {
    var count = 0L
    var divisions = 0L
    for (m in 1 until limit) {
        var x = m
        var cnt = 0
        for (p in PRIMES) {
            divisions++
            if (x % p == 0L) {
                cnt++
                while (x % p == 0L) {
                    x /= p
                    divisions++
                }
            }
        }
        if (cnt >= 4) count++
    }
    return count to divisions
}

/** JIT 预热后 3 轮取最优。 */
private fun bestOf3(tag: String, f: () -> Pair<Long, Long>): Pair<Double, Pair<Long, Long>> {
    var best = Double.MAX_VALUE
    var out: Pair<Long, Long>? = null
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val r = f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) {
            best = ms
            out = r
        }
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best to out!!
}

fun main() {
    val (anchor, div1) = brute(1_000L)
    check(anchor == 23L) { "n<1000 应得 23，实得 $anchor" }
    println("n < 1000：$anchor（除法 $div1 次），与题面锚点 23 一致")

    brute(100_000L) // 预热
    brute(1_000_000L)

    val (ms5, r5) = bestOf3("直接暴力 n<10^5") { brute(100_000L) }
    val (ms6, r6) = bestOf3("直接暴力 n<10^6") { brute(1_000_000L) }
    val (ms7, r7) = bestOf3("直接暴力 n<10^7") { brute(10_000_000L) }

    println()
    println("n < 10^5：${r5.first}（除法 ${r5.second} 次），最优 ${"%.1f".format(ms5)} ms")
    println("n < 10^6：${r6.first}（除法 ${r6.second} 次），最优 ${"%.1f".format(ms6)} ms")
    println("n < 10^7：${r7.first}（除法 ${r7.second} 次），最优 ${"%.1f".format(ms7)} ms")
    println()
    println("bruteForceBaselineMs 取 n<10^7 的 ${"%.1f".format(ms7)} ms；" +
        "外推 n<10^16 需约 ${"%.0f".format(ms7 * 1e9 / 1e3 / 1e3 / 1e3 * 1e3)} 秒量级（10^9 倍规模）")
}
