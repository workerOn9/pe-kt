package dev.pekt.problems

/**
 * Problem 189: Tri-colouring a Triangular Grid
 *
 * 思路：
 * 三角形网格有 8 行，从上往下第 r 行（1 <= r <= 8）有 r 个朝上的正三角形和 r-1 个朝下的倒三角形。
 * 倒三角形位于第 r 行的第 j 个朝上三角形与第 j+1 个朝上三角形之间，且其上底边恰好与第 r-1 行的第 j 个朝上三角形相接。
 * 因此，连接第 r-1 行与第 r 行的状态，完全由第 r-1 行的 r-1 个朝上三角形的颜色决定！
 * 颜色只有 3 种（0, 1, 2），第 r 行朝上三角形有 3^r 种状态，第 8 行最多只有 3^8 = 6561 种状态。
 * 使用轮廓线动态规划（Row-by-row DP）：
 * 令 dp[state] 表示当前行朝上三角形着色为 state 的方案数。
 * 从第 r-1 行转移到第 r 行时：
 * 依次生成第 r 行的朝上三角形颜色序列 u[0..r-1]，
 * 对于每个倒三角形 d[j]（0 <= j <= r-2），其颜色必须同时不同于 u[j]、u[j+1] 以及上一行的 prev_u[j]。
 * 故倒三角形 d[j] 可选的颜色数为 3 - countDistinct(prev_u[j], u[j], u[j+1])。
 * 如果该数目为 0，则当前选择无效；否则方案数乘以该因子。
 * 逐行转移，最终求和第 8 行所有状态的方案数即可。
 *
 * 复杂度：
 * 时间复杂度 O(sum_{r=2}^8 3^{r-1} * 3^r) = O(3^15) 极其微小，实测耗时约 35 ms。
 * 空间复杂度 O(3^8) = 6561 个 Long 元素。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve0189(): Long {
    // 状态采用三进制整数压缩编码：长度为 len 的序列压缩为 0 到 3^len - 1 的整数
    // 第 i 位的颜色为 (state / 3^i) % 3

    val pow3 = IntArray(10) { 1 }
    for (i in 1..9) pow3[i] = pow3[i - 1] * 3

    // 第一行：1 个朝上三角形，3 种颜色各 1 种方案
    var dp = LongArray(3) { 1L }

    for (row in 2..8) {
        val prevLen = row - 1
        val curLen = row
        val newDp = LongArray(pow3[curLen])

        // 解码辅助函数
        fun getColor(state: Int, idx: Int): Int = (state / pow3[idx]) % 3

        // 递归生成第 row 行的每一种颜色序列
        val curColors = IntArray(curLen)

        fun dfs(col: Int, prevColors: IntArray, ways: Long) {
            if (col == curLen) {
                // 计算当前序列的三进制编码
                var state = 0
                for (i in 0 until curLen) {
                    state += curColors[i] * pow3[i]
                }
                newDp[state] += ways
                return
            }

            for (c in 0..2) {
                curColors[col] = c
                var factor = 1L
                if (col > 0) {
                    val p = prevColors[col - 1]
                    val uLeft = curColors[col - 1]
                    val uRight = c
                    // 倒三角形与 p, uLeft, uRight 相邻
                    val distinct = booleanArrayOf(false, false, false)
                    distinct[p] = true
                    distinct[uLeft] = true
                    distinct[uRight] = true
                    var count = 0
                    if (distinct[0]) count++
                    if (distinct[1]) count++
                    if (distinct[2]) count++
                    val validChoices = 3 - count
                    if (validChoices <= 0) continue
                    factor = validChoices.toLong()
                }
                dfs(col + 1, prevColors, ways * factor)
            }
        }

        val prevColors = IntArray(prevLen)
        for (prevState in 0 until pow3[prevLen]) {
            val ways = dp[prevState]
            if (ways == 0L) continue
            for (i in 0 until prevLen) {
                prevColors[i] = getColor(prevState, i)
            }
            dfs(0, prevColors, ways)
        }

        dp = newDp
    }

    return dp.sum()
}

fun main() {
    println(solve0189())
}
