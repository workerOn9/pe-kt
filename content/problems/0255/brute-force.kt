#!/usr/bin/env kotlin
/**
 * Project Euler 255 — Rounded Square Roots（四舍五入的平方根）：暴力对照
 *
 * 与 content/problems/0255/solution.kt 不共享任何代码：完全照题面定义逐 n 模拟——
 * 从 x_0 出发反复执行 x ← floor((x + ceil(n/x)) / 2)，直到 x 不再变化，数执行了几次，
 * 再对区间内所有 n 求和、求平均（精确编码到 10 位小数）。
 *
 * 规模：d 位数的区间有 9×10^(d-1) 个数，逐 n 模拟约 4×10^(d-1) 次迭代。
 *   d = 5:  约 3×10^5 次迭代      → 亚毫秒
 *   d = 6:  约 3×10^6             → 几毫秒
 *   d = 7:  约 3×10^7             → 约 26 ms
 *   d = 8:  约 3×10^8             → 约 270 ms（默认上限，meta.bruteForceBaselineMs 取此值）
 *   d = 9:  约 4×10^9             → 约 2.7 s（需显式传参数 9）
 *   d = 14: 约 4×10^14            → 外推约 3 天，不可行，由 solution.kt 的区间聚合方法完成
 *
 * 用法：java -cp <outdir>:<stdlib> Brute_forceKt [dMax]   （dMax ∈ 4..9，默认 8）
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

private fun pow10(e: Int): Long {
    var r = 1L
    repeat(e) { r *= 10 }
    return r
}

/** 题面初值：d 位奇数取 2×10^((d−1)/2)，偶数取 7×10^((d−2)/2)。 */
private fun x0For(d: Int): Long =
    if (d % 2 == 1) 2 * pow10((d - 1) / 2) else 7 * pow10((d - 2) / 2)

/** 按定义模拟：返回执行递推的次数（x_{k+1} = x_k 时停止）。 */
private fun iterationCount(n: Long, x0: Long): Int {
    var x = x0
    var steps = 0
    while (true) {
        val m = (n + x - 1) / x                 // ceil(n/x)
        val nx = (x + m) ushr 1                 // floor((x+m)/2)
        steps++
        if (nx == x) return steps
        x = nx
    }
}

/** 区间内所有 n 的迭代次数之和。 */
private fun sumIterations(lo: Long, hi: Long, x0: Long): Long {
    var total = 0L
    var n = lo
    while (n < hi) {
        total += iterationCount(n, x0)
        n++
    }
    return total
}

/** round(total / count × 10^10)（BigInteger 精确）。 */
private fun encodeAverage(total: Long, count: Long): BigInteger {
    var scale = BigInteger.ONE
    repeat(10) { scale = scale.multiply(BigInteger.TEN) }
    val den = BigInteger.valueOf(count)
    val (q, r) = BigInteger.valueOf(total).multiply(scale).divideAndRemainder(den)
    return if (r.multiply(BigInteger.TWO) >= den) q + BigInteger.ONE else q
}

private fun humanReadable(scaled: BigInteger): String {
    val s = scaled.toString().padStart(11, '0')
    return s.substring(0, s.length - 10) + "." + s.substring(s.length - 10)
}

fun main(args: Array<String>) {
    val dMax = if (args.isEmpty()) 8 else args[0].toInt()
    require(dMax in 4..9) { "dMax 取 4..9（默认 8），d = 14 全范围只能由区间聚合方法完成" }
    println("PE 255 暴力对照（按定义逐 n 模拟），d = 4..$dMax")

    // 题面样例先行校准：n = 4321 应为 2 次迭代、结果 66
    check(iterationCount(4321L, x0For(4)) == 2) { "4321 的迭代次数应为 2" }
    println("题面样例 n = 4321：${iterationCount(4321L, x0For(4))} 次迭代 ✓（实际 √4321 = 65.7343…）")

    for (d in 4..dMax) {
        val lo = pow10(d - 1)
        val hi = pow10(d)
        val x0 = x0For(d)
        val count = hi - lo
        sumIterations(lo, hi, x0)                       // 预热
        var best = Double.MAX_VALUE
        var total = 0L
        repeat(3) { round ->
            val t0 = System.nanoTime()
            total = sumIterations(lo, hi, x0)
            val ms = (System.nanoTime() - t0) / 1e6
            if (ms < best) best = ms
        }
        val encoded = encodeAverage(total, count)
        println(
            "d = $d：区间 [$lo, $hi) 共 $count 个数，总迭代 = $total，" +
                "平均 = ${humanReadable(encoded)}（round(avg×10^10) = $encoded），" +
                "${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）",
        )
    }

    // 题面 5 位数数值复核（题面给 3.2102888889）
    val t5 = sumIterations(pow10(4), pow10(5), x0For(5))
    val e5 = encodeAverage(t5, 9L * pow10(4))
    check(e5 == BigInteger.valueOf(32_102_888_889L)) { "5 位数平均与题面不符：$e5" }
    println("题面数值复核：5 位数平均 = ${humanReadable(e5)}（题面 3.2102888889）✓")
    println("暴力对照完成")
}
