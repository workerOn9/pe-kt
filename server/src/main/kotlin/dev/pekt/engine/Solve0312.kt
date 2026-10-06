package dev.pekt.engine

/**
 * PE 312 — Cycles in the Sierpinski Graph（谢尔宾斯基图上的环路）
 *
 * C(n) = 2^{e2(n)} · 3^{e3(n)}，e2(n) = 3^{n-2}，e3(n) = (3^{n-2} − 3)/2。
 * 欧拉降幂阶梯自底向上计算 C(C(C(10⁴))) mod 13⁸。
 * 最终答案：324681947。
 */
internal fun solve0312Impl(): Long {
    fun powMod(base: Long, exp: Long, mod: Long): Long {
        if (mod == 1L) return 0
        var b = base % mod
        var e = exp
        var r = 1L % mod
        while (e > 0) {
            if (e and 1L == 1L) r = r * b % mod
            b = b * b % mod
            e = e shr 1
        }
        return r
    }

    fun pow13(e: Int): Long = powMod(13L, e.toLong(), Long.MAX_VALUE)

    fun phi(n: Long): Long {
        var rest = n
        var res = n
        var p = 2L
        while (p * p <= rest) {
            if (rest % p == 0L) {
                while (rest % p == 0L) rest /= p
                res -= res / p
            }
            p++
        }
        if (rest > 1) res -= res / rest
        return res
    }

    fun cyclesMod(n: Long, m: Long): Long {
        if (n <= 2L) return 1L % m
        if (m == 1L) return 0
        val ph = phi(m)
        val t = powMod(3L, n - 2, 2 * ph)
        val e2 = t % ph
        val e3 = ((t - 3) % (2 * ph) + 2 * ph) % (2 * ph) / 2
        return powMod(2L, e2, m) * powMod(3L, e3, m) % m
    }

    fun modInverse(a: Long, m: Long): Long {
        var oldR = a % m
        var r = m
        var oldS = 1L
        var s = 0L
        while (r != 0L) {
            val q = oldR / r
            val t1 = oldR - q * r; oldR = r; r = t1
            val t2 = oldS - q * s; oldS = s; s = t2
        }
        return ((oldS % m) + m) % m
    }

    fun crt2(r: Long, m: Long, s: Long, n: Long): Long {
        val k = (((s - r) % n + n) % n) * modInverse(m % n, n) % n
        var x = r + m * k
        x %= (m * n)
        if (x < 0) x += m * n
        return x
    }

    val p13 = LongArray(9) { pow13(it) }
    val M = p13[8]
    val n1 = cyclesMod(10_000L, 48L * p13[4])
    val e2mod = (n1 - 2) % (48L * p13[6])
    val a = crt2(0L, 3L, powMod(3L, e2mod, 8L * p13[7]), 8L * p13[7]) % (24L * p13[5])
    val ph136 = 12L * p13[6]
    val n2On13 = powMod(2L, a % ph136, p13[6]) * powMod(3L, ((a - 3) % (2 * ph136) + 2 * ph136) % (2 * ph136) / 2, p13[6]) % p13[6]
    val n2 = crt2(crt2(0L, 16L, 0L, 3L), 48L, n2On13, p13[6])
    val e3mod = (n2 - 2) % (48L * p13[6])
    val e = crt2(0L, 3L, powMod(3L, e3mod, 8L * p13[7]), 8L * p13[7]) % (24L * p13[7])
    val ph138 = 12L * p13[7]
    val a2 = e % ph138
    val b2 = ((e - 3) % (2 * ph138) + 2 * ph138) % (2 * ph138) / 2
    return powMod(2L, a2, M) * powMod(3L, b2, M) % M
}
