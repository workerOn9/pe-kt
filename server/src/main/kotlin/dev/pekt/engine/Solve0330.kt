package dev.pekt.engine

import dev.pekt.math.modInverse
import dev.pekt.math.modPow

/**
 * PE 330 — Euler's Number（欧拉常数递推）
 *
 * 题目：a(n)=1 (n<0)，a(n)=Σ_{i≥1} a(n−i)/i! (n≥0)。已知 a(n)=(A(n)e+B(n))/n!，
 *      a(10)=(328161643e−652694486)/10!。求 A(10⁹)+B(10⁹) mod 77777777。
 *
 * 推导（详见 content/problems/0330/solution.kt 头部与 0330/analysis.md）：
 *
 *   1. 令 c(n)=n!·a(n)=A(n)e+B(n)，把递推乘 n! 并分离 e 系数 / 常数项：
 *        A(n)=Σ_{i<n}C(n,i)A(i)+n!,   B(n)=Σ_{i<n}C(n,i)B(i)−Σ_{i=0}^{n}n!/i!。
 *   2. 二项变换的 EGF 是乘 eˣ，得 Â(x)=1/((1−x)(2−eˣ))、B̂=−eˣÂ，于是
 *        B(n)=n!−2A(n)  ⟹  A(n)+B(n)=n!−A(n)。
 *   3. 展开 1/(1−x)·1/(2−eˣ)：1/(2−eˣ)=Σ_m(eˣ−1)^m，系数是 Fubini 数
 *        A(n)=Σ_{d=0}^{n}(n)_d·F(n−d)。
 *   4. M=77777777=7·11·73·101·137 无平方因子且 M|137!，故 (n)_d=d!C(n,d)≡0 (d≥137)；
 *      又 n=10⁹ 时 n!≡0，答案 ≡ −Σ_{d=0}^{136}(10⁹)_d F(10⁹−d)。
 *   5. F(m)≡Σ_{k=0}^{p−1}k!S2(m,k)=Σ_{j=0}^{p−1}j^m·c_j (mod p)，c_j=Σ_{k=j}^{p−1}(−1)^{k−j}C(k,j)；
 *      对 5 个素因子求值后 CRT 合并。
 *
 * 自证：精确递推给出 A(10)=328161643、B(10)=−652694486；截断公式与 O(n²) 直接递推在
 *      n∈{500,1000,2000,4000} 上模 M 一致。
 *
 * 最终答案：15955822。本机实测（JIT 预热后 3 轮最快）：约 4.7 ms。
 */
internal fun solve0330Impl(): Long {
    val mod = 77_777_777L
    val primes = intArrayOf(7, 11, 73, 101, 137)
    val dMax = 136
    val n = 1_000_000_000L

    val cs = Array(primes.size) { coeffs330(primes[it]) }

    // (n)_d mod M
    val ff = LongArray(dMax + 1)
    ff[0] = 1L
    for (d in 1..dMax) ff[d] = ff[d - 1] * Math.floorMod(n - (d - 1), mod) % mod

    var a = 0L
    for (d in 0..dMax) a = (a + ff[d] * fubiniModM330(n - d, primes, cs, mod)) % mod
    return (mod - a) % mod
}

/** 每个素数 p 的系数 c_j，使 F(m)≡Σ_j c_j·j^m (mod p)。 */
private fun coeffs330(p: Int): LongArray {
    val binom = Array(p) { LongArray(p) }
    for (k in 0 until p) {
        binom[k][0] = 1L
        for (j in 1..k) binom[k][j] = (binom[k - 1][j - 1] + binom[k - 1][j]) % p
    }
    val c = LongArray(p)
    for (j in 0 until p) {
        var s = 0L
        for (k in j until p) {
            val t = binom[k][j]
            s = if (((k - j) and 1) == 0) (s + t) % p else (s - t + p) % p
        }
        c[j] = s
    }
    return c
}

/** F(m) mod p；F(0)=1。 */
private fun fubiniModP330(m: Long, p: Int, c: LongArray): Long {
    if (m == 0L) return 1L % p
    var s = 0L
    for (j in 1 until p) s = (s + c[j] * modPow(j.toLong(), m, p.toLong())) % p
    return s
}

/** F(m) mod M：对各素因子求值后用 CRT 合并。 */
private fun fubiniModM330(m: Long, primes: IntArray, cs: Array<LongArray>, mod: Long): Long {
    var x = 0L
    for (i in primes.indices) {
        val p = primes[i].toLong()
        val mi = mod / p
        val inv = modInverse(mi % p, p)
        val r = fubiniModP330(m, primes[i], cs[i])
        x = (x + r * mi % mod * inv) % mod
    }
    return x
}
