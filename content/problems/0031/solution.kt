/**
 * Project Euler 031 — Coin Sums
 *
 * 优化解：完全背包计数：dp[i] 表示凑出 i 便士的方案数，按面额外层循环、金额内层递增，
 * dp[i] += dp[i-c]，保证每种面额组合只按「面额升序」计一次。O(8×200)。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val coins = intArrayOf(1, 2, 5, 10, 20, 50, 100, 200)
    val dp = LongArray(201); dp[0] = 1L
    for (c in coins) for (i in c..200) dp[i] += dp[i - c]
    return dp[200]
}

fun main() {
    println(solve())
}
