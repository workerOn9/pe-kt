/**
 * PE 181 — 60 个黑球与 40 个白球的多重集拆分（二维完全背包）。
 *
 * 原理：每个组由 (b, w) 决定（0 <= b <= 60, 0 <= w <= 40，且 (b,w) != (0,0)）。
 * 组的顺序不计，每个非空组可以取任意非负整数次。
 * 其生成函数为：
 *   ∏_{(b,w) != (0,0)} 1 / (1 - x^b y^w)
 * 相当于容量为 (60, 40) 的二维完全背包方案数统计。
 * 外层遍历所有可能的物品 (b, w)，内层二维空间正向递推累加：
 *   dp[i][j] += dp[i - b][j - w]
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
fun solve181(B: Int = 60, W: Int = 40): Long {
    val dp = Array(B + 1) { LongArray(W + 1) }
    dp[0][0] = 1L

    for (b in 0..B) {
        for (w in 0..W) {
            if (b == 0 && w == 0) continue
            for (i in b..B) {
                val rowI = dp[i]
                val rowPrev = dp[i - b]
                for (j in w..W) {
                    rowI[j] += rowPrev[j - w]
                }
            }
        }
    }
    return dp[B][W]
}

fun main() {
    // 样例自检：3 个黑球，1 个白球 -> 7 种方案
    val testSample = solve181(3, 1)
    check(testSample == 7L) { "Sample (3, 1) should be 7, got $testSample" }

    val answer = solve181(60, 40)
    println(answer)
}
