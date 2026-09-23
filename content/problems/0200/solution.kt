package dev.pekt.problems

import java.math.BigInteger

/**
 * Problem 200: Prime-proof Squbes
 *
 * 思路：sqube = p^2 * q^3（p、q 相异素数）。
 *   1) 埃氏筛出 p <= sqrt(1e13/8) ≈ 1.12e6 的全部素数；
 *   2) 枚举全部有序素数对 p^2*q^3 <= 1e13，先做子串粗筛（十进制含 "200"），
 *      候选从约 40 万缩到 2889；升序排序；
 *   3) 逐个做素数免疫检测：每一位改成 0-9 全试（首位改 0 按整数解释），
 *      Miller-Rabin（n < 3.3e17 下 12 个基确定性）判素，任一变体为素数即非免疫；
 *   4) 第 200 个命中即答案（枚举上界 1e13 远超答案 2.29e11，余量充分）。
 *   校验：前两个命中应为 200 与 1992008，与题面描述一致。
 *
 * 答案：229161792008
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
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
    var count = 0
    for (i in 2..limit) if (!composite[i]) count++
    val out = IntArray(count)
    var idx = 0
    for (i in 2..limit) if (!composite[i]) out[idx++] = i
    return out
}

private fun isqrt(x: Long): Long {
    var r = Math.floor(Math.sqrt(x.toDouble())).toLong()
    while ((r + 1) * (r + 1) <= x) r++
    while (r * r > x) r--
    return r
}

private fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    for (p in intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        if (n % p == 0L) return n.toLong() == p.toLong()
    }
    var d = n - 1
    var s = 0
    while (d and 1L == 0L) { d = d shr 1; s++ }
    val nb = BigInteger.valueOf(n)
    val nMinus1 = nb.subtract(BigInteger.ONE)
    for (a in intArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)) {
        var x = BigInteger.valueOf(a.toLong()).modPow(BigInteger.valueOf(d), nb)
        if (x == BigInteger.ONE || x == nMinus1) continue
        var composite = true
        repeat(s - 1) {
            x = x.multiply(x).mod(nb)
            if (x == nMinus1) { composite = false; return@repeat }
        }
        if (composite) return false
    }
    return true
}

/** 素数免疫：改动任意一位（0-9，含首位改 0 的整数解释）都不能得到素数。 */
private fun isPrimeProof(n: Long): Boolean {
    val s = n.toString()
    for (i in s.indices) {
        val cur = s[i] - '0'
        for (d in 0..9) {
            if (d == cur) continue
            val t = s.substring(0, i) + d + s.substring(i + 1)
            if (isPrimeLong(t.toLong())) return false
        }
    }
    return true
}

fun solve200(): Long {
    val limit = 10_000_000_000_000L            // 枚举上界 1e13
    val primes = sievePrimes(isqrt(limit / 8).toInt() + 1)
    val cands = ArrayList<Long>(4096)
    for (q in primes) {
        val q3 = q.toLong() * q * q
        if (q3 > limit / 4) break              // p^2 >= 4
        val pLimit = isqrt(limit / q3)
        for (p in primes) {
            if (p > pLimit) break
            if (p == q) continue
            val n = p.toLong() * p * q3
            if (n.toString().contains("200")) cands.add(n)
        }
    }
    cands.sort()
    var hit = 0
    for (n in cands) {
        if (isPrimeProof(n)) {
            hit++
            if (hit == 200) return n
        }
    }
    error("only $hit prime-proof candidates")
}

fun main() {
    var best = Long.MAX_VALUE
    var ans = 0L
    repeat(3) {
        val t = System.nanoTime()
        ans = solve200()
        best = minOf(best, System.nanoTime() - t)
    }
    println("answer=$ans bestMs=${"%.1f".format(best / 1e6)}")
}
