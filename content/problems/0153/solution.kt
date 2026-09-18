/**
 * Project Euler 153 — Investigating Gaussian Integers（探究高斯整数）
 *
 * 思路：关键观察是高斯整数除法的性质。
 * n / (a + bi) 为高斯整数 ⇔ n·(a - bi) / (a² + b²) 为高斯整数
 *    ⇔ a² + b² 整除 n·a 且 a² + b² 整除 n·b
 *    ⇔ a² + b² | n·gcd(a,b)。特别地，当 gcd(a,b)=1 时，要求 a² + b² | n。
 *
 * 因此，高斯整数 d = a + bi（实部 > 0）整除 n 当且仅当 |d|² = a² + b² 整除 n 或 n·gcd(a,b)。
 *
 * 定义：设 σ₂(n) 为所有满足 a² + b² | n 的 (a,b) 对的个数（实部>0），并记 s(n) 为这些因数的和。
 *
 * 进一步简化：对每个正整数 n，其高斯因数分为两类：
 *  1. 有理整数因数 d（即 b=0）：贡献 d 到 s(n)
 *  2. 虚高斯整数 d = a + bi（a>0, b≠0）：贡献 a 到 s(n)，虚部不计入和（题目要求的 s(n) 是实部和）
 *
 * 但题面说 s(n) 是「所有实部为正的因子的和」，并按复数加法。这意味着 s(n) 是一个高斯整数！
 * 最终问题是：Σ s(n) 对所有 n=1..10⁸ 的实部和是多少？
 *
 * 关键观察：
 * - 每个有理因子 d|n 都贡献 d 到 s(n)（实部和）
 * - 每个非零虚因子 a+bi（a>0）贡献 a 到实部和，同时其共轭 a-bi 也贡献 a，故总贡献 2a
 *
 * 换序计算：枚举所有可能的 a² + b² = k ≤ n，统计它作为模数整除 n 的次数。
 * 公式：∑ s(n) = ∑_{k=1}^{10^8} floor(10^8/k) · Σ_{a²+b²=k, a>0} 2a
 */

import dev.pekt.math.primesUpTo

fun solve(): Long {
    val LIMIT = 100_000_000L
    
    // Precompute: for each k, S[k] = sum of 2*a over all a²+b²=k, a>0
    val maxK = LIMIT.toInt()
    val S = LongArray(maxK + 1) { 0 }
    
    // Generate sums of squares a²+b²=k
    for (a in 1 until 4500) {  // sqrt(LIMIT) ≈ 31622
        val a2 = a * a
        if (a2 > maxK) break
        for (b in 0 until 4500) {
            val k = a2 + b * b
            if (k > maxK) break
            val contribution = if (b == 0) a else 2L * a
            S[k] += contribution
        }
    }
    
    // Sum over k: floor(LIMIT/k) * S[k]
    var totalSum = 0L
    var k = 1
    while (k <= maxK) {
        val nextK = (LIMIT / (LIMIT / k)).toInt() + 1
        // Range [k, min(nextK-1, maxK)]
        val end = kotlin.math.min(nextK - 1, maxK)
        val count = LIMIT / k
        var sumS = 0L
        for (i in k..end) sumS += S[i]
        totalSum += count * sumS
        k = end + 1
    }
    
    return totalSum
}

fun main() {
    repeat(3) { solve() }  // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
