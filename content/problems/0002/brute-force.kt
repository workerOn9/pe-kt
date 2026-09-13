/**
 * Project Euler 002 — 暴力解（教学对比用）
 *
 * 枚举全部斐波那契项再过滤偶数，O(项数)。
 */

fun solveBruteForce(limit: Long = 4_000_000): Long {
    var sum = 0L
    var a = 1L
    var b = 2L
    while (a <= limit) {
        if (a % 2 == 0L) sum += a
        val next = a + b
        a = b
        b = next
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
