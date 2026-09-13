/**
 * Project Euler 096 — Su Doku
 *
 * 思路：把每行、每列、每宫的已用数字压成 9 位掩码，某个空格的候选集合就是
 * ~(row | col | box) 的置位数字；搜索时每次挑候选最少的空格（MRV）展开，
 * 一旦发现候选为 0 立即剪枝回溯。五十个盘面全部唯一解，左上角三位数求和。
 * 需从题目目录运行（读取同目录的 sudoku.txt）。
 * 复杂度：最坏 O(9^空位数)，实际在约束传播 + MRV 下只探索极少量分支（五十盘约百毫秒内）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private fun boxOf(idx: Int): Int = (idx / 27) * 3 + (idx % 9) / 3

private fun solveGrid(g: IntArray): Boolean {
    val rows = IntArray(9)
    val cols = IntArray(9)
    val boxes = IntArray(9)
    for (i in 0 until 81) {
        val d = g[i]
        if (d != 0) {
            val bit = 1 shl d
            rows[i / 9] = rows[i / 9] or bit
            cols[i % 9] = cols[i % 9] or bit
            boxes[boxOf(i)] = boxes[boxOf(i)] or bit
        }
    }
    return search(g, rows, cols, boxes)
}

private fun search(g: IntArray, rows: IntArray, cols: IntArray, boxes: IntArray): Boolean {
    var best = -1
    var bestUsed = 0
    var bestCount = 10
    for (i in 0 until 81) {
        if (g[i] != 0) continue
        val used = rows[i / 9] or cols[i % 9] or boxes[boxOf(i)]
        var cnt = 0
        for (d in 1..9) if (used and (1 shl d) == 0) cnt++
        if (cnt == 0) return false
        if (cnt < bestCount) {
            bestCount = cnt
            best = i
            bestUsed = used
            if (cnt == 1) break
        }
    }
    if (best < 0) return true
    val r = best / 9
    val c = best % 9
    val b = boxOf(best)
    for (d in 1..9) {
        val bit = 1 shl d
        if (bestUsed and bit != 0) continue
        g[best] = d
        rows[r] = rows[r] or bit
        cols[c] = cols[c] or bit
        boxes[b] = boxes[b] or bit
        if (search(g, rows, cols, boxes)) return true
        g[best] = 0
        rows[r] = rows[r] and bit.inv()
        cols[c] = cols[c] and bit.inv()
        boxes[b] = boxes[b] and bit.inv()
    }
    return false
}

fun solve(path: String = "sudoku.txt"): Long {
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
        if (!solveGrid(g)) error("unsolvable grid")
        sum += g[0] * 100 + g[1] * 10 + g[2]
    }
    return sum
}

fun main() {
    println(solve())
}
