/**
 * Project Euler 083 — Path Sum: Four Ways
 *
 * 暴力解：用 Bellman–Ford 全图松弛代替 Dijkstra——不做「边权非负 ⟹ 出堆即定型」的推理，
 * 每轮遍历所有格子并向四个邻居松弛，跑满 V−1 = n²−1 轮。
 * 需从题目目录运行（读取 matrix.txt）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.io.File

fun readMatrix83(path: String): Array<IntArray> =
    File(path).readLines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

fun solveBruteForce(path: String = "matrix.txt"): Long {
    val g = readMatrix83(path)
    val n = g.size
    val inf = Long.MAX_VALUE / 4
    val best = Array(n) { LongArray(n) { inf } }
    best[0][0] = g[0][0].toLong()
    val dr = intArrayOf(-1, 1, 0, 0)
    val dc = intArrayOf(0, 0, -1, 1)
    repeat(n * n - 1) {
        for (r in 0 until n) {
            for (c in 0 until n) {
                val cur = best[r][c]
                if (cur >= inf) continue
                for (k in 0 until 4) {
                    val nr = r + dr[k]
                    val nc = c + dc[k]
                    if (nr < 0 || nr >= n || nc < 0 || nc >= n) continue
                    val v = cur + g[nr][nc]
                    if (v < best[nr][nc]) best[nr][nc] = v
                }
            }
        }
    }
    return best[n - 1][n - 1]
}

fun main() {
    println(solveBruteForce())
}
