/**
 * Project Euler 016 — 暴力解（教学对比用）
 *
 * 不用 BigInteger：用十进制位数组模拟竖式乘法，从 1 开始连续翻倍 1000 次。
 * 每位只存 0..9，逐位乘 2 加进位。
 */

fun solveBruteForce(exp: Int = 1000): Long {
    val digits = IntArray(400)          // 2^1000 共 302 位，留足余量（低位在前）
    digits[0] = 1
    repeat(exp) {
        var carry = 0
        for (i in digits.indices) {
            val v = digits[i] * 2 + carry
            digits[i] = v % 10
            carry = v / 10
        }
    }
    return digits.fold(0L) { acc, d -> acc + d }
}

fun main() {
    println(solveBruteForce())
}
