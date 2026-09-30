package dev.pekt.engine

import kotlin.math.asin
import kotlin.math.sqrt

/**
 * PE 285 — Pythagorean Odds（勾股概率）：每轮取正整数 k 与均匀独立的 a, b ∈ [0,1]，
 * 四舍五入 √((ka+1)²+(kb+1)²)，等于 k 就得 k 分。求 k = 1..10^5 的总得分期望（题面要求
 * 5 位小数）。按项目惯例把小数答案编码为整数 round(期望 × 10^5) = 15705580999。
 *
 * 推导（详见 content/problems/0285/solution.kt 头部与 0285/analysis.md）：
 *   令 u = ka+1、v = kb+1，则 (u,v) 在 [1,k+1]² 上均匀，第 k 轮得分 ⟺
 *   (k−1/2)² ≤ u²+v² < (k+1/2)²。圆盘够不到正方形的上/右边界（(k+1)² − (k±1/2)² = k+3/4 > 0），
 *   记 Q(R) = area{u ≥ 1, v ≥ 1, u²+v² ≤ R²} = πR²/4 + 1 − √(R²−1) − R²·asin(1/R)（R ≥ √2），
 *   则 k·P(k) = [Q(k+1/2) − Q(k−1/2)]/k，
 *     E(N) = Σ_{k≤N} [Q(k+1/2) − Q(k−1/2)]/k。
 *   逐项用 π/2 − 2/(√(Ro²−1)+√(Ri²−1)) − [g(Ro) − g(Ri)]/k 的稳定形式（g(R) = R² asin(1/R)），
 *   避免两个 ~k² 量级的 Q 相减；k = 1 用 Q(1.5)（Q(0.5) = 0）。
 *
 * 复核路径：分部求和 E = Σ_{k<N} Q(k+1/2)/(k(k+1)) + Q(N+1/2)/N（另一条求和结构），两条路径
 *   在函数内互检（差值 < 1e-6）；solution.kt 另有 40 位 BigDecimal 路径与复合 Simpson 数值积分
 *   路径给出同一值 157055.80998672744…，暴力积分路径见 content/problems/0285/brute-force.kt。
 *
 * 复杂度：O(N)，每项 2 次 sqrt、1 次 asin 与常数次四则运算；N = 10^5 时约 2 ms（两条路径合计），
 *   远低于 10 s 熔断线。真值离 5 位小数分界点 157055.809985 有 1.7×10^-6 的余量，而两条
 *   路径的偏差在 10^-10 量级，取整安全。
 */
internal fun solve0285Impl(): Long {
    val n = 100_000
    val scale = 100_000L

    fun q(r: Double): Double =
        if (r * r < 2.0) 0.0
        else Math.PI * r * r / 4.0 + 1.0 - sqrt(r * r - 1.0) - r * r * asin(1.0 / r)

    fun g(r: Double): Double = r * r * asin(1.0 / r)

    // 主路径：稳定形式 + Kahan 累加
    var sum = q(1.5)
    var c = 0.0
    for (k in 2..n) {
        val ro = k + 0.5
        val ri = k - 0.5
        val term = Math.PI / 2.0 -
            2.0 / (sqrt(ro * ro - 1.0) + sqrt(ri * ri - 1.0)) -
            (g(ro) - g(ri)) / k
        val y = term - c
        val t = sum + y
        c = (t - sum) - y
        sum = t
    }

    // 复核：分部求和 E = Σ_{k<N} Q(k+1/2)/(k(k+1)) + Q(N+1/2)/N
    var alt = q(n + 0.5) / n
    var c2 = 0.0
    for (k in 1 until n) {
        val term = q(k + 0.5) / (k.toDouble() * (k + 1))
        val y = term - c2
        val t = alt + y
        c2 = (t - alt) - y
        alt = t
    }
    check(kotlin.math.abs(sum - alt) < 1e-6) { "主路径 $sum 与分部求和 $alt 不一致" }

    return Math.round(sum * scale)
}
