#!/usr/bin/env kotlin
/**
 * Project Euler 236 — 暴力对照：纯 DFS 判定
 *
 * 与 solution.kt 用同一候选集（同一套范围论证：m < 2.6149；v ≤ 9060 一段，
 * 41 | u 且 59 | v 时 v ≤ 70500 另一段），但**判定方法完全不同**：
 * 不做 gcd 归一化、不合并同系数项、不用扩展 gcd 收尾，只是把 Σ D_i·k_i = 0
 * 当朴素搜索，靠「剩余项可达区间」剪枝，逐项穷举 k_i。
 *
 * 实跑（本机）：count = 35（与题面自述的 35 一致）、最小 m = 1476/1475（与题面一致）、
 * 最大 m = 123/59；DFS 节点 1.28×10^11，耗时约 316 s。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar
 */

private val PRODS = arrayOf(
    longArrayOf(41, 5, 128),
    longArrayOf(41, 59, 32),
    longArrayOf(41, 59, 64),
    longArrayOf(90, 59, 64),
    longArrayOf(41, 59, 96),
)
private const val SUM_A = 18880L
private const val SUM_B = 15744L
private const val MMAX_NUM = 2614L
private const val MMAX_DEN = 1000L

private fun gcd(a: Long, b: Long): Long {
    var x = a; var y = b
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

private var nodes = 0L

/** 剩余项可达区间：Σ_{j>=i} d_j·k_j 的最小/最大值 */
private fun bounds(d: LongArray, cap: LongArray, i: Int): LongArray {
    var lo = 0L
    var hi = 0L
    for (j in i..4) {
        if (d[j] >= 0) { lo += d[j]; hi += d[j] * cap[j] }
        else { lo += d[j] * cap[j]; hi += d[j] }
    }
    return longArrayOf(lo, hi)
}

private fun dfs(d: LongArray, cap: LongArray, i: Int, acc: Long): Boolean {
    nodes++
    if (i == 5) return acc == 0L
    val b = bounds(d, cap, i)
    if (acc + b[0] > 0L || acc + b[1] < 0L) return false
    var kk = 1L
    while (kk <= cap[i]) {
        if (dfs(d, cap, i + 1, acc + d[i] * kk)) return true
        kk++
    }
    return false
}

private fun feasible(u: Long, v: Long): Boolean {
    val p = LongArray(5); val s = LongArray(5); val cap = LongArray(5)
    var sumP = 0L; var sumS = 0L
    for (i in 0..4) {
        val a1 = PRODS[i][0]; val b1 = PRODS[i][1]; val g = PRODS[i][2]
        val gg = gcd(v * a1, u * b1)
        p[i] = v * a1 / gg
        s[i] = u * b1 / gg
        cap[i] = minOf(g * a1 / p[i], g * b1 / s[i], SUM_A / p[i], SUM_B / s[i])
        if (cap[i] < 1L) return false
        sumP += p[i]
        if (sumP > SUM_A) return false
        sumS += s[i]
        if (sumS > SUM_B) return false
    }
    val d = LongArray(5) { 246L * v * p[it] - 295L * u * s[it] }
    var pos = false; var neg = false
    for (x in d) { if (x > 0L) pos = true; if (x < 0L) neg = true }
    if (!pos || !neg) return false
    // 系数绝对值大的项先枚举，剪枝更早生效
    val order = (0..4).sortedByDescending { Math.abs(d[it]) }
    val d2 = LongArray(5) { d[order[it]] }
    val cap2 = LongArray(5) { cap[order[it]] }
    return dfs(d2, cap2, 0, 0L)
}

fun main() {
    val t0 = System.currentTimeMillis()
    val hits = ArrayList<LongArray>()
    val seen = HashSet<Long>()

    fun scan(u: Long, v: Long) {
        if (gcd(u, v) != 1L) return
        if (u <= v || u * MMAX_DEN >= MMAX_NUM * v) return
        val key = u * 1_000_000L + v
        if (key in seen) return
        if (feasible(u, v)) {
            seen.add(key)
            hits.add(longArrayOf(u, v))
        }
    }

    for (v in 1L..9060L) {
        var u = v + 1
        val uMax = MMAX_NUM * v / MMAX_DEN
        while (u <= uMax) { scan(u, v); u++ }
    }
    println("第一段完成 (${System.currentTimeMillis() - t0} ms, hits=${hits.size}, nodes=$nodes)")
    var v = 59L
    while (v <= 70500L) {
        var u = 41L
        val uMax = MMAX_NUM * v / MMAX_DEN
        while (u <= uMax) {
            if (u > v) scan(u, v)
            u += 41
        }
        v += 59
    }

    hits.sortBy { it[0].toDouble() / it[1] }
    println("count = ${hits.size}   (题面自述 35)")
    println("smallest m = ${hits.first()[0]}/${hits.first()[1]}")
    val best = hits.last()
    println("largest m = ${best[0]}/${best[1]}")
    println("answer = " + (best[0] * 1_000_000 + best[1]))
    println("dfs nodes = $nodes, elapsed ${System.currentTimeMillis() - t0} ms")
}
