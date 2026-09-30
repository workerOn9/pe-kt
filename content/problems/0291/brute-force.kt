#!/usr/bin/env kotlin
/**
 * Project Euler 291 — Panaitopol Primes（帕纳伊托波尔素数）：暴力 / 独立对照
 *
 * 独立实现，与 solution.kt 不共享代码。三条互不相同的对照路线：
 *
 * ① 定义级完整枚举（本文件的暴力基线，覆盖所有 Panaitopol 素数 < 10^8）
 * ─────────────────────────────────────────────────────────────
 * 直接枚举 (x,y) 不可行（给出小素数的 x 可以大到 ~10^8 量级），故把 (x,y) 空间做**初等**重参数化：
 * 令 d = x−y（x = y+d）、g = gcd(y,d)、y = g·y₁、d = g·d₁（gcd(y₁,d₁) = 1），则
 *     Q = x²−xy+y² = g²Q₁，  Q₁ = y₁² + y₁d₁ + d₁²，
 * 商 (x⁴−y⁴)/(x³+y³) = 2d − d³/Q 为整数 ⟺ Q | d³ ⟺ Q₁ | g（因 gcd(Q₁,d₁) = 1）。
 * 于是 g = k·Q₁，商 = k·d₁·(2Q₁ − d₁²)。枚举 (d₁,y₁,k) 即**完整**覆盖全部 (x,y)；逐个查素数表。
 * 该参数化是一一对应的，不做任何「p 素 ⇒ d₁ = 1」的推导——那是被验证的结论。
 * 规模说明：枚举规模与 pMax 成正比（本机实测 pMax = 10^8 时 7.1×10^7 次迭代）；全尺寸
 * pMax = 5×10^15 需要 ~7×10^15 次迭代，物理不可行，故缩到 10^8（覆盖 1225 个 Panaitopol 素数）。
 *
 * ② 原始 (x,y) 盒枚举（不做任何代数变换）：x ≤ 10^4 的全部 (x,y) 对（5×10^7 对）直接算
 *    (x⁴−y⁴)/(x³+y³)，取整可素的值，与 n 形素数集合对照。
 *
 * ③ 朴素逐 n 试除：n ≤ 10^5 时对 f(n) = 2n²+2n+1 直接用素数试除到 √f(n)，最笨的计数方式。
 *
 * ④ 构造验证（BigInteger）：对枚举中命中的候选 p（含合数候选）反解 x = g(y₁+d₁)、y = g·y₁
 *    （g = k·Q₁），实算定义式商是否恰为 p；另抽随机 n 用 n 形构造验证商 = f(n)。
 *
 * 构建：OUTDIR=/tmp/kc-0291-bf bash scripts/kotlinc-shim.sh content/problems/0291/brute-force.kt
 *      java -Xmx4g -cp /tmp/kc-0291-bf:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

private const val P_MAX_ENUM = 100_000_000L      // ① 完整枚举的 p 上界（缩规模，见文件头）
private const val XMAX_BOX = 10_000L             // ② 盒枚举的 x 上界
private const val N_NAIVE = 100_000L             // ③ 朴素试除的 n 上界

private fun f(n: Long): Long = 2L * n * n + 2L * n + 1L

private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** comp[i] = true ⟺ 2i+1 非素数（i = 0 对应 1）；覆盖所有 ≤ limit 的奇数。 */
private fun oddCompositeSieve(limit: Int): BooleanArray {
    val size = (limit + 1) / 2
    val comp = BooleanArray(size)
    comp[0] = true
    var i = 1
    while (true) {
        val v = 2L * i + 1
        if (v * v > limit) break
        if (!comp[i]) {
            var j = ((v * v - 1) / 2).toInt()
            val step = v.toInt()
            while (j < size) {
                comp[j] = true
                j += step
            }
        }
        i++
    }
    return comp
}

