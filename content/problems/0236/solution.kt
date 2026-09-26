#!/usr/bin/env kotlin
/**
 * Project Euler 236 — Luxury Hampers（豪华礼篮）
 *
 * 思路：
 *   记 A_i、B_i 为两家供应商第 i 种商品的供货量，a_i、b_i 为各自损耗掉的件数（均为正整数）。
 *   题设给出
 *       b_i/B_i = m · a_i/A_i（五种商品同时成立），  且  Σa_i/ΣA_i = m · Σb_i/ΣB_i.
 *   提出公共因子 A_i = g_i·a'_i、B_i = g_i·b'_i，并设 m = u/v（最简分数），每个商品的条件
 *   b_i·a'_i·v = a_i·b'_i·u 的通解为
 *       a_i = p_i·k_i,  b_i = s_i·k_i,
 *       p_i = v·a'_i / G_i,  s_i = u·b'_i / G_i,  G_i = gcd(v·a'_i, u·b'_i),  k_i ≥ 1.
 *   总量条件（ΣA = 18880 = 64·295、ΣB = 15744 = 64·246）化为一个线性方程
 *       Σ_i D_i·k_i = 0,   D_i = 246·v·p_i − 295·u·s_i,   1 ≤ k_i ≤ cap_i
 *   （cap_i 同时受 a_i ≤ A_i、b_i ≤ B_i、Σa_i ≤ ΣA、Σb_i ≤ ΣB 限制）。
 *
 *   搜索范围（保证穷尽）：
 *     · 1 < m < √(246·41/(295·5)) = 2.6149…：m 超过该值时全部 D_i ≤ 0 且不会同时为 0，无解。
 *     · Σp_i·k_i ≤ Σa_i ≤ ΣA、Σs_i·k_i ≤ ΣB 是两条廉价必要条件，刷掉绝大多数候选。
 *     · G_i | a'_i·b'_i，故对 (41,59) 的商品 G_i ∈ {1,41,59,2419}：
 *         41 ∤ u      ⟹ G_i ≤ 59 ⟹ p_i ≥ 41v/59 ⟹ v ≤ 9056；
 *         41 | u，59 ∤ v ⟹ G_i = 41 ⟹ p_i = v ⟹ v ≤ 6293（含于上）；
 *         41 | u，59 | v ⟹ v 可达 70500，单独扫 u = 41u'、v = 59v'。
 *       两段并集即全部候选（旧版只扫 u ≤ 5248 的 7.7×10⁶ 对，范围论证不完整）。
 *
 *   逐候选判定：D_i 全体除以公因数；把 D 相同的项合并为连续区间（k 之和可取 [项数, 上界之和]
 *   中的一切值）；按区间从小到大枚举，最后两项用扩展 gcd 精确解出。既不依赖朴素 DFS 在大 cap 上的
 *   组合爆炸，也不需要二维 DP 的 HashMap（旧版正是在这里 OOM）。
 *
 * 旁证：
 *   1. 去重后共 35 个 m，与题面自述「There are thirty-five m>1」一致；
 *   2. 最小的 m 恰为题面给出的 1476/1475 —— 两条自证同时复现，说明候选集与判定都完整。
 *   3. Python 参考实现（独立写的 Fraction/区间版本）给出同一集合与同一最大解。
 *
 * 答案：123/59（Long 编码 u×10⁶ + v = 123000059，约定见 analysis.md）
 * 复杂度：候选约 2.3×10⁷ 个，每个先做 5 次 gcd 与必要条件，通过者才做区间/egcd 判定，实测约 4 s。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private val PRODS = arrayOf(
    longArrayOf(41, 5, 128),      // Beluga Caviar       5248 = 128·41,  640 = 128·5
    longArrayOf(41, 59, 32),      // Christmas Cake      1312 =  32·41, 1888 =  32·59
    longArrayOf(41, 59, 64),      // Gammon Joint        2624 =  64·41, 3776 =  64·59
    longArrayOf(90, 59, 64),      // Vintage Port        5760 =  64·90, 3776 =  64·59
    longArrayOf(41, 59, 96),      // Champagne Truffles  3936 =  96·41, 5664 =  96·59
)
private const val SUM_A = 18880L
private const val SUM_B = 15744L
private const val MMAX_NUM = 2614L     // m < √(10086/1475) ≈ 2.61495，按 2614/1000 取整
private const val MMAX_DEN = 1000L

fun main() {
    val t0 = System.currentTimeMillis()
    val hits = ArrayList<LongArray>()      // (u, v)
    val seen = HashSet<Long>()

    // 第一段：v ≤ 9060（41 ∤ u 的全部情况 + 41 | u 且 59 ∤ v 的情况）
    for (v in 1L..9060L) {
        var u = v + 1
        val uMax = MMAX_NUM * v / MMAX_DEN
        while (u <= uMax) {
            scan(u, v, hits, seen)
            u++
        }
    }
    println("第一段（v ≤ 9060）完成：hits=${hits.size}  (${System.currentTimeMillis() - t0} ms)")

    // 第二段：41 | u 且 59 | v
    var v = 59L
    while (v <= 70500L) {
        var u = 41L
        val uMax = MMAX_NUM * v / MMAX_DEN
        while (u <= uMax) {
            if (u > v) scan(u, v, hits, seen)
            u += 41
        }
        v += 59
    }

    hits.sortBy { it[0].toDouble() / it[1] }
    println("distinct m count = ${hits.size}   (题面自述 35)")
    println("smallest m = ${hits.first()[0]}/${hits.first()[1]}   (题面给出 1476/1475)")
    println("全部 m：")
    for (h in hits) println("   %d/%d = %.10f".format(h[0], h[1], h[0].toDouble() / h[1]))
    val best = hits.last()
    println("largest  m = ${best[0]}/${best[1]}  = %.10f".format(best[0].toDouble() / best[1]))
    println("answer = " + (best[0] * 1_000_000 + best[1]))
    println("elapsed ${System.currentTimeMillis() - t0} ms")
}

private fun scan(u: Long, v: Long, hits: ArrayList<LongArray>, seen: HashSet<Long>) {
    if (gcd(u, v) != 1L) return
    if (u <= v || u * MMAX_DEN >= MMAX_NUM * v) return
    val a = analyse(u, v) ?: return
    val key = u * 1_000_000L + v
    if (key in seen) return
    if (feasible(a[0], a[1])) {
        seen.add(key)
        hits.add(longArrayOf(u, v))
    }
}

/** 返回 [D, cap]，不满足必要条件时返回 null */
private fun analyse(u: Long, v: Long): Array<LongArray>? {
    val d = LongArray(5)
    val cap = LongArray(5)
    var sumP = 0L
    var sumS = 0L
    for (i in 0..4) {
        val a1 = PRODS[i][0]
        val b1 = PRODS[i][1]
        val g = PRODS[i][2]
        val gg = gcd(v * a1, u * b1)
        val p = v * a1 / gg
        val s = u * b1 / gg
        val c = minOf(g * a1 / p, g * b1 / s, SUM_A / p, SUM_B / s)
        if (c < 1L) return null
        sumP += p
        if (sumP > SUM_A) return null
        sumS += s
        if (sumS > SUM_B) return null
        cap[i] = c
        d[i] = 246L * v * p - 295L * u * s
    }
    var pos = false
    var neg = false
    for (x in d) {
        if (x > 0L) pos = true
        if (x < 0L) neg = true
    }
    if (!pos || !neg) return null
    return arrayOf(d, cap)
}

