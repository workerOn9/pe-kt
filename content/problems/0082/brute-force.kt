/**
 * Project Euler 082 — Path Sum: Three Ways
 *
 * 暴力解：不对列结构做任何利用，把网格视为一般带权有向图（允许向上、向下、向右），
 * 以最左列所有格为多源起点做 Bellman–Ford 全图松弛，跑满 V−1 = n²−1 轮，
 * 最后取最右列的最小值。
 * 需从题目目录运行（读取 matrix.txt）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.io.File

fun readMatrix82(path: String): Array<IntArray> =
    File(path).readLines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

fun solveBruteForce(path: String = "matrix.txt"): Long {
    val g = readMatrix82(path)
    val n = g.size
    val inf = Long.MAX_VALUE / 4
    val best = Array(n) { LongArray(n) { inf } }
    for (r in 0 until n) best[r][0] = g[r][0].toLong()
    repeat(n * n - 1) {
        for (r in 0 until n) {
            for (c in 0 until n) {
                val cur = best[r][c]
                if (cur >= inf) continue
                if (r > 0) {
                    val v = cur + g[r - 1][c]
                    if (v < best[r - 1][c]) best[r - 1][c] = v
                }
                if (r + 1 < n) {
                    val v = cur + g[r + 1][c]
                    if (v < best[r + 1][c]) best[r + 1][c] = v
                }
                if (c + 1 < n) {
                    val v = cur + g[r][c + 1]
                    if (v < best[r][c + 1]) best[r][c + 1] = v
                }
            }
        }
    }
    var ans = inf
    for (r in 0 until n) if (best[r][n - 1] < ans) ans = best[r][n - 1]
    return ans
}

fun main() {
    println(solveBruteForce())
}
