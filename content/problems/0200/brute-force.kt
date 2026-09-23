package dev.pekt.problems

/**
 * PE 200 暴力解：不做子串预过滤，枚举上界内全部 sqube（p^2*q^3）
 * 排序后按自然序扫描，命中含 "200" 的才做素数免疫检测；
 * 判素用 6k±1 试除法（对照优化解的 Miller-Rabin + 子串剪枝）。
 */
private fun sievePrimes(limit: Int): IntArray {
    val composite = BooleanArray(limit + 1)
    var p = 2
    while (p.toLong() * p <= limit) {
        if (!composite[p]) {
            var m = p * p
            while (m <= limit) { composite[m] = true; m += p }
        }
        p++
    }
    val out = ArrayList<Int>()
    for (i in 2..limit) if (!composite[i]) out.add(i)
    return out.toIntArray()
}

private fun isPrimeTrial(n: Long): Boolean {
    if (n < 2) return false
    if (n % 2 == 0L) return n == 2L
    if (n % 3 == 0L) return n == 3L
    var i = 5L
    while (i * i <= n) {
        if (n % i == 0L || n % (i + 2) == 0L) return false
        i += 6
    }
    return true
}

private fun isPrimeProofTrial(n: Long): Boolean {
    val s = n.toString()
    for (i in s.indices) {
        val cur = s[i] - '0'
        for (d in 0..9) {
            if (d == cur) continue
            val t = s.substring(0, i) + d + s.substring(i + 1)
            if (isPrimeTrial(t.toLong())) return false
        }
    }
    return true
}

fun bruteForce200(target: Int = 200): Long {
    val limit = 10_000_000_000_000L
    val primes = sievePrimes(1_118_040)
    val all = ArrayList<Long>(1 shl 19)
    for (qi in primes.indices) {
        val q = primes[qi].toLong()
        val q3 = q * q * q
        if (q3 > limit / 4) break
        for (pi in primes.indices) {
            val p = primes[pi].toLong()
            val n = p * p * q3
            if (n > limit) break          // p 升序，之后只会更大，精确比较防 sqrt 取整偏差
            if (p == q) continue
            all.add(n)
        }
    }
    all.sort()
    var hit = 0
    for (n in all) {
        if (!n.toString().contains("200")) continue
        if (isPrimeProofTrial(n)) {
            hit++
            if (hit == target) return n
        }
    }
    error("only $hit")
}

fun main() {
    val t = System.nanoTime()
    val ans = bruteForce200()
    val ms = (System.nanoTime() - t) / 1e6
    println("brute answer=$ans bestMs=${"%.1f".format(ms)}")
}
