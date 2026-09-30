package dev.pekt.engine

import dev.pekt.math.modPow

/**
 * PE 291 — 帕奈托波尔素数（Panaitopol Primes）：小于 5×10^15 的计数 = 4037526。
 *
 * 推导（详见 content/problems/0291/solution.kt 头部与 0291/analysis.md）：
 *   (x⁴−y⁴)/(x³+y³) = (x−y)(x²+y²)/(x²−xy+y²)。令 g = gcd(x,y)、x = gX、y = gY、
 *   D = X²−XY+Y²，则 gcd(D, (X−Y)(X²+Y²)) = 1，故 D | g；写 g = D·t 得
 *   p = t·(X−Y)(X²+Y²)。素数只允许 t = 1 且 X−Y = 1，即
 *     p = X² + (X−1)² = 2k² + 2k + 1   （k = X−1 ≥ 1）。
 *
 *   计数：p < 5×10^15 ⇔ k ≤ 49 999 999。若素数 q | p，则 (2k+1)² = 2p−1 ≡ −1 (mod q)，
 *   故 q ≡ 1 (mod 4) 且 q < 70710679。对每个 q ≡ 1 (mod 4) 求 √−1（Jacobi 找非剩余 +
 *   一次模幂），把 k 的两条同余类划掉；只划 p ≥ q²（既避免误划 p = q，也不漏划 p = q²）。
 *   存活即素数。
 *
 * 复杂度：素数筛到 7.07×10^7 ≈ 0.35 s；每个 q≡1(4) 一次 26 bit 模幂 ≈ 0.5 s；
 *   约 1.6×10^8 次划除 ≈ 0.6 s，本机合计约 1.5 s。内存用位压缩：
 *   候选位图 6.25 MB + 素数筛位图 4.4 MB。远低于 10 s 熔断线。
 */
internal fun solve0291Impl(): Long {
    val kMax = 49_999_999
    val maxP = 2L * kMax * kMax + 2L * kMax + 1L
    var qMax = Math.sqrt(maxP.toDouble()).toLong()
    while (qMax * qMax > maxP) qMax--
    while ((qMax + 1) * (qMax + 1) <= maxP) qMax++           // = 70 710 678

    // ── 素数筛（位图，编号 i 代表奇数 2i+1）──
    val qHalf = (qMax / 2).toInt() + 2
    val qWords = LongArray((qHalf + 63) ushr 6)
    var i = 1L
    while ((2 * i + 1) * (2 * i + 1) <= qMax) {
        if ((qWords[(i ushr 6).toInt()] ushr ((i and 63L).toInt())) and 1L == 0L) {
            val p = 2 * i + 1
            var j = (p * p - 1) / 2
            while (j < qHalf) {
                qWords[(j ushr 6).toInt()] = qWords[(j ushr 6).toInt()] or (1L shl (j and 63L).toInt())
                j += p
            }
        }
        i++
    }
    fun qIsPrime(oddIdx: Long): Boolean =
        (qWords[(oddIdx ushr 6).toInt()] ushr ((oddIdx and 63L).toInt())) and 1L == 0L

    // ── 候选位图：bit k ⇒ p = 2k²+2k+1 已判合数 ──
    val kW = LongArray((kMax + 64) ushr 6)
    fun kMark(k: Long) {
        kW[(k ushr 6).toInt()] = kW[(k ushr 6).toInt()] or (1L shl (k and 63L).toInt())
    }

    // i 为偶数 ⇔ q = 2i+1 ≡ 1 (mod 4)，只有这些素数可能整除候选
    i = 2L
    while (2 * i + 1 <= qMax) {
        if (qIsPrime(i)) {
            val q = 2 * i + 1
            var z = 2L
            while (jacobi291(z, q) != -1) z++                // 二次非剩余
            val r = modPow(z, (q - 1) / 4, q)                // r² ≡ −1 (mod q)
            // 首个满足 p ≥ q² 的 k（$p = q$ 不划、$p = q²$ 要划）
            var kStart = Math.floor((Math.sqrt(2.0 * q * q - 1) - 1) / 2).toLong()
            if (kStart < 0) kStart = 0
            while (kStart > 0 && 2 * kStart * kStart + 2 * kStart + 1 >= q * q) kStart--
            while (2 * (kStart + 1) * (kStart + 1) + 2 * (kStart + 1) + 1 < q * q) kStart++
            kStart++
            for (s in longArrayOf(r, q - r)) {
                val y0 = if (s and 1L == 1L) s else s + q        // Y = 2k+1 取奇数代表
                var kk = (y0 - 1) / 2
                if (kk < kStart) kk += (kStart - kk + q - 1) / q * q
                while (kk <= kMax) {
                    kMark(kk)
                    kk += q
                }
            }
        }
        i += 2L
    }

    var count = 0L
    for (k in 1..kMax) if ((kW[(k ushr 6)] ushr (k and 63)) and 1L == 0L) count++
    return count
}

/** Jacobi 符号 (a/n)，n 为正奇数；只用移位与加减。 */
private fun jacobi291(a0: Long, n0: Long): Int {
    var a = Math.floorMod(a0, n0)
    var n = n0
    var result = 1
    while (a != 0L) {
        while (a and 1L == 0L) {
            a = a shr 1
            val r8 = (n and 7L).toInt()
            if (r8 == 3 || r8 == 5) result = -result
        }
        val t = a
        a = n
        n = t
        if ((a and 3L) == 3L && (n and 3L) == 3L) result = -result
        a %= n
    }
    return if (n == 1L) result else 0
}
