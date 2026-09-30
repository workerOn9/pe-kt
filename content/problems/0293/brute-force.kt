#!/usr/bin/env kotlin
/**
 * Project Euler 293 — Pseudo-Fortunate Numbers：暴力对照
 *
 * 两条与 solution.kt 的 DFS 前缀生成相互独立的路径：
 *
 *   B1. 定义级逐值扫描：N 从 2 到 LIM 步长 2，用试除法分解出不同素因子，
 *       直接按题面定义判定「不同素因子恰为从 2 开始的连续素数前缀」。
 *       对每个 admissible 的 N 用试除法找 M。只跑到 LIM（小规模对拍）。
 *
 *   B2. 23-光滑超集 + 前缀过滤：枚举所有 2^a·3^b·5^c·…·23^k < 10^9（23 个素数前缀的
 *       幂积），再按定义过滤出 admissible 数，最后用试除法求 M。这条路径不依赖
 *       solution.kt 的「引入下一个素因子」DFS 结构，是结构完全不同的全量实现。
 *
 * 运行：
 * OUTDIR=/tmp/kc-293-bt bash scripts/kotlinc-shim.sh content/problems/0293/brute-force.kt
 * java -cp /tmp/kc-293-bt:<kotlin-stdlib> Brute_forceKt
 */

private const val LIMIT = 1_000_000_000L

private fun isPrimeTrial(n: Long): Boolean {
    if (n < 2L) return false
    if (n % 2L == 0L) return n == 2L
    var d = 3L
    while (d <= n / d) {
        if (n % d == 0L) return false
        d += 2L
    }
    return true
}

/** 试除法求不同素因子（升序）。 */
private fun distinctPrimeFactors(n0: Long): List<Long> {
    var n = n0
    val out = ArrayList<Long>()
    var d = 2L
    while (d <= n / d) {
        if (n % d == 0L) {
            out.add(d)
            while (n % d == 0L) n /= d
        }
        d = if (d == 2L) 3L else d + 2L
    }
    if (n > 1L) out.add(n)
    return out
}

private val CONSECUTIVE = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23)

/** 题面定义：2 的幂，或不同素因子恰为从 2 开始的连续素数前缀。 */
private fun isAdmissibleByDefinition(n: Long): Boolean {
    if (n % 2L != 0L) return false
    val f = distinctPrimeFactors(n)
    for (i in f.indices) if (f[i] != CONSECUTIVE[i]) return false
    return true
}

/** 试除法版 M：N 偶 ⇒ N+2 偶合数 ⇒ 从 3 起逐个奇数试。 */
private fun minMByTrial(n: Long): Int {
    var m = 3
    while (!isPrimeTrial(n + m)) m += 2
    return m
}

/** B1：定义级逐值扫描 [2, lim)，返回 (admissible 数个数, 不同 M 之和)。 */
private fun scanByDefinition(lim: Long): Pair<Int, Long> {
    var cnt = 0
    val ms = sortedSetOf<Int>()
    var n = 2L
    while (n < lim) {
        if (isAdmissibleByDefinition(n)) {
            cnt++
            ms.add(minMByTrial(n))
        }
        n += 2L
    }
    return cnt to ms.sumOf { it.toLong() }
}

/** B2：23-光滑（仅 2 与奇素数前缀的乘积）枚举 + 定义过滤 + 试除求 M。 */
private fun smoothSuperset(lim: Long): Pair<Int, Long> {
    val ms = sortedSetOf<Int>()
    var cnt = 0
    // 枚举 2^a × 3^b × 5^c × … × 23^k（每个奇素数指数 ≥ 0），只保留 ≥ 2 的偶数
    fun rec(idx: Int, cur: Long) {
        if (idx == CONSECUTIVE.size) {
            if (cur >= 2L && isAdmissibleByDefinition(cur)) {
                cnt++
                ms.add(minMByTrial(cur))
            }
            return
        }
        val p = CONSECUTIVE[idx]
        var v = cur
        while (v < lim) {
            rec(idx + 1, v)
            if (v > (lim - 1) / p) break
            v *= p
        }
    }
    // idx = 0 是 2：单独处理，保证每支至少乘一次 2 前的奇数部分是前缀结构
    var two = 1L
    while (two < lim) {
        rec(1, two)
        if (two > (lim - 1) / 2) break
        two *= 2L
    }
    return cnt to ms.sumOf { it.toLong() }
}

fun main() {
    println("== B1. 定义级逐值扫描（试除分解 + 试除求 M） ==")
    for (lim in longArrayOf(100_000L, 1_000_000L, 4_000_000L)) {
        val t0 = System.nanoTime()
        val (cnt, sum) = scanByDefinition(lim)
        val ms = (System.nanoTime() - t0) / 1e6
        println("N < $lim：admissible $cnt 个，不同 M 之和 = $sum（${"%.1f".format(ms)} ms）")
    }
    val (c1e5, s1e5) = scanByDefinition(100_000L)
    val (c1e6, s1e6) = scanByDefinition(1_000_000L)
    check(c1e5 == 350 && s1e5 == 231L) { "N < 10^5 与 solution.kt 表不符：($c1e5, $s1e5)" }
    check(c1e6 == 784 && s1e6 == 599L) { "N < 10^6 与 solution.kt 表不符：($c1e6, $s1e6)" }
    println("N < 10^5 与 N < 10^6 的 (个数, 和) 与 solution.kt 的小规模表一致 ✓")

    println()
    println("== B2. 23-光滑超集 + 定义过滤（全量 N < 10^9，基线口径） ==")
    var best = Double.MAX_VALUE
    var res: Pair<Int, Long> = 0 to 0L
    repeat(3) { r ->
        val t0 = System.nanoTime()
        res = smoothSuperset(LIMIT)
        val ms = (System.nanoTime() - t0) / 1e6
        println("  第 ${r + 1} 轮：admissible ${res.first} 个，不同 M 之和 = ${res.second}，${"%.1f".format(ms)} ms")
        if (ms < best) best = ms
    }
    println("brute-force 基线：${"%.1f".format(best)} ms")
    check(res.first == 6656) { "admissible 个数应为 6656，实得 ${res.first}" }

    println()
    println("== 结果 ==")
    println("N < 10^9 的全部 admissible 数对应的不同 M 之和 = ${res.second}（与 solution.kt 的 2209 一致）")
}
