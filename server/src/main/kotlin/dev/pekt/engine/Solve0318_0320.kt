package dev.pekt.engine

/**
 * PE 318 — 2011 Nines（2011 个 9）
 *
 * (√p+√q)^{2n} 的小数部分开头连续 9 的个数 >= 2011 <=> y^{2n} <= 10^{-2011}。
 * N(p,q) = ceil( 2011 / (-2 * log10 y) )，y = (q-p)/(√q+√p)。
 * 最终答案：709313889。
 */
internal fun solve0318Impl(): Long {
    val target = 2011
    val limit = 2011
    var total = 0L
    for (p in 1 until limit) {
        for (q in p + 1..limit - p) {
            val t = q - p - 1
            if (t * t >= 4 * p) continue
            val y = (q - p).toDouble() / (Math.sqrt(q.toDouble()) + Math.sqrt(p.toDouble()))
            val n = Math.ceil(target / (-2.0 * Math.log10(y))).toLong()
            total += n
        }
    }
    return total
}

/**
 * PE 319 — Bounded Sequences（有界数列）
 *
 * t(n) = sum_{q 无平方因子} mu(q) * W(floor(n/q))。杜教筛 + 整除分块。
 * 最终答案：268457129。
 */
internal fun solve0319Impl(): Long {
    val mod = 1_000_000_000L
    val twoMod = 2_000_000_000L
    val n = 10_000_000_000L

    fun powMod(base: Long, exp: Long, m: Long): Long {
        var b = base % m
        var e = exp
        var r = 1L % m
        while (e > 0) {
            if (e and 1L == 1L) r = r * b % m
            b = b * b % m
            e = e shr 1
        }
        return r
    }

    fun wMod(m: Long): Long {
        val a = (powMod(3L, m + 1, twoMod) - 3 + twoMod) % twoMod / 2
        val b = (powMod(2L, m + 1, twoMod) - 2 + twoMod) % twoMod
        return ((a - b) % mod + mod) % mod
    }

    val n0 = (Math.pow(n.toDouble(), 2.0 / 3.0)).toLong() + 10
    val mu = IntArray(n0.toInt() + 1)
    mu[1] = 1
    val isComp = BooleanArray(n0.toInt() + 1)
    val list = ArrayList<Int>()
    for (i in 2..n0.toInt()) {
        if (!isComp[i]) { list.add(i); mu[i] = -1 }
        for (p in list) {
            if (i.toLong() * p > n0) break
            isComp[i * p] = true
            if (i % p == 0) { mu[i * p] = 0; break }
            mu[i * p] = -mu[i]
        }
    }

    val m0 = IntArray(n0.toInt() + 1)
    var s = 0
    for (i in 1..n0.toInt()) { s += mu[i]; m0[i] = s }
    val cache = HashMap<Long, Int>()

    fun mertens(x: Long): Int {
        if (x <= n0) return m0[x.toInt()]
        cache[x]?.let { return it }
        var res = 1L
        var i = 2L
        while (i <= x) {
            val q = x / i
            val j = x / q
            res -= (j - i + 1) * mertens(q).toLong()
            i = j + 1
        }
        val r = res.toInt()
        cache[x] = r
        return r
    }

    var total = 0L
    var i = 1L
    while (i <= n) {
        val w = n / i
        val hi = n / w
        val d = ((mertens(hi) - mertens(i - 1)).toLong() % mod + mod) % mod
        total = (total + d * wMod(w)) % mod
        i = hi + 1
    }
    return total
}

/**
 * PE 320 — Factorials Divisible by Giant Integers（被巨大整数整除的阶乘）
 *
 * 勒让德定理 + 历史最大值单调推进。
 * 最终答案：535603486780279315。
 */
internal fun solve0320Impl(): Long {
    val M = 1234567890L
    val limit = 1_000_000
    val mod = 1_000_000_000_000_000_000L

    val s = IntArray(limit + 1) { it }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (s[i] == i) {
            var j = i.toLong() * i
            while (j <= limit) {
                if (s[j.toInt()] == j.toInt()) s[j.toInt()] = i
                j += i
            }
        }
        i++
    }

    fun vFact(n: Long, p: Int): Long {
        var m = n
        var v = 0L
        while (m > 0) { m /= p; v += m }
        return v
    }

    fun nPrime(target: Long, p: Int, from: Long): Long {
        var n = maxOf(from, target * (p - 1))
        n -= n % p
        if (n < 0) n = 0
        while (true) {
            val v = vFact(n, p)
            if (v >= target) {
                val back = n - p
                if (back >= 0 && vFact(back, p) >= target) return nPrime(target, p, back)
                return n
            }
            n += p
        }
    }

    val exp = HashMap<Int, Long>()
    val cur = HashMap<Int, Long>()
    var best = 0L
    var sum = 0L

    for (k in 2..limit) {
        var t = k
        while (t > 1) {
            val p = s[t]
            var c = 0
            while (t % p == 0) { t /= p; c++ }
            val e = (exp[p] ?: 0L) + c.toLong() * M
            exp[p] = e
            val nv = nPrime(e, p, cur[p] ?: 0L)
            cur[p] = nv
            if (nv > best) best = nv
        }
        if (k >= 10) sum += best
    }
    return sum % mod
}
