/**
 * Project Euler 020 — 暴力解（教学对比用）
 *
 * 不用 BigInteger：用十进制位数组模拟竖式乘法，从 1 起依次乘 2..100。
 * 每次乘法逐位展开、统一处理进位。
 */

fun solveBruteForce(n: Int = 100): Long {
    val digits = IntArray(200)          // 100! 共 158 位，留足余量（低位在前）
    digits[0] = 1
    var len = 1
    for (k in 2..n) {
        var carry = 0
        for (i in 0 until len) {
            val v = digits[i] * k + carry
            digits[i] = v % 10
            carry = v / 10
        }
        while (carry > 0) {             // 进位可能扩展多位
            digits[len] = carry % 10
            carry /= 10
            len++
        }
    }
    return (0 until len).fold(0L) { acc, i -> acc + digits[i] }
}

fun main() {
    println(solveBruteForce())
}
