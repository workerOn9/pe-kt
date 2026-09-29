package dev.pekt.engine

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.log10

/**
 * PE 267 — Billionaire（亿万富翁）：从 £1 出发，1000 次公平硬币投掷中每次按资本的固定比例 f
 * 下注，正面资本乘 (1+2f)、反面乘 (1−f)。选择使「最终资本 ≥ £10⁹」概率最大的 f，求该概率
 * （12 位小数答案 ×10¹² 编码）。
 *
 * 推导（详见 content/problems/0267/solution.kt 头部与 0267/analysis.md）：
 *   h 次正面时资本 C_h(f) = (1+2f)^h (1−f)^{1000−h}，关于 h 严格递增，故成功事件为
 *   {H ≥ k(f)}（H ~ Bin(1000,1/2)，k(f) 为所需最少正面次数），且
 *     max_f P(f) = Pr(H ≥ k₀),  k₀ = min_f k(f)。
 *   固定 k 最大化 C_k 得 f_k = (3k−1000)/2000，代回并去分母后可行性等价于整数不等式
 *     3^1000 · k^k · (1000−k)^{1000−k} ≥ 10⁹ · 1000^k · 2000^{1000−k}。
 *   实跑：k=431 时最大资本 8.990218×10⁸ < 10⁹（不可行）；k=432 时 1.364742×10⁹ ≥ 10⁹
 *   （可行，f=37/250=0.148），故 k₀=432（最优 f 区间 [0.129515, 0.166570]）。
 *   精确概率 = (2^1000 − Σ_{h=0}^{431} C(1000,h)) / 2^1000 = 0.99999283618671359…
 *   取 12 位小数：round(×10¹²) = 999992836187。
 *
 * 复杂度：先用双精度对数粗筛定位 k₀（~100 次 log10），再对临界 k 做一次 BigInteger 整数幂
 *   精确校验；概率分子用 1000 次大整数乘除（数位 ~300）。本机 JIT 预热后约 0.6 ms。
 *   逻辑与 content/problems/0267/solution.kt 的主路径（方法 A）一致；独立复核见该文件方法 B
 *   （double 递推生存函数，与精确分数差 4.4×10⁻¹⁶）与 brute-force.kt（f 网格扫描，12 位一致）。
 */
internal fun solve0267Impl(): Long {
    val n = 1000
    val target = BigInteger.TEN.pow(9)

    fun maxCapLog10(k: Int): Double {
        val f = (3.0 * k - 1000.0) / 2000.0
        return k * log10(1.0 + 2.0 * f) + (n - k) * log10(1.0 - f)
    }
    fun exactFeasible(k: Int): Boolean {
        val lhs = BigInteger.valueOf(3).pow(n) *
            BigInteger.valueOf(k.toLong()).pow(k) *
            BigInteger.valueOf((n - k).toLong()).pow(n - k)
        val rhs = target *
            BigInteger.valueOf(1000L).pow(k) *
            BigInteger.valueOf(2000L).pow(n - k)
        return lhs >= rhs
    }
    // 定位 k0：f_k > 0 要求 k ≥ 334；用对数粗筛，临界处再用 BigInteger 精确判定
    var k0 = -1
    for (k in 334..n) {
        val lg = maxCapLog10(k)
        if (lg > 9.0) { k0 = k; break }
        if (abs(lg - 9.0) < 1e-9) { if (exactFeasible(k)) { k0 = k; break } }
    }
    check(k0 > 0)

    // 精确概率分子：favourable = 2^n − Σ_{h<k0} C(n,h)
    var sum = BigInteger.ZERO
    var binom = BigInteger.ONE
    for (h in 0 until k0) {
        sum += binom
        binom = binom * BigInteger.valueOf((n - h).toLong()) / BigInteger.valueOf((h + 1).toLong())
    }
    val pow2 = BigInteger.ONE.shiftLeft(n)
    val favourable = pow2 - sum
    // 12 位小数，round half up：floor((2·favourable·10¹² + 2^n) / 2^{n+1})
    val scale = BigInteger.TEN.pow(12)
    return ((favourable * scale * BigInteger.TWO + pow2) / (pow2 * BigInteger.TWO)).toLong()
}
