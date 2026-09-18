/**
 * Project Euler 151 — Paper sheets of standard sizes（偏好 A5）
 *
 * 思路：状态用信封中各尺寸纸张数量描述。每次取纸后，信封中各尺寸纸张数量会变化。
 * 定义 dp[batch][a1][a2][a3][a4][a5] 表示概率，但状态空间过大。
 *
 * 优化思路：注意到只有 A2-A5 的尺寸重要，A1 只会初始化为 1 后就被裁切。
 * 实际上只需要跟踪信封中各尺寸的纸张数量。
 *
 * 状态：(a2, a3, a4, a5) 表示信封中 A2-A5 纸张数量。
 * 初始状态：裁剪一次 A1 → (1, 0, 0, 0)（得到一张 A2）
 *
 * 每次取纸：
 * - 若信封中有 A5（概率 a5/total），使用它，a5 减 1
 * - 若有 A4（概率 a4/total），裁成两张 A5，信封增加一张 A5，a4 减 1
 * - 类似处理 A3、A2
 *
 * 动态规划模拟 16 个批次的过程，统计信封中只剩一张纸的次数（排除首尾批次）。
 *
 * 复杂度：状态数 O(n^4)，n=16，实际更小因为总纸张数有限。
 */

fun solve(): Double {
    // dp[a2][a3][a4][a5] = 概率
    val maxPapers = 16
    val dp = Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { 0.0 } } } }
    
    // 初始：裁 A1 → A2，信封中有 1 张 A2
    dp[1][0][0][0] = 1.0
    
    var expectedSinglePaper = 0.0
    
    // 遍历 16 个批次（索引 0-15），排除第 1 个和第 16 个
    for (batch in 1 until 16) {
        val newDp = Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { Array(maxPapers + 1) { 0.0 } } } }
        
        for (a2 in 0..maxPapers) {
            for (a3 in 0..maxPapers) {
                for (a4 in 0..maxPapers) {
                    for (a5 in 0..maxPapers) {
                        val prob = dp[a2][a3][a4][a5]
                        if (prob <= 0) continue
                        
                        val total = a2 + a3 + a4 + a5
                        if (total == 0) continue
                        
                        // 统计本批次开始时空信封的概率
                        if (total == 1 && batch > 1 && batch < 15) {
                            expectedSinglePaper += prob
                        }
                        
                        // 随机取一张纸
                        if (a5 > 0) {
                            // 取到 A5：使用它，信封减少一张 A5
                            val p = a5.toDouble() / total
                            newDp[a2][a3][a4][a5 - 1] += prob * p
                        }
                        if (a4 > 0) {
                            // 取到 A4：裁成两张 A5，信封 a4 减 1，a5 加 1
                            val p = a4.toDouble() / total
                            newDp[a2][a3][a4 - 1][a5 + 1] += prob * p
                        }
                        if (a3 > 0) {
                            // 取到 A3：裁成两张 A4，信封 a3 减 1，a4 加 1
                            val p = a3.toDouble() / total
                            newDp[a2][a3 - 1][a4 + 1][a5] += prob * p
                        }
                        if (a2 > 0) {
                            // 取到 A2：裁成两张 A3，信封 a2 减 1，a3 加 1
                            val p = a2.toDouble() / total
                            newDp[a2 - 1][a3 + 1][a4][a5] += prob * p
                        }
                    }
                }
            }
        }
        
        dp.replace(newDp)
    }
    
    return expectedSinglePaper
}

private fun <T> Array<Array<Array<Array<T>>>>.replace(other: Array<Array<Array<Array<T>>>>) {
    for (i in indices) {
        for (j in indices[i].indices) {
            for (k in indices[i][j].indices) {
                for (l in indices[i][j][k].indices) {
                    this[i][j][k][l] = other[i][j][k][l]
                }
            }
        }
    }
}

fun main() {
    val result = solve()
    println("%.6f".format(result))
}
