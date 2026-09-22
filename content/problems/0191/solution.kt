package dev.pekt.problems

/**
 * Problem 191: Prize Strings
 *
 * 思路：状态压缩 DP
 *
 * 问题约束翻译：
 *  - 连续缺席不能超过 2 天（不含 "AAA" 子串）
 *  - 迟到次数不能超过 1 次
 *
 * DP 状态设计：
 *   dp[day][consecA][late] = 满足条件的字符串数
 *  - day: 当前是第几天 (0..n)
 *  - consecA: 连续缺席天数 (0..2，达到 3 就非法)
 *  - late: 迟到次数 (0..1，超过 1 就非法)
 *
 * 复杂度：O(n × 3 × 2) = O(n)，极快。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve0191(): Long {
    val n = 30
    val dp = Array(n + 1) { Array(3) { LongArray(2) } }
    dp[0][0][0] = 1L

    for (day in 0 until n) {
        for (c in 0..2) {
            for (l in 0..1) {
                val cur = dp[day][c][l]
                if (cur == 0L) continue

                // L: 迟到 → late 变为 2 标记非法（不再加到后续状态）
                if (l == 0) dp[day + 1][0][1] += cur
                // O: 准时 → 连续 A 归零
                dp[day + 1][0][l] += cur
                // A: 缺席 → 只要没到 3 即可
                if (c + 1 < 3) dp[day + 1][c + 1][l] += cur
            }
        }
    }

    var ans = 0L
    for (c in 0..2) for (l in 0..1) ans += dp[n][c][l]
    return ans
}

fun main() { println(solve0191()) }
