#!/usr/bin/env kotlin
// PE 215 — Crack-free Walls（无通缝的墙）
// 思路：先枚举宽度内所有由 2、3 号砖拼成的「层」，用位掩码记录该层的竖直缝隙位置
//       （位置 x 上有缝 ⇔ 第 x 列是两块砖的交界，墙两端不算缝）。两层能相邻
//       ⇔ 两个掩码按位与为 0（没有任何缝对齐）。之后对层数做状态 DP：
//       dp[k][j] = 前 k 层以第 j 种层收尾的砌法数，dp[1][*] = 1，
//       dp[k+1][j] = Σ_i compat(i,j) · dp[k][i]，答案为 Σ_j dp[10][j]。
// 复杂度：层数 R = 3329（宽度 32 的 2/3 拼法数），DP 为 O(LAYERS · R^2) ≈ 10^8。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

private const val WIDTH = 32
private const val LAYERS = 10

private fun buildRows(width: Int): IntArray {
    val rows = ArrayList<Int>(4096)
    fun rec(pos: Int, mask: Int) {
        if (pos == width) {
            rows.add(mask)
            return
        }
        for (len in intArrayOf(2, 3)) {
            val next = pos + len
            if (next > width) continue
            // 缝隙出现在下一块砖的起点，且只有墙内部的位置才算缝
            rec(next, if (next < width) mask or (1 shl next) else mask)
        }
    }
    rec(0, 0)
    return rows.toIntArray()
}

private fun solve215(): Long {
    val rows = buildRows(WIDTH)
    val r = rows.size
    var dp = LongArray(r) { 1L }
    var next = LongArray(r)
    repeat(LAYERS - 1) {
        java.util.Arrays.fill(next, 0L)
        for (j in 0 until r) {
            var sum = 0L
            val maskJ = rows[j]
            for (i in 0 until r) {
                if (rows[i] and maskJ == 0) sum += dp[i]
            }
            next[j] = sum
        }
        val swap = dp
        dp = next
        next = swap
    }
    var total = 0L
    for (v in dp) total += v
    return total
}

fun main() {
    println(solve215())
}
