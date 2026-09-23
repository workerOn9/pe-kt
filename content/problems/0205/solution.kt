#!/usr/bin/env kotlin
// PE 205 — Dice Game（骰子游戏概率）
// 思路：DP 求两边总点数的精确分布计数（9d4 总方案 4^9=262144，6d6 总方案 6^6=46656，
//       全部远小于 Long 上限），P(A>B) = Σ_a cntA(a) · (Σ_{b<a} cntB(b)) / (4^9·6^6)。
//       答案按题意保留 7 位小数：以 round(p × 10^7) 的 Long 存储（= 5731441）。
// 复杂度：O(9·4·36 + 6·6·36 + 36²) —— 微秒级。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

fun main() {
    fun dist(n: Int, faces: Int): LongArray {
        var dp = LongArray(n * faces + 1)
        dp[0] = 1
        repeat(n) {
            val nd = LongArray(n * faces + 1)
            for (s in dp.indices) {
                if (dp[s] == 0L) continue
                for (f in 1..faces) nd[s + f] += dp[s]
            }
            dp = nd
        }
        return dp
    }
    val A = dist(9, 4)
    val B = dist(6, 6)
    val totA = A.sum()          // 4^9
    val totB = B.sum()          // 6^6
    var prefB = 0L
    var win = 0L
    for (a in A.indices) {
        win += A[a] * prefB     // prefB = Σ_{b<a} B[b]
        prefB += B[a]
    }
    val tot = totA * totB
    // round(win / tot × 10^7) 的精确整数运算
    val scaled = (win * 10_000_000L * 2 + tot) / (2 * tot)
    println(scaled)
}
