/**
 * Project Euler 072 — Counting Fractions（暴力对照解）
 *
 * 暴力解：不用筛法，对每个 d 用试除法现场分解质因数，再按
 * φ(d) = d · Π_{p|d} (1 − 1/p) 计算，逐个累加。这是最贴近定义的写法：
 * 每个 d 独立求解，不复用任何其它 d 的信息。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

fun solveBruteForce(): Long {
    val n = 1_000_000
    var total = 0L
    for (d in 2..n) {
        var x = d
        var phi = d
        var p = 2
        while (p.toLong() * p <= x) {
            if (x % p == 0) {
                while (x % p == 0) x /= p
                phi -= phi / p
            }
            p++
        }
        if (x > 1) phi -= phi / x
        total += phi
    }
    return total
}

fun main() {
    println(solveBruteForce())
}
