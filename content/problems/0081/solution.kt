/**
 * Project Euler 081 — Path Sum: Two Ways
 *
 * 优化解：只允许向右/向下 ⟹ 状态图是有向无环图，按行列的拓扑序一遍 DP：
 * dp[r][c] = a[r][c] + min(dp[r−1][c], dp[r][c−1])。
 * 需从题目目录运行（读取 matrix.txt）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

fun readMatrix81(path: String): Array<IntArray> =
    File(path).readLines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

fun solve(path: String = "matrix.txt"): Long {
    val g = readMatrix81(path)
    val n = g.size
    val dp = Array(n) { LongArray(n) }
    for (r in 0 until n) {
        for (c in 0 until n) {
            val cell = g[r][c].toLong()
            dp[r][c] = if (r == 0 && c == 0) cell else {
                val up = if (r > 0) dp[r - 1][c] else Long.MAX_VALUE
                val left = if (c > 0) dp[r][c - 1] else Long.MAX_VALUE
                cell + minOf(up, left)
            }
        }
    }
    return dp[n - 1][n - 1]
}

fun main() {
    println(solve())
}
