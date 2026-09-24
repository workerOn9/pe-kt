#!/usr/bin/env kotlin
// PE 213 — Flea Circus（跳蚤马戏团）
// 思路：跳蚤彼此独立，各自的落点是一条 900 个状态的无向图随机游走（30x30 网格，
//       每步以 1/deg 的概率移向某个相邻格）。对每个起点 s 迭代 50 步得到分布
//       p_50^{(s)}；格子 t 为空置的概率是各跳蚤都不在其上的概率之积
//       P(t 空) = Π_s (1 - p_50^{(s)}[t])，期望空格数 = Σ_t P(t 空)。
//       答案要求六位小数，故返回 round(期望 × 10^6) 作为 Long。
// 复杂度：O(50 · C · 4C) = O(50 · 900 · 3600)，C = 900 个格子、约 1740 条边。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

private const val SIDE = 30
private const val CELLS = SIDE * SIDE
private const val RINGS = 50

private fun solve213(): Long {
    val deg = IntArray(CELLS)
    val nbr = Array(CELLS) { IntArray(4) }
    for (i in 0 until SIDE) {
        for (j in 0 until SIDE) {
            val c = i * SIDE + j
            var dcount = 0
            if (i > 0) nbr[c][dcount++] = c - SIDE
            if (i < SIDE - 1) nbr[c][dcount++] = c + SIDE
            if (j > 0) nbr[c][dcount++] = c - 1
            if (j < SIDE - 1) nbr[c][dcount++] = c + 1
            deg[c] = dcount
        }
    }

    // empty[t] 逐步乘上「每只跳蚤都不在 t」的概率
    val empty = DoubleArray(CELLS) { 1.0 }
    var p = DoubleArray(CELLS)
    var q = DoubleArray(CELLS)
    for (start in 0 until CELLS) {
        java.util.Arrays.fill(p, 0.0)
        p[start] = 1.0
        repeat(RINGS) {
            java.util.Arrays.fill(q, 0.0)
            for (c in 0 until CELLS) {
                val x = p[c]
                if (x == 0.0) continue
                val share = x / deg[c]
                val nb = nbr[c]
                for (k in 0 until deg[c]) q[nb[k]] += share
            }
            val swap = p; p = q; q = swap
        }
        for (t in 0 until CELLS) empty[t] *= 1.0 - p[t]
    }

    var expectation = 0.0
    for (t in 0 until CELLS) expectation += empty[t]
    return Math.round(expectation * 1_000_000.0)
}

fun main() {
    println(solve213())
}
