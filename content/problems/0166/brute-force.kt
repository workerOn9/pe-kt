/**
 * PE 166 — brute force：按行回溯，第一行确定 S 后逐格剪枝。
 * 这里实现单类 S=20 的计数（保持与 analysis.md 数据一致），供耗时对比。
 */
import kotlin.math.max
import kotlin.math.min

fun bruteForceForSum(S: Int): Long {
    val g = IntArray(16)
    var count = 0L

    fun rec(p: Int) {
        val r = p / 4
        val c = p % 4
        for (v in 0..9) {
            g[p] = v
            // 行剪枝
            val rowPartial = (0..c).sumOf { g[4 * r + it] }
            val rowMax = rowPartial + 9 * (3 - c)
            if (c == 3) { if (rowPartial != S) continue }
            else if (rowPartial > S || rowMax < S) continue
            // 列剪枝
            val colPartial = (0..r).sumOf { g[4 * it + c] }
            if (r == 3) { if (colPartial != S) continue }
            else if (colPartial > S || colPartial + 9 * (3 - r) < S) continue
            // 主对角线 (r == c)
            if (r == c && r > 0) {
                val d0 = (0..r).sumOf { g[4 * it + it] }
                if (r == 3) { if (d0 != S) continue }
                else if (d0 > S || d0 + 9 * (3 - r) < S) continue
            }
            // 次对角线 (r + c == 3)
            if (r + c == 3 && r > 0) {
                val d1 = (0..r).sumOf { g[4 * it + (3 - it)] }
                if (r == 3) { if (d1 != S) continue }
                else if (d1 > S || d1 + 9 * (3 - r) < S) continue
            }
            if (p == 15) count++
            else rec(p + 1)
        }
        g[p] = 0
    }
    rec(0)
    return count
}

fun main() {
    println(bruteForceForSum(20))
}
