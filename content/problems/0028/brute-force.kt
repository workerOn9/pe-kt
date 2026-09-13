/**
 * Project Euler 028 — 暴力解（教学对比用）
 *
 * 按题目描述逐格生成 1001×1001 的螺旋矩阵，再遍历两条对角线求和。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun solveBruteForce(): Long {
    val n = 1001
    val g = Array(n) { IntArray(n) }
    var r = n / 2; var c = n / 2
    var v = 1
    g[r][c] = 1
    val dr = intArrayOf(0, -1, 0, 1)
    val dc = intArrayOf(1, 0, -1, 0)
    var dir = 0
    var len = 1
    while (v < n * n) {
        repeat(2) {
            repeat(len) { if (v < n * n) { r += dr[dir]; c += dc[dir]; g[r][c] = ++v } }
            dir = (dir + 1) % 4
        }
        len++
    }
    var s = 0L
    for (i in 0 until n) s += g[i][i] + g[i][n - 1 - i]
    return s - 1
}

fun main() {
    println(solveBruteForce())
}
