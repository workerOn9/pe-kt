/**
 * Project Euler 010 — 暴力解（教学对比用）
 *
 * 对每个候选数用试除法判素：只试不超过 sqrt 的奇数因子，再全部求和。
 * 200 万个数逐个试除，量级 ~10⁸ 次取余，秒级。
 */

fun isPrime(n: Int): Boolean {
    if (n < 2) return false
    if (n % 2 == 0) return n == 2
    var d = 3
    while (d * d <= n) {
        if (n % d == 0) return false
        d += 2
    }
    return true
}

fun solveBruteForce(limit: Int = 2_000_000): Long {
    var sum = 0L
    for (i in 2 until limit) {
        if (isPrime(i)) sum += i
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
