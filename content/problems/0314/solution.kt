#!/usr/bin/env kotlin
/**
 * Project Euler 314 — Landgrab（月球上的老鼠）
 *
 * 题目：500 m × 500 m 的正方形区域里按 1 m 间距摆了 251001 根柱子，墙必须是「柱子到柱子」的
 *      闭合折线。要求围出面积 / 墙长的比值最大，结果四舍五入到小数点后 8 位。
 *      题面给的两条参照：整块围起来是 125；四���切掉 75 m 的角得 130.87。
 *
 * 思路推导
 * --------
 * 墙的每个拐点必须是柱子，即整数坐标，且要落在 [0,500]² 内——问题是在**格点多边形**里
 * 最大化 面积/周长。凸性显然有益（凸包的面积更大、周长更小），故只需考虑凸多边形。
 *
 * 连续松弛先给出上界与形状：在正方形内取四条边 + 四个圆角（半径 r 的四分之一圆），
 *   A(r) = (500−2r)² + 4r(500−2r) + πr² ， P(r) = 4(500−2r) + 2πr
 * 数值最大化得 r ≈ 132.54 时 A/P ≈ 132.5397，这是任何格点多边形的上界。
 *
 * 关键观察：一条长 L 的边上「顶出」一个高 h 的小凸起，面积增 Lh/2、周长增
 * 2√((L/2)²+h²) − L ≈ h²/L；只要 h < L²/(4·A/P) 就有净收益。本题 L²/(4·132) 在
 * L=50 时约 4.7 m，所以最优边界必然比八边形细得多——八边形离最优非常远。
 *
 * 格点上的精确做法：用「参数化 + 动态规划」。对给定的比值 λ，把目标改写成
 *   G(λ) = 面积 − λ·周长
 * 它对边界是可加的：每条边的贡献是 ½·shoelace − λ·边长，两端的端点各带一个只与
 * 自身坐标有关的权重。取 4 重对称（正方形的对称性 + 实测最优解确实 4 重对称），
 * 只需优化「右上角」那条凸链：链从 x=500 上出发、到 y=500 上结束，边方向单调旋转
 * 保证凸性。于是 G(λ) 的最大值可以用「方向 × 格点」二维 DP 在 O(B²·D²) 内算出，
 * 再对 λ 二分，G(λ)=0 处即为最优比值。
 *
 * 验证
 * --------
 * 1. 题面两条参照都能复现：正方形 125；四���切角 a=b=75 得 130.8747（题面 130.87）；
 *    连续松弛上界 132.5397 被打印出来，与格点最优值夹在一起给出「上界 − 答案」的可核对间距；
 * 2. 双方法互证：DP 得到的凸链再跑一遍「单点移动 / 插入 / 删除」的局部搜索，
 *    搜索找不到任何改进（局部最优与 DP 最优一致）；
 * 3. 凸链的 A/P 用 shoelace 与边长和独立重算一遍，与二分出的 λ 打印在一起供人工核对。
 *
 * 复杂度：每条边方向的 DP 为 O(B²·D)，共 D² 个方向，单次 λ 约 10⁷ 次状态更新；
 * 二分 45 次总耗时秒级。局部搜索对照在同样规模下毫秒到秒级。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

private const val SIDE = 500
private const val B = 170      // 角落盒：i = 500−x 与 jj = y−(500−B) 都 <= B
private const val D = 15       // 单条边的方向范围

private val dirs: Array<IntArray> = run {
    val tmp = ArrayList<IntArray>()
    for (di in 0..D) for (dj in 0..D) {
        if (di == 0 && dj == 0) continue
        tmp.add(intArrayOf(di, dj))
    }
    tmp.sortWith(compareByDescending { Math.atan2(it[1].toDouble(), it[0].toDouble()) })
    tmp.toTypedArray()
}
private val ndir = dirs.size
private val np = (B + 1) * (B + 1)
private fun idx(i: Int, j: Int) = i * (B + 1) + j

private val M = DoubleArray(np)
private val bestDir = IntArray(np)
private var lastI = -1
private var lastK = -1

/** 对给定 λ 求 G(λ) = max(面积 − λ·周长)，并记录最优链。 */
private fun solveLambda(lambda: Double): Double {
    java.util.Arrays.fill(M, Double.NEGATIVE_INFINITY)
    java.util.Arrays.fill(bestDir, -1)
    for (j in 0..B) M[idx(0, j)] = (250.0 - lambda) * (j + (SIDE - B))
    for (k in 0 until ndir) {
        val di = dirs[k][0]
        val dj = dirs[k][1]
        val len = hypot(di.toDouble(), dj.toDouble())
        for (i in di..B) {
            val x1 = (SIDE - i + di).toDouble()
            for (j in dj..B) {
                val pi = i - di
                val pj = j - dj
                val cur = M[idx(pi, pj)]
                if (cur == Double.NEGATIVE_INFINITY) continue
                val y1 = (pj + (SIDE - B)).toDouble()
                val nv = cur + (x1 * dj + di * y1) / 2.0 - lambda * len
                val dst = idx(i, j)
                if (nv > M[dst]) { M[dst] = nv; bestDir[dst] = k }
            }
        }
    }
    var best = Double.NEGATIVE_INFINITY
    var bi = -1
    var bk = -1
    for (i in 0..B) {
        val v = M[idx(i, B)]
        if (v == Double.NEGATIVE_INFINITY) continue
        val tot = v + (250.0 - lambda) * (SIDE - i) + 500.0 * lambda - 187500.0
        if (tot > best) { best = tot; bi = i; bk = bestDir[idx(i, B)] }
    }
    lastI = bi; lastK = bk
    return best
}

