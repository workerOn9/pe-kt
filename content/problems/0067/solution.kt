import java.io.File

/**
 * Project Euler 067 — Maximum Path Sum II
 *
 * 思路：自底向上的原地 DP。令 dp[c] 表示当前行第 c 列到底行的最大路径和，
 * 则 dp[c] = triangle[r][c] + max(dp[c], dp[c+1])，dp 数组长度逐行收缩一位。
 * 与枚举 2⁹⁹ 条路径相比，这里只在每行做一次线性扫描。
 * 复杂度：O(R²) 时间、O(R) 空间（R = 100 行）。
 * 需从题目目录运行（读取同目录的 triangle.txt）：
 *   kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(path: String = "triangle.txt"): Long {
    val rows = File(path).readLines()
        .filter { it.isNotBlank() }
        .map { line -> line.trim().split(Regex("\\s+")).map(String::toInt) }
    val dp = rows.last().toIntArray()
    for (r in rows.size - 2 downTo 0) {
        val row = rows[r]
        for (c in row.indices) {
            dp[c] = row[c] + maxOf(dp[c], dp[c + 1])
        }
    }
    return dp[0].toLong()
}

fun main() {
    println(solve())
}