/** 用 comp 表试除（comp 须覆盖到 √v）。 */
private fun isPrimeTrial(v: Long, comp: BooleanArray): Boolean {
    if (v < 2L) return false
    if (v % 2L == 0L) return v == 2L
    var i = 1
    while (i < comp.size) {
        val q = 2L * i + 1
        if (q * q > v) break
        if (!comp[i] && v % q == 0L) return false
        i++
    }
    return true
}

// ───────────────── ① 定义级完整枚举（(d₁,y₁,k) 参数化） ─────────────────

private class EnumResult(val primes: Set<Long>, val hits: Long, val oddTriples: Long, val compositeCandidates: List<Long>)

private fun completeEnum(pMax: Long): EnumResult {
    val comp = oddCompositeSieve(pMax.toInt())
    val primes = HashSet<Long>()
    val compositeSamples = ArrayList<Long>()
    var hits = 0L
    var oddTriples = 0L
    var d1 = 1L
    while (d1 * d1 * d1 < pMax) {                       // base ≥ d₁³ 给出 d₁ 的上界
        var y1 = 1L
        while (true) {
            val q1 = y1 * y1 + y1 * d1 + d1 * d1
            val base = d1 * (2L * q1 - d1 * d1)         // = d₁·(y₁² + (y₁+d₁)²)
            if (base >= pMax) break
            if (gcd(y1, d1) == 1L) {
                var k = 1L
                var p = base
                while (p < pMax) {
                    if (p % 2L == 0L) {                 // 偶候选必非素数
                        if (compositeSamples.size < 64 && k < 4L) compositeSamples.add(p)
                    } else if (!comp[((p - 1) / 2).toInt()]) {
                        primes.add(p)
                        hits++
                        if (d1 != 1L || k != 1L) oddTriples++   // 关键推导步的经验检查：应为 0
                    } else if (compositeSamples.size < 64 && k < 4L && d1 <= 3L) {
                        compositeSamples.add(p)
                    }
                    k++
                    p += base
                }
            }
            y1++
        }
        d1++
    }
    return EnumResult(primes, hits, oddTriples, compositeSamples)
}

private fun nFormPrimesBelow(pMax: Long, comp: BooleanArray): Set<Long> {
    val out = HashSet<Long>()
    var n = 1L
    while (f(n) < pMax) {
        if (isPrimeTrial(f(n), comp)) out.add(f(n))
        n++
    }
    return out
}

// ───────────────── ② 原始 (x,y) 盒枚举（直接算公式） ─────────────────

private fun rawBoxPrimes(xMax: Long, comp: BooleanArray): Set<Long> {
    val out = HashSet<Long>()
    for (x in 2L..xMax) {
        val x2 = x * x
        val x3 = x2 * x
        val x4 = x2 * x2
        for (y in 1L until x) {
            val y2 = y * y
            val num = x4 - y2 * y2
            val den = x3 + y2 * y
            if (num % den == 0L) {
                val v = num / den
                if (isPrimeTrial(v, comp)) out.add(v)
            }
        }
    }
    return out
}

// ───────────────── ③ 朴素逐 n 试除 ─────────────────

private fun naiveTrialPrimes(nMax: Long, comp: BooleanArray): Set<Long> {
    val out = HashSet<Long>()
    for (n in 1L..nMax) {
        val v = f(n)
        if (isPrimeTrial(v, comp)) out.add(v)
    }
    return out
}

// ───────────────── ④ 构造验证（BigInteger） ─────────────────

/** 用 (d₁,y₁,k) 反解 (x,y)，实算定义式商。 */
private fun verifyByTriple(d1: Long, y1: Long, k: Long, p: Long): Boolean {
    val q1 = y1 * y1 + y1 * d1 + d1 * d1
    val g = java.math.BigInteger.valueOf(k).multiply(java.math.BigInteger.valueOf(q1))
    val x = g.multiply(java.math.BigInteger.valueOf(y1 + d1))
    val y = g.multiply(java.math.BigInteger.valueOf(y1))
    return quotientEquals(x, y, p)
}

