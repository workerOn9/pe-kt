/**
 * Project Euler 027 — 暴力解（教学对比用）
 *
 * 完全不预筛，每个候选值都用试除法现场判定素性，重复判定次数远高于筛法版本。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) { if (n % d == 0L) return false; d += 2 }
    return true
}

fun solveBruteForce(): Long {
    var bestA = 0; var bestB = 0; var bestN = 0
    for (a in -999..999) {
        for (b in 2..1000) {
            if (!isPrimeLong(b.toLong())) continue
            var n = 0
            while (isPrimeLong((n * n + a * n + b).toLong())) n++
            if (n > bestN) { bestN = n; bestA = a; bestB = b }
        }
    }
    return (bestA * bestB).toLong()
}

fun main() {
    println(solveBruteForce())
}
