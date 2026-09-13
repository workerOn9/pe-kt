/**
 * Project Euler 012 — 暴力解（教学对比用）
 *
 * 逐个生成三角数，对每个 t 用试除法 O(√t) 数因子。
 * 答案 t ≈ 7.6 × 10^7，单次开方试除约 8700 次，累计千万级操作，秒级可出。
 */

fun divisorCountBrute(n: Long): Int {
    var count = 0
    var i = 1L
    while (i * i <= n) {
        if (n % i == 0L) count += if (i * i == n) 1 else 2
        i++
    }
    return count
}

fun solveBruteForce(target: Int = 500): Long {
    var n = 1L
    while (true) {
        val t = n * (n + 1) / 2
        if (divisorCountBrute(t) > target) return t
        n++
    }
}

fun main() {
    println(solveBruteForce())
}