/** 是否存在 k_i ∈ [1, cap_i] 使 Σ D_i·k_i = 0 */
private fun feasible(d0: LongArray, cap0: LongArray): Boolean {
    var g = 0L
    for (x in d0) g = gcd(g, Math.abs(x))
    val d = LongArray(5) { if (g > 1L) d0[it] / g else d0[it] }

    // 合并 D 相同的项：k 之和可遍历 [项数, 上界之和] 中的每个整数
    val ds = ArrayList<Long>()
    val los = ArrayList<Long>()
    val his = ArrayList<Long>()
    for (i in 0..4) {
        if (d[i] == 0L) continue
        var merged = false
        for (j in ds.indices) {
            if (ds[j] == d[i]) {
                los[j] += 1L
                his[j] += cap0[i]
                merged = true
                break
            }
        }
        if (!merged) {
            ds.add(d[i]); los.add(1L); his.add(cap0[i])
        }
    }
    val n = ds.size
    if (n == 0) return true
    if (n == 1) return false
    // 区间小的先枚举
    val order = (0 until n).sortedBy { his[it] - los[it] }
    val dd = LongArray(n) { ds[order[it]] }
    val ll = LongArray(n) { los[order[it]] }
    val hh = LongArray(n) { his[order[it]] }

    fun remMin(i: Int): Long {
        var acc = 0L
        for (j in i until n) acc += if (dd[j] > 0L) dd[j] * ll[j] else dd[j] * hh[j]
        return acc
    }

    fun remMax(i: Int): Long {
        var acc = 0L
        for (j in i until n) acc += if (dd[j] > 0L) dd[j] * hh[j] else dd[j] * ll[j]
        return acc
    }

    fun rec(i: Int, acc: Long): Boolean {
        if (i == n) return acc == 0L
        if (i == n - 2) return twoVar(dd[i], ll[i], hh[i], dd[i + 1], ll[i + 1], hh[i + 1], -acc)
        if (acc + remMin(i) > 0L || acc + remMax(i) < 0L) return false
        var k = ll[i]
        while (k <= hh[i]) {
            if (rec(i + 1, acc + dd[i] * k)) return true
            k++
        }
        return false
    }

    return if (n == 2) twoVar(dd[0], ll[0], hh[0], dd[1], ll[1], hh[1], 0L) else rec(0, 0L)
}

