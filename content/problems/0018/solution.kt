/**
 * Project Euler 018 — Maximum Path Sum I
 *
 * 优化解：自底向上动态规划，O(n²) 时间、O(n) 空间。
 * 滚动数组从下往上收拢：dp[j] = 三角[i][j] + max(dp[j], dp[j+1])，
 * 处理完顶行后 dp[0] 即最大路径和。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

const val TRIANGLE = """
75
95 64
17 47 82
18 35 87 10
20 04 82 47 65
19 01 23 75 03 34
88 02 77 73 07 63 67
99 65 04 28 06 16 70 92
41 41 26 56 83 40 80 70 33
41 48 72 33 47 32 37 16 94 29
53 71 44 65 25 43 91 52 97 51 14
70 11 33 28 77 73 17 78 39 68 17 57
91 71 52 38 17 14 91 43 58 50 27 29 48
63 66 04 68 89 53 67 30 73 16 69 87 40 31
04 62 98 27 23 09 70 98 73 93 38 53 60 04 23
"""

fun parseTriangle(text: String): List<IntArray> =
    text.trim().lines().map { line -> line.trim().split(" ").map { it.toInt() }.toIntArray() }

fun solve(triangle: List<IntArray> = parseTriangle(TRIANGLE)): Long {
    val dp = triangle.last().map { it.toLong() }.toLongArray()      // 从底行出发
    for (i in triangle.size - 2 downTo 0) {
        val row = triangle[i]
        for (j in row.indices) {
            dp[j] = row[j] + maxOf(dp[j], dp[j + 1])
        }
    }
    return dp[0]
}

fun main() {
    println(solve())
}
