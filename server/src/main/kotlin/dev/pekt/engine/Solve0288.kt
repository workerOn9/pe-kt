package dev.pekt.engine

import dev.pekt.math.modInverse

/**
 * PE 288 — An Enormous Factorial（巨大的阶乘）：N(p, q) = Σ_{n=0}^{q} T_n·pⁿ，其中
 * S₀ = 290797、S_{n+1} = S_n² mod 50515093、T_n = S_n mod p；NF(p, q) 是 N(p, q)! 中因子 p 的
 * 个数。已知 NF(3, 10000) mod 3^20 = 624955285，求 NF(61, 10^7) mod 61^10（本题答案
 * 605857431263981935 是 61^10 ≈ 7.13×10^17 以下的数，直接作为 Long 返回）。
 *
 * 推导（与 content/problems/0288/solution.kt 的主路径逐行一致）：
 *   1) 勒让德公式 NF(p, q) = Σ_{k≥1} ⌊N/pᵏ⌋ = (N − s_p(N)) / (p − 1)，s_p(N) 为 N 的 p 进制数位和。
 *      本题 N 的 p 进制表示恰为 T_q T_{q−1} … T_0（每个 T_n < p，无进位），故 s_p(N) = Σ T_n。
 *   2) 只需 N mod 61^10 与 s_p(N) mod 61^10，再乘 (p − 1)⁻¹ mod 61^10。
 *   3) N 有 10^7 + 1 位 p 进制数字，用流式 Horner 从最高位递推 N ← N·p + T_n (mod 61^10)。
 *      61^10 < 2^60，乘 61 后 ≈ 4.35×10^19 超出 Long：用 Math.multiplyHigh 把 a·61 拆成
 *      hi·2^64 + lo，2^64 mod 61^10 为常数，模余结果恰好落在 Long 内（hi ≤ 2，条件减法即可）。
 *   4) 数字需先由低位生成、再按高位到低位消费，缓存进 ByteArray（T_n < 61，1 字节一项，
 *      约 10 MB），同一遍里累加数位和。
 *
 * 与 solution.kt 的差异：逆元改用工具库 [modInverse]（solution 文件里用的是闭式
 * (p−1)⁻¹ ≡ −(1 + p + ⋯ + p^{k−1}) (mod p^k)），两者在自检中互为验证；
 * 其余步骤（RNG、数位和、Horner、128 位乘模、二进制乘模收尾）完全一致。
 *
 * 复杂度：时间 O(q) ≈ 2×10^7 次 Long 级运算，本机实测约 165 ms（多次测量下 3 轮最优在
 * 160–184 ms 之间波动）；Vercel 单 vCPU 容器按 3–6 倍慢估计约 1 s 量级，远低于引擎 10 s
 * 熔断线。空间 O(q) 字节。
 *
 * 校验（本轮实跑）：
 *   题面给定校验 NF(3, 10000) mod 3^20 = 624955285 由同一实现复现（模数换成 3^20）；
 *   中小规模对照表 (p=61, q ∈ {1000, 2000, 5000, 10000, 20000}) 与 (p=3, q ∈ {10000, 30000})
 *   与 brute-force.kt 的 BigInteger 定义级路径（精确 N + Σ⌊N/pᵏ⌋ 逐次除法）逐行一致；
 *   主规模上另与「N mod p^10 只由最低 10 位数字决定」的精确求和路径互证。
 */
internal fun solve0288Impl(): Long {
    val p = 61
    val q = 10_000_000
    val mod = 713_342_911_662_882_601L          // 61^10

    val digits = ByteArray(q + 1)
    var s = 290_797L
    var digitSum = 0L
    for (n in 0..q) {
        val t = (s % p).toInt()
        digits[n] = t.toByte()
        digitSum += t
        s = s * s % 50_515_093L
    }

    val twoPow64Mod = twoPow64Mod(mod)
    var nMod = 0L
    for (n in q downTo 0) {
        nMod = addMod(mulModByP(nMod, p.toLong(), mod, twoPow64Mod), digits[n].toLong(), mod)
    }

    val diff = Math.floorMod(nMod - digitSum % mod, mod)
    val inv = modInverse((p - 1).toLong(), mod)
    return mulModBinary(diff, inv, mod)
}

/** (a + b) mod m，要求 a、b ∈ [0, m) 且 2m 不溢出。 */
private fun addMod(a: Long, b: Long, m: Long): Long = if (a >= m - b) a - (m - b) else a + b

/** 2^64 mod m（m = 61^10 > 2^32，逐次翻倍路径安全）。 */
private fun twoPow64Mod(m: Long): Long {
    var x = 1L
    repeat(64) { x = addMod(x, x, m) }
    return x
}

/**
 * (a · p) mod m：a ∈ [0, m)、p = 61、m = 61^10 < 2^60。
 * a·p = hi·2^64 + lo（hi = Math.multiplyHigh，lo = 低 64 位），
 * a·p ≡ hi·(2^64 mod m) + lo (mod m)；此时 hi ≤ 2，两部分都用条件减法归约。
 */
private fun mulModByP(a: Long, p: Long, m: Long, twoPow64Mod: Long): Long {
    val hi = Math.multiplyHigh(a, p)
    val lo = a * p
    var lowMod = lo % m
    if (lowMod < 0) lowMod += m
    if (lo < 0) lowMod = addMod(lowMod, twoPow64Mod, m)
    if (hi == 0L) return lowMod
    var hiMod = hi * twoPow64Mod
    if (hiMod >= m) hiMod -= m
    if (hiMod >= m) hiMod -= m
    return addMod(lowMod, hiMod, m)
}

/** 通用 (a · b) mod m：二进制倍增，只在乘逆元时调用一次（b 可达 m 量级）。 */
private fun mulModBinary(a: Long, b: Long, m: Long): Long {
    var result = 0L
    var base = a % m
    var k = b
    while (k > 0L) {
        if (k and 1L == 1L) result = addMod(result, base, m)
        base = addMod(base, base, m)
        k = k shr 1
    }
    return result
}
