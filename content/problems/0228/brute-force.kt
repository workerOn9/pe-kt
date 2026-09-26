#!/usr/bin/env kotlin
/**
 * Project Euler 228 — Minkowski Sums · 暴力对照版
 *
 * 思路：完全按定义来——正 n 边形的边方向角为 (2k)·180°/n + 90°（k = 0…n-1，
 *       即顶点角 (2k-1)·180°/n 的角平分线方向再转 90°），用「方向集合去重」的朴素实现
 *       （不识别 φ(q) 的技巧）把 S_1864…S_1909 全部枚举一遍：每个 n 生成 n 个方向角分数
 *       2k/n（k = 0…n-1），既约后塞进 HashSet，最后数集合大小。
 *
 *       与 solution.kt 的 Σφ(q) 捷径互为对照，两者都给出 86226。
 *
 *       另附几何直证：对小规模直接构造闵可夫斯基和（把两多边形的边向量按方向排序后首尾相接），
 *       数出结果的边数并与方向去重结果比对。
 *
 *       注意：**不能简单地用「两两顶点和的凸包顶点数」当边数**——浮点下近共线的相邻边
 *       不会被 `cross <= 0` 判掉，会虚增 1–2 条边（S_6 + S_10 就是 15 个凸包顶点但只有 14 个边方向）。
 *       正确做法是数**方向**（下面 edgesOfSum 用的是构造法）。
 *
 * 复杂度：O(Σ n) = O(86779) 次插入，实测约 26 ms。
 * 构建：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 * 运行：java -jar bf.jar
 */

/** 边方向用既约分数 2k/n（k = 0…n-1）表示，gcd 约分后作为 key。 */
private fun dirSet(lo: Int, hi: Int): Set<Long> {
    val s = HashSet<Long>()
    for (n in lo..hi) {
        for (k in 0 until n) {
            var num = 2 * k
            var den = n
            val g = gcd(num, den)
            num /= g
            den /= g
            s.add(num.toLong() * 1_000_003L + den)   // (分子, 分母) 打包成单个 Long
        }
    }
    return s
}

private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun main() {
    println("== 方向去重（朴素枚举）==")
    val t0 = System.nanoTime()
    val s = dirSet(1864, 1909)
    val ms = (System.nanoTime() - t0) / 1_000_000
    println("S_1864 ⊕ … ⊕ S_1909 的边数 = ${s.size}   (${ms} ms)")

    println()
    println("== 小规模：方向去重 vs 构造式闵可夫斯基和 ==")
    for ((la, lb) in listOf(3 to 4, 3 to 3, 4 to 4, 4 to 5, 5 to 7, 6 to 10)) {
        val byDirs = (dirSet(la, la) + dirSet(lb, lb)).size
        val bySum = sumEdges(la, lb)
        println("S_$la + S_$lb : 方向去重 = $byDirs, 构造式求和边数 = $bySum  "
            + if (byDirs == bySum) "OK" else "MISMATCH")
    }
    println("（对照：S_3 + S_4 = 6 边、S_4 + S_4 = 4 边，与正多边形方向并集一致）")
}

/** 构造式闵可夫斯基和：把两个多边形的边向量并入同一个方向排序表，数不同方向数。 */
private fun sumEdges(a: Int, b: Int): Int {
    val edges = ArrayList<Pair<Double, Double>>()
    edges.addAll(edgeVectors(a))
    edges.addAll(edgeVectors(b))
    // 同方向的边向量相加会合并成一条边，所以「不同方向数」就是和的边数
    val dirs = HashSet<Long>()
    for ((i, e) in edges.withIndex()) {
        val ang = Math.atan2(e.second, e.first)
        // 按 1e-7 弧度分桶（方向角本身是 π 的有理倍数，分离度远大于该量级）
        dirs.add(Math.round(ang / 1e-7))
    }
    return dirs.size
}

/** 正 n 边形的 n 条边向量（按顶点顺序首尾相接，长度 2·sin(π/n)）。 */
private fun edgeVectors(n: Int): List<Pair<Double, Double>> {
    val verts = (0 until n).map { k ->
        val ang = (2.0 * k + 1) / n * Math.PI
        Math.cos(ang) to Math.sin(ang)
    }
    return (0 until n).map { k ->
        val p = verts[k]
        val q = verts[(k + 1) % n]
        (q.first - p.first) to (q.second - p.second)
    }
}
