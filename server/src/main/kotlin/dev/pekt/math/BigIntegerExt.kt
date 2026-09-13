package dev.pekt.math

import java.math.BigInteger

/**
 * 十进制各位数字之和（对绝对值计算，负号不计）。
 *
 * 典型应用：PE 16（2^1000 的 digitSum = 1366）、PE 20（100! 的 digitSum = 648）。
 *
 * - 时间复杂度：O(d)，d 为十进制位数
 * - 空间复杂度：O(d)（toString 的字符数组）
 */
fun BigInteger.digitSum(): Int =
    this.abs().toString().sumOf { it - '0' }

/**
 * 阶乘 n!，返回 [BigInteger]。
 *
 * 0! = 1，1! = 1。
 *
 * - 时间复杂度：O(n) 次 BigInteger 乘法
 * - 空间复杂度：O(n log n) 位
 *
 * @param n 必须 ≥ 0，否则抛 [IllegalArgumentException]。
 */
fun factorial(n: Int): BigInteger {
    require(n >= 0) { "n must be non-negative, was $n" }
    var result = BigInteger.ONE
    for (i in 2..n) result = result.multiply(BigInteger.valueOf(i.toLong()))
    return result
}
