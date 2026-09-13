/**
 * Project Euler 001 — Multiples of 3 or 5
 *
 * 优化解：容斥原理 + 等差数列求和，O(1)。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 小于 [below] 的所有 [k] 的倍数之和（等差数列求和） */
fun sumOfMultiples(k: Long, below: Long): Long {
    val n = (below - 1) / k          // 倍数个数：k, 2k, ..., nk < below
    return k * n * (n + 1) / 2
}

fun solve(limit: Long = 1000): Long =
    sumOfMultiples(3, limit) + sumOfMultiples(5, limit) - sumOfMultiples(15, limit)

fun main() {
    println(solve())
}
