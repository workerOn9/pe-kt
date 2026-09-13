/**
 * Project Euler 047 — 暴力解（教学对比用）
 *
 * 不建 SPF 表，每个数都用试除法现场分解质因数，重复做大量除法。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun distinctPrimeFactors(n0: Long): List<Long> {
    var n = n0; val out = ArrayList<Long>(); var d = 2L
    while (d * d <= n) {
        if (n % d == 0L) { out.add(d); while (n % d == 0L) n /= d }
        d++
    }
    if (n > 1) out.add(n)
    return out
}

fun solveBruteForce(): Long {
    var run = 0; var n = 2
    while (true) {
        if (distinctPrimeFactors(n.toLong()).size == 4) { run++; if (run == 4) return (n - 3).toLong() }
        else run = 0
        n++
    }
}

fun main() {
    println(solveBruteForce())
}
