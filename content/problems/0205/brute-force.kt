#!/usr/bin/env kotlin
// PE 205 暴力参照：不做前缀和优化，直接双重循环枚举所有 (a,b) 对统计 a>b 的方案数。
// 与 solution.kt 的前缀和版互为交叉验证。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

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
    val tot = A.sum() * B.sum()
    var win = 0L
    for (a in A.indices) for (b in B.indices) if (a > b) win += A[a] * B[b]
    println((win * 10_000_000L * 2 + tot) / (2 * tot))
}
