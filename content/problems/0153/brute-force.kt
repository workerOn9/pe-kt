/**
 * Project Euler 153 — Investigating Gaussian Integers（探究高斯整数）· 暴力对照解
 *
 * 思路：直接枚举每个 n 的所有高斯因数，计算 s(n)，然后求和。
 * 对小规模验证正确性。
 */

fun solveBruteForce(limit: Int = 100): Long {
    // 对每个 n，枚举所有 a²+b² ≤ n 的 (a,b)，检查是否整除
    var totalSum = 0L
    for (n in 1..limit) {
        val s_n = GaussianSum(n)
        totalSum += s_n
    }
    return totalSum
}

/** 计算 s(n)：n 的所有实部为正的高斯因数的实部之和。 */
fun GaussianSum(n: Int): Int {
    var sum = 0
    // 有理整数因数
    for (d in 1..n) {
        if (n % d == 0) sum += d
    }
    // 虚高斯因数 a+bi (a>0, b≠0)：a²+b² | n
    for (a in 1 until n) {
        val a2 = a * a
        if (a2 >= n) break
        for (b in 1 until n) {
            val k = a2 + b * b
            if (k > n) break
            if (n % k == 0) {
                sum += 2 * a  // a+bi 和 a-bi 都贡献 a
            }
        }
    }
    return sum
}

fun main() {
    val start = System.nanoTime()
    val answer = solveBruteForce()
    val elapsed = (System.nanoTime() - start).toDouble() / 1e6
    System.err.printf("brute: %.4f ms%n", elapsed)
    println(answer)
}
