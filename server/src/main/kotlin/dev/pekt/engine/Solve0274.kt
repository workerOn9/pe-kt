package dev.pekt.engine

import dev.pekt.math.modInverse
import dev.pekt.math.sieve

/**
 * PE 274 — Divisibility Multipliers（整除乘数）：对每个与 10 互素的素数 p，求满足
 * 「f(n) = ⌊n/10⌋ + (n mod 10)·m 保持被 p 整除」的乘数 m ∈ (0,p)，再对 p < 10^7 求和。
 *
 * 推导（详见 content/problems/0274/solution.kt 头部与 0274/analysis.md）：
 *   写 n = 10q + d（d = n mod 10）。取 d = 1 的倍数见证 n = 10q+1 ≡ 0 (mod p)，即 q ≡ −10⁻¹，
 *   题设强制 f(n) = q + m ≡ 0，故必须 m ≡ 10⁻¹ (mod p)；反过来 m = 10⁻¹ mod p 时
 *   f(n) ≡ (10q+d)·10⁻¹ = n·10⁻¹ (mod p)，可逆元不改变是否为零，整除性双向保持。
 *   于是答案 = Σ_{p 素数, p ≠ 2,5, p < 10^7} 10⁻¹ mod p。
 *
 * 复杂度：埃氏筛 O(N log log N) + 每个素数一次逆元 O(log p)，N = 10^7，共 664577 项，实测 < 40 ms。
 * 校验：题面锚点 m(113)=34、f(76275)=7797、f(12345)=1404 且 Σ_{p<1000} = 39517 复现；
 *   方法 B（商枚举 10m = xp+1）在 p < 10^7 上与本法逐素数一致；brute-force.kt 的完全定义式扫描
 *   （n ∈ [1,10p] 逐个检验）复现 39517 并在 p = 9999991 上做 10^8 级完全检验。
 *   逻辑与 content/problems/0274/solution.kt 的主路径（方法 A）一致。
 */
internal fun solve0274Impl(): Long {
    val limit = 10_000_000
    val isPrime = sieve(limit)
    var sum = 0L
    for (p in 3..limit step 2) {           // p = 2、5 与 10 不互素，直接跳过
        if (!isPrime[p] || p == 5) continue
        sum += modInverse(10L, p.toLong())
    }
    return sum
}
