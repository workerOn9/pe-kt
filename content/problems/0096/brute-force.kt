/**
 * Project Euler 096 — Su Doku（暴力解）
 *
 * 思路：不用掩码也不做启发式。按格子编号 0..80 顺序递归，遇到空格依次试 1..9，
 * 每次都用行/列/宫的线性扫描判断合法性，第一个走通的完整填法即为解。
 * 需从题目目录运行（读取同目录的 sudoku.txt）。
 * 复杂度：O(9^空位数 × 27) 最坏，靠唯一解与顺序填格剪枝。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private fun naiveValid(g: IntArray, pos: Int, d: Int): Boolean {
    val r = pos / 9
    val c = pos % 9
    for (k in 0 until 9) {
        if (g[r * 9 + k] == d) return false
        if (g[k * 9 + c] == d) return false
    }
    val br = r / 3 * 3
    val bc = c / 3 * 3
    for (i in 0 until 3) for (j in 0 until 3) if (g[(br + i) * 9 + bc + j] == d) return false
    return true
}

private fun fill(g: IntArray, pos: Int): Boolean {
    if (pos == 81) return true
    if (g[pos] != 0) return fill(g, pos + 1)
    for (d in 1..9) {
        if (!naiveValid(g, pos, d)) continue
        g[pos] = d
        if (fill(g, pos + 1)) return true
        g[pos] = 0
    }
    return false
}

fun solveBruteForce(path: String = "sudoku.txt"): Long {
    val lines = java.io.File(path).readLines().map { it.trim() }.filter { it.isNotEmpty() }
    var sum = 0L
    var i = 0
    while (i < lines.size) {
        if (lines[i].startsWith("Grid")) {
            i++
            continue
        }
        val g = IntArray(81)
        for (r in 0 until 9) {
            val row = lines[i + r]
            for (c in 0 until 9) g[r * 9 + c] = row[c] - '0'
        }
        i += 9
        if (!fill(g, 0)) error("unsolvable grid")
        sum += g[0] * 100 + g[1] * 10 + g[2]
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
