/**
 * Project Euler 014 — 暴力解（教学对比用）
 *
 * 每个起点独立走完整条 Collatz 链，不做任何缓存。
 * 一百万条链、平均每条上百步，累计上亿次迭代。
 */

fun chainLength(start: Long): Int {
    var n = start
    var len = 1
    while (n != 1L) {
        n = if (n % 2 == 0L) n / 2 else 3 * n + 1
        len++
    }
    return len
}

fun solveBruteForce(limit: Int = 1_000_000): Int {
    var bestStart = 1
    var bestLen = 1
    for (start in 2 until limit) {
        val len = chainLength(start.toLong())
        if (len > bestLen) {
            bestLen = len
            bestStart = start
        }
    }
    return bestStart
}

fun main() {
    println(solveBruteForce())
}
