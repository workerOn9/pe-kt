/**
 * Project Euler 058 — Spiral Primes（暴力对照）
 *
 * 暴力解：同样的对角线枚举，但素性判定用最朴素的试除（除 2 后只试奇数到 √n）。
 * 每层要判 3 个数，判定到边长 26241 时单次试除最多约 1.3 万次取模，
 * 与 Miller-Rabin 的 O(log³n) 相比是完全不同的量级。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun p058IsPrimeTrial(n: Long): Boolean {
    if (n < 2L) return false
    if (n % 2L == 0L) return n == 2L
    var d = 3L
    while (d * d <= n) {
        if (n % d == 0L) return false
        d += 2L
    }
    return true
}

fun solveBruteForce(): Long {
    var side = 3L
    var primeCount = 0L
    while (true) {
        val corner = side * side
        val step = side - 1L
        for (k in 1..3) if (p058IsPrimeTrial(corner - k * step)) primeCount++
        if (10L * primeCount < 2L * side - 1L) return side
        side += 2L
    }
}

fun main() {
    println(solveBruteForce())
}
