/**
 * Project Euler 017 — 暴力解（教学对比用）
 *
 * 逐个把 1..1000 按英式拼写规则拼成单词并数字母（不建字符串，只累计长度）。
 */

private val ONES = intArrayOf(3, 3, 5, 4, 4, 3, 5, 5, 4)            // one..nine
private val TEENS = intArrayOf(3, 6, 6, 8, 8, 7, 7, 9, 8, 8)        // ten..nineteen
private val TENS = intArrayOf(6, 6, 5, 5, 5, 7, 6, 6)               // twenty..ninety

/** 数字 [n]（1 <= n <= 1000）英式拼写的字母数 */
fun letters(n: Int): Int {
    if (n == 1000) return 11                                        // one thousand
    var total = 0
    var rest = n
    if (rest >= 100) {
        total += ONES[rest / 100 - 1] + 7                           // X hundred
        rest %= 100
        if (rest > 0) total += 3                                    // and
    }
    when {
        rest == 0 -> Unit
        rest < 10 -> total += ONES[rest - 1]
        rest < 20 -> total += TEENS[rest - 10]
        else -> {
            total += TENS[rest / 10 - 2]
            if (rest % 10 > 0) total += ONES[rest % 10 - 1]
        }
    }
    return total
}

fun solveBruteForce(): Long = (1..1000).fold(0L) { acc, n -> acc + letters(n) }

fun main() {
    println(solveBruteForce())
}
