/**
 * Project Euler 050 — Consecutive Prime Sum
 *
 * 优化解：前缀和 + 长度剪枝：prefix[j]−prefix[i] 即连续素数段之和，
 * 内层从「当前最优长度 + 1」起步，一旦和超过 10^6 立即跳出。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun sieveBool(limit: Int): BooleanArray {
    val composite = BooleanArray(limit + 1)
    val result = BooleanArray(limit + 1) { it >= 2 }
    if (limit < 2) return result
    var p = 2
    while (p.toLong() * p <= limit) {
        if (!composite[p]) {
            var m = p * p
            while (m <= limit) { composite[m] = true; m += p }
        }
        p++
    }
    for (i in 2..limit) result[i] = !composite[i]
    return result
}

fun solve(): Long {
    val N = 1_000_000
    val isP = sieveBool(N)
    val primes = ArrayList<Int>()
    for (i in 2 until N) if (isP[i]) primes.add(i)
    val prefix = LongArray(primes.size + 1)
    for (i in primes.indices) prefix[i + 1] = prefix[i] + primes[i]
    var bestLen = 0; var bestPrime = 0L
    for (i in 0 until primes.size) {
        if (primes.size - i <= bestLen) break
        for (j in i + bestLen + 1..primes.size) {
            val s = prefix[j] - prefix[i]
            if (s >= N) break
            if (isP[s.toInt()]) { bestLen = j - i; bestPrime = s }
        }
    }
    return bestPrime
}

fun main() {
    println(solve())
}
