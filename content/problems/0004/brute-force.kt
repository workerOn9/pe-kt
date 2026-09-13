/**
 * Project Euler 004 — 暴力解（教学对比用）
 *
 * 双重循环枚举全部三位数对，O(n²)，约 40 万个乘积。
 */

fun isPalindromeBrute(n: Long): Boolean {
    val s = n.toString()
    return s == s.reversed()
}

fun solveBruteForce(): Long {
    var best = 0L
    for (a in 100..999) {
        for (b in a..999) {
            val product = a.toLong() * b
            if (product > best && isPalindromeBrute(product)) best = product
        }
    }
    return best
}

fun main() {
    println(solveBruteForce())
}
