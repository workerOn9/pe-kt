#!/usr/bin/env kotlin
// PE 201 — Subsets with a Unique Sum（唯一子集和）
// 思路：DP 计数子集和出现次数。dp[k][s] = 从 {1..i} 取 k 个数凑出和 s 的方案数，
//       按 0/1 背包三重循环推进（k 倒序避免同一元素重复选取）。
//       最后对 d50[s] 里 count==1 的 s 求和。
// 复杂度：O(n · k · S) ≈ 100 × 50 × 2526 ≈ 1.3e7 次大数加法，64 位计数饱和前即得唯一项，
//        实测毫秒级。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

import java.math.BigInteger

fun main() {
    val n = 100
    val k = 50
    val maxS = n * (n + 1) / 2                 // 全体和的上界（含 0 个元素=0）
    var dp = Array(k + 1) { arrayOfNulls<BigInteger>(maxS + 1) }
    for (row in dp) java.util.Arrays.fill(row, BigInteger.ZERO)
    dp[0][0] = BigInteger.ONE

    for (v in 1..n) {
        // k 倒序：每个数最多选一次
        for (kk in minOf(k, v) downTo 1) {
            val cur = dp[kk]
            val prev = dp[kk - 1]
            for (s in (maxS - v) downTo 0) {
                val c = prev[s]
                if (c.signum() > 0) {
                    cur[s + v] = cur[s + v]!!.add(c)
                }
            }
        }
    }

    val d50 = dp[k]
    var ans = BigInteger.ZERO
    for (s in 0..maxS) {
        if (d50[s].signum() == 1) ans = ans.add(BigInteger.valueOf(s.toLong()))
    }
    println(ans)
}
