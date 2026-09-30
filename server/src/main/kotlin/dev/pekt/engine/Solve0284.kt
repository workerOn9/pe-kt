package dev.pekt.engine

import java.math.BigInteger

/**
 * PE 284 — Steady Squares（稳定平方数）：数位和统计 + 14 进制答案。
 *
 * 题目：14 进制中满足 x² 与 x 末 n 位相同的数叫 n 位稳定平方数（无前导零）；求 1 ≤ n ≤ 10000 时
 * 全部 n 位稳定平方数的数位之和，答案用 14 进制小写字母输出。
 *
 * 推导（完整过程见 content/problems/0284/solution.kt 头部与 0284/analysis.md）：
 *   「末 n 位相同」= 幂等方程 x² ≡ x (mod 14ⁿ)，14ⁿ = 2ⁿ·7ⁿ 且 x、x−1 互素，故 2ⁿ、7ⁿ 各自
 *   整除掉其中一个因子，CRT 给出模 14ⁿ 恰有 4 个幂等元：0、1、Aₙ(≡0 mod 2ⁿ, ≡1 mod 7ⁿ)、
 *   Bₙ(≡1 mod 2ⁿ, ≡0 mod 7ⁿ)。已知 xₙ 时提升唯一：x_{n+1} = xₙ + d·14ⁿ，d ∈ [0,14) 由
 *     d ≡ uₙ (mod 2)，d ≡ −vₙ·(2ⁿ)^{-1} (mod 7)
 *   唯一确定（u、v 定义：A 支 u = xₙ/2ⁿ、v = (xₙ−1)/7ⁿ；B 支 u = (xₙ−1)/2ⁿ、v = xₙ/7ⁿ），
 *   提升后 u ← (u + d·7ⁿ)/2、v ← (v + d·2ⁿ)/7。又 x_{n+1} mod 14ⁿ = xₙ，故两条支的 14 进制
 *   数字串一旦写出就固定：xₙ 是同一条无限数字串的 n 位截断，各层数位和是前缀和。
 *   每层计入 Aₙ、Bₙ 中最高位非零者（题面禁止前导零），n = 1 另加数 1 本身。
 *
 * 复杂度：10000 层 × 2 支，每层对 O(n) 位的大整数做常数次加法/移位/小整数除法，总量
 *   ΣO(n/64) ≈ 3×10⁶ 词运算，本机 JIT 预热后约 130 ms，远低于 10 s 熔断线。
 *
 * 校验：定义级暴力（n ≤ 6、枚举区间内全部数）与主路径逐层一致；题面样例 Σ_{n≤9} = 582 复现；
 *   n ≤ 4 全枚举证实幂等元集合恰为 {0,1,A,B}，n ≤ 400 逐层逐候选验证提升唯一；独立 CRT 闭式
 *   Aₙ = 2ⁿ·((2ⁿ)^{-1} mod 7ⁿ)、Bₙ = 7ⁿ·((7ⁿ)^{-1} mod 2ⁿ) 在 n ≤ 512 与 n = 1000/4096/9999/10000
 *   与主路径一致，Aₙ + Bₙ = 14ⁿ + 1 与幂等性复验通过；brute-force.kt 的定义级枚举（n ≤ 8，
 *   1.6×10⁹ 个候选）与候选提升暴力给出同一数字串。本机实跑答案 604557993₁₀ = 5a411d7b₁₄。
 *
 * 本题没有素数筛 / 组合数 / gcd 等通用步骤（只用 BigInteger 的大数运算），未用到 dev.pekt.math 工具。
 */
internal fun solve0284Impl(): String {
    val maxN = 10_000

    // 幂等元分支：A ≡ 0 (mod 2ⁿ)、≡ 1 (mod 7ⁿ)；B ≡ 1 (mod 2ⁿ)、≡ 0 (mod 7ⁿ)
    val u = arrayOf(BigInteger.valueOf(4), BigInteger.valueOf(3))     // A: 8/2、B: (7−1)/2
    val v = arrayOf(BigInteger.ONE, BigInteger.ONE)                   // A: (8−1)/7、B: 7/7
    var pow2 = BigInteger.TWO
    var pow7 = BigInteger.valueOf(7)
    val b14 = BigInteger.valueOf(14)
    val two = BigInteger.TWO
    val seven = BigInteger.valueOf(7)
    val inv2Pow = intArrayOf(1, 4, 2)                                 // (2ⁿ)^{-1} mod 7，周期 3

    // 数位和前缀与逐层累计：n = 1 层 A₁ = 8、B₁ = 7 与数 1 本身
    var sumA = 8L
    var sumB = 7L
    var total = 1L + 8L + 7L
    var n = 1
    while (n < maxN) {
        for (kind in 0..1) {
            val d2 = u[kind].mod(two).toInt()                          // d ≡ u (mod 2)
            val d7 = ((-v[kind].mod(seven).toInt() * inv2Pow[n % 3]) % 7 + 7) % 7
            var d = 0
            while (d < 14 && !(d % 2 == d2 && d % 7 == d7)) d++
            u[kind] = u[kind].add(pow7.multiply(BigInteger.valueOf(d.toLong()))).shiftRight(1)  // (u+d·7ⁿ)/2
            v[kind] = v[kind].add(pow2.multiply(BigInteger.valueOf(d.toLong()))).divide(seven)  // (v+d·2ⁿ)/7
            if (kind == 0) {
                sumA += d
                if (d != 0) total += sumA                             // Aₙ₊₁ 最高位非 0 才是合法 n+1 位数
            } else {
                sumB += d
                if (d != 0) total += sumB
            }
        }
        pow2 = pow2.shiftLeft(1)
        pow7 = pow7.multiply(seven)
        n++
    }

    // 十进制结果按 14 进制小写字母输出（题面要求 14 进制）
    val sb = StringBuilder()
    var rest = total
    while (rest > 0L) {
        val d = (rest % 14L).toInt()
        sb.append(if (d < 10) ('0' + d) else ('a' + d - 10))
        rest /= 14L
    }
    return sb.reverse().toString()
}
