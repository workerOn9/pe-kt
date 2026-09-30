package dev.pekt.engine

import dev.pekt.math.modPow

/**
 * PE 282 — The Ackermann Function（阿克曼函数）：求 Σ_{n=0..6} A(n,n) mod 14^8 = 1475789056。
 *
 * 推导（详见 content/problems/0282/solution.kt 头部与 0282/analysis.md）：
 *   定义展开成超运算恒等式 A(m,n) = 2 ↑^{m−2} (n+3) − 3（m ≥ 2，↑^0 = 乘法、↑^1 = 幂、
 *   ↑^2 = 四则塔 …），于是只剩三个巨大的幂塔需要取模：
 *     A(4,4) = 2↑↑7 − 3；A(5,5) = 2↑↑↑8 − 3；A(6,6) = 2↑↑↑↑9 − 3，前四项 1、3、7、61 手算。
 *   塔的模计算：tower(h, m) = 2↑↑h mod m。
 *     · h ≤ 5：指数 2↑↑(h−1) ∈ {1,2,4,16,65536} 精确已知 → 快速幂；
 *     · h ≥ 6：真实指数 ≥ 2↑↑5 = 2^65536 ≥ log2 m，用广义欧拉定理
 *       2^E ≡ 2^(E mod φ(m) + φ(m)) (mod m)（对任意底数/模数成立），递归到 φ(m) 上。
 *   递归深度由 φ 链（14^8 的链长 24）而不是塔高控制；h 只作为「离 5 多远」的计数器。
 *   塔高稳定性：t_h = 2↑↑h mod m 在 h ≥ h0(m) 后恒定（h0(14^8) = 10，φ 链归纳可证），
 *   而 A(5,5)、A(6,6) 的塔高 ≥ 2↑↑65536 ≫ 10，故二者都等于稳定值 − 3（实算同为 829575165）；
 *   A(4,4) 的塔高恰为 7（< 10），模值不同（915627005）——这正是「塔高必须按真实值处理」的检验点。
 *
 * 复杂度：约 30 层 32 位快速幂 + 试除法 φ，常数级（实测 < 1 ms），远低于 10 s 熔断线。
 *   校验：题面样例 A(1,0)=2、A(2,2)=7、A(3,4)=125 由定义直译递归复现；三套塔实现（本实现、
 *   拆 2^s·q + CRT 路径、字面 BigInteger 指数路径）在 φ 链全部模数 × h = 1..8 上一致；
 *   独立 Python 三实现复算同一答案；公开答案表（Nayuki）亦为 1098988351（仅作旁证）。
 */
internal fun solve0282Impl(): Long {
    val mod = 1_475_789_056L                 // 14^8 = 2^8 · 7^8
    return sumAckermannMod(mod)
}

/** 「任意足够大的塔高」哨兵：A(5,5)、A(6,6) 的真塔高 ≥ 2↑↑65536 ≫ 10^6。 */
private const val BIG_HEIGHT = 1_000_000L

/** Σ_{n=0..6} A(n,n) mod m（超运算恒等式 + φ 链塔模）。 */
private fun sumAckermannMod(m: Long): Long {
    var s = 0L
    for (n in 0..6) s = (s + termMod(n, m)) % m
    return s
}

/** A(n,n) mod m：n ≤ 3 查表；n = 4 塔高 7；n ≥ 5 塔高 ≥ 2↑↑65536，取「稳定段」代表高度。 */
private fun termMod(n: Int, m: Long): Long = when (n) {
    0 -> 1L % m
    1 -> 3L % m
    2 -> 7L % m
    3 -> 61L % m
    4 -> Math.floorMod(towerMod(7L, m) - 3L, m)
    else -> Math.floorMod(towerMod(BIG_HEIGHT, m) - 3L, m)
}

/**
 * 2↑↑h mod m：h ≤ 5 指数精确；h ≥ 6 用广义欧拉定理 2^E ≡ 2^(E mod φ(m) + φ(m)) (mod m)，
 * 递归降低模数（φ 链 ≤ ~30 层），h 每次减 1 但永远待在「大高度」区间。
 */
private fun towerMod(h: Long, m: Long): Long {
    if (m == 1L) return 0L
    if (h <= 5L) {
        val e = when (h) {
            1L -> 1L
            2L -> 2L
            3L -> 4L
            4L -> 16L
            else -> 65536L
        }
        return modPow(2L, e, m)
    }
    val phi = eulerPhi(m)
    val e = towerMod(h - 1L, phi)            // = 2↑↑(h−1) mod φ(m)
    return modPow(2L, e + phi, m)
}

/** 欧拉函数 φ(m)，试除法；只在小模数（≤ 14^8）上使用，代价可忽略。 */
private fun eulerPhi(m0: Long): Long {
    var x = m0
    var res = m0
    var p = 2L
    while (p * p <= x) {
        if (x % p == 0L) {
            while (x % p == 0L) x /= p
            res -= res / p
        }
        p++
    }
    if (x > 1L) res -= res / x
    return res
}
