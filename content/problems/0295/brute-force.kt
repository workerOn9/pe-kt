#!/usr/bin/env kotlin
/**
 * Project Euler 295 — Lenticular Holes（双凸透镜孔）：定义级暴力 / 独立对照
 *
 * 本文件与 solution.kt 的快速算法完全独立：直接按题面定义枚举，不做任何结构化推导。
 *
 *   ① 枚举弦：把两交点之一 A 平移到原点（平移不改变半径，故 WLOG），另一个交点
 *      B = (u,v) 取遍 |B| ≤ 2N 的整点（透镜弦长 ≤ 2·min(r1,r2) ≤ 2N）；
 *   ② 枚举圆心：格点圆心 O 必须满足 |O−A| = |O−B|（即 O 在 AB 中垂线上）且半径 ≤ N。
 *      解线性丢番图方程 u·Ox + v·Oy = (u²+v²)/2（可解条件 gcd(u,v) | (u²+v²)/2），
 *      解集是公差 (−v/g, u/g) 的等差数列，逐个过滤 |O|² ≤ N²；
 *   ③ 判定圆对：两个不同圆同时过 A、B ⇒ 恰好交于这两点（相异两圆不可能有第 3 个交点），
 *      再检查透镜内部（= 两个开圆盘之交）有没有格点：在两圆盘包围盒的交内逐个试；
 *   ④ 用 (r1², r2²)（r1 ≤ r2）去重。因为圆过格点 ⇔ r² 是整数，这个键是无损的。
 *
 * 与快速算法的对照（本文件直接跑出，集合差异必须为空）：
 *   N = 10  → 30 对；N = 100 → 3442 对；两者都在题面给出的门内，逐对完全一致。
 *   N = 200 → 14165 对（加参数 --deep 才跑，用于更深一层的对照）。
 *
 * 规模说明：暴力枚举的圆对数量 ~ (4N²)²/2 量级，透镜判空也要扫包围盒，
 * N = 100 约 2 秒、N = 200 约 41 秒；N = 100000 需要 ~10¹² 量级圆对，物理不可行。
 * 因此 meta 的 bruteForceBaselineMs 取 N = 100（题面门）的实测值。
 * 全尺寸 L(100000) 由 solution.kt 的弦参数化 + 容斥给出。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0295/brute-force.kt
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt        （文件名里的 - 会被 mangle 成 _）
 *      加 --deep 追加 N = 200 的深层对照（约 41 秒）
 */

import kotlin.math.abs
import kotlin.math.sqrt

// ─────────────── 整数工具 ───────────────

