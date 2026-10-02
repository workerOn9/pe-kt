package dev.pekt.engine

import dev.pekt.math.sieve

/**
 * PE 304 — Primonacci（素数斐波那契）：a(1) = next_prime(10^14)、a(n) = next_prime(a(n−1))，
 * b(n) = F_{a(n)}，求 Σ_{n=1}^{100000} b(n) mod 1234567891011。
 *
 * 推导（详见 content/problems/0304/solution.kt 头部与 0304/analysis.md）：
 *   · 区间筛 [10^14, 10^14+4×10^6]：埃氏表到 √ ≈ 10^7，标记区间合数，得 124000 个素数；
 *   · 对每个素数 p 用 fast doubling（F(2k)=F(k)(2F(k+1)−F(k))、F(2k+1)=F(k+1)²+F(k)²）
 *     按二进制位迭代 O(log p) 步；
 *   · M ≈ 1.23×10^12 时两余数乘积超 Long，mulMod 把乘数拆 16 位块逐块乘模。
 * 答案 = 283988410192（F_200 与 BigInteger 直算一致；与公开答案表一致）。
 */
internal fun solve0304Impl(): Long {
    val M = 1_234_567_891_011L
    val start = 100_000_000_000_000L   // 10^14
    val span = 4_000_000

    fun mulMod(a: Long, b: Long, m: Long): Long {
        var r = 0L
        var mult = a % m
        var bb = b
        while (bb != 0L) {
            val blk = bb and 0xFFFFL
            if (blk != 0L) r = (r + mult * blk) % m
            bb = bb ushr 16
            mult = mult * (1L shl 16) % m
        }
        return r
    }

    fun fibMod(n: Long, m: Long): Long {
        var a = 0L
        var b = 1L
        var bit = 62
        while (bit >= 0) {
            val twoBmA = (2 * b % m - a % m + m) % m
            val c = mulMod(a, twoBmA, m)
            val d = (mulMod(a, a, m) + mulMod(b, b, m)) % m
            if ((n ushr bit) and 1L == 1L) {
                a = d
                b = (c + d) % m
            } else {
                a = c
                b = d
            }
            bit--
        }
        return a
    }

    val root = Math.sqrt((start + span).toDouble()).toLong().toInt() + 1
    val isP = sieve(root)
    val seg = BooleanArray(span + 1)
    for (p in 2..root) if (isP[p]) {
        val pl = p.toLong()
        var x = ((start + pl - 1) / pl) * pl
        if (x < pl * pl) x = pl * pl
        while (x <= start + span) { seg[(x - start).toInt()] = true; x += pl }
    }

    var sum = 0L
    var count = 0
    for (i in 0..span) {
        if (seg[i]) continue
        val p = start + i
        if (p <= start) continue
        sum = (sum + fibMod(p, M)) % M
        count++
        if (count >= 100000) break
    }
    return sum
}
