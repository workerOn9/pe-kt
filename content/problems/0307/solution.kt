#!/usr/bin/env kotlin
/**
 * Project Euler 307 — Chip Defects（芯片缺陷）
 *
 * 题目：n 块芯片上随机分布 k 个缺陷（一块芯片可有多个，缺陷彼此独立）。
 * p(k,n) = 「存在某块芯片带 ≥3 个缺陷」的概率。求 p(20000, 1000000)，四舍五入到 10 位小数。
 *
 * 思路推导
 * ────────
 * 每个缺陷独立等概率落在 n 块芯片之一，共 k^n 种等可能分配（缺陷可区分）。
 * 补事件「每块芯片至多 2 个缺陷」：单块芯片带 0/1/2 个缺陷的指数生成函数 f(x) = 1 + x + x²/2，
 * n 块独立 ⇒ k^n 等可能分配中该补事件的方案数 = k!·[x^k] f(x)^n。
 *
 * 设 f = 1 + x + x²/2，g = f^n = Σ c_j x^j，两边求导得 f·g' = n·f'·g：
 *
 *   (1 + x + x²/2)·Σ_j (j+1)c_{j+1} x^j  =  n(1 + x)·Σ_j c_j x^j
 *
 * 比较 x^j 系数：
 *
 *   (j+1)c_{j+1} + j·c_j + (j-1)/2·c_{j-1} = n·c_j + n·c_{j-1}
 *
 *   ⟹ (j+1)·c_{j+1} = (n-j)·c_j + (n - (j-1)/2)·c_{j-1}        （c_0 = 1, c_{-1} = 0）
 *
 * 自检：j=0 给 c_1 = n；j=1 给 2c_2 = (n-1)n + n = n²，即 c_2·2! = n²，
 * 与「2 个可区分缺陷随便投到 n 块芯片共 n² 种」一致。
 *
 * 两边乘 j! 换成整数递推（记 A_j = j!·c_j = j 个可区分缺陷投 n 块芯片、每块至多 2 个的方案数）：
 *
 *   A_{j+1} = (n-j)·A_j + [ j(2n-j+1) / 2 ]·A_{j-1},   A_0 = 1, A_1 = n
 *
 * 系数 j(2n-j+1)/2 恒整数：j 偶则 j 偶，j 奇则 2n-j+1 偶。自检 j=1：A_2 = (n-1)n + n = n²。
 *
 * 于是 p(k,n) = 1 - A_k / n^k。
 *
 * 数值
 * ────
 * n^k = 10^120000、k! ≈ 10^77326，A_k 同量级；直接构造大整数再相除既慢又难保精度。
 * 一律用 BigDecimal + MathContext(40) 递推：BigDecimal 本身就是「尾数 × 10^-scale」的归一化表示，
 * 精度固定 40 位有效数字而 scale 可任意大，故 A_k、k!、n^k 都只保留 40 位有效数字即可，
 * 既不溢出也不丢有效位。最后 BigDecimal.divide(mc) 取 40 位、1 减、setScale(10, HALF_UP)。
 *
 * 验证
 * ────
 * 1. 题面样例 p(3,7) = 1/49 = 0.02040816326530…，四舍五入到 10 位小数 = 0.0204081633；
 * 2. 直接 O(k²) 双重求和 A_k = Σ_m Σ_l C(n,m)·C(n-m,l)·k!/2^m（约束 2m+l=k, m+l≤n，BigInteger 精确求和）
 *    在 n=1000,k=50 与 n=100,k=20 上与递推实现比对到 12 位小数；
 * 3. n=7,k=3：A_3 = (7-2)·49 + [2·(14-2+1)/2]·7 = 245 + 91 = 336 = 7³ − 7，
 *    即「三缺陷落在同一块」的 7 种之外的 336 种，p = 7/343 = 1/49。
 *
 * 复杂度：递推 O(k) 次大数运算、O(1) 个大数；双重求和 O(k²) 次组合数运算。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

private val MC = MathContext(40)

/** 递推法：k 个可区分缺陷投到 n 块芯片、每块至多 2 个的方案数 A_k（BigDecimal 40 位有效数字）。 */
private fun countAtMostTwoRecurrence(n: Int, k: Int, mc: MathContext): BigDecimal {
    if (k == 0) return BigDecimal.ONE
    var aPrev = BigDecimal.ONE                  // A_0
    var aCur = BigDecimal(n)                    // A_1
    for (j in 1 until k) {
        val coeff = j.toLong() * (2L * n - j + 1L) / 2L   // j(2n-j+1)/2，恒整数
        val next = BigDecimal((n - j).toLong()).multiply(aCur, mc)
            .add(BigDecimal(coeff).multiply(aPrev, mc), mc)
        aPrev = aCur
        aCur = next
    }
    return aCur                                // A_k 本身即分配方案数（= c_k · k!）
}