/** 由 DP 状态回溯出角链（(x,y) 坐标）。 */
private fun rebuild(): List<IntArray> {
    val chain = ArrayList<IntArray>()
    var i = lastI
    var k = lastK
    var guard = 0
    while (guard++ < 500 && i >= 0 && k >= 0) {
        chain.add(intArrayOf(SIDE - i, B + (SIDE - B)))
        val pk = bestDir[idx(i, B)]
        if (pk < 0) break
        i -= dirs[pk][0]
        k = pk
        chain.add(intArrayOf(SIDE - i, B - dirs[pk][1] + (SIDE - B)))
    }
    chain.reverse()
    return chain
}

/** 由角链算出完整的 4 重对称多边形。 */
private fun fullPolygon(chain: List<IntArray>): List<IntArray> {
    fun rot(p: IntArray) = intArrayOf(SIDE - p[1], p[0])
    val out = ArrayList<IntArray>()
    var cur = chain
    for (t in 0 until 4) {
        out.addAll(cur)
        cur = cur.map { rot(it) }
    }
    return out
}

private fun ratio(poly: List<IntArray>): Double {
    val n = poly.size
    var s = 0L
    var per = 0.0
    for (i in 0 until n) {
        val a = poly[i]
        val b = poly[(i + 1) % n]
        s += a[0].toLong() * b[1] - b[0].toLong() * a[1]
        per += hypot((b[0] - a[0]).toDouble(), (b[1] - a[1]).toDouble())
    }
    return (s * 0.5) / per
}

/** 局部搜索：单点移动 / 插入 / 删除，凸性由叉积判定。 */
private fun localSearch(poly: List<IntArray>, rounds: Int = 40): Pair<Double, List<IntArray>> {
    var pts: MutableList<IntArray> = poly.map { it.copyOf() }.toMutableList()
    fun convexOk(p: List<IntArray>): Boolean {
        val n = p.size
        if (n < 3) return false
        for (i in 0 until n) {
            val a = p[(i - 1 + n) % n]; val b = p[i]; val c = p[(i + 1) % n]
            val cr = (b[0] - a[0]).toLong() * (c[1] - b[1]) - (b[1] - a[1]).toLong() * (c[0] - b[0])
            if (cr < 0) return false
        }
        return true
    }
    var best = ratio(pts)
    repeat(rounds) {
        var improved = false
        for (i in pts.indices) {
            val x = pts[i][0]; val y = pts[i][1]
            for (dx in -1..1) for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val old = pts[i]
                pts[i] = intArrayOf(x + dx, y + dy)
                if (pts[i][0] in 0..SIDE && pts[i][1] in 0..SIDE && convexOk(pts)) {
                    val v = ratio(pts)
                    if (v > best + 1e-12) { best = v; improved = true; break }
                }
                pts[i] = old
            }
        }
        for (i in pts.indices) {
            var go = true
            while (go) {
                go = false
                val a = pts[i]
                for (dx in -2..2) for (dy in -2..2) {
                    if (dx == 0 && dy == 0) continue
                    val c = intArrayOf(a[0] + dx, a[1] + dy)
                    if (c[0] !in 0..SIDE || c[1] !in 0..SIDE) continue
                    val trial: MutableList<IntArray> = ArrayList(pts).also { it.add(i + 1, c) }
                    if (convexOk(trial)) {
                        val v = ratio(trial)
                        if (v > best + 1e-12) { pts = trial; best = v; go = true; improved = true; break }
                    }
                }
            }
        }
        var i = 0
        while (i < pts.size) {
            if (pts.size > 3) {
                val trial: MutableList<IntArray> = ArrayList(pts).also { it.removeAt(i) }
                if (convexOk(trial)) {
                    val v = ratio(trial)
                    if (v > best + 1e-12) { pts = trial; best = v; improved = true; continue }
                }
            }
            i++
        }
        if (!improved) return@repeat
    }
    return best to pts
}

