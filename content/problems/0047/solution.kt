/**
 * Project Euler 047 — Distinct Primes Factors
 *
 * 优化解：线性筛出每个数的最小质因子（SPF）表，用 SPF 反复除即可 O(log n) 数出
 * 不同质因数个数；维护连续 4 个数的计数器，命中即返回。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun distinctFactorCount(n0: Int, spf: IntArray): Int {
    var n = n0; var c = 0
    while (n > 1) { val p = spf[n]; c++; while (n % p == 0) n /= p }
    return c
}

fun solve(): Long {
    val N = 1_000_000
    val spf = IntArray(N + 1)
    for (i in 2..N) if (spf[i] == 0) { var m = i; while (m <= N) { if (spf[m] == 0) spf[m] = i; m += i } }
    var run = 0; var n = 2
    while (true) {
        if (distinctFactorCount(n, spf) == 4) { run++; if (run == 4) return (n - 3).toLong() }
        else run = 0
        n++
    }
}

fun main() {
    println(solve())
}
