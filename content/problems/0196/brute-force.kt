package dev.pekt.problems

/**
 * PE 196 暴力解：直接枚举每个小行，手动验证邻居关系。
 * 仅用于小规模验证（如 n=8, n=9）。
 */
fun bruteForce196(n: Int = 10): Long {
    // 对于小 n，直接构建三角形的邻接表，标记素数，检查三元组
    val limit = tri(n)
    if (limit < 2) return 0L
    val isPrime = sieve(limit)
    var sum = 0L
    for (r in 1..n) {
        val start = tri(r - 1) + 1
        val end = tri(r)
        for (v in start..end) {
            if (!isPrime[v]) continue
            if (hasAtLeastTwoPrimeNeighbors(v, r, limit, isPrime)) {
                sum += v
            }
        }
    }
    return sum
}

private fun tri(n: Long): Long = n * (n + 1) / 2

private fun sieve(n: Int): BooleanArray {
    val isPrime = BooleanArray(n + 1) { true }
    isPrime[0] = false; isPrime[1] = false
    for (i in 2..Math.sqrt(n.toDouble()).toInt()) {
        if (isPrime[i]) {
            for (j in i * i..n step i) isPrime[j] = false
        }
    }
    return isPrime
}

private fun hasAtLeastTwoPrimeNeighbors(v: Int, row: Int, limit: Int, isPrime: BooleanArray): Boolean {
    var count = 0
    val neighbors = listOf(v - 1, v + 1, v - row, v - row - 1, v + row, v + row + 1)
    for (u in neighbors) {
        if (u >= 2 && u <= limit && isPrime[u]) count++
    }
    return count >= 2
}

fun main() {
    println("S(8) = ${bruteForce196(8)} (expected 60)")
    println("S(9) = ${bruteForce196(9)} (expected 37)")
}
