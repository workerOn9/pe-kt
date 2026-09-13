/**
 * Project Euler 055 — Lychrel Numbers（暴力解，教学对比用）
 *
 * 与 solution.kt 相对的「不做提前退出」写法：对每个 n 老老实实跑满 50 次迭代，
 * 用布尔量记录这 50 步里是否曾经出现回文数，最后据此判定。
 * 对那些前几步就回文的数（占绝大多数）完全做了无用功，这正是对比点。
 *
 * 复杂度：O(N · 50 · D²)，迭代次数恒为 50·N，与是否提前回文无关。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun reverseDigitsOf(n: BigInteger): BigInteger = n.toString().reversed().toBigInteger()

fun isPalindromeOf(n: BigInteger): Boolean {
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

fun solveBruteForce(): Long {
    var count = 0L
    for (i in 1 until 10000) {
        var n = BigInteger.valueOf(i.toLong())
        var sawPalindrome = false
        repeat(50) {
            n = n + reverseDigitsOf(n)
            if (isPalindromeOf(n)) sawPalindrome = true
        }
        if (!sawPalindrome) count++
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
