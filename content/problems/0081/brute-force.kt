/**
 * Project Euler 081 — Path Sum: Two Ways
 *
 * 暴力解：不做拓扑序分析，把网格当成一般带权图，用 Bellman–Ford 反复全图松弛：
 * 每轮遍历所有格子，用「上/左」邻居的最优值更新自己，跑满 V−1 = n²−1 轮。
 * 需从题目目录运行（读取 matrix.txt）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.io.File

fun readMatrix81(path: String): Array<IntArray> =
    File(path).readLines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

fun solveBruteForce(path: String = "matrix.txt"): Long {
    val g = readMatrix81(path)
    val n = g.size
    val inf = Long.MAX_VALUE / 4
    val best = Array(n) { LongArray(n) { inf } }
    best[0][0] = g[0][0].toLong()
    repeat(n * n - 1) {
        for (r in 0 until n) {
            for (c in 0 until n) {
                val cur = best[r][c]
                if (cur >= inf) continue
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
    return best[n - 1][n - 1]
}

fun main() {
    println(solveBruteForce())
}
