/**
 * Project Euler 055 — Lychrel Numbers
 *
 * 优化解：对每个 n 迭代「反转相加」，一旦得到回文数立即停止（绝大多数数在前几步就回文），
 * 50 次内始终未回文的才算 Lychrel 数。
 * 反转用十进制字符串重排、回文用双指针比较，都是 O(D)；但 10677 这类数迭代后会有 28 位，
 * 远超 Long 范围，所以必须用 BigInteger。
 *
 * 复杂度：O(N · 50 · D²)（BigInteger 加法 O(D)，字符串反转 O(D)，N=9999、D≤28）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

fun reverseDigits(n: BigInteger): BigInteger = n.toString().reversed().toBigInteger()

fun isPalindromic(n: BigInteger): Boolean {
    val s = n.toString()
    var i = 0
    var j = s.length - 1
    while (i < j) {
        if (s[i] != s[j]) return false
        i++
        j--
    }
    return true
}

fun solve(): Long {
    var count = 0L
    for (i in 1 until 10000) {
        var n = BigInteger.valueOf(i.toLong())
        var isLychrel = true
        var iteration = 0
        while (iteration < 50) {
            n += reverseDigits(n)
            iteration++
            if (isPalindromic(n)) {
                isLychrel = false
                break
            }
        }
        if (isLychrel) count++
    }
    return count
}

fun main() {
    println(solve())
}
