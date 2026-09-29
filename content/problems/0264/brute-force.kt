#!/usr/bin/env kotlin
/**
 * Project Euler 264 — Triangle Centres：直接暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 完全按题面定义枚举：
 *   · 外心在原点 ⇒ 三个顶点到原点等距；把全部格点按 x²+y² 分组（同一组即同一外接圆）；
 *   · 对同一组内的每个有序点对 (A,B)，令 C = (5,0) − A − B（这正是「垂心 = A+B+C」的直译），
 *     要求 C 也落在同一圆上、三点互异；
 *   · 用 50 位精度的 BigDecimal 计算周长并按「精确 ≤ pMax」过滤、累加。
 *
 * 规模：Pmax = 5×10³ 时半径上界 R = √((Pmax²+25)/9) ≈ 1666.7，圆内约 8.7×10⁶ 个格点，
 * 组内点对总数 ≈ 7×10⁷，可在秒级完成；完整规模 10⁵（R ≤ 33333）的格点约 3.5×10⁹，不可行——
 * 这就是「缩小规模 + 外推」口径的来源。
 *
 * 输出：
 *   · 题面锚点：Pmax = 50 恰有 9 个三角形、与题面列出的九个逐一相同、周长和 → 291.0089；
 *   · Pmax = 200 / 1000 的三角形个数与周长和（供与 solution.kt 对照）；
 *   · JIT 预热后 3 轮最优毫秒数（meta.bruteForceBaselineMs 的数据来源，口径为 Pmax = 5×10³）。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private const val SQRT_PRECISION = 50

private data class Pt(val x: Int, val y: Int) : Comparable<Pt> {
    override fun compareTo(other: Pt): Int =
        if (x != other.x) x.compareTo(other.x) else y.compareTo(other.y)
}

private data class Tri(val a: Pt, val b: Pt, val c: Pt)

private fun tri(u: Pt, v: Pt, w: Pt): Tri {
    val l = listOf(u, v, w).sorted()
    return Tri(l[0], l[1], l[2])
}

private fun exactPerimeter(t: Tri): BigDecimal {
    val mc = MathContext(SQRT_PRECISION)
    fun d2(p: Pt, q: Pt): Long {
        val dx = (p.x - q.x).toLong()
        val dy = (p.y - q.y).toLong()
        return dx * dx + dy * dy
    }
    return BigDecimal(d2(t.a, t.b)).sqrt(mc)
        .add(BigDecimal(d2(t.b, t.c)).sqrt(mc))
        .add(BigDecimal(d2(t.c, t.a)).sqrt(mc))
}

private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

/** 直接枚举：返回周长精确 ≤ pMax 的全部三角形及其精确周长和。 */
private fun bruteDirect(pMax: Long): Pair<Set<Tri>, BigDecimal> {
    val r2 = (pMax * pMax + 25) / 9                 // R² 上界
    val rlim = isqrt(r2).toInt() + 2
    val off = 4000
    // 1) 每个范数的点数
    val counts = IntArray(r2.toInt() + 1)
    for (x in -rlim..rlim) {
        val x2 = x * x
        for (y in -rlim..rlim) {
            val nn = x2 + y * y
            if (nn.toLong() <= r2) counts[nn]++
        }
    }
    // 2) 前缀和定界
    val start = IntArray(counts.size + 1)
    for (i in counts.indices) start[i + 1] = start[i] + counts[i]
    val cursor = start.copyOf()
    val pts = IntArray(start[counts.size])
    for (x in -rlim..rlim) {
        val x2 = x * x
        for (y in -rlim..rlim) {
            val nn = x2 + y * y
            if (nn.toLong() <= r2) {
                pts[cursor[nn]++] = ((x + off) shl 14) or (y + off)
            }
        }
    }
    // 3) 逐个范数（圆）枚举点对
    val found = HashSet<Tri>()
    val limit = BigDecimal(pMax)
    val mc = MathContext(SQRT_PRECISION)
    var total = BigDecimal.ZERO
    for (n in counts.indices) {
        val lo = start[n]
        val hi = start[n + 1]
        if (hi - lo < 2) continue
        for (ia in lo until hi) {
            val ax = (pts[ia] shr 14) - off
            val ay = (pts[ia] and 0x3FFF) - off
            for (ib in lo until hi) {
                if (ia == ib) continue
                val bx = (pts[ib] shr 14) - off
                val by = (pts[ib] and 0x3FFF) - off
                val cx = 5 - ax - bx
                val cy = -ay - by
                if (cx * cx + cy * cy != n) continue      // C 必须在同一圆上
                val t = tri(Pt(ax, ay), Pt(bx, by), Pt(cx, cy))
                if (t.a == t.b || t.b == t.c || t.a == t.c) continue
                if (found.contains(t)) continue
                val p = exactPerimeter(t)
                if (p.compareTo(limit) <= 0) {
                    found.add(t)
                    total = total.add(p, mc)
                }
            }
        }
    }
    return found to total
}

