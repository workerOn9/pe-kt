package dev.pekt.engine

import java.math.BigInteger

/**
 * PE 294 — 数位和体验 #23（Sum of Digits - Experience #23）：S(11^12) mod 10^9 = 789184709。
 *
 * 推导（详见 content/problems/0294/solution.kt 头部与 0294/analysis.md）：
 *   k < 10^n ⇔ n 位十进制串，数位和 = 23 且 Σ d_i·10^i ≡ 0 (mod 23)。
 *   ord_23(10) = 22（10^11 ≡ −1），位权按周期分组：n = 22q+r，r 个权类出现 q+1 次、
 *   其余出现 q 次。权类 w（c 个位置、数字和 j）的贡献为 w·j，方案数
 *     a_c(j) = Σ_t (−1)^t C(c,t)·C(c+j−10t−1, j−10t)（t ≤ 2，隔板 + 上界容斥）。
 *   22 个权类做卷积 DP：状态 (已用数位和 ≤ 23, 加权和 mod 23) = 24×23 格，全部模 10^9。
 *
 *   本题 n = 11^12 = 3138428376721，n mod 22 = 11。
 *
 * 复杂度：约 3×10^5 次乘加，毫秒级；内存 24×23 两层 LongArray。
 */
internal fun solve0294Impl(): Long {
    val n = 3_138_428_376_721L                        // 11^12
    val mod = 1_000_000_000L

    // 位权类计数
    val cnt = LongArray(23)
    val q = n / 22
    val r = (n % 22).toInt()
    var w = 1L
    for (i in 0 until 22) {
        cnt[w.toInt()] += q
        if (i < r) cnt[w.toInt()] += 1L
        w = w * 10L % 23L
    }

    fun binomMod(nn: Long, k: Int): Long {
        if (k < 0 || nn < k) return 0L
        var num = BigInteger.ONE
        var den = BigInteger.ONE
        for (i in 0 until k) {
            num = num.multiply(BigInteger.valueOf(nn - i))
            den = den.multiply(BigInteger.valueOf((i + 1).toLong()))
        }
        return num.divide(den).mod(BigInteger.valueOf(mod)).toLong()
    }

    fun coeffs(c: Long): LongArray {
        val out = LongArray(24)
        if (c == 0L) {
            out[0] = 1L
            return out
        }
        for (j in 0..23) {
            var tot = 0L
            var t = 0
            while (10 * t <= j) {
                val term = binomMod(c, t) * binomMod(c + j - 10 * t - 1, j - 10 * t) % mod
                tot = if (t % 2 == 0) (tot + term) % mod else (tot - term + mod) % mod
                t++
            }
            out[j] = tot
        }
        return out
    }

    var dp = Array(24) { LongArray(23) }
    dp[0][0] = 1L
    for (w in 1..22) {
        if (cnt[w] == 0L) continue
        val a = coeffs(cnt[w])
        val ndp = Array(24) { LongArray(23) }
        for (s in 0..23) {
            for (m in 0 until 23) {
                val v = dp[s][m]
                if (v == 0L) continue
                var j = 0
                while (s + j <= 23) {
                    if (a[j] != 0L) {
                        val nm = (m + w * j) % 23
                        ndp[s + j][nm] = (ndp[s + j][nm] + v * a[j]) % mod
                    }
                    j++
                }
            }
        }
        dp = ndp
    }
    return dp[23][0]
}
