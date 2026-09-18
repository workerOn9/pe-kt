/**
 * Project Euler 121 — 暴力解 / 独立复核（教学对比用）
 *
 * solution.kt 走的是「按蓝碟次数聚合的 DP」：把每种结果的概率合并成一维数组。
 * 本文件走完全相反的路——**把 2^n 条颜色序列一条条列出来**，逐条累加它的概率，
 * 再筛出蓝碟多于红碟的那些：
 *
 *   - 第 k 轮抽到蓝碟的概率是 1/(k+1)、抽到红碟是 k/(k+1)，两者分母之积恰为
 *     Π_{k=1..n}(k+1) = (n+1)!，所以对一条序列而言，「以 (n+1)! 放大后的分子」
 *     就是把每轮的分子乘起来：蓝碟贡献 1、红碟贡献 k。于是整条序列的权重是
 *     一个纯整数 Π w_k，连分数都不必出现。
 *   - 枚举 2^15 = 32768 条序列、对每条做 15 次乘法即可，用位掩码遍历二进制数即可覆盖，
 *     不需要递归。
 *
 * 题面锚点（4 轮 11/120、奖金 £10）同样在 main 里自检。两条路径的机制完全不同：
 * 一条按「结果计数」聚合，一条按「过程序列」展开。
 *
 * 复杂度：O(2^n · n) 时间、O(1) 空间；n = 15 时约 5×10⁵ 次乘法，毫秒级。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 显式枚举全部 2^turns 条颜色序列，累加蓝碟多于红碟者的权重（放大 (turns+1)! 倍）。 */
fun winningScaledByEnumeration(turns: Int): BigInteger {
    var total = BigInteger.ZERO
    val masks = 1 shl turns
    for (mask in 0 until masks) {
        var weight = BigInteger.ONE
        var blues = 0
        for (turn in 1..turns) {
            val isBlue = mask shr (turn - 1) and 1 == 1
            if (isBlue) {
                blues++                                     // 蓝碟分子为 1
            } else {
                weight *= BigInteger.valueOf(turn.toLong()) // 红碟分子为 turn
            }
        }
        if (blues * 2 > turns) total += weight
    }
    return total
}

fun fractionByEnumeration(turns: Int): Pair<BigInteger, BigInteger> {
    var denominator = BigInteger.ONE
    for (k in 1..turns) denominator *= BigInteger.valueOf((k + 1).toLong())
    return winningScaledByEnumeration(turns) to denominator
}

fun solveBruteForce(): Long {
    val (winning, denominator) = fractionByEnumeration(15)
    return denominator.divide(winning).longValueExact()
}

fun main() {
    val (numerator4, denominator4) = fractionByEnumeration(4)
    println("4 轮：获胜概率 = $numerator4/$denominator4（题面 11/120），奖金 = ${denominator4 / numerator4}")
    check(numerator4 == BigInteger.valueOf(11) && denominator4 == BigInteger.valueOf(120)) {
        "题面锚点：4 轮获胜概率应为 11/120，实际 $numerator4/$denominator4"
    }
    check(denominator4.divide(numerator4).longValueExact() == 10L) { "题面锚点：4 轮奖金应为 £10" }

    val (winning15, denominator15) = fractionByEnumeration(15)
    println("15 轮：获胜概率 = $winning15/$denominator15")
    println("solveBruteForce() = ${solveBruteForce()}")
}