/** 题面列出的九个周长 ≤ 50 的三角形。 */
private val ANCHOR: Set<Tri> = setOf(
    tri(Pt(-4, 3), Pt(5, 0), Pt(4, -3)),
    tri(Pt(4, 3), Pt(5, 0), Pt(-4, -3)),
    tri(Pt(-3, 4), Pt(5, 0), Pt(3, -4)),
    tri(Pt(3, 4), Pt(5, 0), Pt(-3, -4)),
    tri(Pt(0, 5), Pt(5, 0), Pt(0, -5)),
    tri(Pt(1, 8), Pt(8, -1), Pt(-4, -7)),
    tri(Pt(8, 1), Pt(1, -8), Pt(-4, 7)),
    tri(Pt(2, 9), Pt(9, -2), Pt(-6, -7)),
    tri(Pt(9, 2), Pt(2, -9), Pt(-6, 7)),
)

private fun checkDefinition(t: Tri) {
    fun n2(p: Pt) = p.x.toLong() * p.x + p.y.toLong() * p.y
    check(n2(t.a) == n2(t.b) && n2(t.b) == n2(t.c)) { "外心不在原点：$t" }
    check(t.a.x + t.b.x + t.c.x == 5 && t.a.y + t.b.y + t.c.y == 0) { "垂心不是 (5,0)：$t" }
}

private fun bestOf3(tag: String, f: () -> Unit): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 题面锚点 ----------
    val (a50, s50) = bruteDirect(50)
    check(a50 == ANCHOR) { "周长 ≤ 50 的集合与题面不符（实得 ${a50.size} 个）" }
    a50.forEach { checkDefinition(it) }
    val scaled50 = s50.scaleByPowerOfTen(4).setScale(0, RoundingMode.HALF_UP).toLong()
    check(scaled50 == 2_910_089L) { "锚点周长和应为 291.0089，实得 $s50" }
    println("题面锚点：周长 ≤ 50 恰 9 个三角形，与题面所列逐一相同，周长和 = $s50 → 291.0089")

    // ---------- 中间规模（供 solution.kt 对照） ----------
    for (pm in longArrayOf(200L, 1000L)) {
        val (set, sum) = bruteDirect(pm)
        set.forEach { checkDefinition(it) }
        println("Pmax=$pm：${set.size} 个三角形，周长和 = $sum")
    }

    // ---------- 计时 ----------
    bruteDirect(1000)
    val ms2k = bestOf3("暴力 Pmax=2×10³") { bruteDirect(2000) }
    val ms5k = bestOf3("暴力 Pmax=5×10³") { bruteDirect(5000) }
    println()
    println("bruteForceBaselineMs 取 Pmax=5×10³ 的 ${"%.1f".format(ms5k)} ms" +
        "（口径：缩小规模；完整规模 10⁵ 的格点数约 ×(33333/1666.7)² ≈ 400 倍，不可行）")
}