/** 组合数 C(top, r)，乘法公式精确计算。 */
private fun binom(top: Long, r: Int): BigInteger {
    if (r < 0 || r > top) return BigInteger.ZERO
    val rr = minOf(r, (top - r).toInt())
    var c = BigInteger.ONE
    for (i in 1..rr) c = c.multiply(BigInteger.valueOf(top - rr + i)).divide(BigInteger.valueOf(i.toLong()))
    return c
}

/** 直接法（对照）：A_k = Σ_m Σ_l C(n,m)·C(n-m,l)·k!/2^m，约束 2m+l=k、m+l≤n。O(k²) 次组合数运算，全整数精确求和。 */
private fun countAtMostTwoDoubleSum(n: Int, k: Int): BigInteger {
    var fact = BigInteger.ONE
    for (i in 2..k) fact = fact.multiply(BigInteger.valueOf(i.toLong()))
    var sum = BigInteger.ZERO
    for (m in 0..minOf(n, k / 2)) {
        for (l in 0..k) {
            if (2 * m + l != k || m + l > n) continue
            sum = sum.add(binom(n.toLong(), m).multiply(binom((n - m).toLong(), l)).multiply(fact).shiftRight(m))
        }
    }
    return sum
}

/** p(k,n) 的递推实现。 */
private fun probabilityRecurrence(n: Int, k: Int): BigDecimal {
    val good = countAtMostTwoRecurrence(n, k, MC)
    val total = BigDecimal(n).pow(k, MC)
    return BigDecimal.ONE.subtract(good.divide(total, MC), MC)
}

/** p(k,n) 的直接求和实现（对照）。 */
private fun probabilityDoubleSum(n: Int, k: Int, mc: MathContext): BigDecimal {
    val good = countAtMostTwoDoubleSum(n, k)
    val total = BigDecimal(n).pow(k, mc)   // n^k = 10^(6k)，BigDecimal 只留 40 位有效数字
    return BigDecimal.ONE.subtract(BigDecimal(good).divide(total, mc), mc)
}

private fun rounded10(x: BigDecimal): String = x.setScale(10, RoundingMode.HALF_UP).toPlainString()

/** 预热 1 次 + 5 次取中位数。 */
private fun medianMs(block: () -> BigDecimal): Double {
    block()
    val xs = DoubleArray(5)
    for (i in 0 until 5) {
        val t0 = System.nanoTime()
        block()
        xs[i] = (System.nanoTime() - t0) / 1_000_000.0
    }
    xs.sort()
    return xs[2]
}

fun main() {
    val n = 1_000_000
    val k = 20_000

    // ---- 验证 1：题面样例 p(3,7) ----
    val sample = probabilityRecurrence(7, 3)
    println("样例 p(3,7) = ${"%.15f".format(sample)}  → ${rounded10(sample)}  (题面 0.0204081633)")
    println("样例直接求和 p(3,7) = ${"%.15f".format(probabilityDoubleSum(7, 3, MC))}  A_3 = ${countAtMostTwoDoubleSum(7, 3)}")

    // ---- 验证 2：双方法互证 ----
    for ((bn, bk) in listOf(7 to 3, 20 to 8, 100 to 20, 1000 to 50, 1000 to 300)) {
        val a = probabilityRecurrence(bn, bk)
        val b = probabilityDoubleSum(bn, bk, MC)
        val diff = a.subtract(b).abs()
        println("互证 n=%d,k=%d: 递推=${"%.15f".format(a)} 直接求和=${"%.15f".format(b)} 差=${"%.1e".format(diff)} ${if (diff < BigDecimal("1e-12")) "一致" else "不一致!"}")
    }

    // ---- 主计算 ----
    val result = probabilityRecurrence(n, k)
    println("p($k, $n) = ${"%.15f".format(result)}")

    // ---- 计时：brute = 双重求和 (n=1000,k=500)，optimized = O(k) 递推跑满全规模 ----
    val bruteMs = medianMs { probabilityDoubleSum(1000, 500, MC) }
    val optMs = medianMs { probabilityRecurrence(n, k) }
    println("对照规模：brute = 直接双重求和 n=1000,k=500（optimized 跑满 n=1000000,k=20000）")
    println("BRUTE_MS: $bruteMs")
    println("OPT_MS: $optMs")
    println("ANSWER: ${rounded10(result)}")
}