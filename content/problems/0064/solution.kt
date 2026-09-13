/**
 * Project Euler 064 — Odd Period Square Roots
 *
 * 思路：√N 的连分数可以用纯整数递推展开（不必碰浮点）：
 *   m₀ = 0, d₀ = 1, a₀ = ⌊√N⌋
 *   mₖ₊₁ = dₖ·aₖ − mₖ，dₖ₊₁ = (N − mₖ₊₁²)/dₖ，aₖ₊₁ = ⌊(a₀ + mₖ₊₁)/dₖ₊₁⌋
 * 完全平方数的连分数有限（周期 0），跳过；其余在再次出现 aₖ = 2a₀ 时闭合周期，
 * 统计周期长度的奇偶即可。
 * 复杂度：O(#N · L)，L 为周期长度（N ≤ 10⁴ 时 L < 2√N ≤ 200）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isqrt(n: Int): Int {
    var r = Math.sqrt(n.toDouble()).toInt()
    while (r.toLong() * r > n) r--
    while ((r + 1).toLong() * (r + 1) <= n) r++
    return r
}

fun solve(): Long {
    var count = 0L
    for (n in 2..10000) {
        val a0 = isqrt(n)
        if (a0 * a0 == n) continue
        var m = 0
        var d = 1
        var a = a0
        var period = 0
        while (a != 2 * a0) {
            m = d * a - m
            d = (n - m * m) / d
            a = (a0 + m) / d
            period++
        }
        if (period and 1 == 1) count++
    }
    return count
}

fun main() {
    println(solve())
}
