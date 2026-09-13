/**
 * Project Euler 001 — 暴力解（教学对比用）
 *
 * 逐个枚举 1..limit-1，O(n)。
 */

fun solveBruteForce(limit: Long = 1000): Long {
    var sum = 0L
    for (i in 1 until limit) {
        if (i % 3 == 0L || i % 5 == 0L) sum += i
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
