package dev.pekt.engine

import kotlin.math.sqrt

/**
 * PE 311 — Biclinic Integral Quadrilaterals（双斜整数四边形）
 *
 * 凸整边四边形 ABCD，1 ≤ AB < BC < CD < AD，BD 为整数，O 为 BD 中点且 AO 为整数；
 * 若 AO = CO ≤ BO = DO 则称双斜整数四边形。求 B(10¹⁰)。
 *
 * 中线公式化归为平方和计数：四边平方和 = 4(k² + d²) = 4n ≤ N。
 * 条件 |x_A| < k 且四边形凸等价于 u > d − k，对应各表示按 u 升序后的三元组 r < i < j。
 * 故 n 的贡献为 C(m, 3)，m = (P + Z − D) / 2。
 * 最终答案：2466018557。
 */
internal fun solve0311Impl(): Long {
    val x = 10_000_000_000L / 4
    val limit = maxOf(16, (x / 25).toInt())

    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (isPrime[i]) {
            var j = i.toLong() * i
            while (j <= limit) { isPrime[j.toInt()] = false; j += i }
        }
        i++
    }
    val primes = ArrayList<Int>()
    val seg = 4_000_000
    val base = sqrt(limit.toDouble()).toInt() + 1
    val mark = BooleanArray(seg + 2)
    var lo = 2
    while (lo <= limit) {
        val hi = minOf(limit, lo + seg - 1)
        java.util.Arrays.fill(mark, false)
        for (q in 2..base) {
            if (!isPrime[q]) continue
            var start = Math.max(q.toLong() * q, ((lo.toLong() + q - 1) / q) * q)
            while (start <= hi) { mark[(start - lo).toInt()] = true; start += q }
        }
        var v = lo
        while ((v and 3) != 1) v++
        while (v <= hi) {
            if (!mark[(v - lo).toInt()]) primes.add(v)
            v += 4
        }
        lo = hi + 1
    }

    val maxY = sqrt(x.toDouble()).toInt()
    val isP = BooleanArray(maxY + 1) { it >= 2 }
    var p = 2
    while (p.toLong() * p <= maxY) {
        if (isP[p]) {
            var j = p.toLong() * p
            while (j <= maxY) { isP[j.toInt()] = false; j += p }
        }
        p++
    }
    val bad = BooleanArray(maxY + 1)
    for (y in 2..maxY step 2) bad[y] = true
    for (q in 5..maxY) {
        if (isP[q] && q % 4 == 1) {
            var j = q
            while (j <= maxY) { bad[j] = true; j += q }
        }
    }
    val t = IntArray(maxY + 2)
    for (y in 1..maxY) t[y] = t[y - 1] + (if (bad[y]) 0 else 1)

    fun isqrt(n: Long): Long {
        if (n <= 0) return 0
        var r = sqrt(n.toDouble()).toLong()
        while ((r + 1) * (r + 1) <= n) r++
        while (r * r > n) r--
        return r
    }

    var total = 0L

    fun contribute(u: Long, prod: Long, square: Boolean) {
        var lim = x / u
        var a = 0
        while (lim >= 1) {
            val shift = if (square) { if (a and 1 == 1) 1 else -1 } else 0
            val m = (prod + shift) / 2
            if (m >= 3) total += m * (m - 1) * (m - 2) / 6 * t[isqrt(lim).toInt()]
            lim /= 2
            a++
        }
    }

    fun dfs(startIdx: Int, u: Long, prod: Long, square: Boolean) {
        if (u > 1 && prod >= 5) contribute(u, prod, square)
        var idx = startIdx
        while (idx < primes.size) {
            val prime = primes[idx].toLong()
            if (u > x / prime) break
            var v = u
            var e = 1
            while (v <= x / prime) {
                v *= prime
                dfs(idx + 1, v, prod * (e + 1), square && (e % 2 == 0))
                e++
            }
            idx++
        }
    }

    dfs(0, 1L, 1L, true)
    return total
}
