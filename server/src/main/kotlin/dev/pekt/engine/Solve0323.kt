package dev.pekt.engine

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * PE 323 — Bitwise-OR Operations on Random Integers（随机整数的按位或）
 *
 * x₀ = 0, xᵢ = xᵢ₋₁ | yᵢ₋₁，y 为随机 32 位整数。N = 首次使 xᵢ = 2³²−1 的下标。
 * E[N] = Σ_{k≥0} P(N > k) = Σ_{k≥0} (1 − (1 − 2^{−k})³²)。
 * 级数指数收敛，求和到 k=100 保证 10 位小数精度。
 * 最终答案：6.3551758451。
 */
internal fun solve0323Impl(): String {
    var sum = 0.0
    var twoPowK = 1.0
    for (k in 0..100) {
        val oneMinus = 1.0 - 1.0 / twoPowK
        sum += 1.0 - Math.pow(oneMinus, 32.0)
        twoPowK *= 2.0
    }
    return BigDecimal.valueOf(sum).setScale(10, RoundingMode.HALF_UP).toPlainString()
}
