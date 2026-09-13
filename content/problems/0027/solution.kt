/**
 * Project Euler 027 — Quadratic Primes
 *
 * 优化解：筛法预生成 200 万以内素数表，枚举 |a|<1000、|b|≤1000 的所有组合，
 * 从 n=0 连续计数能产生素数的长度。b 必须为素数，故内层只遍历素数 b。
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
    val N = 2_000_000
    val isP = sieveBool(N)
    var bestA = 0; var bestB = 0; var bestN = 0
    for (a in -999..999) {
        for (b in 2..1000) {
            if (!isP[b]) continue
            var n = 0
            while (n * n + a * n + b > 1 && n * n + a * n + b < N && isP[n * n + a * n + b]) n++
            if (n > bestN) { bestN = n; bestA = a; bestB = b }
        }
    }
    return (bestA * bestB).toLong()
}

fun main() {
    println(solve())
}