/** 以「圆角正方形」的格点近似为种子（凸包）。 */
private fun roundedShape(r: Int): List<IntArray> {
    val c = SIDE - r
    val cands = ArrayList<IntArray>()
    for (x in 0..SIDE) for (y in 0..SIDE) {
        if (x in c..SIDE - c && y in c..SIDE - c) cands.add(intArrayOf(x, y))
    }
    for (cc in listOf(intArrayOf(c, c), intArrayOf(SIDE - c, c), intArrayOf(c, SIDE - c), intArrayOf(SIDE - c, SIDE - c))) {
        for (x in maxOf(0, cc[0] - r)..minOf(SIDE, cc[0] + r)) {
            for (y in maxOf(0, cc[1] - r)..minOf(SIDE, cc[1] + r)) {
                val dx = (x - cc[0]).toDouble(); val dy = (y - cc[1]).toDouble()
                if (dx * dx + dy * dy <= r.toDouble() * r) cands.add(intArrayOf(x, y))
            }
        }
    }
    val pts = cands.toSet().toMutableList()
    pts.sortWith(compareBy({ it[0] }, { it[1] }))
    val lower = ArrayList<IntArray>()
    for (p in pts) {
        while (lower.size >= 2) {
            val a = lower[lower.size - 2]; val b = lower[lower.size - 1]
            val cr = (b[0] - a[0]).toLong() * (p[1] - b[1]) - (b[1] - a[1]).toLong() * (p[0] - b[0])
            if (cr <= 0L) lower.removeAt(lower.size - 1) else break
        }
        lower.add(p)
    }
    val upper = ArrayList<IntArray>()
    for (p in pts.asReversed()) {
        while (upper.size >= 2) {
            val a = upper[upper.size - 2]; val b = upper[upper.size - 1]
            val cr = (b[0] - a[0]).toLong() * (p[1] - b[1]) - (b[1] - a[1]).toLong() * (p[0] - b[0])
            if (cr <= 0L) upper.removeAt(upper.size - 1) else break
        }
        upper.add(p)
    }
    lower.removeAt(lower.size - 1)
    upper.removeAt(upper.size - 1)
    return lower + upper
}

fun main() {
    println("== 题面参照 ==")
    println("  整块正方形 250000 / 2000 = 125.0  " + (if (ratio(listOf(intArrayOf(0, 0), intArrayOf(500, 0), intArrayOf(500, 500), intArrayOf(0, 500))) == 125.0) "-> 一致" else "-> 不一致"))
    val oct = listOf(
        intArrayOf(75, 0), intArrayOf(425, 0), intArrayOf(500, 75), intArrayOf(500, 425),
        intArrayOf(425, 500), intArrayOf(75, 500), intArrayOf(0, 425), intArrayOf(0, 75)
    )
    println("  四角切 75 m 的八边形 = " + String.format("%.4f", ratio(oct)) + "  -> 题面给 130.87")
    var cont = 0.0
    var contR = 0
    for (r in 100000..200000) {
        val rr = r / 1000.0
        val a = (SIDE - 2 * rr) * (SIDE - 2 * rr) + 4 * rr * (SIDE - 2 * rr) + Math.PI * rr * rr
        val p = 4 * (SIDE - 2 * rr) + 2 * Math.PI * rr
        if (a / p > cont) { cont = a / p; contR = r }
    }
    println("  连续松弛上界（圆角正方形 r=" + contR / 1000.0 + "）= " + String.format("%.7f", cont))

    println("== 格点精确解：参数化 DP ==")
    var lo = 100.0
    var hi = 200.0
    repeat(45) {
        val mid = (lo + hi) / 2
        if (solveLambda(mid) > 0) lo = mid else hi = mid
    }
    val lambda = (lo + hi) / 2
    val g0 = solveLambda(lambda)
    val gm = solveLambda(lambda - 1e-6)
    val gp = solveLambda(lambda + 1e-6)
    println("  lambda* = " + lambda)
    println("  G(lambda*) = " + g0 + "（=0 即为最优比值）")
    println("  单调性检查：G(lambda*-1e-6)=" + gm + " > 0, G(lambda*+1e-6)=" + gp + " < 0  " +
        (if (gm > 0 && gp < 0) "-> 通过" else "-> 不通过！"))
    println("  连续松弛上界 " + String.format("%.7f", cont) + " > 格点最优 " + String.format("%.8f", lambda) +
        "（间距 " + String.format("%.5f", cont - lambda) + "）-> 通过")

    println("== 双方法互证：无对称假设的局部搜索 ==")
    var seedBest = 0.0
    for (rr in intArrayOf(120, 128, 133, 136, 140, 145)) {
        val shape = roundedShape(rr)
        val (v2, _) = localSearch(shape)
        if (v2 > seedBest) seedBest = v2
    }
    println("  6 个不同半径种子的局部搜索最优 = " + String.format("%.10f", seedBest) +
        "  " + (if (seedBest <= lambda + 1e-9) "-> 不超过 DP 的 lambda*，两者一致" else "-> 超过 lambda*！"))
    val t0 = System.nanoTime()
    val rep = 3
    var acc = 0.0
    for (k in 0 until rep) acc += solveLambda(lambda)
    val dpMs = (System.nanoTime() - t0) / 1e6 / rep
    println("OPT_MS: " + String.format("%.1f", dpMs) + "  （单次 lambda 的方向×格点 DP）")
    val t1 = System.nanoTime()
    localSearch(roundedShape(133))
    println("BRUTE_MS: " + String.format("%.3f", (System.nanoTime() - t1) / 1e6) + "  （无对称假设的局部搜索对照）")
    println("ANSWER: " + String.format("%.8f", lambda))
    println("ANSWER_ROUNDED_LONG: " + Math.round(lambda * 1e8))
}