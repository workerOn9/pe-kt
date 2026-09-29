package dev.pekt.engine

import dev.pekt.math.binomial
import dev.pekt.math.primesUpTo

/**
 * PE 268 — At Least Four Distinct Prime Factors Less Than 100（至少被 4 个小于 100 的相异素数整除）：
 * 数出 n < 10^16 中被至少四个小于 100 的相异素数整除的个数。
 *
 * 推导（详见 content/problems/0268/solution.kt 头部与 0268/analysis.md）：
 *   记 P = 小于 100 的 25 个素数，m(n) = #{p ∈ P : p | n}。二项式反演/有限差分给出
 *     [m ≥ 4] = Σ_{j≥4} w(j)·C(m, j)，  w(j) = (−1)^{j−4} C(j−1, 3)（四面体数），
 *   于是交换求和次序
 *     答案 = Σ_{|S| ≥ 4} w(|S|) · ⌊N / ∏_{p∈S} p⌋,   N = 10^16 − 1。
 *   凡 ∏_{p∈S} p > N 的子集，⌊N/∏⌋ = 0、该项恒为零 → 枚举时按乘积剪枝是精确的。
 *
 * 复杂度：只需枚举「乘积 ≤ N」的素数子集（实跑 9 595 097 个，远小于 2^25），
 *   每项 O(1) 乘除，递归深度 ≤ 25，内存 O(25)。
 * 实测与校验：本机 JIT 预热后约 28 ms；题面锚点 n < 1000 得 23，
 *   brute-force（逐个分解，n < 10^7）与 meet-in-the-middle 独立实现三方在
 *   全部小规模上逐值一致，完整答案 785478606870985。
 * 逻辑与 content/problems/0268/solution.kt 的主路径（方法 A：DFS 枚举子集）一致；
 * 素数表与二项式系数换用 dev.pekt.math 工具库（primesUpTo / binomial）。
 */
internal fun solve0268Impl(): Long {
    val primes = primesUpTo(100).map { it.toInt() } // 25 个小于 100 的素数
    val n = 10_000_000_000_000_000L - 1L            // 题面口径「小于 10^16」⇒ n ≤ 10^16 − 1

    // 容斥权重 w(j) = (−1)^{j−4} C(j−1,3)（j ≥ 4；j < 4 的位保持 0）
    val w = LongArray(primes.size + 1)
    for (j in 4..primes.size) {
        val c = binomial(j - 1, 3).toLong()
        w[j] = if ((j - 4) % 2 == 0) c else -c
    }

    var total = 0L
    fun dfs(start: Int, size: Int, prod: Long) {
        if (size >= 4) total += w[size] * (n / prod)   // 可被 ∏ 整除的 n ≤ N 有 ⌊N/∏⌋ 个
        for (i in start until primes.size) {
            val p = primes[i].toLong()
            if (prod > n / p) break                    // ∏·p > N ⇒ 该项与更长的扩展项全为 0
            dfs(i + 1, size + 1, prod * p)
        }
    }
    dfs(0, 0, 1L)
    return total
}
