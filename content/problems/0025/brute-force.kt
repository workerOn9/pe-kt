/**
 * Project Euler 025 — 暴力解（教学对比用）
 *
 * 同样用 BigInteger 迭代，但每一步都转十进制字符串来数位数，
 * 每步多一次 O(n) 的字符串分配与转换。
 */

import java.math.BigInteger

fun solveBruteForce(digits: Int = 1000): Int {
    var prev = BigInteger.ONE                       // F_1
    var curr = BigInteger.ONE                       // F_2
    var index = 2
    while (curr.toString().length < digits) {
        val next = prev + curr
        prev = curr
        curr = next
        index++
    }
    return index
}

fun main() {
    println(solveBruteForce())
}
