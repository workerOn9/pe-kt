package dev.pekt.engine

import java.math.BigInteger

/**
 * PE 326 — Modulo Summations（模和配对）
 *
 * a₁=1，aₙ=(Σ_{k<n} k·aₖ) mod n。记 S(n)=Σ k·aₖ、P(n)=Σ aᵢ，则区间和被 M 整除
 * ⟺ P(q)≡P(p−1) (mod M)，故 f(N,M)=Σ_r C(c_r,2)（c_r 为 P(0..N) 模 M 余 r 的个数）。
 * P(n) mod M 以周期 T=6M 循环，于是只递推一个周期即可处理 N=10¹²。
 * S(n) 峰值约 2⁶⁵ 超出 Long，用 BigInteger 精确递推。
 *
 * 最终答案：f(10¹², 10⁶) = 1966666166408794329。
 */
internal fun solve0326Impl(): Long {
    val m = 1_000_000L
    val n = 1_000_000_000_000L
    val t = 6 * m
    val rem = n % t
    val mm = m.toInt()
    val cnt1 = IntArray(mm)
    val cnt2 = IntArray(mm)
    var s = BigInteger.ZERO
    var p = 0L
    cnt1[0]++
    cnt2[0]++
    var i = 1L
    while (i < t) {
        val a = if (i == 1L) 1L else s.mod(BigInteger.valueOf(i)).toLong()
        s = s.add(BigInteger.valueOf(i * a))
        p = (p + a) % m
        val idx = p.toInt()
        cnt1[idx]++
        if (i <= rem) cnt2[idx]++
        i++
    }
    val q = n / t
    var tot = BigInteger.ZERO
    for (r in 0 until mm) {
        val c = q * cnt1[r] + cnt2[r]
        if (c >= 2) {
            tot = tot.add(BigInteger.valueOf(c).multiply(BigInteger.valueOf(c - 1)).shiftRight(1))
        }
    }
    return tot.toLong()
}
