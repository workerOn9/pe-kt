#!/usr/bin/env kotlin
/**
 * Project Euler 271 — Modular Cubes, Part 1（模立方 I）
 *
 * 思路
 * ────
 * 记 R(n) 为 x³ ≡ 1 (mod n) 在 [0, n) 中的解集，则题面要求的
 *
 *   S(n) = Σ_{x ∈ R(n), 1 < x < n} x = (Σ_{x ∈ R(n)} x) − 1,
 *
 * 因为 x = 1 恒是解、x = 0 只在 n = 1 时是解（n > 1 时单独处理这一特例）。
 *
 * 按中国剩余定理，n = ∏ pᵢ^eᵢ 时 R(n) ≅ ∏ R(pᵢ^eᵢ)：先对每个素因子幂分别求根，再 CRT 组合即可穷尽。
 * 每个 p^e 处 x³ = 1 的解数 = 循环群 (Z/p^e)* 中指数整除 3 的元素个数 = gcd(3, φ(p^e))：
 *   · p = 2 或 p ≡ 2 (mod 3)：gcd(3, φ) = 1 → 唯一解 x ≡ 1；
 *   · p = 3, e = 1：φ = 2，唯一解 x ≡ 1；p = 3, e ≥ 2：φ = 2·3^{e−1}，三个解
 *     1, 1 + 3^{e−1}, 1 + 2·3^{e−1}（mod 3^e）；
 *   · p ≡ 1 (mod 3)：三解 1, ω, ω²，其中 ω 是原始三次单位根；注意 1 + ω + ω² ≡ 0。
 *
 * 原始根的求法（模 p ≡ 1 (mod 3)）：取最小的 a ≥ 2 使 a^{(p−1)/3} ≢ 1 (mod p)，
 * 则 ω = a^{(p−1)/3} 满足 ω³ = a^{p−1} ≡ 1 且 ω ≠ 1，故 ω 的阶恰为 3。
 * e ≥ 2 时（p ≠ 3 保证 3r² ≢ 0）对每个根做牛顿/Hensel 提升：
 *   r ← r − (r³ − 1)·(3r²)^{−1} (mod p^{k+1})，从 p^k 唯一提升到 p^{k+1}。
 *
 * 本题的 n = 13082761331670030 = 43# = 2·3·5·7·11·13·17·19·23·29·31·37·41·43
 * （前 14 个素数之积，无平方因子、9 ∤ n）：其中 ≡ 1 (mod 3) 的素因子是 7, 13, 19, 31, 37, 43
 * 共 6 个，故 |R(n)| = 3⁶ = 729，逐个 CRT 组合求和即可。
 *
 * 复杂度
 * ──────
 * 分解 O(√n)（对 43# 试除只需到 43）；求根 O(Σ log p)；CRT 组合 O(3^k)，k = ≡1(3) 的素因子个数。
 * 暴力「逐个 x ∈ (1, n) 验证 x³ ≡ 1」需 O(n) 次模乘，n ≈ 1.3×10¹⁶ 完全不可行。
 *
 * 验证
 * ────
 * 1. 题面锚点：x = 9, 16, 22, 29, 53, 74, 79, 81（8 个解），S(91) = 363；
 * 2. 暴力对照：直接枚举 x ∈ (1, n) 的路径与 CRT 路径在 n = 2…3000 全部一致，
 *    并在 n = 819（= 3²·7·13）、1729、15561、53599、1983163 等含 p = 3 幂次/多个三次素因子的中等规模上继续一致；
 * 3. 独立复核（方法 B）：模 p^e 的根改用「逐个试数」找到（不用 a^{(p−1)/3} 与 Hensel），
 *    CRT 改用显式幂等元 + 大整数求和，并用恒等式
 *      Σ_{x∈R(n)} x ≡ 3^{r−1}·Σᵢ tᵢ·(Σ_{ν∈R(pᵢ^eᵢ)} ν)  (mod n)
 *    交叉验证（tᵢ 为素因子幂对应的 CRT 幂等元，r 为素因子个数）。
 *
 * 答案：4617456485273129588
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0271/solution.kt -d /tmp/kc-0271
 * java -cp /tmp/kc-0271:/Users/samuel/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/2.1.21/97a0975aa19d925e109537af60eb46902920015c/kotlin-stdlib-2.1.21.jar SolutionKt
 */

import java.math.BigInteger

// ───────────────────────── 基础数论工具 ─────────────────────────

private fun powLong(b: Long, e: Int): Long {
    var r = 1L
    repeat(e) { r *= b }
    return r
}

