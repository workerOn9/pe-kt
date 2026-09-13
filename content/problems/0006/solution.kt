/**
 * Project Euler 006 — Sum Square Difference
 *
 * 优化解：两条求和公式直接算，O(1)。
 *   1..n 的和       S1 = n(n+1)/2
 *   1..n 的平方和   S2 = n(n+1)(2n+1)/6
 *   答案 = S1² − S2
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 1..n 之和的平方 */
fun squareOfSum(n: Long): Long {
    val s = n * (n + 1) / 2
    return s * s
}

/** 1..n 的平方和 */
fun sumOfSquares(n: Long): Long = n * (n + 1) * (2 * n + 1) / 6

fun solve(n: Long = 100): Long = squareOfSum(n) - sumOfSquares(n)

fun main() {
    println(solve())
}
