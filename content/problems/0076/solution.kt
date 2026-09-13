/**
 * Project Euler 076 — Counting Summations
 *
 * 思路：把「n 拆成若干个正整数之和」看作硬币找零：硬币面额为 1..n，每种面额可重复使用，
 * 求凑出 n 的组合数（顺序无关），即分拆数 p(n)。经典一维 DP：
 *   dp[0] = 1；对每种硬币 c，从大到小（或正序）更新 dp[s] += dp[s - c]，
 * 逐枚硬币推进保证每个组合只被计数一次（面额的使用是有序决策，不会重复排列）。
 * 题目要求「至少两部分」，即 p(n) 减去单部分 {n} 这一种，故答案为 p(100) − 1。
 * 复杂度：O(n²) 时间，O(n) 空间。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val n = 100
    val dp = LongArray(n + 1)
    dp[0] = 1L
    for (coin in 1..n) {
        for (s in coin..n) dp[s] += dp[s - coin]
    }
    return dp[n] - 1L
}

fun main() {
    println(solve())
}
