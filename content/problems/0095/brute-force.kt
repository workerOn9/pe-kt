/**
 * Project Euler 095 — Amicable Chains
 *
 * 暴力解：不预先筛真因数和，对每个数按需用试除法算 σ(n)（顺便缓存结果），
 * 然后从 1 到 10^6 逐个起点走链，只有当链回到起点本身时才认定找到了一个环，
 * 记录环长与该起点。因为环上每个元素走一圈长度相同，取「长度严格更大」即可留下最小起点。
 *
 * 复杂度：时间 O(N·√N + Σ 链长)，N = 10^6，空间 O(N)
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val limit = 1_000_000
    val cache = IntArray(limit + 1) { -1 }

    fun sigmaOf(n: Int): Int {
        if (n <= 1) return 0
        if (cache[n] >= 0) return cache[n]
        var s = 1
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) {
                s += d
                val o = n / d
                if (o != d) s += o
            }
            d++
        }
        cache[n] = s
        return s
    }

    var bestLen = 0
    var bestMin = 0
    for (start in 1..limit) {
        var cur = start
        var len = 0
        while (true) {
            cur = sigmaOf(cur)
            if (cur < 1 || cur > limit || len > limit) break
            len++
            if (cur == start) {
                if (len > bestLen) {
                    bestLen = len
                    bestMin = start
                }
                break
            }
        }
    }
    return bestMin.toLong()
}

fun main() {
    println(solveBruteForce())
}
