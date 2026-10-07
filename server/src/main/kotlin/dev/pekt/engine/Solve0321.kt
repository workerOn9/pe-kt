package dev.pekt.engine

/**
 * PE 321 — Swapping Counters（交换棋子）
 *
 * 2n+1 方格，n 红 n 蓝分列两端，中间空格。M(n) = 完全对调的最少步数 = n² + 2n。
 * M(n) 为三角形数 ⟺ (2k+1)² − 8(n+1)² = −7，佩尔方程 x² − 8y² = −7。
 * 两条轨道：(5,2) → n = 1,10,63,...；(1,1) → n = 0,3,22,...（n=0 舍去）。
 * 合并排序取前 40 项求和。
 * 最终答案：2470433131948040。
 */
internal fun solve0321Impl(): Long {
    val count = 40
    var xa = 5L; var ya = 2L   // 轨道 A：n = 1, 10, 63, ...
    var xb = 11L; var yb = 4L  // 轨道 B：n = 3, 22, 133, ...
    var sum = 0L
    var added = 0
    while (added < count) {
        val na = ya - 1
        val nb = yb - 1
        if (na <= nb) {
            sum += na
            val nx = 3 * xa + 8 * ya
            val ny = xa + 3 * ya
            xa = nx; ya = ny
        } else {
            sum += nb
            val nx = 3 * xb + 8 * yb
            val ny = xb + 3 * yb
            xb = nx; yb = ny
        }
        added++
    }
    return sum
}
