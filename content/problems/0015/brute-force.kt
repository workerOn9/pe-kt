/**
 * Project Euler 015 — 暴力解（教学对比用）
 *
 * 动态规划：ways[r][c] = ways[r-1][c] + ways[r][c-1]，O(n²) 时间、O(n) 空间。
 * 真正的指数级暴力（无记忆递归 2^40 条路径逐一走完）在本题规模下不可行，
 * 故以 DP 作为「朴素但可行」的对比基线。
 */

import java.math.BigInteger

fun solveBruteForce(gridSize: Int = 20): BigInteger {
    val ways = Array(gridSize + 1) { BigInteger.ONE }
    for (r in 1..gridSize) {
        for (c in 1..gridSize) {
            ways[c] = ways[c] + ways[c - 1]
        }
    }
    return ways[gridSize]
}

fun main() {
    println(solveBruteForce())
}
