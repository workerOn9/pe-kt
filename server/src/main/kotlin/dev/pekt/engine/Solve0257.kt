package dev.pekt.engine

import dev.pekt.math.gcd

/**
 * PE 257 — 角平分线（Angular Bisectors）：统计周长 ≤ 10⁸ 且 area(ABC)/area(AEG) 为整数的整边三角形个数。
 *
 * 推导（详见 content/problems/0257/solution.kt 头部与 analysis.md）：
 *   角平分线定理给出 AE = cb/(a+b)、AG = bc/(a+c)，角 A 为公共角，所以
 *       R = area(ABC)/area(AEG) = (a+b)(a+c)/(bc)。
 *   由 a ≤ b、a ≤ c 得 1 < R ≤ 4，故整数 R ∈ {2,3,4}：
 *     · R = 4 ⇔ a = b = c，即全部等边三角形，共 ⌊N/3⌋ 个；
 *     · R = 2 ⇔ (a+b)(a+c) = 2bc；R = 3 ⇔ (a+b)(a+c) = 3bc。
 *
 *   令 x = b/a = p/q（最简分数）、y = c/a = (1+x)/((R−1)x−1)；写 p = (R−1)q + m，
 *   则 a₀ = lcm(q, Q)（Q 为 y 约分后的分母）、L = a₀(1 + p/q + P/Q) 是族内最小周长，
 *   每对 (p,q) 贡献 ⌊N/L⌋ 个三角形（族内 a = a₀t）。闭式：
 *     · R = 2：g = gcd(2q, q+m) ∈ {1,2}，a₀ = q(q+m)/g，L = (2q+m)(3q+m)/g；
 *     · R = 3：g = gcd(2q+m, q+2m) ∈ {1,3}，d = gcd(q, Q)（q 偶时为 2，否则 1），
 *              a₀ = q(q+2m)/(gd)，L = 2(q+m)(2q+m)/(gd)。
 *   条件 2 < x ≤ 1+√2（R=2）与 1 < x ≤ (1+√3)/2（R=3）分别等价于
 *   m²+2qm−q² ≤ 0 与 2m²+2qm−q² ≤ 0（m 单调失效，可作循环终止）。
 *
 * 复杂度：≈ 4.8×10⁷ 次 gcd 级运算，实测约 1.6 s（熔断线 10 s）。
 * 答案 = 139012411（与 content/problems/0257/solution.kt 的两条独立路径一致）。
 */
internal fun solve0257Impl(): Long {
    val n = 100_000_000L
    var total = n / 3                                     // R = 4：全部等边三角形
    total += count257R2(n)
    total += count257R3(n)
    return total
}

/** ⌊√n⌋（整数精确）。 */
private fun isqrt257(n: Long): Long {
    var x = Math.sqrt(n.toDouble()).toLong()
    while (x > 0L && x * x > n) x--
    while ((x + 1) * (x + 1) <= n) x++
    return x
}

/** R = 2 分支：(a+b)(a+c) = 2bc，即 uv = 2a²（u = b−a、v = c−a）。 */
private fun count257R2(limit: Long): Long {
    var total = 0L
    val qMax = isqrt257(limit) + 1                        // L > q²
    var q = 1L
    while (q <= qMax) {
        var m = 1L
        while (m * m + 2 * m * q - q * q <= 0L) {         // 2 < x ≤ 1+√2
            if (gcd(m, q) == 1L) {                        // gcd(p,q) = gcd((R−1)q+m, q) = gcd(m,q)
                val g = if (((q + m) and 1L) == 0L) 2L else 1L
                val len = (2 * q + m) * (3 * q + m) / g
                if (len <= limit) total += limit / len
            }
            m++
        }
        q++
    }
    return total
}

/** R = 3 分支：(a+b)(a+c) = 3bc，即 w² = u²+10uv+v²。 */
private fun count257R3(limit: Long): Long {
    var total = 0L
    val qMax = isqrt257(limit * 3 / 2) + 1               // L > 2q²/3
    var q = 1L
    while (q <= qMax) {
        var m = 1L
        while (2 * m * m + 2 * m * q - q * q <= 0L) {     // 1 < x ≤ (1+√3)/2
            if (gcd(m, q) == 1L) {
                val g = if ((2 * q + m) % 3L == 0L) 3L else 1L
                val d = if ((q and 1L) == 0L) 2L else 1L
                val len = 2 * (q + m) * (2 * q + m) / (g * d)
                if (len <= limit) total += limit / len
            }
            m++
        }
        q++
    }
    return total
}
