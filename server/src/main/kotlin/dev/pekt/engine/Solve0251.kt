package dev.pekt.engine

import dev.pekt.math.gcd
import dev.pekt.math.modInverse

/**
 * PE 251 — 卡尔达诺三元组（Cardano Triplets）：∛(a+b√c) + ∛(a−b√c) = 1，计数 a+b+c ≤ 1.1×10⁸。
 *
 * 推导（详见 content/problems/0251/solution.kt 头部）：
 *   记 x = ∛(a+b√c)、y = ∛(a−b√c)，由 (x+y)³ = x³+y³+3xy(x+y) 与 x+y = 1 得
 *   1 = 2a + 3∛(a²−b²c)，即 27b²c = 27a²+(2a−1)³ = (a+1)²(8a−1)，从而 a ≡ 2 (mod 3)。
 *   取 p = gcd(b,m)、b = pr、m = pq（m = (a+1)/3，gcd(q,r) = 1），则 r² | 8m−3；
 *   写 s = (8m−3)/r² 得双射参数化
 *       a = 3pq−1，b = pr，c = sq²，8pq = sr²+3（r 奇），约束 p(3q+r) + sq² ≤ N+1。
 *
 * 算法（方法 A）：按 (q,r) 枚举，p 落在模 r² 的等差数列上
 *   p ≡ p₀ = 3·(8q)^{-1} (mod r²)，p 每加 r² 总和增加 step = r²(3q+r)+8q³，
 *   贡献 = ⌊(L−base)/step⌋+1；r = 1 单独用一次除法整列计数。
 *   每个 (q,r) 的 s₀ = (−3/r²) mod 8q 用同一模数下的批量求逆取得
 *   （前缀积 + 一次 modInverse + 回代），把 4.9×10⁷ 次模逆压到每个 q 一次。
 *
 * 复杂度：约 4.9×10⁷ 个 (q,r) 对 + 每 q 一次模逆；实测（本机 JIT 预热后）≈ 1.8 s，
 * 远低于 10 s 的引擎熔断线。答案 = 18946051。
 */
internal fun solve0251Impl(): Long {
    val n = 110_000_000L
    val limit = n + 1
    var total = 0L
    val maxR = 40_000
    val rVals = IntArray(maxR)
    val squareMods = IntArray(maxR)
    val prefix = LongArray(maxR + 1)
    var q = 1L
    while (q * q + 3 * q <= n) {
        val q2 = q * q
        val q3 = q2 * q
        val mod = 8 * q
        // r = 1：s = 8pq−3，约束化为 p(8q³+3q+1) ≤ L+3q²
        total += (limit + 3 * q2) / (8 * q3 + 3 * q + 1)
        // 收集候选 r：奇、gcd(q,r)=1、r³+3qr²+8q³ ≤ 8qL（由 s ≥ 1 的必要界）
        var count = 0
        var r = 3L
        while (r * r * r + 3 * q * r * r + 8 * q3 <= 8 * q * limit) {
            if (gcd(r, q) == 1L) {
                rVals[count] = r.toInt()
                squareMods[count] = ((r * r) % mod).toInt()
                count++
            }
            r += 2
        }
        if (count > 0) {
            // 批量求逆：prefix[i] = x₁·x₂·…·x_i (mod 8q)，x_i = r_i² mod 8q
            prefix[0] = 1L
            for (i in 0 until count) prefix[i + 1] = prefix[i] * squareMods[i] % mod
            var invAll = modInverse(prefix[count], mod)
            for (i in count - 1 downTo 0) {
                val invX = invAll * prefix[i] % mod
                invAll = invAll * squareMods[i] % mod
                val s0 = mod - 3 * invX % mod          // (−3·x⁻¹) mod 8q ∈ [1, 8q)
                val rv = rVals[i].toLong()
                val r2 = rv * rv
                val step = r2 * (3 * q + rv) + 8 * q3
                if (s0 * step <= 8 * q * limit - 9 * q - 3 * rv) {
                    val p0 = (3 + s0 * r2) / mod
                    val base = p0 * (3 * q + rv) + s0 * q2
                    total += (limit - base) / step + 1
                }
            }
        }
        q++
    }
    return total
}
