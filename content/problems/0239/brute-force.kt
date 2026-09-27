#!/usr/bin/env kotlin
/**
 * Project Euler 239 — brute-force：全排列枚举（小规模 N=8）。
 *
 * 直接枚举 {1..N} 的全部 N! 个排列，数一数「恰有 P 个素数盘不在原位」的排列有多少个。
 * 取 N=8（素数 2,3,5,7 共 4 个）、P=2（即恰有 2 个素数盘错位、2 个在原位），
 * 与 solution.kt 的容斥公式在同一规模上逐位对照。
 *
 * 换成 1..100 / 22 的话 100! ≈ 9.3×10^157 —— 全排列枚举彻底不可行，这正是本文件的意义：
 * 它是「公式对不对」的裁判，不是「100 规模怎么算」的方案。
 */

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

private fun main() {
    val N = 8
    val isPrime = BooleanArray(N + 1) { it >= 2 }
    var i = 2
    while (i * i <= N) {
        if (isPrime[i]) { var j = i * i; while (j <= N) { isPrime[j] = false; j += i } }
        i++
    }
    val primes = (2..N).filter { isPrime[it] }
    val nPrimes = primes.size
    val displaced = 2                              // 恰有 2 个素数盘错位
    val pinned = nPrimes - displaced
    println("N=$N, primes=$primes (nPrimes=$nPrimes), displaced=$displaced, pinned=$pinned")

    // 枚举全部 N! 个排列
    val perm = IntArray(N) { it + 1 }
    var hits = 0L
    var total = 0L
    fun countHits() {
        do {
            total++
            var bad = 0
            for (p in primes) if (perm[p - 1] != p) bad++
            if (bad == displaced) hits++
        } while (nextPerm(perm))
    }
    countHits()

    val probBF = BigDecimal.valueOf(hits).divide(BigDecimal.valueOf(total), MathContext(30))
    println("hits = $hits / total = $total")
    println("brute probability = $probBF")

    // 公式： C(nPrimes, pinned) · [Σ_j (-1)^j C(displaced,j)(N-pinned-j)!] / N!
    // 分母是 N!（全部盘的全排列），不是 (N-pinned)!。
    val free = N - pinned
    val choose = binomI(nPrimes, pinned)
    var good = BigInteger.ZERO
    for (j in 0..displaced) {
        val c = BigInteger.valueOf(choose)
            .multiply(BigInteger.valueOf(binomI(displaced, j)))
            .multiply(factI(free - j))
        good = if (j % 2 == 0) good.add(c) else good.subtract(c)
    }
    val probF = BigDecimal(good).divide(BigDecimal(factI(N)), MathContext(30))
    println("formula  = $probF")

    check(probBF == probF) { "brute 与公式不一致：$probBF vs $probF" }
    println("12dp = " + probBF.setScale(12, RoundingMode.HALF_UP))
    println("brute-force OK（公式与全排列枚举逐位一致）")
}

private fun nextPerm(a: IntArray): Boolean {
    var i = a.size - 2
    while (i >= 0 && a[i] > a[i + 1]) i--
    if (i < 0) return false
    var j = a.size - 1
    while (a[j] < a[i]) j--
    val t = a[i]; a[i] = a[j]; a[j] = t
    var lo = i + 1; var hi = a.size - 1
    while (lo < hi) { val s = a[lo]; a[lo] = a[hi]; a[hi] = s; lo++; hi-- }
    return true
}

private fun binomI(n: Int, k: Int): Long {
    if (k < 0 || k > n) return 0
    var r = 1L
    for (i in 0 until k) r = r * (n - i) / (i + 1)
    return r
}

private fun factI(n: Int): BigInteger {
    var r = BigInteger.ONE
    for (i in 2..n) r = r.multiply(BigInteger.valueOf(i.toLong()))
    return r
}
