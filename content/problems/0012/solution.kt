/**
 * Project Euler 012 — Highly Divisible Triangular Number
 *
 * 优化解：利用三角数的互素分解，O(n log log n)。
 * t_n = n(n+1)/2，且 gcd(n, n+1) = 1，所以两个因子除掉唯一的 2 之后仍然互素：
 *   n 偶：d(t_n) = d(n/2) * d(n+1)
 *   n 奇：d(t_n) = d(n) * d((n+1)/2)
 * 其中 d() 是因子个数函数。只需对 n 和 n+1 级别（约 1.2 万）的数分解质因数，
 * 用最小质因子筛（SPF）把每次分解降到 O(log n)。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private const val SPF_LIMIT = 1_000_000

/** 最小质因子表：spf[x] = x 的最小质因子（x >= 2） */
fun buildSpf(limit: Int): IntArray {
    val spf = IntArray(limit + 1)
    for (i in 2..limit) {
        if (spf[i] == 0) {
            spf[i] = i
            if (i.toLong() * i <= limit) {
                var j = i * i
                while (j <= limit) {
                    if (spf[j] == 0) spf[j] = i
                    j += i
                }
            }
        }
    }
    return spf
}

/** 用 SPF 表求 [x] 的因子个数 */
fun divisorCount(x0: Int, spf: IntArray): Int {
    var x = x0
    var count = 1
    while (x > 1) {
        val p = spf[x]
        var e = 0
        while (x % p == 0) { x /= p; e++ }
        count *= e + 1
    }
    return count
}

fun solve(target: Int = 500): Long {
    val spf = buildSpf(SPF_LIMIT)
    var n = 1
    while (true) {
        val divisors = if (n % 2 == 0) {
            divisorCount(n / 2, spf) * divisorCount(n + 1, spf)
        } else {
            divisorCount(n, spf) * divisorCount((n + 1) / 2, spf)
        }
        if (divisors > target) return n.toLong() * (n + 1) / 2
        n++
    }
}

fun main() {
    println(solve())
}
