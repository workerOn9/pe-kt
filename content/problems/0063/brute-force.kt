/**
 * Project Euler 063 — 暴力解（教学对比用）
 *
 * 不推导终止条件：直接对 n = 1..30、a = 1..100 的全部组合算出 a^n，
 * 用 10^(n-1) <= a^n < 10^n 判定位数，累计计数。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    val ten = BigInteger.TEN
    var count = 0L
    for (n in 1..30) {
        val lo = ten.pow(n - 1)
        val hi = ten.pow(n)
        for (a in 1..100) {
            val p = BigInteger.valueOf(a.toLong()).pow(n)
            if (p.compareTo(lo) >= 0 && p.compareTo(hi) < 0) count++
        }
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
