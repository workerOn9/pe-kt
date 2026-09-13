package dev.pekt.math

import java.math.BigInteger

/**
 * 二项式系数 C(n, k)，返回 [BigInteger] 以支持大结果。
 *
 * 实现采用乘法约分式 `C(n,k) = ∏_{i=1..k} (n - k + i) / i`，
 * 每一步 `result * (n - k + i)` 必被 `i` 整除（中间值恒为整数，数学上等于 C(n-k+i, i)），
 * 因此先乘后除、每步精确整除，避免阶乘式计算的巨大中间值。
 * 利用对称性 `C(n, k) = C(n, n - k)` 将 k 规约到 `min(k, n - k)`。
 *
 * - 时间复杂度：O(min(k, n-k)) 次 BigInteger 乘除
 * - 空间复杂度：O(结果位数)
 *
 * @param n 必须 ≥ 0；k 必须满足 0 ≤ k ≤ n，否则抛 [IllegalArgumentException]。
 */
fun binomial(n: Int, k: Int): BigInteger {
    require(n >= 0) { "n must be non-negative, was $n" }
    require(k in 0..n) { "k must be in 0..$n, was $k" }
    val kk = minOf(k, n - k)
    var result = BigInteger.ONE
    for (i in 1..kk) {
        result = result.multiply(BigInteger.valueOf((n - kk + i).toLong()))
            .divide(BigInteger.valueOf(i.toLong()))
    }
    return result
}
