/**
 * Project Euler 082 — Path Sum: Three Ways
 *
 * 优化解：逐列推进。cur[r] 表示「从最左列任意格出发、终止于当前列第 r 行」的最小路径和：
 * 新列先按从上一列向右走过来求和，再在列内做一次向下、一次向上的松弛
 * （允许上下移动等价于列内取「最优折返点」），最后取末列最小值。
 * 需从题目目录运行（读取 matrix.txt）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

fun readMatrix82(path: String): Array<IntArray> =
    File(path).readLines().filter { it.isNotBlank() }
        .map { line -> line.trim().split(",").map { it.trim().toInt() }.toIntArray() }
        .toTypedArray()

fun solve(path: String = "matrix.txt"): Long {
    val g = readMatrix82(path)
    val n = g.size
    var cur = LongArray(n) { g[it][0].toLong() }
    for (c in 1 until n) {
        val next = LongArray(n) { cur[it] + g[it][c] }
        for (r in 1 until n) next[r] = minOf(next[r], next[r - 1] + g[r][c])
        for (r in n - 2 downTo 0) next[r] = minOf(next[r], next[r + 1] + g[r][c])
        cur = next
    }
    return cur.min()
}

fun main() {
    println(solve())
}