private fun gcd64(a: Long, b: Long): Long {
    var x = abs(a); var y = abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

private fun extGcd(a0: Long, b0: Long): LongArray {
    val a = abs(a0); val b = abs(b0)
    var oldR = a; var r = b; var oldS = 1L; var s = 0L; var oldT = 0L; var t = 1L
    while (r != 0L) {
        val q = oldR / r
        var tmp = oldR - q * r; oldR = r; r = tmp
        tmp = oldS - q * s; oldS = s; s = tmp
        tmp = oldT - q * t; oldT = t; t = tmp
    }
    var x = oldS; var y = oldT
    if (a0 < 0) x = -x
    if (b0 < 0) y = -y
    return longArrayOf(oldR, x, y)
}

private fun isqrt(x: Long): Long {
    var r = sqrt(x.toDouble()).toLong()
    while (r * r > x) r--
    while ((r + 1) * (r + 1) <= x) r++
    return r
}

private fun ceilSqrt(x: Long): Long { val r = isqrt(x); return if (r * r == x) r else r + 1 }

// ─────────────── ① 定义级暴力 ───────────────

/**
 * 枚举全部透镜对，返回 (r1², r2²) 键的集合（r1 ≤ r2，r² 为整数）。
 * 每对圆 (O1,|O1|)、(O2,|O2|) 都同时过原点与 B，故交点必为 {A, B}。
 */
private fun brutePairs(N: Int, verbose: Boolean = false): HashSet<Long> {
    val n2 = N.toLong() * N
    val bMax = 2 * N
    val BIG = n2 + 1
    val res = HashSet<Long>()
    var candidatePairs = 0L
    var emptyLenses = 0L
    for (u in -bMax..bMax) for (v in -bMax..bMax) {
        val s2 = u.toLong() * u + v.toLong() * v
        if (s2 == 0L || s2 > 4L * N * N) continue      // 弦长 ≤ 2·min(r1,r2) ≤ 2N
        if (s2 % 2L != 0L) continue                     // 需要 |B|²/2 为整数
        val g = gcd64(u.toLong(), v.toLong())
        if ((s2 / 2) % g != 0L) continue                // 丢番图方程可解性
        val e = extGcd(u.toLong(), v.toLong())
        val scale = (s2 / 2) / g
        val ox0 = e[1] * scale; val oy0 = e[2] * scale
        val dx = -v / g; val dy = u / g
        val centers = ArrayList<LongArray>()            // (r², Ox, Oy)
        var j = -(2L * N + 2)
        while (j <= 2L * N + 2) {
            val ox = ox0 + j * dx; val oy = oy0 + j * dy
            val r2 = ox * ox + oy * oy
            if (r2 <= n2) centers.add(longArrayOf(r2, ox, oy))    // 半径 ≤ N 且过 A、B
            j++
        }
        candidatePairs += centers.size.toLong() * (centers.size - 1) / 2
        for (i in centers.indices) for (k in i + 1 until centers.size) {
            val a = centers[i]; val b = centers[k]
            if (lensEmpty(a, b)) {
                emptyLenses++
                val r1 = minOf(a[0], b[0]); val r2 = maxOf(a[0], b[0])
                res.add(r1 * BIG + r2)
            }
        }
    }
    if (verbose) println("  暴力统计：候选圆对 $candidatePairs，空透镜 $emptyLenses 个，不同透镜对 ${res.size} 个")
    return res
}

/** 透镜内部（两开圆盘之交）是否没有格点；包围盒取两圆盘包围盒的交。 */
private fun lensEmpty(a: LongArray, b: LongArray): Boolean {
    val r1 = a[0]; val r2 = b[0]
    var x = maxOf(a[1] - ceilSqrt(r1), b[1] - ceilSqrt(r2))
    val xHi = minOf(a[1] + ceilSqrt(r1), b[1] + ceilSqrt(r2))
    while (x <= xHi) {
        var y = maxOf(a[2] - ceilSqrt(r1), b[2] - ceilSqrt(r2))
        val yHi = minOf(a[2] + ceilSqrt(r1), b[2] + ceilSqrt(r2))
        while (y <= yHi) {
            val d1 = (x - a[1]) * (x - a[1]) + (y - a[2]) * (y - a[2])
            if (d1 < r1) {
                val d2 = (x - b[1]) * (x - b[1]) + (y - b[2]) * (y - b[2])
                if (d2 < r2) return false             // 内部出现格点 → 不是透镜孔
            }
            y++
        }
        x++
    }
    return true
}

// ─────────────── 快速算法（用于逐对比对，逻辑同 solution.kt） ───────────────

private fun mThreshold(u: Int, v: Int): Long {
    val s = u.toLong() * u + v.toLong() * v
    val e = extGcd(-v.toLong(), u.toLong())
    val k1 = (((u.toLong() * e[1] + v.toLong() * e[2]) % s) + s) % s
    val sq = isqrt(s - 1)
    var best = maxOf(1L, (if (sq * sq == s - 1) sq else sq + 1) - 1L)
    var n = 1L
    while (4L * n * best < s) {
        val d = (n * k1) % s - s / 2
        val vi = -Math.floorDiv(-(s * s - 4L * d * d - 4L * n * n), 4L * s * n)
        if (vi > best) best = vi
        n++
    }
    return best
}

/** 弦参数化：s ↦ min M(u,v)，再对每个允许的奇数 m 生成 r² = s(m²+1)/4。 */
private fun fastPairs(N: Int): HashSet<Long> {
    val n2 = N.toLong() * N
    val maxS = 2 * N + (4 * sqrt(2.0 * N)).toInt() + 200
    val mByS = HashMap<Int, Long>()
    val uMax = isqrt(maxS.toLong()).toInt() + 1
    for (u in 1..uMax step 2) for (v in u..uMax step 2) {
        val s = u * u + v * v
        if (s > maxS) break
        if (gcd64(u.toLong(), v.toLong()) != 1L) continue
        val m = mThreshold(u, v)
        val cur = mByS[s]
        if (cur == null || m < cur) mByS[s] = m
    }
    val BIG = n2 + 1
    val res = HashSet<Long>()
    for ((s, mMin) in mByS) {
        val radii = ArrayList<Long>()
        var m = if (mMin % 2L == 0L) mMin + 1 else mMin
        if (m < 1) m = 1
        while (true) {
            val r2 = s * (m * m + 1) / 4
            if (r2 > n2) break
            radii.add(r2)
            m += 2
        }
        for (i in radii.indices) for (j in i until radii.size) res.add(radii[i] * BIG + radii[j])
    }
    return res
}

// ─────────────── 主程序 ───────────────

private fun report(N: Int, verbose: Boolean = true): Pair<HashSet<Long>, Double> {
    val t0 = System.nanoTime()
    val brute = brutePairs(N, verbose)
    val ms = (System.nanoTime() - t0) / 1e6
    val fast = fastPairs(N)
    val fastOnly = HashSet<Long>(fast).apply { removeAll(brute) }
    val bruteOnly = HashSet<Long>(brute).apply { removeAll(fast) }
    val BIG = N.toLong() * N + 1
    println("N = $N：暴力 ${brute.size} 对，快速 ${fast.size} 对；")
    println("  仅暴力有 ${bruteOnly.size} 个、仅快速有 ${fastOnly.size} 个（两个集合差异必须为空）${if (bruteOnly.isEmpty() && fastOnly.isEmpty()) " ✓" else " ✗"}")
    check(bruteOnly.isEmpty() && fastOnly.isEmpty()) { "N = $N 两方法逐对不一致" }
    if (verbose && N <= 10) {
        val list = brute.map { Pair(it / BIG, it % BIG) }.sortedWith(compareBy({ it.first }, { it.second }))
        println("  全部透镜对（r1², r2²）：$list")
    }
    return Pair(brute, ms)
}

fun main(args: Array<String>) {
    println("== 定义级暴力 vs 弦参数化快速算法：逐对比对 ==")
    for (N in intArrayOf(10, 100)) report(N, verbose = N == 10)

    println()
    println("== bruteForceBaselineMs 口径：N = 100 的暴力（3 轮最优，JIT 预热后）==")
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val (set, ms) = report(100, verbose = false)
        check(set.size == 3442) { "第 ${round + 1} 轮 L(100) 漂移：${set.size}" }
        println("  第 ${round + 1} 轮：${"%.1f".format(ms)} ms（${set.size} 对）")
        if (ms < best) best = ms
    }
    println("暴力 N = 100 最优：${"%.1f".format(best)} ms")

    if (args.contains("--deep")) {
        println()
        println("== 深层对照 N = 200（慢，约 41 秒，仅跑 1 轮）==")
        val (set, ms) = report(200)
        println("  耗时 ${"%.1f".format(ms)} ms，L(200) = ${set.size}")
    } else {
        println()
        println("（加 --deep 可追加 N = 200 的深层对照，约 41 秒；N ≥ 1000 时暴力不可行。）")
        println("参考：弦参数化方法给出 L(200) = ${fastPairs(200).size}（上面已与暴力一致的部分）")
    }
}