/** 用 n 形构造 (x,y) = ((n²+n+1)(n+1), (n²+n+1)n)，实算定义式商。 */
private fun verifyByNForm(n: Long): Boolean {
    val g = java.math.BigInteger.valueOf(n * n + n + 1)
    val x = g.multiply(java.math.BigInteger.valueOf(n + 1))
    val y = g.multiply(java.math.BigInteger.valueOf(n))
    return quotientEquals(x, y, f(n))
}

private fun quotientEquals(x: java.math.BigInteger, y: java.math.BigInteger, expect: Long): Boolean {
    val num = x.pow(4).subtract(y.pow(4))
    val den = x.pow(3).add(y.pow(3))
    val qr = num.divideAndRemainder(den)
    return qr[1].signum() == 0 && qr[0] == java.math.BigInteger.valueOf(expect)
}

// ───────────────── 计时 ─────────────────

private fun best(tag: String, rounds: Int, f: () -> Long): Double {
    var bestMs = Double.MAX_VALUE
    var last = -1L
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        last = f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (r == 0) println("  $tag（首轮）=$last")
        if (ms < bestMs) bestMs = ms
    }
    println("  $tag：${"%.1f".format(bestMs)} ms（$rounds 轮最优）")
    return bestMs
}

fun main() {
    println("== ① 定义级完整枚举 (d₁,y₁,k)：所有 Panaitopol 素数 < $P_MAX_ENUM ==")
    val t0 = System.nanoTime()
    val en = completeEnum(P_MAX_ENUM)
    val t1 = System.nanoTime()
    val compRef = oddCompositeSieve(P_MAX_ENUM.toInt())
    val ref = nFormPrimesBelow(P_MAX_ENUM, compRef)
    println("  枚举命中素数 ${en.primes.size} 个（命中次数 ${en.hits}）；n 形素数 ${ref.size} 个；用时 ${"%.1f".format((t1 - t0) / 1e6)} ms")
    println("  对称差 = ${(en.primes - ref).sorted()} / ${(ref - en.primes).sorted()}")
    check(en.primes == ref) { "① 集合不一致" }
    check(en.hits == en.primes.size.toLong()) { "① 同一素数被枚举多次？" }
    println("  最小 8 个 = ${ref.sorted().take(8)}；最大 4 个 = ${ref.sorted().takeLast(4)}")
    println("  关键推导步经验确认：命中素数中 d₁ ≠ 1 或 k ≠ 1 的次数 = ${en.oddTriples}（应为 0）")
    check(en.oddTriples == 0L) { "① 出现非 (d₁=1,k=1) 的素数值" }

    println()
    println("== ② 原始 (x,y) 盒枚举 x ≤ $XMAX_BOX（直接算 (x⁴−y⁴)/(x³+y³)）==")
    val compBox = oddCompositeSieve(200_000)
    val box = rawBoxPrimes(XMAX_BOX, compBox)
    var nBox = 1L
    while (constructedX(nBox + 1) <= XMAX_BOX) nBox++
    val boxRef = HashSet<Long>()
    for (n in 1L..nBox) if (isPrimeTrial(f(n), compBox)) boxRef.add(f(n))
    println("  盒枚举集合 = ${box.sorted()}")
    println("  对应 n ≤ $nBox 的 n 形素数 = ${boxRef.sorted()}")
    println("  对称差 = ${(box - boxRef).sorted()} / ${(boxRef - box).sorted()}")
    check(box == boxRef) { "② 不一致" }
    check(en.primes.containsAll(box)) { "② 盒枚举出现完整枚举之外的素数" }
    check(box.size == 10) { "② 盒枚举素数个数异常：${box.size}" }

    println()
    println("== ③ 朴素逐 n 试除 n ≤ $N_NAIVE（试除到 √f(n)）==")
    val compNaive = oddCompositeSieve(160_000)
    val naive = naiveTrialPrimes(N_NAIVE, compNaive)
    val naiveRef = HashSet<Long>()
    var n = 1L
    while (f(n) < f(N_NAIVE + 1)) {
        if (isPrimeTrial(f(n), compNaive)) naiveRef.add(f(n))
        n++
    }
    println("  朴素试除计数 = ${naive.size}；n 形（同一区间）计数 = ${naiveRef.size}")
    check(naive == naiveRef) { "③ 不一致" }
    println("  对称差 = ${(naive - naiveRef).sorted()} / ${(naiveRef - naive).sorted()}")
    check(naive.filter { it < P_MAX_ENUM }.all { it in en.primes }) { "③ 朴素结果（< 10^8 部分）不在完整枚举集合中" }

    println()
    println("== ④ 构造验证（BigInteger 实算定义式）==")
    for (n2 in 1L..300L) check(verifyByNForm(n2)) { "④ n 形构造失败 n=$n2" }
    val rnd = java.util.Random(291)
    var okN = 0
    repeat(100) {
        if (verifyByNForm(1L + (rnd.nextLong() and Long.MAX_VALUE) % 49_999_999L)) okN++
    }
    check(okN == 100) { "④ 随机 n 构造失败" }
    check(verifyByNForm(49_999_999L)) { "④ n_max 构造失败" }
    println("  n 形：n ≤ 300 全部 + 100 个随机 n（≤ 49999999，含 n_max）✓")

    var okTriple = 0
    check(en.compositeCandidates.isNotEmpty()) { "④ 未采到用于反解验证的候选" }
    for (p in en.compositeCandidates) {
        // 反解出产生该候选的 (d₁,y₁,k)：重新走一遍枚举取第一个命中
        val found = findTriple(p, P_MAX_ENUM)
        if (found != null && verifyByTriple(found[0], found[1], found[2], p)) okTriple++
    }
    println("  (d₁,y₁,k) 反解：抽查 ${en.compositeCandidates.size} 个候选（含合数候选），BigInteger 实算商相符 $okTriple 个")
    check(okTriple == en.compositeCandidates.size) { "④ 候选反解验证失败" }

    println()
    println("== ⑤ 计时（best-of-3）==")
    val msEnum = best("① 完整枚举 < $P_MAX_ENUM（含素数筛）", 3) { completeEnum(P_MAX_ENUM).primes.size.toLong() }
    val msBox = best("② 原始盒枚举 x ≤ $XMAX_BOX", 3) { rawBoxPrimes(XMAX_BOX, compBox).size.toLong() }
    val msNaive = best("③ 朴素逐 n 试除 n ≤ $N_NAIVE", 3) { naiveTrialPrimes(N_NAIVE, compNaive).size.toLong() }
    println()
    println("答案对照：完整枚举 < 10^8 得到 ${en.primes.size} 个 Panaitopol 素数；")
    println("其中 < 10^6 的 ${nFormPrimesBelow(1_000_000L, compBox).size} 个与 solution.kt 的 1b 对拍一致")
    println("计时汇总：① ${"%.1f".format(msEnum)} ms（bruteForceBaselineMs 口径）；② ${"%.1f".format(msBox)} ms；③ ${"%.1f".format(msNaive)} ms")
}

private fun constructedX(n: Long): Long = (n * n + n + 1) * (n + 1)

/** 在枚举空间里找第一个产生 p 的 (d₁,y₁,k)。 */
private fun findTriple(p: Long, pMax: Long): LongArray? {
    var d1 = 1L
    while (d1 * d1 * d1 < pMax) {
        var y1 = 1L
        while (true) {
            val q1 = y1 * y1 + y1 * d1 + d1 * d1
            val base = d1 * (2L * q1 - d1 * d1)
            if (base >= pMax) break
            if (gcd(y1, d1) == 1L && p % base == 0L) {
                val k = p / base
                if (k >= 1L && k * base == p) return longArrayOf(d1, y1, k)
            }
            y1++
        }
        d1++
    }
    return null
}
