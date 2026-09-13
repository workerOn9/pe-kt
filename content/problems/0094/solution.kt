/**
 * Project Euler 094 — Almost Equilateral Triangles
 *
 * 优化解：设两腰为 n、底为 n+1 或 n−1。
 *   底为 n+1 时面积 = (n+1)/4·√(3n²−2n−1)，需要 3n²−2n−1 为完全平方 m²；
 *   底为 n−1 时面积 = (n−1)/4·√(3n²+2n−1)，需要 3n²+2n−1 = m²。
 * 两式都等价于 Pell 型方程 (3n∓1)² − 3m² = 4：令 x = 3n+1（底 n−1）或 x = 3n−1（底 n+1），
 * 则周长恰为 x，而 x²−3y²=4 的全部正解可用递推 x_{k+1} = 4x_k − x_{k−1} 从 2, 4 生成：
 *   2, 4, 14, 52, 194, 724, 2702, …（x ≡ 2 (mod 3) 时底为 n+1，x ≡ 1 (mod 3) 时底为 n−1）。
 * 只需沿这条递推枚举到 x ≤ 10^9，并用完全平方校验兜底，n ≤ 1 的退化三角形直接跳过。
 *
 * 复杂度：时间 O(log P)（P = 10^9，序列长度约 15），空间 O(1)
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

fun solve(): Long {
    val limit = 1_000_000_000L
    var prev = 2L
    var cur = 4L
    var sum = 0L
    while (cur <= limit) {
        val x = cur
        val n = if (x % 3L == 1L) (x - 1) / 3 else (x + 1) / 3
        if (n >= 2) {
            val sq = if (x % 3L == 1L) 3 * n * n + 2 * n - 1 else 3 * n * n - 2 * n - 1
            val r = isqrt(sq)
            if (r * r == sq) sum += x
        }
        val next = 4 * cur - prev
        prev = cur
        cur = next
    }
    return sum
}

fun main() {
    println(solve())
}
