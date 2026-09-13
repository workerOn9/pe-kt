/**
 * Project Euler 053 — Combinatoric Selections（暴力解，教学对比用）
 *
 * 用 BigInteger 逐行累加帕斯卡三角（C(n,r) = C(n-1,r-1) + C(n-1,r)），
 * 把 n ≤ 100 的全部 5151 个取值算出来再逐个与 10^6 比较。
 * 不做对称性截断、不做超标提前退出，是「先算完再筛」的直白写法。
 *
 * 复杂度：O(N²) 次 BigInteger 加法与比较。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    val limit = BigInteger.valueOf(1_000_000L)
    var count = 0L
    var row = listOf(BigInteger.ONE)
    for (n in 1..100) {
        val next = ArrayList<BigInteger>(n + 1)
        for (r in 0..n) {
            val left = if (r > 0) row[r - 1] else BigInteger.ZERO
            val right = if (r < row.size) row[r] else BigInteger.ZERO
            val value = left + right
            next.add(value)
            if (value > limit) count++
        }
        row = next
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
