#!/usr/bin/env kotlin
/**
 * Project Euler 267 — Billionaire（亿万富翁）
 *
 * 思路
 * ────
 * 初始资本 £1，每投掷按资本的比例 f 下注：正面资本乘 (1+2f)（投入 b 净赢 b，
 * 例 f=1/4 时 1 → 1.5），反面乘 (1−f)。1000 次后若出现 h 次正面，资本为
 *
 *   C_h(f) = (1+2f)^h · (1−f)^{1000−h}.
 *
 * 固定 f 时 C_h 关于 h 递增（相邻比 (1+2f)/(1−f) > 1），所以「至少 £1e9」这个事件
 * 就是 {H ≥ k(f)}，其中 k(f) 是使 C_h(f) ≥ 1e9 的最小整数 h；于是
 *
 *   P(f) = P(H ≥ k(f)),   H ~ Bin(1000, 1/2).
 *
 * P(f) 是 k 的阶梯函数，要最大概率只需**让所需正面次数最少**：k₀ = min_f k(f)。
 * 对固定 k 最大化 C_k(f)：由 2k/(1+2f) = (1000−k)/(1−f) 得
 *
 *   f_k = (3k − 1000)/2000,
 *
 * 代回并乘去分母，条件 C_k(f_k) ≥ 1e9 等价于**整数不等式**
 *
 *   3^1000 · k^k · (1000−k)^{1000−k}  ≥  10^9 · 1000^k · 2000^{1000−k} .
 *
 * 实跑：k=431 时左/右比值为 8.9902×10⁸ < 10⁹（不可行），k=432 时为 1.3647×10⁹ ≥ 10⁹
 * （可行，f₄₃₂ = 37/250 = 0.148 附近），故 k₀ = 432。任何 f 的所需正面次数都不少于 432，
 * 而区间内的 f 恰好达到 432，所以最大概率
 *
 *   P* = P(H ≥ 432) = 1 − Σ_{h=0}^{431} C(1000,h) / 2^1000
 *      = 0.9999928361867135946707…  →  四舍五入 12 位：0.999992836187。
 *
 * 「所有计算精确」体现在两处：k=431/432 的临界判断用 BigInteger 整数不等式精确判定；
 * 概率用精确分数 (2^1000 − ΣC)/2^1000 再一次性做半数进位取 12 位小数，不用浮点累加。
 *
 * 复杂度
 * ──────
 * k₀ 的定位：先用双精度对数粗筛（1000 次幂的 log10），再对临界 k 做一次 BigInteger
 * 精确校验（O(k₀) 位的整数幂，毫秒级）；概率求和 O(1000) 次 BigInteger 乘除（数位 ~300）。
 * 方法 B 用 double 递推生存函数 O(1000)，两者互证。
 *
 * 验证
 * ────
 * 1. 题面锚点：f=1/4 时一次正面后 £1.5、正面接反面后 £1.125，程序 check 通过；
 * 2. 临界性：k=431 的最大资本 8.9902×10⁸（不足 1e9）、k=432 为 1.3647×10⁹（足够），
 *    均由 BigInteger 精确不等式给出；再确认所有 f ∈ [0,1] 都做不到 431 次以内成功；
 * 3. 独立复核（方法 B）：double 递推的生存函数 P(H≥k) 与精确分数一致到 1e-15 以内；
 * 4. 暴力扫描（brute-force.kt）：对 f 做网格扫描（含直接枚举 k(f)），得到的最大值与解析
 *    结果在 12 位小数上完全相同。
 *
 * 答案编码：12 位小数 → meta.json 存 round(0.999992836186713…×10¹²) = 999992836187。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0267/solution.kt && java -cp … SolutionKt
 */
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

/** 1000 次投掷中出现 h 次正面时的资本（f 为下注比例）。 */
fun capital(f: Double, h: Int, n: Int): Double = (1.0 + 2.0 * f).pow(h) * (1.0 - f).pow(n - h)

/** 固定 f 时达到 10^9 所需的最少正面次数；不可能时返回 n+1。 */
fun neededWins(f: Double, n: Int, logTarget: Double): Int {
    for (h in 0..n) {
        val lg = h * ln(1.0 + 2.0 * f) + (n - h) * ln(1.0 - f)
        if (lg >= logTarget) return h
    }
    return n + 1
}

