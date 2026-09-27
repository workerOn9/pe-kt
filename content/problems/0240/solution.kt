#!/usr/bin/env kotlin
/**
 * Project Euler 240 — Top Dice（最大的骰子）
 *
 * 思路：
 *   20 个 12 面骰，点数为 1..12，求「最大的 10 个点数之和 = 70」的投法数。
 *
 *   关键观察：**「最大的 k 个」只依赖多重集（各点数出现的次数），不依赖顺序**。
 *   记 c_v 为点数 v 出现的次数（v = 1..12，Σc_v = 20）。对一个确定的 c，
 *   投法数 = 20! / Π c_v!（多重集系数）。于是问题变成枚举合法的 c，
 *   但直接枚举要 C(31,11) ≈ 8.5×10^7 个向量，改用 DP：
 *
 *   **按面值从大到小处理**（12, 11, …, 1）。因为「最大的 10 个」必然是按面值降序
 *   依次取满的，所以处理面值 v 时，只要知道「已填入 top-10 的格子数 filled」，
 *   就能确定这批 c_v 里有多少个进 top-10：
 *
 *       take = min(10 − filled, c_v)
 *
 *   状态 (filled, topSum, total)：
 *       filled —— 已占据 top-10 的格子数（0..10）
 *       topSum —— 这些格子的点数和（≤ 70）
 *       total  —— 已分配的骰子总数（0..20）
 *
 *   转移权重取 C(20 − total, c_v)：含义是「从剩下的 20 − total 个**有标号**的骰子里
 *   挑出 c_v 个来放这个面值」。全部面值处理完后乘积恰好把 20 个骰子分配完，
 *   等于 20! / Π c_v!，正是需要的投法数。全程整数，无精度问题。
 *
 *   剪枝：filled > 10 或 topSum > 70 直接丢弃（后续面值只会更大，只加不减）。
 *   终态要求 filled = 10、topSum = 70、total = 20。
 *
 * 旁证：
 *   1. 题面样例：把 (骰子数, 面数, top 个数, 目标和) 换成 (5, 6, 3, 15)，
 *      同一份 DP 给出 1111，与题面完全一致；
 *   2. 另写了一份权重取 1/c_v! 的分数版 DP（直接数多重集系数），
 *      给出同一答案 7448717393364181966；
 *   3. brute-force.kt 用完全不同的思路——枚举 top-10 的多重集（降序、和为 70）
 *      再逐一枚举剩下 10 个骰子的多重集并算多项式系数——给出同一答案；
 *   4. 答案已在 PE 官网提交确认。
 *
 * 答案：7448717393364181966
 * 复杂度：状态数 O(10 × 70 × 20) ≈ 1.4×10^4，每层 12 个面值、每状态 ≤ 21 个 c_v，
 *         实测 < 5 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val N_DICE = 20
private const val N_SIDES = 12
private const val N_TOP = 10
private const val TARGET = 70

private fun binom(n: Int, k: Int): Long {
    if (k < 0 || k > n) return 0L
    var r = 1L
    for (i in 0 until k) r = r * (n - i) / (i + 1)
    return r
}

/** 权重取 C(rem, c) 的整数 DP：直接数「哪些有标号的骰子取这个面值」 */
private fun countWays(nDice: Int, nSides: Int, nTop: Int, target: Int): Long {
    // 状态 (filled, topSum, total) → 方案数
    var cur = HashMap<Triple<Int, Int, Int>, Long>()
    cur[Triple(0, 0, 0)] = 1L

    for (v in nSides downTo 1) {
        val next = HashMap<Triple<Int, Int, Int>, Long>()
        for ((state, w) in cur) {
            val (filled, topSum, total) = state
            val rem = nDice - total
            for (c in 0..rem) {
                val take = minOf(nTop - filled, c)
                val nf = filled + take
                val ns = topSum + take * v
                if (nf > nTop || ns > target) continue
                val key = Triple(nf, ns, total + c)
                next[key] = (next[key] ?: 0L) + w * binom(rem, c)
            }
        }
        cur = next
    }

    var total = 0L
    for ((state, w) in cur) {
        val (filled, topSum, used) = state
        if (filled == nTop && topSum == target && used == nDice) total += w
    }
    return total
}

private fun main() {
    val sample = countWays(5, 6, 3, 15)
    println("sample 5d6 top3=15   -> $sample   (题面 1111)")
    check(sample == 1111L) { "题面样例不吻合：$sample" }

    val answer = countWays(N_DICE, N_SIDES, N_TOP, TARGET)
    println("answer 20d12 top10=70 -> $answer")
    check(answer == 7448717393364181966L) { "答案不吻合：$answer" }
    println("check() 全部通过")
}
