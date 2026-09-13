/**
 * Project Euler 083 — Path Sum: Four Ways
 *
 * 优化解：四个方向都能走，状态图不再是 DAG，但边权全为正（矩阵元素），
 * 于是用 Dijkstra + 二叉堆求单源最短路，每个格子出堆一次即定型。
 * 需从题目目录运行（读取 matrix.txt）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File
import java.util.PriorityQueue

val dr83 = intArrayOf(-1, 1, 0, 0)
val dc83 = intArrayOf(0, 0, -1, 1)

fun readMatrix83(path: String): Array<IntArray> =
    File(path).readLines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

fun solve(path: String = "matrix.txt"): Long {
    val g = readMatrix83(path)
    val n = g.size
    val inf = Long.MAX_VALUE / 4
    val dist = Array(n) { LongArray(n) { inf } }
    val pq = PriorityQueue<LongArray>(compareBy { it[0] })
    dist[0][0] = g[0][0].toLong()
    pq.add(longArrayOf(dist[0][0], 0L, 0L))
    while (pq.isNotEmpty()) {
        val top = pq.poll()
        val d = top[0]
        val r = top[1].toInt()
        val c = top[2].toInt()
        if (d > dist[r][c]) continue
        if (r == n - 1 && c == n - 1) return d
        for (k in 0 until 4) {
            val nr = r + dr83[k]
            val nc = c + dc83[k]
            if (nr < 0 || nr >= n || nc < 0 || nc >= n) continue
            val nd = d + g[nr][nc]
            if (nd < dist[nr][nc]) {
                dist[nr][nc] = nd
                pq.add(longArrayOf(nd, nr.toLong(), nc.toLong()))
            }
        }
    }
    return dist[n - 1][n - 1]
}

fun main() {
    println(solve())
}
