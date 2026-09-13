/**
 * Project Euler 050 — 暴力解（教学对比用）
 *
 * 双重循环枚举每个起点并逐个累加素数，不借助前缀和与长度剪枝。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
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

fun solveBruteForce(): Long {
    val N = 1_000_000
    val primes = ArrayList<Int>()
    val isP = sieveBool(N)
    for (i in 2 until N) if (isP[i]) primes.add(i)
    var bestLen = 0; var bestPrime = 0L
    for (i in 0 until primes.size) {
        var s = 0L
        for (j in i until primes.size) {
            s += primes[j]
            if (s >= N) break
            if (j - i + 1 > bestLen && isP[s.toInt()]) { bestLen = j - i + 1; bestPrime = s }
        }
    }
    return bestPrime
}

fun main() {
    println(solveBruteForce())
}
