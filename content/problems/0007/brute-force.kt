/**
 * Project Euler 007 — 暴力解（教学对比用）
 *
 * 从 2 起逐个试数，用试除法（试到 sqrt）判素，数到第 10001 个。
 */

fun isPrime(n: Long): Boolean {
    if (n < 2) return false
    if (n % 2 == 0L) return n == 2L
    var d = 3L
    while (d * d <= n) {
        if (n % d == 0L) return false
        d += 2
    }
    return true
}

fun solveBruteForce(n: Int = 10001): Long {
    var count = 0
    var candidate = 1L
    while (count < n) {
        candidate++
        if (isPrime(candidate)) count++
    }
    return candidate
}

fun main() {
    println(solveBruteForce())
}
