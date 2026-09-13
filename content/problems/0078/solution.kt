/**
 * Project Euler 078 — Coin Partitions
 *
 * 思路：p(n) 是分拆数，题目要最小的 n 使 p(n) ≡ 0 (mod 10^6)。
 * 不用硬币 DP（O(n^2) 且数值爆炸），改用欧拉五边形数定理给出的递推：
 *   p(n) = Σ_{k≠0} (−1)^{k+1} · p(n − g_k),  g_k = k(3k−1)/2, k = 1, −1, 2, −2, …
 * 递推式的系数只有 ±1，所以只需保留 p(n) mod 10^6 即可判断整除性，
 * 状态空间压到 Int，且求和项只有 O(√n) 个，整体 O(n√n)。
 * 从 n = 1 起递增扫描，第一个模 10^6 为 0 的 n 即答案。
 * 复杂度：O(n√n) 时间，O(n) 空间（n ≈ 5.5×10^4）。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val mod = 1_000_000L
    val limit = 100_000
    val p = IntArray(limit + 1)
    p[0] = 1

    for (n in 1..limit) {
        var total = 0L
        var k = 1
        while (true) {
            val g1 = k * (3 * k - 1) / 2
            if (g1 > n) break
            val sign = if (k % 2 == 1) 1L else -1L
            total += sign * p[n - g1]
            val g2 = k * (3 * k + 1) / 2
            if (g2 <= n) total += sign * p[n - g2]
            k++
        }
        val value = ((total % mod) + mod) % mod
        p[n] = value.toInt()
        if (value == 0L) return n.toLong()
    }
    error("limit $limit too small")
}

fun main() {
    println(solve())
}
