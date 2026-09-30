#!/usr/bin/env kotlin
/**
 * Project Euler 294 — Sum of Digits - Experience #23：暴力对照
 *
 * 两条与 solution.kt 分组法独立的路径：
 *
 *   C. 逐位 DP：每位一个转移，状态 (已用数位和, 加权和 mod 23)，O(n·24·23·10)。
 *      与分组法的机制完全不同（不分组、不做隔板容斥、不用 BigInteger 系数）。
 *   D. 直接枚举：k 以 23 为步长扫到 10^9，逐值求数位和判定。定义级暴力，
 *      给出 n = 8/9 的精确值（n = 9 即题面样例 263626）。
 *
 * 运行：
 * OUTDIR=/tmp/kc-294-bt bash scripts/kotlinc-shim.sh content/problems/0294/brute-force.kt
 * java -cp /tmp/kc-294-bt:<kotlin-stdlib> Brute_forceKt
 */

private fun digitSum(k: Long): Int {
    var n = k
    var s = 0
    while (n > 0L) {
        s += (n % 10L).toInt()
        n /= 10L
    }
    return s
}

/** 直接枚举 k < lim（步长 23），数位和为 23 则计数。 */
private fun enumerate(lim: Long): Long {
    var cnt = 0L
    var k = 23L
    while (k < lim) {
        if (digitSum(k) == 23) cnt++
        k += 23L
    }
    return cnt
}

/** 逐位 DP（带模，Long.MAX_VALUE/2 视为精确）。 */
private fun positionDp(n: Int, mod: Long): Long {
    var dp = Array(24) { LongArray(23) }
    dp[0][0] = 1L
    var w = 1L
    for (pos in 0 until n) {
        val ndp = Array(24) { LongArray(23) }
        for (s in 0..23) {
            for (m in 0 until 23) {
                val v = dp[s][m]
                if (v == 0L) continue
                for (d in 0..9) {
                    if (s + d > 23) break
                    val nm = ((m + d * w) % 23L).toInt()
                    ndp[s + d][nm] = (ndp[s + d][nm] + v) % mod
                }
            }
        }
        dp = ndp
        w = w * 10L % 23L
    }
    return dp[23][0]
}

fun main() {
    println("== C. 逐位 DP（n = 9/42/100/421/1234） ==")
    for (n in intArrayOf(9, 42, 100, 421, 1234)) {
        val t0 = System.nanoTime()
        val v = positionDp(n, 1_000_000_000L)
        val ms = (System.nanoTime() - t0) / 1e6
        println("n = $n：$v（${"%.2f".format(ms)} ms）")
    }
    val exact42 = positionDp(42, Long.MAX_VALUE / 2)
    check(exact42 == 6_377_168_878_570_056L) { "逐位 DP 精确 S(42) 不符：$exact42" }
    println("逐位 DP 精确 S(42) = $exact42（题面值 6377168878570056）✓")

    println()
    println("== D. 直接枚举（定义级，步长 23） ==")
    for (lim in longArrayOf(10_000_000L, 100_000_000L)) {
        val t0 = System.nanoTime()
        val v = enumerate(lim)
        val ms = (System.nanoTime() - t0) / 1e6
        println("k < $lim：$v 个（${"%.1f".format(ms)} ms）")
    }
    val e8 = enumerate(100_000_000L)
    check(e8 == positionDp(8, 1_000_000_000L)) { "n=8：枚举 $e8 vs 逐位 DP 不符" }
    println("n = 8：枚举 = 逐位 DP = $e8 ✓")

    println()
    println("== 基线：直接枚举到 10^9（对应题面 n = 9 的样例） ==")
    var best = Double.MAX_VALUE
    var v9 = 0L
    repeat(3) { r ->
        val t0 = System.nanoTime()
        v9 = enumerate(1_000_000_000L)
        val ms = (System.nanoTime() - t0) / 1e6
        println("  第 ${r + 1} 轮：$v9（${"%.1f".format(ms)} ms）")
        if (ms < best) best = ms
    }
    check(v9 == 263_626L) { "S(9) 枚举不符：$v9" }
    println("brute-force 基线：${"%.1f".format(best)} ms（k < 10^9，题面 S(9) = 263626）")

    println()
    println("== 结果 ==")
    println("枚举口径只能覆盖 n ≤ 9（k < 10^9）；全量 n = 11^12 需 k < 10^{11^12}，不可行。")
    println("S(9) = $v9（与题面一致），供 meta 的 bruteForceBaselineMs 口径使用。")
}
