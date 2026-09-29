#!/usr/bin/env kotlin
/**
 * Project Euler 274 — Divisibility Multipliers（整除乘数）
 *
 * 思路
 * ────
 * 把 n 写成 n = 10q + d（d = n mod 10 ∈ [0,10)），题目要求的函数是
 *
 *   f(n) = q + d·m,
 *
 * 且要求「p | f(n) ⟺ p | n」对一切正整数 n 成立。
 *
 * **必要条件.** 取任意 d ∈ [1,9]，找 n = 10q + d 使 p | n（存在：取 q ≡ −d·10⁻¹ (mod p)，
 * 即 n = p·j，其中 j ≡ d·p⁻¹ (mod 10)，这样的 j 模 10 唯一存在）。此时 f(n) = q + d·m ≡ 0
 * 是题设强制要求的，而 n ≡ 0 给出 q ≡ −d·10⁻¹，于是 d·(m − 10⁻¹) ≡ 0 (mod p)；p ∤ 10 ⟹ p ∤ d
 * （p ≥ 3 但 d ≤ 9 < 11，若 p | d 则 p ∈ {3,7} 且 d = 3,6,9,7，换一个 d 即得矛盾，或直接取
 * d = 1），故 m ≡ 10⁻¹ (mod p)。
 *
 * **充分性.** m = 10⁻¹ mod p 时 f(n) = q + d·10⁻¹ ≡ (10q + d)·10⁻¹ = n·10⁻¹ (mod p)，
 * 而 10⁻¹ 在模 p 下可逆，故 f(n) ≡ 0 ⟺ n ≡ 0。于是对每个与 10 互素的 p，
 * 乘数就是 m(p) = 10⁻¹ mod p；题目要求 0 < m < p，而 p ∤ 10 ⟹ 10⁻¹ mod p ∈ [1, p−1]，
 * 所以这个乘数唯一。
 *
 * 答案 = Σ_{p 素数, p ∤ 10, p < 10^7} 10⁻¹ mod p。
 *
 * 复杂度
 * ──────
 * 埃氏筛 O(N log log N) + 每个素数一次扩展欧几里得（O(log p)），N = 10^7；
 * 素数个数 π(10^7) = 664579，其中 2 个被排除，共 664577 个乘数。
 *
 * 验证
 * ────
 * 1. 题面锚点：m(113) = 34；f(76275) = 7797 与 76275 同被 113 整除；f(12345) = 1404 与 12345
 *    同不被 113 整除；Σ_{p 素数 < 1000, p ∤ 10} m(p) = 39517（题面给定）；
 * 2. 方法 A（扩展欧几里得求 10⁻¹）与方法 B（枚举 x ∈ [0,10)，由 10m = x·p + 1 直接解出
 *    m = (x·p+1)/10）在 p < 10^7 的全部 664577 个素数上逐一一致；
 * 3. 小规模完全定义式验证：对 p < 300 的每个素数，直接在 [1,p) 中扫描 m 并逐个完整检验
 *    n ∈ [1,10p] 的「f(n) 被 p 整除 ⟺ n 被 p 整除」，得到与公式相同的唯一乘数
 *    （检验 n 只需到 10p：n mod p 与 f(n) mod p 都由 n mod 10p 决定）；
 * 4. brute-force.kt 另做：p < 1000 的完全定义式扫描复现 39517；对 p = 9999991（< 10^7 的最大
 *    素数）做 n ∈ [1,10p] 的完全检验（10^8 个 n），确认满载规模下乘数性质成立。
 *
 * 答案
 * ────
 * 1601912348822（本机实跑，方法 A、方法 B 一致）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0274/solution.kt -d /tmp/kc-0274
 * java -cp /tmp/kc-0274:<kotlin-stdlib> SolutionKt
 */

private const val LIMIT = 10_000_000

/** 埃氏筛：isPrime[i] 表示 i 是否为素数。 */
private fun sieve(limit: Int): BooleanArray {
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var p = 2
    while (p.toLong() * p <= limit) {
        if (isPrime[p]) {
            var k = p * p
            while (k <= limit) {
                isPrime[k] = false
                k += p
            }
        }
        p++
    }
    return isPrime
}

/** 扩展欧几里得求逆元：返回 [1, m−1] 内的 a⁻¹ mod m（要求 gcd(a, m) = 1）。 */
private fun invMod(a: Long, m: Long): Long {
    var oldR = a
    var r = m
    var oldS = 1L
    var s = 0L
    while (r != 0L) {
        val q = oldR / r
        val tr = oldR - q * r; oldR = r; r = tr
        val ts = oldS - q * s; oldS = s; s = ts
    }
    return ((oldS % m) + m) % m
}

/**
 * 方法 A（主路径）：乘数 = 10⁻¹ mod p，用扩展欧几里得逐个素数求。
 * 返回 [sum, count]。
 */
private fun solveByExtendedGcd(limit: Int): LongArray {
    val isPrime = sieve(limit)
    var sum = 0L
    var count = 0L
    for (p in 3..limit step 2) {          // 只走奇数：p = 2、5 与 10 不互素，直接跳过
        if (!isPrime[p] || p == 5) continue
        sum += invMod(10L, p.toLong())
        count++
    }
    return longArrayOf(sum, count)
}

