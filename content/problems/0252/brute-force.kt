#!/usr/bin/env kotlin
/**
 * Project Euler 252 — Convex Holes：小规模暴力对照（brute-force.kt）
 *
 * 与 solution.kt 完全不共享核心代码路径：
 *   · 枚举点集的全部子集（2^n）；凸性用 Jarvis 步进法（gift wrapping）求凸包，
 *     凸包顶点数 == 子集大小才说明这组点严格处于凸位置（有点在包内或共线都会被剔除）；
 *   · 空穴判定用射线法：数一条向右水平射线与多边形边的交点数（半开规则处理
 *     顶点与水平边），落在边界上的点先短路排除；
 *   · 面积用鞋带公式（双倍面积，全程整数）。
 *
 * 可行规模 n ≤ 20：题面的 20 点官方样例答案 1049694.5（双倍面积 2099389），
 * 本文件用暴力枚举独立复现它，并给出计时（bruteForceBaselineMs 的数据来源）。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private const val GEN_MOD = 50_515_093L

private fun generatePoints(count: Int): Pair<IntArray, IntArray> {
    val xs = IntArray(count)
    val ys = IntArray(count)
    var s = 290797L
    for (k in 0 until count) {
        s = s * s % GEN_MOD
        xs[k] = (s % 2000L).toInt() - 1000
        s = s * s % GEN_MOD
        ys[k] = (s % 2000L).toInt() - 1000
    }
    return Pair(xs, ys)
}

/** 点 q 是否严格在（逆时针）多边形 poly 内部：射线法 */
private fun strictlyInside(n: Int, xs: IntArray, ys: IntArray, poly: IntArray, q: Int): Boolean {
    val qx = xs[q]
    val qy = ys[q]
    val k = poly.size
    // 先排除边界：与某条边共线且落在该边的外接矩形内
    for (i in 0 until k) {
        val a = poly[i]
        val b = poly[(i + 1) % k]
        val ax = xs[a]
        val ay = ys[a]
        val bx = xs[b]
        val by = ys[b]
        val d = (bx - ax).toLong() * (qy - ay) - (by - ay).toLong() * (qx - ax)
        if (d == 0L &&
            qx >= minOf(ax, bx) && qx <= maxOf(ax, bx) &&
            qy >= minOf(ay, by) && qy <= maxOf(ay, by)
        ) {
            return false
        }
    }
    var crossings = 0
    for (i in 0 until k) {
        val a = poly[i]
        val b = poly[(i + 1) % k]
        val ax = xs[a]
        val ay = ys[a]
        val bx = xs[b]
        val by = ys[b]
        if ((ay > qy) != (by > qy)) {
            val d = (bx - ax).toLong() * (qy - ay) - (by - ay).toLong() * (qx - ax)
            // 交点在 q 右侧才计数：(by > ay) 时 ⟺ d > 0；反向边则相反
            if ((by > ay) == (d > 0L)) crossings++
        }
    }
    return crossings % 2 == 1
}

/**
 * 全子集暴力：返回最大凸空穴的双倍面积（无解返回 0）。
 * 说明：n 个点里任意 3 点共线或有点落在凸包内，凸包顶点数都会小于子集大小，
 * 直接用 Jarvis 步进法得到的严格凸包来筛「严格凸位置」。
 */
private fun bruteMaxHole(n: Int, xs: IntArray, ys: IntArray): Long {
    var best = 0L
    val member = BooleanArray(n)
    val hull = IntArray(n)
    val limit = 1 shl n
    for (mask in 7 until limit) {
        var k = 0
        java.util.Arrays.fill(member, false)
        for (i in 0 until n) if ((mask ushr i) and 1 == 1) {
            member[i] = true
            k++
        }
        if (k < 3) continue
        // Jarvis 步进：从最左下点出发，每次取「最逆时针」的下一个点
        var start = -1
        for (i in 0 until n) if (member[i]) {
            if (start < 0 || ys[i] < ys[start] || (ys[i] == ys[start] && xs[i] < xs[start])) start = i
        }
        var h = 0
        var cur = start
        do {
            hull[h++] = cur
            var nxt = -1
            for (i in 0 until n) {
                if (!member[i] || i == cur) continue
                if (nxt < 0) {
                    nxt = i
                    continue
                }
                val cr = (xs[nxt] - xs[cur]).toLong() * (ys[i] - ys[cur]) -
                    (ys[nxt] - ys[cur]).toLong() * (xs[i] - xs[cur])
                if (cr < 0L) {
                    nxt = i
                } else if (cr == 0L) {
                    // 共线：取更远的点（保证凸包顶点数能反映共线退化）
                    val dn = (xs[nxt] - xs[cur]).toLong() * (xs[nxt] - xs[cur]) +
                        (ys[nxt] - ys[cur]).toLong() * (ys[nxt] - ys[cur])
                    val di = (xs[i] - xs[cur]).toLong() * (xs[i] - xs[cur]) +
                        (ys[i] - ys[cur]).toLong() * (ys[i] - ys[cur])
                    if (di > dn) nxt = i
                }
            }
            cur = nxt
        } while (cur != start && h < k + 1)
        if (h != k) continue // 有点在凸包内或共线 —— 不是严格凸位置
        // 严格左转检查（Jarvis 在共线时的取舍可能留下 180° 顶点）
        var strict = true
        for (i in 0 until h) {
            val a = hull[i]
            val b = hull[(i + 1) % h]
            val c = hull[(i + 2) % h]
            if ((xs[b] - xs[a]).toLong() * (ys[c] - ys[a]) - (ys[b] - ys[a]).toLong() * (xs[c] - xs[a]) <= 0L) {
                strict = false
                break
            }
        }
        if (!strict) continue
        var area2 = 0L
        for (i in 0 until h) {
            val a = hull[i]
            val b = hull[(i + 1) % h]
            area2 += xs[a].toLong() * ys[b] - xs[b].toLong() * ys[a]
        }
        if (area2 <= best) continue
        var empty = true
        for (q in 0 until n) {
            if (member[q]) continue
            if (strictlyInside(n, xs, ys, hull.copyOf(h), q)) {
                empty = false
                break
            }
        }
        if (empty) best = area2
    }
    return best
}

private fun areaText(area2: Long): String =
    "${area2 / 2}.${if (area2 % 2L == 0L) "0" else "5"}"

fun main() {
    val (xs, ys) = generatePoints(20)
    val n = xs.size
    println("20 点官方样例（n = $n，2^$n = ${1 shl n} 个子集）")
    val ans = bruteMaxHole(n, xs, ys)
    check(ans == 2_099_389L) { "暴力枚举得到 $ans，与题面 1049694.5（双倍面积 2099389）不符" }
    println("暴力枚举最大双倍面积 = $ans，即面积 = ${areaText(ans)}，与题面一致")

    // 计时段：JIT 预热后 3 轮取最优（bruteForceBaselineMs 的来源）
    var bestMs = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val v = bruteMaxHole(n, xs, ys)
        val ms = (System.nanoTime() - t0) / 1e6
        check(v == ans)
        if (ms < bestMs) bestMs = ms
        println("  第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("暴力枚举 20 点：${"%.1f".format(bestMs)} ms（3 轮最优，JIT 预热后）")
}