/** 是否存在 k1 ∈ [l1,h1]、k2 ∈ [l2,h2] 使 d1·k1 + d2·k2 = rem（扩展 gcd 精确判定） */
private fun twoVar(d1: Long, l1: Long, h1: Long, d2: Long, l2: Long, h2: Long, rem: Long): Boolean {
    if (d1 == 0L && d2 == 0L) return rem == 0L
    if (d1 == 0L) return if (rem % d2 == 0L) rem / d2 in l2..h2 else false
    if (d2 == 0L) return if (rem % d1 == 0L) rem / d1 in l1..h1 else false
    val g = gcd(Math.abs(d1), Math.abs(d2))
    if (rem % g != 0L) return false
    val a2 = d1 / g
    val b2 = d2 / g
    val r2 = rem / g
    val e = egcd(Math.abs(a2), Math.abs(b2))
    val x0 = if (a2 < 0L) -e[1] else e[1]
    val y0 = if (b2 < 0L) -e[2] else e[2]
    val t0 = x0 * r2                       // k1 = t0 + b2·t
    val t1 = y0 * r2                       // k2 = t1 − a2·t
    var lo = Long.MIN_VALUE / 4
    var hi = Long.MAX_VALUE / 4
    if (b2 > 0L) {
        lo = maxOf(lo, cdiv(l1 - t0, b2)); hi = minOf(hi, Math.floorDiv(h1 - t0, b2))
    } else if (b2 < 0L) {
        lo = maxOf(lo, cdiv(h1 - t0, b2)); hi = minOf(hi, Math.floorDiv(l1 - t0, b2))
    } else if (t0 < l1 || t0 > h1) return false
    if (a2 > 0L) {
        lo = maxOf(lo, cdiv(t1 - h2, a2)); hi = minOf(hi, Math.floorDiv(t1 - l2, a2))
    } else if (a2 < 0L) {
        lo = maxOf(lo, cdiv(t1 - l2, a2)); hi = minOf(hi, Math.floorDiv(t1 - h2, a2))
    } else if (t1 < l2 || t1 > h2) return false
    return lo <= hi
}

/** 返回 (d, x, y) 使 d = gcd(a,b) = a·x + b·y（a、b ≥ 0） */
private fun egcd(a: Long, b: Long): LongArray {
    if (b == 0L) return longArrayOf(a, 1L, 0L)
    val r = egcd(b, a % b)
    return longArrayOf(r[0], r[2], r[1] - (a / b) * r[2])
}

private fun cdiv(a: Long, b: Long): Long = -Math.floorDiv(-a, b)

private fun gcd(a: Long, b: Long): Long {
    var x = Math.abs(a)
    var y = Math.abs(b)
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}