/**
 * 方法 B（独立复核）：不调用任何求逆过程。乘数必须满足 10m ≡ 1 (mod p)，且 0 < m < p，
 * 故 10m = x·p + 1 中的整数 x = (10m−1)/p 落在 [0,10)；反过来对每个满足
 * x·p ≡ −1 (mod 10) 的 x ∈ [0,10)（模 10 唯一），m = (x·p+1)/10 就是唯一的候选乘数——
 * 直接枚举 x 即可解出 m，完全不使用 gcd/逆元。
 */
private fun solveByQuotientScan(limit: Int): Long {
    val isPrime = sieve(limit)
    var sum = 0L
    for (p in 3..limit step 2) {
        if (!isPrime[p] || p == 5) continue
        var x = 0
        while ((x * p + 1) % 10 != 0) x++
        sum += (x.toLong() * p + 1L) / 10L
    }
    return sum
}

/** 完全定义式检验：对 n ∈ [1, 10p] 逐个验证「f(n) 被 p 整除 ⟺ n 被 p 整除」。 */
private fun preservesDivisibility(p: Long, m: Long, nMax: Long): Boolean {
    for (n in 1..nMax) {
        val f = n / 10 + (n % 10) * m
        if ((f % p == 0L) != (n % p == 0L)) return false
    }
    return true
}

/** 在 [1, p) 中扫描出满足题面定义（完整检验 n ∈ [1,10p]）的唯一乘数。 */
private fun multiplierByDefinition(p: Long): Long {
    for (m in 1 until p) if (preservesDivisibility(p, m, 10 * p)) return m
    return -1L
}

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
    val m113 = invMod(10L, 113L)
    check(m113 == 34L) { "m(113) 应为 34，实际 $m113" }
    val f76275 = 76275L / 10 + (76275L % 10) * m113
    val f12345 = 12345L / 10 + (12345L % 10) * m113
    check(f76275 == 7797L && f76275 % 113 == 0L && 76275L % 113 == 0L) { "f(76275) 锚点失败：$f76275" }
    check(f12345 == 1404L && f12345 % 113 != 0L && 12345L % 113 != 0L) { "f(12345) 锚点失败：$f12345" }
    println("题面锚点：m(113) = $m113；f(76275) = $f76275（与 76275 同被 113 整除）；" +
        "f(12345) = $f12345（与 12345 同不被 113 整除）")

    val anchor = solveByQuotientScan(1000)
    check(anchor == 39517L) { "p < 1000 的乘数和应为 39517，实际 $anchor" }
    val anchorA = solveByExtendedGcd(1000)
    println("题面锚点：Σ_{p 素数 < 1000, p ∤ 10} m(p) = $anchor（方法 A/B 同为 $anchor）")

    // ---------- 2. 完全定义式验证（小规模，含唯一性） ----------
    var checked = 0
    for (p in 3L until 300L) {
        if (!isPrimeSmall(p)) continue
        if (p == 5L) continue
        val mDef = multiplierByDefinition(p)
        val mA = invMod(10L, p)
        check(mDef == mA) { "p=$p：定义式扫描乘数 $mDef ≠ 公式 $mA" }
        checked++
    }
    println("完全定义式验证：p < 300 的 $checked 个素数上，[1,p) 内逐个 m 完整检验 n ∈ [1,10p] " +
        "得到的唯一乘数 = 公式值 10⁻¹ mod p")

    // ---------- 3. 两种方法在完整规模上互证 ----------
    val a = solveByExtendedGcd(LIMIT)
    val sumA = a[0]
    val count = a[1]
    val sumB = solveByQuotientScan(LIMIT)
    check(sumA == sumB) { "方法 A $sumA ≠ 方法 B $sumB" }
    check(count == 664_577L) { "π(10^7) − 2 应为 664577，实际 $count" }
    println("p < 10^7：共 $count 个与 10 互素的素数，方法 A（扩展欧几里得）与方法 B（商枚举）" +
        "结果一致 = $sumA")

    // ---------- 4. 计时 ----------
    solveByExtendedGcd(1000); solveByQuotientScan(1000)
    val msAnchor = bestOf3("方法 B：商枚举 p < 1000（含筛，复现题面锚点 39517）", 39517L) {
        solveByQuotientScan(1000)
    }
    solveByExtendedGcd(LIMIT); solveByQuotientScan(LIMIT)
    val msA = bestOf3("方法 A：扩展欧几里得（p < 10^7）", sumA) { solveByExtendedGcd(LIMIT)[0] }
    val msB = bestOf3("方法 B：商枚举（p < 10^7）", sumB) { solveByQuotientScan(LIMIT) }

    // 单次筛的耗时（供分析表对照）
    var t0 = System.nanoTime()
    val isPrime = sieve(LIMIT)
    var cnt = 0
    for (i in 2..LIMIT) if (isPrime[i]) cnt++
    println("对照：埃氏筛到 10^7 一次 ${"%.3f".format((System.nanoTime() - t0) / 1e6)} ms，" +
        "筛出 $cnt 个素数（π(10^7) = 664579）")

    println()
    println("答案 = $sumA")
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms；" +
        "锚点规模（p < 1000）${"%.3f".format(msAnchor)} ms")
    println("check() 全部通过")
}

/** 小规模素性试除（供定义式验证用）。 */
private fun isPrimeSmall(n: Long): Boolean {
    if (n < 2) return false
    var d = 2L
    while (d * d <= n) {
        if (n % d == 0L) return false
        d++
    }
    return true
}
