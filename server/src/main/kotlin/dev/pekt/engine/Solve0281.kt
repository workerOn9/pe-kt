package dev.pekt.engine

import dev.pekt.math.binomial
import java.math.BigInteger

/**
 * PE 281 — Pizza Toppings（比萨配料）：圆形比萨切成 m·n 块等大扇形，用 m 种配料每块放一种、
 * 每种恰好用在 n 块上；旋转同一、反射不同。求所有 f(m,n) ≤ 10^15 之和。
 *
 * 推导（详见 content/problems/0281/solution.kt 头部与 0281/analysis.md）：
 *   把方案看成长度 N = mn 的环形珠串（每种颜色恰 n 次），旋转群 C_N 的 Burnside 引理给出
 *     f(m,n) = (1/N) Σ_{r<N} Fix(r)，平移 r 的循环分解有 g = gcd(r,N) 个循环、循环长 L = N/g；
 *     被 r 固定的着色在每个循环上常色，故 L | n 时 Fix(r) = g!/((n/L)!^m)（否则 0）。
 *   按循环长归并（gcd(r,N) = N/d 的 r 有 φ(d) 个，且 d | N ∧ d | n ⟺ d | n）得主路径
 *     f(m,n) = (1/(mn)) Σ_{d | n} φ(d) · (mn/d)! / ((n/d)!^m),
 *   多项系数用 dev.pekt.math.binomial 逐次相乘；末尾除 mn 用除法余数断言自检（Burnside 保证整除）。
 *
 * 枚举范围：f 关于 m、n 严格递增（solution.kt 在 m ≤ 22、n ≤ 40 的 840 个网格点上逐点断言），
 *   故 n 列到第一个超限值即可停、m 在 (m−1)! > 10^15 的 m = 19 处收口。共 74 个 (m,n) 对。
 *
 * 复杂度：Σ τ(n) ≈ 160 个多项系数，每个 O(m) 次 BigInteger 乘除，规模最大到 C(58,29) ≈ 3×10^16；
 *   实测约 0.1 ms（JIT 预热后），远低于 10 s 熔断线。
 *   校验：题面四个样例 1/2/2/16；方法 B（逐旋转）与非周期项链 / Möbius 反演路径在 solution.kt 与
 *   brute-force.kt 中给出同一总和 1485776387445623；二面体群对照组 (3,2) = 11 ≠ 16 钉死群的选择。
 */
internal fun solve0281Impl(): Long {
    val limit = BigInteger.valueOf(1_000_000_000_000_000L)      // 10^15

    fun phi(n: Int): Int {
        var x = n
        var r = n
        var p = 2
        while (p * p <= x) {
            if (x % p == 0) {
                while (x % p == 0) x /= p
                r -= r / p
            }
            p++
        }
        if (x > 1) r -= r / x
        return r
    }

    /** (mk)!/(k!^m)（把 mk 个位置均分给 m 种颜色）= ∏ C(剩余位置, k)。 */
    fun multinomial(colors: Int, perColor: Int): BigInteger {
        var r = BigInteger.ONE
        var rest = colors * perColor
        for (left in colors downTo 2) {
            r = r.multiply(binomial(rest, perColor))
            rest -= perColor
        }
        return r
    }

    /** f(m,n) = (1/(mn)) Σ_{d | n} φ(d)·(mn/d)!/((n/d)!^m)。 */
    fun f(m: Int, n: Int): BigInteger {
        var s = BigInteger.ZERO
        var d = 1
        while (d * d <= n) {
            if (n % d == 0) {
                s = s.add(multinomial(m, n / d).multiply(BigInteger.valueOf(phi(d).toLong())))
                val e = n / d
                if (e != d) s = s.add(multinomial(m, n / e).multiply(BigInteger.valueOf(phi(e).toLong())))
            }
            d++
        }
        val mn = BigInteger.valueOf(m.toLong() * n)
        val (q, r) = s.divideAndRemainder(mn)
        check(r.signum() == 0) { "Burnside 总和不能被 mn 整除：m=$m n=$n" }
        return q
    }

    var total = 0L
    var m = 2
    while (true) {
        var n = 1
        while (true) {
            val v = f(m, n)
            if (v > limit) break
            total += v.toLong()
            n++
        }
        if (m >= 19) break                                      // (19−1)! = 6.4×10^15 > 10^15
        m++
    }
    return total
}
