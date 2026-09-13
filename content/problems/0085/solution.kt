/**
 * Project Euler 085 — Counting Rectangles
 *
 * 思路：m×n 网格中矩形总数为“横向线段对 × 纵向线段对”= C(m+1,2)·C(n+1,2) = m(m+1)n(n+1)/4。
 * 固定 m 后目标化为解一元二次方程 n(n+1) = 4T/(m(m+1))，直接取根的整数近似再校验相邻候选，
 * 免去对 n 的枚举；由于 m(m+1)/2 > T 之后矩形数必随 m 单调增大，m 只需枚举到约 2000。
 * 复杂度：O(M)，M ≈ 2000（每个 m 只检查 5 个候选 n）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private const val p085Target = 2_000_000L

private fun p085RectCount(m: Long, n: Long): Long = m * (m + 1) * n * (n + 1) / 4

fun solve(): Long {
    var bestDiff = Long.MAX_VALUE
    var bestArea = 0L
    var m = 1L
    while (m * (m + 1) / 2 <= p085Target) {
        val need = 4.0 * p085Target / (m * (m + 1))
        val n0 = ((Math.sqrt(1.0 + 4.0 * need) - 1.0) / 2.0).toLong()
        var n = maxOf(1L, n0 - 2)
        while (n <= n0 + 2) {
            val diff = Math.abs(p085RectCount(m, n) - p085Target)
            if (diff < bestDiff) {
                bestDiff = diff
                bestArea = m * n
            }
            n++
        }
        m++
    }
    return bestArea
}

fun main() {
    println(solve())
}
