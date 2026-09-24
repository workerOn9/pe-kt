#!/usr/bin/env kotlin
// PE 215 — Crack-free Walls（无通缝的墙）暴力参照
// 思路：不做 DP，直接深度优先枚举「层序列」，逐层检查与上一层的缝隙掩码是否相交。
//       枚举量是 R^LAYERS，只适用于宽度与层数都很小的情况；用于验证 DP 的状态转移
//       与「缝隙掩码按位与为 0」这一相容判据（题面给了 W(9,3) = 8 作为锚点）。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

private fun buildRows(width: Int): IntArray {
    val rows = ArrayList<Int>()
    fun rec(pos: Int, mask: Int) {
        if (pos == width) {
            rows.add(mask)
            return
        }
        for (len in intArrayOf(2, 3)) {
            val next = pos + len
            if (next > width) continue
            rec(next, if (next < width) mask or (1 shl next) else mask)
        }
    }
    rec(0, 0)
    return rows.toIntArray()
}

private fun bruteForce(width: Int, layers: Int): Long {
    val rows = buildRows(width)
    var total = 0L
    fun dfs(layer: Int, previous: Int) {
        if (layer == layers) {
            total++
            return
        }
        for (mask in rows) {
            if (previous == -1 || mask and previous == 0) dfs(layer + 1, mask)
        }
    }
    dfs(0, -1)
    return total
}

fun main() {
    for (spec in listOf(intArrayOf(9, 3), intArrayOf(12, 3), intArrayOf(12, 4), intArrayOf(15, 4))) {
        println("W(${spec[0]},${spec[1]}) = ${bruteForce(spec[0], spec[1])}")
    }
}