private fun extGcd(a: Long, b: Long): Triple<Long, Long, Long> {
    var oldR = if (a < 0) -a else a
    var r = if (b < 0) -b else b
    var oldS = 1L; var s = 0L
    var oldT = 0L; var t = 1L
    while (r != 0L) {
        val q = oldR / r
        var tmp = oldR - q * r; oldR = r; r = tmp
        tmp = oldS - q * s; oldS = s; s = tmp
        tmp = oldT - q * t; oldT = t; t = tmp
    }
    return Triple(oldR, if (a < 0) -oldS else oldS, if (b < 0) -oldT else oldT)
}

private fun modInverse(a: Long, m: Long): Long {
    val (g, x, _) = extGcd(((a % m) + m) % m, m)
    check(g == 1L) { "gcd($a, $m) = $g ≠ 1，逆元不存在" }
    return ((x % m) + m) % m
}

/** 乘法取模：返回 a·b mod m，乘法过程不溢出（能直接乘就直接乘，否则俄式乘法）。 */
private fun mulmod(a: Long, b: Long, m: Long): Long {
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

private fun modPowLong(b: Long, e: Long, m: Long): Long {
    var result = 1L % m
    var base = b % m
    var exp = e
    while (exp > 0) {
        if (exp and 1L == 1L) result = mulmod(result, base, m)
        base = mulmod(base, base, m)
        exp = exp shr 1
    }
    return result
}

/** 试除法分解：返回 (素因子, 指数)，升序。 */
private fun factorize(n: Long): List<Pair<Long, Int>> {
    var m = n
    val res = ArrayList<Pair<Long, Int>>()
    var d = 2L
    while (d <= m / d) {
        if (m % d == 0L) {
            var e = 0
            while (m % d == 0L) { m /= d; e++ }
            res.add(d to e)
        }
        d = if (d == 2L) 3L else d + 2L
    }
    if (m > 1L) res.add(m to 1)
    return res
}

// ───────────────────────── 模 p^e 的三次单位根 ─────────────────────────

/**
 * 返回 x³ ≡ 1 (mod p^e) 的全部解（升序、模 p^e 最小非负代表）。
 * 方法 A：a^{(p−1)/3} 找原始根 + Hensel 提升。
 */
private fun rootsModPrimePower(p: Long, e: Int): List<Long> {
    if (p != 3L && p % 3L != 1L) return listOf(1L) // p = 2 或 p ≡ 2 (mod 3)
    val pe = powLong(p, e)
    if (p == 3L) {
        if (e == 1) return listOf(1L)
        val t = pe / 3L
        return listOf(1L, 1L + t, 1L + 2L * t).sorted()
    }
    // p ≡ 1 (mod 3)：先求模 p 的原始三次单位根
    val exp = (p - 1L) / 3L
    var omega = 0L
    var a = 2L
    while (true) {
        val w = BigInteger.valueOf(a).modPow(BigInteger.valueOf(exp), BigInteger.valueOf(p)).toLong()
        if (w != 1L) { omega = w; break }
        a++
    }
    val baseRoots = listOf(1L, omega, omega * omega % p).sorted()
    if (e == 1) return baseRoots
    return baseRoots.map { liftRoot(it, p, e) }.sorted()
}

/** 把 x³ ≡ 1 (mod p^k) 的根 r 唯一提升到模 p^{k+1}（p ≠ 3 且 r ≢ 0）。 */
private fun liftRoot(root: Long, p: Long, e: Int): Long {
    var r = root
    var pk = p
    var k = 1
    while (k < e) {
        val pk1 = pk * p                                   // p^{k+1}
        val r3 = mulmod(mulmod(r, r, pk1), r, pk1)          // r³ mod p^{k+1}
        val f = (r3 - 1 + pk1) % pk1                        // f(r) ≡ 0 (mod p^k)
        val df = (3L * ((r % p) * (r % p) % p)) % p         // f'(r) mod p，非零
        val t = (p - mulmod(f / pk % p, modInverse(df, p), p)) % p
        r = (r + t * pk) % pk1
        pk = pk1
        k++
    }
    return r
}

// ───────────────────────── 方法 A：CRT 组合 ─────────────────────────

/** 返回 R(n) 的全部元素（升序），n 用 [0, n) 代表。 */
private fun rootsOf(n: Long): List<Long> {
    if (n == 1L) return listOf(0L)
    var xs = listOf(0L)
    var mod = 1L
    for ((p, e) in factorize(n)) {
        val q = powLong(p, e)
        val rs = rootsModPrimePower(p, e)
        val next = ArrayList<Long>(xs.size * rs.size)
        for (x in xs) for (r in rs) next.add(crtStep(x, mod, r, q))
        xs = next
        mod *= q
    }
    return xs.sorted()
}

/** x ≡ a (mod m)、x ≡ b (mod q)（gcd(m,q)=1）→ [0, m·q) 中的唯一解。 */
private fun crtStep(a: Long, m: Long, b: Long, q: Long): Long {
    val t = ((b - a) % q + q) % q
    return a + m * mulmod(t, modInverse(m % q, q), q)
}

/** S(n) = Σ_{x ∈ R(n), 1 < x < n} x，以及解的个数 C(n)（同口径 1 < x < n）。 */
private fun sumAndCount(n: Long): Pair<Long, Long> {
    if (n <= 1L) return 0L to 0L
    val xs = rootsOf(n)
    var s = 0L
    var c = 0L
    for (x in xs) if (x > 1L) { s += x; c++ }
    return s to c
}

// ───────────────────────── 暴力与独立复核 ─────────────────────────

/** 暴力：直接枚举 x ∈ (1, n) 验证 x³ ≡ 1 (mod n)。 */
private fun bruteSumAndCount(n: Long): Pair<Long, Long> {
    var s = 0L
    var c = 0L
    var x = 2L
    while (x < n) {
        // n ≤ 2×10⁹ 时 x² ≤ 4×10¹⁸、(x² mod n)·x ≤ n·x ≤ 4×10¹⁸ 都不溢出，直接乘
        val c3 = if (n <= 2_000_000_000L) x * x % n * x % n else mulmod(mulmod(x, x, n), x, n)
        if (c3 == 1L) { s += x; c++ }
        x++
    }
    return s to c
}

/**
 * 方法 B（独立复核）：模 p^e 的根逐个试数得到（不依赖原始根公式与 Hensel），
 * CRT 用显式幂等元 tᵢ = (n/qᵢ)·((n/qᵢ)^{−1} mod qᵢ)，求和用 BigInteger。
 * 另用恒等式 Σ_{x∈R(n)} x ≡ 3^{r−1}·Σᵢ tᵢ·(Σ_{ν∈R(pᵢ^{eᵢ})} ν) (mod n) 交叉校验。
 */
private fun methodB(n: Long): Pair<BigInteger, Long> {
    val fac = factorize(n)
    val mods = fac.map { powLong(it.first, it.second) }
    val rootLists = fac.map { (p, e) ->
        val q = powLong(p, e)
        check(q <= 1_000_000L) { "方法 B 只对小的素因子幂做试数求根（q = $q）" }
        val rs = ArrayList<Long>()
        var v = 0L
        while (v < q) { // 逐个试数求根，规模小（p ≤ 43 时 q ≤ 43 或 3^e 很小）
            if (mulmod(mulmod(v, v, q), v, q) == 1L) rs.add(v)
            v++
        }
        rs
    }
    val nBig = BigInteger.valueOf(n)
    // 显式幂等元：tᵢ ≡ 1 (mod qᵢ)，≡ 0 (mod q_j) (j ≠ i)
    val idem = mods.map { q ->
        val others = nBig.divide(BigInteger.valueOf(q))
        val inv = others.modInverse(BigInteger.valueOf(q))
        others.multiply(inv).mod(nBig)
    }
    var total = BigInteger.ZERO
    var count = 0L
    val idx = IntArray(mods.size)
    while (true) { // 直接枚举各分量组合（3^k 个，k ≤ 6 时 729 个）
        var x = BigInteger.ZERO
        for (i in mods.indices) x = x.add(idem[i].multiply(BigInteger.valueOf(rootLists[i][idx[i]])))
        total = total.add(x.mod(nBig))
        count++
        var i = 0
        while (i < mods.size) {
            idx[i]++
            if (idx[i] < rootLists[i].size) break
            idx[i] = 0
            i++
        }
        if (i == mods.size) break
    }
    // 恒等式校验：Σ_{x∈R} x ≡ Σᵢ tᵢ·Sᵢ·(|R|/|Rᵢ|) (mod n)，Sᵢ = Σ_{ν ∈ R(pᵢ^{eᵢ})} ν
    val sizes = rootLists.map { it.size.toLong() }
    val totalSize = sizes.fold(1L) { a, b -> a * b }
    var rhs = BigInteger.ZERO
    for (i in mods.indices) {
        var si = 0L
        for (v in rootLists[i]) si += v
        rhs = rhs.add(
            idem[i]
                .multiply(BigInteger.valueOf(si))
                .multiply(BigInteger.valueOf(totalSize / sizes[i]))
        )
    }
    rhs = rhs.mod(nBig)
    check(total.mod(nBig) == rhs) { "幂等元恒等式校验失败：${total.mod(nBig)} ≠ $rhs" }
    return total to count
}

// ───────────────────────── 主程序 ─────────────────────────

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面锚点 ----------
    val (s91, c91) = sumAndCount(91L)
    check(s91 == 363L && c91 == 8L) { "题面锚点 S(91)=363、8 个解 未复现：$s91 / $c91" }
    val (sb91, cb91) = bruteSumAndCount(91L)
    check(sb91 == 363L && cb91 == 8L) { "暴力路径 S(91) 不一致：$sb91 / $cb91" }
    println("题面锚点：n = 91 时 8 个解，S(91) = 363（CRT 路径与暴力路径同时复现）")

    // ---------- 2. 小 n 全量对拍 ----------
    var worstSmall = 0L
    for (n in 2L..3000L) {
        val (sa, ca) = sumAndCount(n)
        val (sb, cb) = bruteSumAndCount(n)
        check(sa == sb && ca == cb) { "n = $n：CRT ($sa, $ca) ≠ 暴力 ($sb, $cb)" }
        if (sa > worstSmall) worstSmall = sa
    }
    println("n = 2…3000：CRT 路径与暴力枚举逐个一致（最大 S = $worstSmall）")

    // ---------- 3. 中等规模（含 p = 3 的幂次、平方因子、多个三次素因子） ----------
    val medium = listOf(343L, 1183L, 819L, 8281L, 12103L, 15561L, 1729L, 53599L, 482391L, 1983163L)
    for (n in medium) {
        val (sa, ca) = sumAndCount(n)
        val (sb, cb) = bruteSumAndCount(n)
        check(sa == sb && ca == cb) { "n = $n：CRT ($sa, $ca) ≠ 暴力 ($sb, $cb)" }
        println("n = $n：解数 $ca，S = $sa（与暴力一致）")
    }

    // ---------- 4. 与目标同结构的大规模暴力对照 ----------
    // n = 7·13·19·31·37·43 = 85276009 带的是目标 n 的全部三次素因子（同样的 3^6 = 729 个根），
    // 但可以直接枚举 8.5×10^7 个 x 暴力验证。
    val anchorLike = 85276009L
    val (saLike, caLike) = sumAndCount(anchorLike)
    val (sbLike, cbLike) = bruteSumAndCount(anchorLike)
    check(saLike == sbLike && caLike == cbLike) { "n = $anchorLike：CRT ($saLike, $caLike) ≠ 暴力 ($sbLike, $cbLike)" }
    println("n = $anchorLike = 7·13·19·31·37·43：解数 $caLike，S = $saLike（与逐 x 暴力一致）")

    // ---------- 4. 独立复核（方法 B） ----------
    for (n in listOf(91L, 819L, 1729L, 15561L, 1983163L, 85276009L)) {
        val (sa, ca) = sumAndCount(n)
        val (tb, cb) = methodB(n)
        check(tb == BigInteger.valueOf(sa + if (n > 1) 1L else 0L) && cb == ca + if (n > 1) 1L else 0L) {
            "n = $n：方法 B 根和 $tb ≠ 方法 A ${sa + 1}（或解数 $cb ≠ ${ca + 1}）"
        }
    }
    println("方法 B（试数求根 + 幂等元 CRT + 恒等式校验）在 6 个规模上与方法 A 一致")

    val target = 13082761331670030L
    val (sTarget, cTarget) = sumAndCount(target)
    val (tBig, cBig) = methodB(target)
    check(cBig == cTarget + 1L && tBig == BigInteger.valueOf(sTarget + 1L)) {
        "目标 n：方法 B $tBig / $cBig ≠ 方法 A ${sTarget + 1} / ${cTarget + 1}"
    }
    println("目标 n：解的个数 $cTarget 个（1 < x < n），含 x=1 的根共 $cBig = 3^6 个，" +
        "全部根之和 = $tBig（方法 A/B 一致）")

    // ---------- 5. 每个根都真的满足 x³ ≡ 1 (mod n) 且互不相同 ----------
    val xs = rootsOf(target)
    check(xs.size == 729 && xs.toSet().size == 729) { "根的个数/去重异常：${xs.size}" }
    for (x in xs) {
        check(x > 0 && x < target && mulmod(mulmod(x, x, target), x, target) == 1L) { "x = $x 不是解" }
    }
    println("729 个根逐一直接验证 x³ mod n = 1，且两两不同")

    // ---------- 6. 计时 ----------
    sumAndCount(target); sumAndCount(1983163L); bruteSumAndCount(1983163L); bruteSumAndCount(85276009L)
    val msA = bestOf3("方法 A（分解 + 原始根/Hensel + CRT 枚举，n = 13082761331670030）", sTarget) {
        sumAndCount(target).first
    }
    val msBrute = bestOf3("暴力逐个 x 验证（n = 85276009，约 8.5×10⁷ 次模乘）", saLike) {
        bruteSumAndCount(anchorLike).first
    }
    val msB = bestOf3("方法 B（试数求根 + 幂等元 CRT，n 为目标）", sTarget + 1L) {
        methodB(target).first.toLong()
    }

    println()
    println("答案 = $sTarget")
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms；暴力 n = 85276009 ${"%.1f".format(msBrute)} ms")
    println("check() 全部通过")
}
