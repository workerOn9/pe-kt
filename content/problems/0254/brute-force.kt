#!/usr/bin/env kotlin
/**
 * Project Euler 254 — 数位阶乘和：小规模按定义暴力对照
 *
 * 完全不共享 solution.kt 的窗口/混合进制逻辑：对 n = 1..N 逐个做最朴素的数位分解，
 *   f(n) = Σ (数位)!
 *   sf(n) = f(n) 的数位和
 * 记录每个 sf 值在 n ≤ N 内首次出现的 n。对满足 g(i) ≤ N 的 i，该值就等于 g(i)。
 *
 * N = 10^7 覆盖 i = 1..44（这 44 个 g(i) 全部 ≤ 10^7；solution.kt 会逐 i 对照）。
 * 输出：g(i)、sg(i) 表（i ≤ 60 部分），Σsg(1..20)（题面样例 = 156），
 * 覆盖统计与校验和，以及 JIT 预热后 3 轮计时最优值（meta.bruteForceBaselineMs 的来源）。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private const val N = 10_000_000L
private val FACT = longArrayOf(1, 1, 2, 6, 24, 120, 720, 5_040, 40_320, 362_880)

private fun runBrute(): LongArray {
    val best = LongArray(200)
    var n = 1L
    while (n <= N) {
        var x = n
        var f = 0L
        while (x > 0) { f += FACT[(x % 10).toInt()]; x /= 10 }
        var y = f
        var s = 0
        while (y > 0) { s += (y % 10).toInt(); y /= 10 }
        if (best[s] == 0L) best[s] = n
        n++
    }
    return best
}

private fun digitSumOf(n: Long): Long {
    var y = n
    var s = 0L
    while (y > 0) { s += y % 10; y /= 10 }
    return s
}

fun main() {
    runBrute()                                     // JIT 预热
    var bestMs = Double.MAX_VALUE
    var best = LongArray(0)
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val r = runBrute()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestMs) { bestMs = ms; best = r }
        println("  第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }

    var covered = 0
    var checksum = 0L
    var sum20 = 0L
    println("i\tg(i)\tsg(i)")
    for (i in 1 until best.size) {
        val n = best[i]
        if (n == 0L) continue
        val sg = digitSumOf(n)
        covered++
        checksum += n
        if (i <= 20) sum20 += sg
        if (i <= 60) println("$i\t$n\t$sg")
    }
    println("暴力 n ≤ $N：覆盖 $covered 个不同 sf 值（g(i) ≤ $N 的 i 由 solution.kt 逐项对照，共 44 个）")
    println("暴力 Σsg(1..20) = $sum20（题面样例应为 156）；Σ g(i)（全部覆盖项）= $checksum")
    println("brute-force 基准：${"%.1f".format(bestMs)} ms（3 轮最优，JIT 预热后）")
}
