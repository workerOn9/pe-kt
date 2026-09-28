package dev.pekt.engine

import dev.pekt.math.factorial
import java.math.BigInteger

/**
 * PE 253 — Tidying Up A（整理毛毛虫）。
 *
 * 一次随机整理对应一个排列 t（t_p = 第 p 号拼图被放下的时刻）。放下 k 块后的段数
 * b(k) = k − #{相邻对 (p, p+1) : max(t_p, t_{p+1}) ≤ k}，是步差 ∈ {−1,0,+1} 的 Motzkin 型路径：
 * 生段 b→b+1、接边 b→b、合段 b→b−1，三种步在高度 h 上的「历史选择数」分别为 h+1、2h、h−1。
 * 于是 C(n,m) = #{M ≤ m} = 高度限制在 1..m 的加权 Motzkin 路径总权（长度 n−1、起止高度 1），
 * #{M=m} = C(n,m) − C(n,m−1)；期望用精确有理数算，四舍五入到六位小数后按仓库惯例编码为
 * round(E×10⁶)×100 = 1149284700。与 content/problems/0253/solution.kt 的最优路径一致
 * （另见该目录 analysis.md：子集格 DP、全排列暴力与特征多项式递推三重对照）。
 * 运行时间毫秒级。
 */
internal fun solve0253Impl(): Long {
    val n = 40
    val mMax = (n + 1) / 2                       // M ≤ ⌈n/2⌉：k 段至少需要 2k−1 块
    val total = factorial(n)
    var prev = BigInteger.ZERO
    var weighted = BigInteger.ZERO               // Σ m · #{M = m}
    for (m in 1..mMax) {
        val c = pe253CountLeq(n, m)
        weighted += (c - prev) * BigInteger.valueOf(m.toLong())
        prev = c
    }
    require(prev == total) { "PE253：分布求和应等于 40!" }
    val scaled = weighted * BigInteger.valueOf(1_000_000L)
    val (q, r) = scaled.divideAndRemainder(total)
    val micro = if (r * BigInteger.TWO >= total) q + BigInteger.ONE else q   // 四舍五入到 10⁻⁶
    return (micro * BigInteger.valueOf(100L)).toLong()
}

/**
 * C(n, m) = #{排列 : M ≤ m}：长度 n−1、起止高度 1、始终 1..m 的加权 Motzkin 路径总权。
 * 高度 h 上的转移：生段 h→h+1 权 h+1；接边 h→h 权 2h；合段 h→h−1 权 h−1。
 */
private fun pe253CountLeq(n: Int, m: Int): BigInteger {
    if (n <= 1) return BigInteger.ONE
    if (n <= 2 * m) return factorial(n)          // 此时全部排列都满足 M ≤ m
    var cur = Array(m + 2) { BigInteger.ZERO }
    cur[1] = BigInteger.ONE
    repeat(n - 1) {
        val next = Array(m + 2) { BigInteger.ZERO }
        for (h in 1..m) {
            val w = cur[h]
            if (w.signum() == 0) continue
            if (h + 1 <= m) next[h + 1] += w * BigInteger.valueOf((h + 1).toLong())
            next[h] += w * BigInteger.valueOf(2L * h)
            if (h - 1 >= 1) next[h - 1] += w * BigInteger.valueOf((h - 1).toLong())
        }
        cur = next
    }
    return cur[1]
}
