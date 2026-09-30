#!/usr/bin/env kotlin
/**
 * Project Euler 297 — Zeckendorf Representation：暴力对照
 *
 * 与 solution.kt 的两条 O(L) 路径相互独立的第三条路径：**值递归**。
 *
 * 记 T(M) = Σ_{0 ≤ n ≤ M} z(n)，w_0 = 1, w_1 = 2, w_i = w_{i−1}+w_{i−2}。
 * 取 w_j ≤ M < w_{j+1}。区间 [0, M] 拆成两段：
 *   · n < w_j：与「长度 j 的合法 0/1 串」一一对应，总和记 table[j]（预计算）；
 *   · w_j ≤ n ≤ M：n = w_j + r，0 ≤ r ≤ M − w_j < w_{j−1}，故 Zeckendorf(n) = w_j + Zeckendorf(r)，
 *     于是 z(n) = 1 + z(r)，整段贡献 (M − w_j + 1) + T(M − w_j)。
 * 得到单支递归
 *     T(M) = table[j] + (M − w_j + 1) + T(M − w_j)，  深度 O(L)、无分支。
 *
 * 另含定义级贪心路径（对 X = 10^k 逐值分解求和）作为小规模对拍：
 * 两条路径在 X ≤ 10^6 上与题面给定值 7894453 一致，并给全尺寸同值。
 *
 * 运行：
 * OUTDIR=/tmp/kc-297-bt bash scripts/kotlinc-shim.sh content/problems/0297/brute-force.kt
 * java -cp /tmp/kc-297-bt:<kotlin-stdlib> Brute_forceKt
 */

private const val TARGET = 100_000_000_000_000_000L   // 10^17

private fun weights(n: Int): LongArray {
    val w = LongArray(n)
    w[0] = 1L
    if (n > 1) w[1] = 2L
    for (i in 2 until n) w[i] = w[i - 1] + w[i - 2]
    return w
}

/** 定义级贪心：z(n) = 每次取 ≤ n 的最大项，项数。 */
private fun zGreedy(n0: Long, w: LongArray): Int {
    var n = n0
    var cnt = 0
    var j = w.size - 1
    while (n > 0L) {
        while (w[j] > n) j--
        n -= w[j]
        cnt++
        j--
    }
    return cnt
}

/** 定义级逐值求和：Σ_{0 < n < X} z(n)。 */
private fun sumByGreedy(X: Long, w: LongArray): Long {
    var s = 0L
    var n = 1L
    while (n < X) {
        s += zGreedy(n, w)
        n++
    }
    return s
}

/**
 * 值递归求和：table[j] = Σ_{n < w_j} z(n)（长度 j 的合法串中 1 的总数）。
 * table[0] = 0, table[1] = 1, table[m] = table[m−1] + table[m−2] + w_{m−2}（计数项）。
 */
private fun buildTable(w: LongArray): LongArray {
    val n = w.size
    val table = LongArray(n)
    table[0] = 0L
    if (n > 1) table[1] = 1L
    for (m in 2 until n) table[m] = table[m - 1] + table[m - 2] + w[m - 2]
    return table
}

/** T(M) = Σ_{0 ≤ n ≤ M} z(n)，用上面的单支递归。 */
private fun sumByValueRecursion(M: Long, w: LongArray, table: LongArray): Long {
    var m = M
    var total = 0L
    while (m > 0L) {
        var j = w.size - 1
        while (w[j] > m) j--
        total += table[j] + (m - w[j] + 1)
        m -= w[j]
    }
    return total
}

fun main() {
    val w = weights(90)
    val table = buildTable(w)

    println("== 1. table 与贪心的一致性抽查 ==")
    for (j in 0..20) {
        val byGreedy = sumByGreedy(w[j], w)            // Σ_{n < w_j} z(n)
        check(byGreedy == table[j]) { "table[$j]=${table[j]} 贪心=$byGreedy" }
    }
    println("table[j] 与定义级贪心在 j ≤ 20 上全部一致 ✓")

    println()
    println("== 2. 小规模三路对拍（贪心 = 值递归） ==")
    for (k in 1..6) {
        val X = run {
            var r = 1L
            repeat(k) { r *= 10L }
            r
        }
        val g = sumByGreedy(X, w)
        val v = sumByValueRecursion(X - 1, w, table)
        check(g == v) { "X=10^$k：贪心 $g vs 值递归 $v" }
        println("n < 10^$k：$g")
    }
    val sample = sumByValueRecursion(999_999L, w, table)
    check(sample == 7894453L) { "题面样例不符：$sample" }
    println("题面给定 Σz(n), n < 10^6 = 7894453：值递归复算 $sample ✓")

    println()
    println("== 3. 全尺寸（值递归） ==")
    val ans = sumByValueRecursion(TARGET - 1, w, table)
    println("Σ z(n)（0 < n < 10^17） = $ans")

    println()
    println("== 4. 计时 ==")
    // 定义级贪心基线（bruteForceBaselineMs 口径）：X = 10^7
    var bestG = Double.MAX_VALUE
    repeat(3) { r ->
        val t0 = System.nanoTime()
        val v = sumByGreedy(10_000_000L, w)
        val ms = (System.nanoTime() - t0) / 1e6
        check(v == sumByValueRecursion(9_999_999L, w, table))
        println("  贪心 10^7 第 ${r + 1} 轮：${"%.1f".format(ms)} ms")
        if (ms < bestG) bestG = ms
    }
    println("brute-force 基线（贪心逐值 10^7）：${"%.1f".format(bestG)} ms")
    var bestV = Double.MAX_VALUE
    repeat(3) { r ->
        val t0 = System.nanoTime()
        val v = sumByValueRecursion(TARGET - 1, w, table)
        val ms = (System.nanoTime() - t0) / 1e6
        check(v == ans)
        println("  值递归全尺寸第 ${r + 1} 轮：${"%.4f".format(ms)} ms")
        if (ms < bestV) bestV = ms
    }
    println("值递归全尺寸：${"%.4f".format(bestV)} ms")

    println()
    println("== 结果 ==")
    println("Σ z(n)（0 < n < 10^17）= $ans（与 solution.kt 的两条 O(L) 路径一致）")
}
