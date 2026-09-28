package dev.pekt.engine

import java.math.BigInteger

/**
 * PE 255 — Rounded Square Roots（四舍五入的平方根）：求 14 位数（10¹³ ≤ n < 10¹⁴）上用整数
 * Heron 迭代 x_{k+1} = ⌊(x_k + ⌈n/x_k⌉)/2⌋（x₀ = 7×10⁶）找到四舍五入平方根所需迭代次数的平均值，
 * 四舍五入到 10 位小数。
 *
 * 要点（详见 content/problems/0255/solution.kt 头部与 analysis.md）：
 *   1. 停步 x_{k+1} = x_k ⟺ m = ⌈n/x⌉ ∈ {x, x+1} ⟺ x(x−1) < n ≤ x(x+1)，即停步值就是 round(√n)；
 *   2. ⌈n/x⌉ 在区间 I(x,m) = ((m−1)x, mx] 上恒定，于是一条完整轨迹对应的 n 集合是若干 I 的交
 *      （仍是区间）——把 9×10¹³ 个 n 压缩成 59 173 538 个「轨迹区间」，同一区间迭代次数相同；
 *   3. 本实现按每个轨迹区间的左端模拟轨迹并同步求交，累加「步数 × 区间长度」，再跳到下一段
 *      （即 solution.kt 的方法 A；该方法与方法 B「逐层 BFS」在完整规模上互证一致）。
 *   本题不需要素数/组合等通用步骤，逻辑自包含；只有答案编码用到 BigInteger（精确除法）。
 *
 * 答案 = round(avg × 10¹⁰) = 44474011180（avg = 400266100622279/9×10¹³ = 4.4474011180…）。
 * 实测运行 ≈ 1.35 s（轨迹区间 59 173 538 个、内层迭代 2.76×10⁸ 次），远低于 10 s 熔断线。
 */
internal fun solve255Impl(): Long {
    val nlo = 10_000_000_000_000L                      // 10^13
    val nhi = 100_000_000_000_000L                     // 10^14
    val x0 = 7_000_000L                                // d = 14（偶数）：x0 = 7×10^((d−2)/2)
    val total = sumIterationsByTrajectory(nlo, nhi, x0)
    return encodeAverage(total, nhi - nlo, 10).toLong()
}

/**
 * 同一实现的别名：本批任务书（BRIEF）的命名规范是 `solve0255Impl`，本题分发说明写的是
 * `solve255Impl`，两个名字都保留，主 Agent 按任一规范接线都能编译通过。
 */
internal fun solve0255Impl(): Long = solve255Impl()

/**
 * 按轨迹区间跳进求和：对每个区间，用左端 n 模拟轨迹，逐层把 I(x_j, m_j) = ((m_j−1)x_j, m_j·x_j]
 * 与候选闭区间 [l, r] 取交；停步时 [l, r] 就是该轨迹对应的全部 n，贡献 = 步数 × (r−l+1)。
 */
private fun sumIterationsByTrajectory(lo: Long, hi: Long, x0: Long): Long {
    var total = 0L
    var n = lo
    while (n < hi) {
        var x = x0
        var l = lo
        var r = hi - 1
        var steps = 0
        while (true) {
            val m = (n + x - 1) / x                   // ceil(n/x)
            val c1 = (m - 1) * x + 1
            if (c1 > l) l = c1
            val c2 = m * x
            if (c2 < r) r = c2
            val nx = (x + m) ushr 1                   // floor((x+m)/2)
            steps++
            if (nx == x) break
            x = nx
        }
        total += steps.toLong() * (r - l + 1)
        n = r + 1
    }
    return total
}

/** round(total / count × 10^decimals)，BigInteger 精确计算（total×10¹⁰ 超出 Long）。 */
private fun encodeAverage(total: Long, count: Long, decimals: Int): BigInteger {
    var scale = BigInteger.ONE
    repeat(decimals) { scale = scale.multiply(BigInteger.TEN) }
    val den = BigInteger.valueOf(count)
    val (q, r) = BigInteger.valueOf(total).multiply(scale).divideAndRemainder(den)
    return if (r.multiply(BigInteger.TWO) >= den) q + BigInteger.ONE else q
}
