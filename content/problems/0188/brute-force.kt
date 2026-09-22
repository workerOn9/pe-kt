package dev.pekt.problems

/**
 * Problem 188: Hyperexponentiation (Brute Force / Iterative Modulo Fixed-Point Baseline)
 *
 * 朴素迭代基准：在模 10^8 意义下，由于幂塔增长极快，末尾数字很快进入周期循环，直接循环乘方直到出现定点。
 */

fun solve0188BruteForce(): Long {
    val a = 1777L
    val b = 1855L
    val mod = 100_000_000L

    fun modPow(base: Long, exp: Long, m: Long): Long {
        var res = 1L
        var cur = base % m
        var e = exp
        while (e > 0) {
            if ((e and 1L) == 1L) res = (res * cur) % m
            cur = (cur * cur) % m
            e = e ushr 1
        }
        return res
    }

    var result = a
    for (i in 2..b) {
        val next = modPow(a, result, mod)
        if (next == result) {
            // 已达模意义下的稳定定点
            return result
        }
        result = next
    }
    return result
}

fun main() {
    println(solve0188BruteForce())
}
