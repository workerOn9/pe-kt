/**
 * Project Euler 154 — Exploring Pascal's Pyramid（探索帕斯卡金字塔）· 暴力对照解
 *
 * 思路：暴力方法——对小规模 n 用动态规划生成 Pascal 金字塔，逐位取模统计。
 * 仅用于验证 Lucas 定理推广的正确性（n≤20）。
 */

fun solveBruteForce(n: Int = 10): Long {
    // Pascal's pyramid: level k has C(k+2, 2) coefficients
    // Build level by level
    val pyramid = Array(n + 1) { Array(n + 1) { LongArray(n + 1, 0) } }
    pyramid[0][0][0] = 1
    
    for (k in 1..n) {
        for (i in 0..k) {
            for (j in 0 until k - i + 1) {
                val l = k - i - j
                if (i > 0) pyramid[k][i][j] += pyramid[k-1][i-1][j]
                if (j > 0) pyramid[k][i][j] += pyramid[k-1][i][j-1]
                if (l > 0) pyramid[k][i][j] += pyramid[k-1][i][j]
            }
        }
    }
    
    // Count divisible by p=7
    val p = 7
    var count = 0L
    for (i in 0..n) {
        for (j in 0 until n - i + 1) {
            if (pyramid[n][i][j] % p == 0) count++
        }
    }
    
    return count
}

fun main() {
    println("Brute force for n=10, p=7: ${solveBruteForce(10)}")
}