fun main() {
    println("=== PE 267 Billionaire ===")
    val n = 1000
    val target = BigInteger.TEN.pow(9)

    // ---- 题面锚点 ----
    check(abs(capital(0.25, 1, 1) - 1.5) < 1e-12)       // 第一次正面后 £1.5
    check(abs(capital(0.25, 1, 2) - 1.125) < 1e-12)      // 正面后接反面得 £1.125
    println("题面锚点通过: f=1/4 → 一次正面资本 = %.4f；正面接反面 = %.4f".format(capital(0.25, 1, 1), capital(0.25, 1, 2)))

    // ---- 方法 A：精确判定 k0 = min_f k(f)（临界 k 用 BigInteger） ----
    val logTarget = 9.0 * log10(10.0)
    fun maxCapLog10(k: Int): Double {                     // log10 max_f C_k(f)
        val f = (3.0 * k - 1000.0) / 2000.0
        return k * log10(1.0 + 2.0 * f) + (n - k) * log10(1.0 - f)
    }
    fun exactFeasible(k: Int): Boolean {                  // 3^1000 k^k (n-k)^{n-k} >= 1e9 1000^k 2000^{n-k}
        val lhs = BigInteger.valueOf(3).pow(n) *
            BigInteger.valueOf(k.toLong()).pow(k) *
            BigInteger.valueOf((n - k).toLong()).pow(n - k)
        val rhs = target *
            BigInteger.valueOf(1000L).pow(k) *
            BigInteger.valueOf(2000L).pow(n - k)
        return lhs >= rhs
    }
    var k0 = -1
    for (k in 334..n) {
        if (abs(maxCapLog10(k) - 9.0) < 1e-9) {           // 双精度恰好落在临界附近，需精确判别
            check(exactFeasible(k))
            k0 = k; break
        }
        if (maxCapLog10(k) > 9.0) { k0 = k; break }
    }
    check(k0 > 0)
    // 临界性双重确认：k0 - 1 不可行、k0 可行
    val feasiblePrev = exactFeasible(k0 - 1)
    val feasibleCur = exactFeasible(k0)
    check(!feasiblePrev && feasibleCur)
    val ratio = if (k0 == 432) "%.6e vs %.6e".format(feasibleCap(k0 - 1), feasibleCap(k0)) else "-"
    println("需要的最少正面次数 k0 = %d（k0-1 不可行、k0 可行；最大资本 %s）".format(k0, ratio))

    // ---- 方法 A：精确概率 P(H >= k0) = (2^n - Σ_{h<k0} C(n,h)) / 2^n ----
    var sum = BigInteger.ZERO
    var binom = BigInteger.ONE
    for (h in 0 until k0) {
        sum += binom
        binom = binom * BigInteger.valueOf((n - h).toLong()) / BigInteger.valueOf((h + 1).toLong())
    }
    val pow2 = BigInteger.ONE.shiftLeft(n)
    val favourable = pow2 - sum
    val scale = BigInteger.TEN.pow(12)
    val rounded = (favourable * scale * BigInteger.TWO + pow2) / (pow2 * BigInteger.TWO)  // round half up
    val digits = rounded.toString().padStart(12, '0')
    println("精确分数：%s / 2^1000".format(favourable.toString().take(6) + "…"))
    println("方法 A（精确分数 → 12 位半数进位）= 0.%s".format(digits))

    // ---- 方法 B：double 递推生存函数（独立数值路线） ----
    val surv = DoubleArray(n + 2)
    var term = 2.0.pow(-n)
    for (h in n downTo 0) {
        surv[h] = surv[h + 1] + term
        if (h > 0) term = term * h / (n - h + 1)
    }
    check(abs(surv[k0] - favourable.toDouble() / 2.0.pow(n)) < 1e-15)
    println("方法 B（double 递推）= %.15f  （与精确分数差 %.2e）".format(surv[k0], abs(surv[k0] - favourable.toDouble() / 2.0.pow(n))))
    check(abs(surv[k0] - favourable.toDouble() / 2.0.pow(n)) < 1e-14)

    // ---- 计时：JIT 预热后 3 轮取最优（整条主路径：定位 k0 + 精确概率 + 取整） ----
    fun fullMainPath(): BigInteger {
        var kk = -1
        for (k in 334..n) {
            if (maxCapLog10(k) > 9.0) { kk = k; break }
        }
        check(exactFeasible(kk))
        var s2 = BigInteger.ZERO
        var b2 = BigInteger.ONE
        for (h in 0 until kk) {
            s2 += b2
            b2 = b2 * BigInteger.valueOf((n - h).toLong()) / BigInteger.valueOf((h + 1).toLong())
        }
        val p2 = BigInteger.ONE.shiftLeft(n)
        val fav2 = p2 - s2
        return (fav2 * scale * BigInteger.TWO + p2) / (p2 * BigInteger.TWO)
    }
    var bestMs = 0.0
    for (round in 0 until 3) {
        val t0 = System.nanoTime()
        val r2 = fullMainPath()
        val ms = (System.nanoTime() - t0) / 1e6
        if (round == 0 || ms < bestMs) bestMs = ms
        check(r2.toString() == rounded.toString())
    }

    // ---- 答案 ----
    println()
    println("答案 = 0.%s".format(digits))
    println("编码 round(值×10¹²) = %s".format(rounded.toString()))
    println("方法 A 最优耗时 = %.1f ms".format(bestMs))
}

/** 供输出诊断：k 对应的最优 f 下的最大资本（double 估算值）。 */
fun feasibleCap(k: Int): Double {
    val f = (3.0 * k - 1000.0) / 2000.0
    return (1.0 + 2.0 * f).pow(k) * (1.0 - f).pow(1000 - k)
}
