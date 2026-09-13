package dev.pekt.math

/**
 * 模幂（快速幂 / 平方-乘算法）：返回 `base^exp mod mod`，结果在 `[0, mod)` 内。
 *
 * - 时间复杂度：O(log exp)
 * - 空间复杂度：O(1)
 *
 * @param exp 指数，必须 ≥ 0，否则抛 [IllegalArgumentException]。
 * @param mod 模数，必须 ≥ 1，否则抛 [IllegalArgumentException]。
 */
fun modPow(base: Long, exp: Long, mod: Long): Long {
    require(mod >= 1L) { "mod must be positive, was $mod" }
    require(exp >= 0L) { "exp must be non-negative, was $exp" }
    if (mod == 1L) return 0L
    var result = 1L
    var b = Math.floorMod(base, mod)
    var e = exp
    while (e > 0) {
        if (e and 1L == 1L) result = result * b % mod
        b = b * b % mod
        e = e ushr 1
    }
    return result
}

/**
 * 模逆元：返回 `x` 使得 `a * x ≡ 1 (mod m)`，结果在 `[0, m)` 内。
 *
 * 实现基于 [extendedGcd]。
 *
 * - 时间复杂度：O(log min(|a|, m))
 * - 空间复杂度：O(1)
 *
 * @param m 模数，必须 ≥ 2，否则抛 [IllegalArgumentException]。
 * @throws ArithmeticException 当 `gcd(a, m) != 1`（逆元不存在）时。
 */
fun modInverse(a: Long, m: Long): Long {
    require(m >= 2L) { "m must be >= 2, was $m" }
    val (g, x, _) = extendedGcd(a, m)
    if (g != 1L) throw ArithmeticException("modInverse does not exist: gcd($a, $m) = $g != 1")
    return Math.floorMod(x, m)
}
