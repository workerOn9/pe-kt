/**
 * Project Euler 085 — Counting Rectangles（暴力对照解）
 *
 * 思路：把 m、n 都在 [1, 2000] 上枚举一遍，直接按 m(m+1)n(n+1)/4 计算矩形数并取与 2·10^6 最接近者。
 * 复杂度：O(M·N) ≈ 4×10^6 次整数运算。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 注：n=2000 时矩形数约 4×10^12，仍远在 Long 范围内。
 */

private const val p085Target = 2_000_000L

fun solveBruteForce(): Long {
    var bestDiff = Long.MAX_VALUE
    var bestArea = 0L
    for (m in 1..2000) {
        for (n in 1..2000) {
            val count = m.toLong() * (m + 1) * n * (n + 1) / 4
            val diff = Math.abs(count - p085Target)
            if (diff < bestDiff) {
                bestDiff = diff
                bestArea = m.toLong() * n
            }
        }
    }
    return bestArea
}

fun main() {
    println(solveBruteForce())
}
