package dev.pekt.math

import kotlin.math.abs

/**
 * 最大公约数（欧几里得算法）。结果恒为非负。
 *
 * - 时间复杂度：O(log min(|a|, |b|))
 * - 空间复杂度：O(1)
 *
 * 约定：`gcd(0, 0) = 0`。
 */
fun gcd(a: Long, b: Long): Long {
    var x = abs(a)
    var y = abs(b)
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/**
 * 最小公倍数。先除后乘（`a / gcd * b`）以避免中间乘积溢出。
 * 结果恒为非负；任一参数为 0 时返回 0。
 *
 * - 时间复杂度：O(log min(|a|, |b|))
 * - 空间复杂度：O(1)
 */
fun lcm(a: Long, b: Long): Long {
    if (a == 0L || b == 0L) return 0L
    val g = gcd(a, b)
    return abs(a / g * b)
}

/**
 * 扩展欧几里得算法。
 *
 * 返回 `Triple(g, x, y)`，满足 `a * x + b * y == g`，其中 `g = gcd(a, b)`（非负）。
 * `a == b == 0` 时返回 `Triple(0, 0, 0)`。
 *
 * - 时间复杂度：O(log min(|a|, |b|))
 * - 空间复杂度：O(1)（迭代实现）
 */
fun extendedGcd(a: Long, b: Long): Triple<Long, Long, Long> {
    if (a == 0L && b == 0L) return Triple(0L, 0L, 0L)

    // 在绝对值上迭代，最后把符号折算回系数
    var oldR = abs(a)
    var r = abs(b)
    var oldS = 1L
    var s = 0L
    var oldT = 0L
    var t = 1L
    while (r != 0L) {
        val q = oldR / r
        var tmp = oldR - q * r; oldR = r; r = tmp
        tmp = oldS - q * s; oldS = s; s = tmp
        tmp = oldT - q * t; oldT = t; t = tmp
    }
    val x = if (a < 0) -oldS else oldS
    val y = if (b < 0) -oldT else oldT
    return Triple(oldR, x, y)
}
