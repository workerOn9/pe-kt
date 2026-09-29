package dev.pekt.engine

import dev.pekt.math.modInverse
import dev.pekt.math.modPow

/**
 * PE 271 — Modular Cubes, Part 1（模立方 I）：求 S(13082761331670030)，其中 S(n) 是满足
 * 1 < x < n 且 x³ ≡ 1 (mod n) 的整数 x 之和（模 n 的「三次单位根」除 1 之外的和）。
 *
 * 推导（详见 content/problems/0271/solution.kt 头部与 0271/analysis.md）：
 *   记 R(n) = {x ∈ [0, n) : x³ ≡ 1 (mod n)}，则 S(n) = Σ_{x∈R(n)} x − 1（n > 1 时 1 恒是解、
 *   0 不是解；n = 1 单独返回 0）。CRT 给出 R(n) ≅ ∏ R(p^e)：先对每个素因子幂独立求根、再组合。
 *   解数 = gcd(3, φ(p^e))：p = 2 或 p ≡ 2 (mod 3) 时只有 x ≡ 1；p = 3, e = 1 时只有 1；
 *   p = 3, e ≥ 2 时为 1, 1+3^{e−1}, 1+2·3^{e−1}；p ≡ 1 (mod 3) 时为 1, ω, ω²
 *   （ω = a^{(p−1)/3} 取最小的 a ≥ 2 使其 ≠ 1；e ≥ 2 时用牛顿/Hensel 提升，3r² ≢ 0 保证唯一）。
 *
 * 本题的 n = 13082761331670030 = 43# = 2·3·5·7·11·13·17·19·23·29·31·37·41·43
 * （前 14 个素数之积，无平方因子）→ 6 个 ≡ 1 (mod 3) 的素因子，|R(n)| = 3⁶ = 729，逐个 CRT 求和。
 *
 * 复杂度：O(√n) 试除（对 43# 只需试到 43）+ O(3^6) 组合，实测 < 1 ms。
 * 校验：题面锚点 S(91) = 363、8 个解；内容侧 solution.kt 另有逐 x 暴力对拍（n ≤ 3000、
 *   85276009 等）与幂等元恒等式复核。逻辑与 content/problems/0271/solution.kt 主路径一致。
 */
internal fun solve0271Impl(): Long {
    val n = 13082761331670030L
    if (n <= 1L) return 0L

    // 分解 n（本题 n = 43#，试除到 43 即结束）
    val fac = ArrayList<Pair<Long, Int>>()
    run {
        var m = n
        var d = 2L
        while (d <= m / d) {
            if (m % d == 0L) {
                var e = 0
                while (m % d == 0L) { m /= d; e++ }
                fac.add(d to e)
            }
            d = if (d == 2L) 3L else d + 2L
        }
        if (m > 1L) fac.add(m to 1)
    }

    // CRT 逐步组合出全部根：xs = 已组合出的根（模 mod），对每个素因子幂摊开
    var xs = listOf(0L)
    var mod = 1L
    for ((p, e) in fac) {
        val q = powLong0271(p, e)
        val rs = rootsModPrimePower0271(p, e)
        val next = ArrayList<Long>(xs.size * rs.size)
        for (x in xs) for (r in rs) next.add(crtStep0271(x, mod, r, q))
        xs = next
        mod *= q
    }

    var sum = 0L
    for (x in xs) if (x > 1L) sum += x
    return sum
}

private fun powLong0271(b: Long, e: Int): Long {
    var r = 1L
    repeat(e) { r *= b }
    return r
}

/** 俄式乘法取模（本题规模下不会用到溢出分支，保留通用性）。 */
private fun mulmod0271(a: Long, b: Long, m: Long): Long {
    val x = ((a % m) + m) % m
    val y = ((b % m) + m) % m
    if (x == 0L || y <= Long.MAX_VALUE / x) return x * y % m
    var xx = x
    var yy = y
    var result = 0L
    while (yy > 0) {
        if (yy and 1L == 1L) result = (result + xx) % m
        xx = (xx + xx) % m
        yy = yy shr 1
    }
    return result
}

/** x³ ≡ 1 (mod p^e) 的全部解（升序，mod p^e 的最小非负代表）。 */
private fun rootsModPrimePower0271(p: Long, e: Int): List<Long> {
    if (p != 3L && p % 3L != 1L) return listOf(1L) // p = 2 或 p ≡ 2 (mod 3)
    val pe = powLong0271(p, e)
    if (p == 3L) {
        if (e == 1) return listOf(1L)
        val t = pe / 3L
        return listOf(1L, 1L + t, 1L + 2L * t).sorted()
    }
    // p ≡ 1 (mod 3)：找原始三次单位根 ω = a^{(p−1)/3} ≠ 1
    val exp = (p - 1L) / 3L
    var omega = 0L
    var a = 2L
    while (true) {
        // 本题 n = 43#，素数 p ≤ 43，库内 modPow 的中间乘积不会溢出
        val w = modPow(a, exp, p)
        if (w != 1L) { omega = w; break }
        a++
    }
    val baseRoots = listOf(1L, omega, omega * omega % p).sorted()
    if (e == 1) return baseRoots
    return baseRoots.map { liftRoot0271(it, p, e) }.sorted()
}

/** 牛顿/Hensel：把 x³ ≡ 1 (mod p^k) 的根唯一提升到模 p^{k+1}（p ≠ 3、r ≢ 0）。 */
private fun liftRoot0271(root: Long, p: Long, e: Int): Long {
    var r = root
    var pk = p
    var k = 1
    while (k < e) {
        val pk1 = pk * p
        val r3 = mulmod0271(mulmod0271(r, r, pk1), r, pk1)
        val f = (r3 - 1 + pk1) % pk1
        val df = (3L * ((r % p) * (r % p) % p)) % p
        val t = (p - mulmod0271(f / pk % p, modInverse(df, p), p)) % p
        r = (r + t * pk) % pk1
        pk = pk1
        k++
    }
    return r
}

/** CRT：x ≡ a (mod m)、x ≡ b (mod q)，gcd(m, q) = 1 → [0, m·q) 中的唯一解。 */
private fun crtStep0271(a: Long, m: Long, b: Long, q: Long): Long {
    val t = ((b - a) % q + q) % q
    return a + m * mulmod0271(t, modInverse(m % q, q), q)
}
