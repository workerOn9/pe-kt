package dev.pekt.engine

import dev.pekt.math.primesUpTo

/**
 * PE 261 — Pivotal Square Sums（关键平方和）：求所有 distinct square-pivot k ≤ 10¹⁰ 之和。
 * 其中「k 是 square-pivot」指存在 m ≥ 1、n ≥ k 使 (k−m)² + … + k² = (n+1)² + … + (n+m)²。
 *
 * 推导（详见 content/problems/0261/solution.kt 头部与 0261/analysis.md）：
 *   用 S₂(x) = x(x+1)(2x+1)/6 展开两边得 (m+1)k(k−m) = mn(n+m+1)；两边配方（X = 2k−m、
 *   Y = 2n+m+1）得 m·Y² − (m+1)·X² = m(m+1)。由 m | X²、(m+1) | Y² 记
 *   m = αa²、m+1 = βc²（α、β 为平方因子自由部分，互素）、X = αab、Y = βcd，
 *   方程化为 Pell 型 βd² − αb² = 1。
 *   把 s = n−k ≥ 0 代入并对 v = X+Y 解二次方程，可把每个 pivot 唯一写成
 *     k = m(z+1) + w,  z = s+m ≥ m,  w² = m(m+1)z(z+1).            （*）
 *   若 m(m+1) = αt²、z(z+1) = αu²（相同平方自由部分 α），则 w = αtu：固定 α 后，
 *   满足 z(z+1) = αu² 的全体 z 就是 Pell 方程 x² − αy² = 1 中 x 为奇数的解族
 *   （x = 2z+1、y = 2u），由连分数求出的基本解生成一条轨道。
 *
 * 算法：枚举 m（k ≥ 2m(m+1) ⇒ m ≤ 70710），读出不变量 (α, t)；按 α 分组，每组求一次
 *   基本解，从 z = m（k = 2m(m+1)）出发沿轨道前进、用（*）算出 k，k > 10¹⁰ 即停；
 *   全部 k 排序去重后求和（同一 k 可由不同 (m,z) 得到，题目要求 distinct）。
 *   m、m+1 的分解用 dev.pekt.math 的 primesUpTo(mMax+1) 试除。
 *
 * 复杂度：O(M log k)（M = 70710，实测连分数总步数约 2×10⁵），内存 O(M)。
 *   本机 JIT 预热后约 10 ms。与 solution.kt 的主路径（方法 A）完全一致：
 *   题面锚点 4/21/24/110 及其分解逐一复现，K ≤ 100000 的定义型直接暴力逐值一致，
 *   独立参数化（方法 B：直接枚举 βd²−αb²=1 的解）给出同一答案；
 *   答案（72067 个 distinct pivot 之和）见 Solve0261 自检输出。
 */
internal fun solve0261Impl(): Long {
    val kMax = 10_000_000_000L
    var mMax = 1
    while (2L * (mMax + 1) * (mMax + 2) <= kMax) mMax++          // 70710
    val primes = primesUpTo(mMax + 1)

    // 每个 m 的不变量：m(m+1) = α·t²
    val alphas = LongArray(mMax + 1)
    val ts = LongArray(mMax + 1)
    for (m in 1..mMax) {
        val f1 = sqfreePart(m, primes)
        val f2 = sqfreePart(m + 1, primes)
        alphas[m] = f1[0] * f2[0]                                 // gcd(m, m+1) = 1
        ts[m] = f1[1] * f2[1]
    }

    // 按 α 分组（key = α·2^17 + m，原生 LongArray 排序）
    val shift = 17
    val keyMask = (1L shl shift) - 1
    val keys = LongArray(mMax) { alphas[it + 1] * (1L shl shift) + (it + 1) }
    keys.sort()

    var ks = LongArray(1 shl 17)
    var kn = 0
    var i = 0
    while (i < keys.size) {
        val alpha = keys[i] shr shift
        var j = i + 1
        while (j < keys.size && keys[j] shr shift == alpha) j++

        val fund = fundamentalSolution(alpha)
        val xh: Long
        val yh: Long
        if (fund[1] % 2 == 0L) {
            xh = fund[0]; yh = fund[1]
        } else {                                                  // 基本解 y 奇 ⇒ 取平方保证 y 偶
            xh = fund[0] * fund[0] + alpha * fund[1] * fund[1]
            yh = 2 * fund[0] * fund[1]
        }
        for (idx in i until j) {
            val m = (keys[idx] and keyMask).toInt()
            val t = ts[m]
            if (kn == ks.size) ks = ks.copyOf(kn * 2)
            ks[kn++] = 2L * m * (m + 1)                           // z = m：k = 2m(m+1)
            var x = 2L * m + 1                                    // (x, y) = (2z+1, 2u)
            var y = 2L * t
            while (true) {
                val nx = x * xh + alpha * y * yh
                val ny = x * yh + y * xh
                x = nx; y = ny
                val z = (x - 1) / 2
                val u = y / 2
                val w = alpha * t * u
                val k = m * (z + 1) + w
                if (k > kMax) break
                if (kn == ks.size) ks = ks.copyOf(kn * 2)
                ks[kn++] = k
            }
        }
        i = j
    }

    ks = ks.copyOf(kn)
    ks.sort()
    val unique = LongArray(kn)
    var un = 0
    for (idx in 0 until kn) {
        if (idx == 0 || ks[idx] != ks[idx - 1]) unique[un++] = ks[idx]
    }
    var sum = 0L
    for (idx in 0 until un) sum += unique[idx]
    return sum
}

/** 把 n 写成 n = α·t²（α 平方因子自由），返回 [α, t]；用素数表试除。 */
private fun sqfreePart(n: Int, primes: List<Long>): LongArray {
    var x = n.toLong()
    var alpha = 1L
    var t = 1L
    for (p in primes) {
        if (p * p > x) break
        if (x % p != 0L) continue
        var e = 0
        while (x % p == 0L) {
            x /= p
            e++
        }
        if (e % 2 == 1) alpha *= p
        repeat(e / 2) { t *= p }
    }
    if (x > 1L) alpha *= x                                       // 剩余素因子指数为 1
    return longArrayOf(alpha, t)
}

/** 连分数求 x² − D·y² = 1 的基本解，返回 [x1, y1]（D 为非平方数）。 */
private fun fundamentalSolution(D: Long): LongArray {
    var a0 = Math.sqrt(D.toDouble()).toLong()
    while (a0 > 0 && a0 * a0 > D) a0--
    while ((a0 + 1) * (a0 + 1) <= D) a0++
    var m = 0L
    var d = 1L
    var a = a0
    var h2 = 0L; var h1 = 1L
    var k2 = 1L; var k1 = 0L
    while (true) {
        val h = a * h1 + h2
        val k = a * k1 + k2
        h2 = h1; h1 = h
        k2 = k1; k1 = k
        if (h * h - D * k * k == 1L) return longArrayOf(h, k)
        m = d * a - m
        d = (D - m * m) / d
        a = (a0 + m) / d
    }
}
