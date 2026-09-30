#!/usr/bin/env kotlin
/**
 * Project Euler 291 — Panaitopol Primes（帕纳伊托波尔素数）
 *
 * 题目
 * ────
 * 若素数 p 能写成 p = (x⁴ − y⁴)/(x³ + y³)（x、y 为正整数），就称 p 为 Panaitopol 素数；
 * 求小于 5×10^15 的 Panaitopol 素数个数。
 *
 * 关键结论：Panaitopol 素数恰好是 p = n² + (n+1)² = 2n²+2n+1 形式的素数
 * ─────────────────────────────────────────────────────────────
 * x⁴−y⁴ = (x−y)(x+y)(x²+y²)，x³+y³ = (x+y)(x²−xy+y²)，约掉正因子 x+y：
 *     p = (x−y)(x²+y²)/(x²−xy+y²)。
 * 取 g = gcd(x,y)，x = gX、y = gY（gcd(X,Y)=1，X>Y≥1），记 d = X−Y、Q = X²−XY+Y²：
 *     p = g·d·(X²+Y²)/Q，    X²+Y² = 2Q − d²。
 * gcd(Q,d) | Y²（由 Q ≡ Y² mod d）且 gcd(Y,d) = gcd(X,Y) = 1，所以 gcd(Q,d) = 1，进而
 * gcd(Q, X²+Y²) = gcd(Q, d²) = 1；于是 Q | g·d。设 h = gcd(Q,d)、Q = hQ₁、d = hd₁
 * （gcd(Q₁,d₁) = 1），由 Q | gd 得 Q₁ | g，即 g = k·Q₁，代回：
 *     p = k·(d/h)·(X²+Y²) = k·d₁·(X²+Y²)。
 * p 是素数、两个因子都 ≥ 1 且 X²+Y² ≥ 2，所以 k·d₁ = 1：即 k = d₁ = 1，也就是 d | Q，且
 * p = X²+Y²。又 Q ≡ X² (mod d) 给出 d | X²，而 gcd(d,X) = gcd(X−Y,X) = gcd(Y,X) = 1，
 * 于是 d = 1：X = Y+1，p = Y² + (Y+1)²。
 * 反过来，对任意 n ≥ 1，取 g = n²+n+1、x = g(n+1)、y = gn，商恰为 n²+(n+1)²。
 * 因此 **Panaitopol 素数 ⇔ f(n) = 2n²+2n+1 为素数**；f(n) < 5×10^15 ⟺ n ≤ 49999999。
 *
 * 计数路线（两条互相独立的实现）
 * ─────────────────────────────
 * A（主路径，全量二次筛）：q | f(n) ⟺ (2n+1)² ≡ −1 (mod q)，故只有 q ≡ 1 (mod 4) 的素数
 *   （q = 2 不行，f(n) 恒奇）能整除 f(n)；反过来 f(n) 的每个素因子都 ≡ 1 mod 4，于是合数
 *   f(n) 必含 ≤ √(f(n_max)) = 70710677 的素因子。用所有 q ≡ 1 (mod 4)、q ≤ 70710677 的素数
 *   标记被整除的 n（每个 q 两个根），**幸存者即素数**，全程不需要素性测试。
 *   边界处理：前 10000 个 n 单独逐个试除，避免「f(n) = q 自身被误标」。
 * B（交叉验证，小筛 + 确定性 MR）：筛掉 q ≤ 10^7 的素因子后，幸存者逐个做 9 基确定性
 *   Miller–Rabin（基 {2,3,5,7,11,13,17,19,23} 对 < 3.8×10^18 判定有效；f(n) < 5×10^15）。
 *   因 f(n) < 2^53，模乘用 Montgomery（R = 2^64）做精确的 128 位乘模。
 *
 * 对拍与自检（main 逐项打印证据）
 * ────────────────────────────
 * ① 定义级：(x,y) 盒枚举 x ≤ 10^4（5×10^7 对）直接算 (x⁴−y⁴)/(x³+y³)，取整可素的值；
 * ② 完整枚举：x = y+d、g = gcd(y,d)、y = gy₁、d = gd₁（gcd(y₁,d₁)=1）⇒ Q₁ = y₁²+y₁d₁+d₁²，
 *    整数性 Q | d³ ⟺ Q₁ | g，故 g = kQ₁、商 = k·d₁·(2Q₁−d₁²)；枚举 (d₁,y₁,k) 完整覆盖所有
 *    (x,y)，对 p < 10^6 与 n 形素数集合逐元素对照；
 * ③ 根计算复核：q ≤ 10^4 的根与 n ∈ [0,q) 全扫对照；大素数抽样验证 (2n+1)² ≡ −1 (mod q)；
 * ④ 素性自检：Miller–Rabin 与埃氏筛 < 10^7 逐值对拍；Montgomery 乘模与 128 位慢速乘模随机对拍；
 * ⑤ 构造验证：n ≤ 500 全部 + 随机大 n（含 n_max）用 BigInteger 实算商，须等于 f(n)。
 *
 * 复杂度
 * ──────
 * A：素数筛到 7.07×10^7（2.08×10^6 个 q ≡ 1 mod 4，各算一次 sqrt(−1)）+ 约 1.16×10^8 次标记；
 * B：筛到 10^7 + 约 4.5×10^6 个幸存者 × 9 基 Miller–Rabin。
 *
 * 运行
 * ────
 * OUTDIR=/tmp/kc-0291 bash scripts/kotlinc-shim.sh content/problems/0291/solution.kt
 * java -Xmx4g -cp /tmp/kc-0291:<kotlin-stdlib> SolutionKt
 */

private const val LIMIT = 5_000_000_000_000_000L      // 题目上界 5×10^15
private const val N_SKIP = 10_000                     // 前 10000 个 n 单独试除（避开 f(n) = q 的伪标记）
private const val SMALL_SIEVE_B = 10_000_000L         // 路径 B 的小素数筛上界
private const val XMAX_BOX = 10_000L                  // 定义级盒枚举的 x 上界
private const val P_SMALL_ENUM = 1_000_000L           // 完整枚举对照的 p 上界
private const val SIEVE_REF = 10_000_000              // MR 自检用的埃氏筛上界

private val MR_BASES = longArrayOf(2, 3, 5, 7, 11, 13, 17, 19, 23)

// ─────────────────────────────── 基础函数 ───────────────────────────────

/** f(n) = 2n² + 2n + 1 = n² + (n+1)²。 */
private fun f(n: Long): Long = 2L * n * n + 2L * n + 1L

private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

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

/** f(n) < limit 的最大 n。由 (2n+1)² + 1 < 2·limit 得 2n+1 ≤ isqrt(2·limit − 2)。 */
private fun maxN(limit: Long): Long = (isqrt(2L * limit - 2L) - 1L) / 2L

/** 构造 x = (n²+n+1)(n+1)（f(n) 素时它给出题面中的 x）。 */
private fun constructedX(n: Long): Long = (n * n + n + 1) * (n + 1)

// ─────────────────────────────── 筛与试除 ───────────────────────────────

/** comp[i] = true ⟺ 2i+1 不是素数（i = 0 对应 1）；覆盖所有 ≤ limit 的奇数。 */
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

/** 用 comp 表试除判定 v 是否素数（要求 comp 至少覆盖到 √v）。 */
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

/** n = 1..N_SKIP 的 f(n) 逐个试除计数。 */
private fun countSmallDirect(comp: BooleanArray): Long {
    var cnt = 0L
    for (n in 1L..N_SKIP.toLong()) if (isPrimeTrial(f(n), comp)) cnt++
    return cnt
}

// ─────────────────────────── 模运算与素性测试 ───────────────────────────

/** (a·b) mod m，a、b < m ≤ 7×10^7：乘积不溢出 Long。 */
private fun powModSmall(a0: Long, e0: Long, m: Long): Long {
    var r = 1L
    var b = a0 % m
    var e = e0
    while (e > 0L) {
        if (e and 1L == 1L) r = r * b % m
        b = b * b % m
        e = e shr 1
    }
    return r
}

/** q ≡ 1 (mod 4) 素数：解 s² ≡ −1 (mod q)（先找二次非剩余 a，再算 a^((q−1)/4)）。 */
private fun sqrtMinusOne(q: Long): Long {
    val e = (q - 1) / 4
    var a = 2L
    while (true) {
        val z = powModSmall(a, e, q)
        if (z * z % q == q - 1) return z
        a++
    }
}

/**
 * q | f(n) ⟺ (2n+1)² ≡ −1 (mod q) ⟺ 2n+1 ≡ ±s (mod q)。
 * 2n+1 必须为奇数，故两个根取 mod 2q 的奇数代表（s 奇取 s，s 偶取 s+q）。
 */
private fun oddRoots(s: Long, q: Long): LongArray {
    val t = if (s and 1L == 1L) s else s + q
    return longArrayOf(t, 2L * q - t)
}

/** 128 位精确乘模 (a·b) mod m，要求 0 ≤ a,b < m < 2^53：拆 a·b = hi·2^64 + lo，高位按 2^64 ≡ K 回代。 */
private fun slowMulMod(a: Long, b: Long, m: Long): Long {
    val hi = Math.multiplyHigh(a, b)
    val lo = a * b
    if (hi == 0L) return java.lang.Long.remainderUnsigned(lo, m)
    var k = java.lang.Long.remainderUnsigned(-1L, m) + 1L
    if (k >= m) k -= m
    var h = hi
    var acc = java.lang.Long.remainderUnsigned(lo, m)
    while (h != 0L) {
        val ph = Math.multiplyHigh(h, k)
        val pl = h * k
        acc += java.lang.Long.remainderUnsigned(pl, m)
        if (acc >= m) acc -= m
        h = ph
    }
    return acc
}

/** 模 m（奇数、m < 2^53）的 Montgomery 乘法，R = 2^64：mul(a,b) = a·b·R⁻¹ mod m。 */
private class Montgomery(private val n: Long) {
    /** −n⁻¹ mod 2^64（Newton 迭代，6 轮 = 64 位精度）。 */
    private val nInv: Long = run {
        var x = 1L
        repeat(6) { x *= 2L - n * x }
        -x
    }

    /** R mod m，即 1 的 Montgomery 形式。 */
    val one: Long = run {
        var k = java.lang.Long.remainderUnsigned(-1L, n) + 1L
        if (k >= n) k -= n
        k
    }

    /** R² mod m，用于把普通整数搬进 Montgomery 形式。 */
    val r2: Long = slowMulMod(one, one, n)

    fun mul(a: Long, b: Long): Long {
        val tLo = a * b
        val tHi = Math.multiplyHigh(a, b)
        val m = tLo * nInv
        val mnLo = m * n
        val mnHi = Math.unsignedMultiplyHigh(m, n)
        var u = tHi + mnHi
        if (java.lang.Long.compareUnsigned(tLo + mnLo, tLo) < 0) u += 1L
        if (java.lang.Long.compareUnsigned(u, n) >= 0) u -= n
        return u
    }

    fun toMont(a: Long): Long = mul(a, r2)

    fun pow(aMont: Long, e0: Long): Long {
        var r = one
        var b = aMont
        var e = e0
        while (e > 0L) {
            if (e and 1L == 1L) r = mul(r, b)
            b = mul(b, b)
            e = e shr 1
        }
        return r
    }
}

/** 确定性 Miller–Rabin：九基 {2,…,23} 对 n < 3.8×10^18 无假阳性（本题 n < 5×10^15 < 2^53）。 */
private fun isPrimeDeterministic(n: Long): Boolean {
    if (n < 2L) return false
    for (b in MR_BASES) if (n % b == 0L) return n == b
    val mont = Montgomery(n)
    var d = n - 1L
    var s = 0
    while (d and 1L == 0L) {
        d = d shr 1
        s++
    }
    val minusOne = n - mont.one
    for (b in MR_BASES) {
        var x = mont.pow(mont.toMont(b), d)
        if (x == mont.one || x == minusOne) continue
        var pass = false
        for (r in 1 until s) {
            x = mont.mul(x, x)
            if (x == minusOne) {
                pass = true
                break
            }
        }
        if (!pass) return false
    }
    return true
}

// ─────────────────────── 定义级 / 代数级对拍用枚举 ───────────────────────

/** 定义级盒枚举：x ≤ xMax 的所有 (x,y) 算 (x⁴−y⁴)/(x³+y³)，收集其中的素数。 */
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

/**
 * 完整枚举所有 Panaitopol 素数 p < pMax（不做任何素数性推导，只是把 (x,y) 空间重参数化）：
 * x = y+d，g = gcd(y,d)，y = gy₁、d = gd₁（gcd(y₁,d₁)=1）⇒ Q = g²Q₁，Q₁ = y₁²+y₁d₁+d₁²；
 * 整数性 Q | d³ ⟺ Q₁ | g，故 g = kQ₁，商 = k·d₁·(2Q₁ − d₁²)。逐个查素数表。
 */
private fun completeEnumPrimes(pMax: Long, comp: BooleanArray): Set<Long> {
    val out = HashSet<Long>()
    var d1 = 1L
    while (d1 * d1 * d1 < pMax) {
        var y1 = 1L
        while (true) {
            val q1 = y1 * y1 + y1 * d1 + d1 * d1
            val base = d1 * (2L * q1 - d1 * d1)
            if (base >= pMax) break
            if (gcd(y1, d1) == 1L) {
                var p = base
                while (p < pMax) {
                    // 偶数候选必不为素数（base ≥ 5）
                    if (p % 2L == 1L && !comp[((p - 1) / 2).toInt()]) out.add(p)
                    p += base
                }
            }
            y1++
        }
        d1++
    }
    return out
}

/** n 形素数集合：f(n) < pMax 且 f(n) 为素数。 */
private fun nFormPrimesBelow(pMax: Long, comp: BooleanArray): Set<Long> {
    val out = HashSet<Long>()
    var n = 1L
    while (f(n) < pMax) {
        if (isPrimeTrial(f(n), comp)) out.add(f(n))
        n++
    }
    return out
}

/** n 形素数集合：n ≤ nMax 且 f(n) 为素数。 */
private fun nFormPrimesUpToN(nMax: Long, comp: BooleanArray): Set<Long> {
    val out = HashSet<Long>()
    for (n in 1L..nMax) if (isPrimeTrial(f(n), comp)) out.add(f(n))
    return out
}

/** 用 BigInteger 实算定义式：验证 (x⁴−y⁴)/(x³+y³) 的商恰为 f(n)，x、y 按结论构造。 */
private fun verifyConstruction(n: Long): Boolean {
    val g = java.math.BigInteger.valueOf(n * n + n + 1)
    val x = g.multiply(java.math.BigInteger.valueOf(n + 1))
    val y = g.multiply(java.math.BigInteger.valueOf(n))
    val num = x.pow(4).subtract(y.pow(4))
    val den = x.pow(3).add(y.pow(3))
    val qr = num.divideAndRemainder(den)
    return qr[1].signum() == 0 && qr[0] == java.math.BigInteger.valueOf(f(n))
}

// ─────────────────────────────── 计数主路径 ───────────────────────────────

/** 路径 A：q ≡ 1 (mod 4)、q ≤ √f(nMax) 的全量二次筛，幸存者即素数。 */
private fun countByFullSieve(nMax: Long, verbose: Boolean = false): Long {
    val qMax = isqrt(f(nMax))
    val comp = oddCompositeSieve(qMax.toInt())
    val nInt = nMax.toInt()
    val alive = BooleanArray(nInt + 1) { true }
    val mMax = 2L * nMax + 1L
    var marks = 0L
    var i = 1
    while (i < comp.size) {
        if (!comp[i]) {
            val q = 2L * i + 1
            if (q % 4L == 1L) {
                val step = 2L * q
                for (r in oddRoots(sqrtMinusOne(q), q)) {
                    var m = r
                    while (m <= mMax) {
                        val n = (m - 1) shr 1
                        if (n > N_SKIP) {
                            alive[n.toInt()] = false
                            marks++
                        }
                        m += step
                    }
                }
            }
        }
        i++
    }
    var cnt = 0L
    for (n in (N_SKIP + 1)..nInt) if (alive[n]) cnt++
    if (verbose) {
        println("  [A] √f(n_max) = $qMax；标记 $marks 次；幸存者 $cnt；小 n 素数 ${countSmallDirect(comp)}")
    }
    return cnt + countSmallDirect(comp)
}

/** 路径 B：筛掉 q ≤ b（q ≡ 1 mod 4）的素因子，幸存者做确定性 Miller–Rabin。 */
private fun countBySmallSievePlusMr(nMax: Long, b: Long, verbose: Boolean = false): Long {
    val comp = oddCompositeSieve(b.toInt())
    val nInt = nMax.toInt()
    val alive = BooleanArray(nInt + 1) { true }
    val mMax = 2L * nMax + 1L
    var i = 1
    while (i < comp.size) {
        if (!comp[i]) {
            val q = 2L * i + 1
            if (q % 4L == 1L) {
                val step = 2L * q
                for (r in oddRoots(sqrtMinusOne(q), q)) {
                    var m = r
                    while (m <= mMax) {
                        val n = (m - 1) shr 1
                        if (n > N_SKIP) alive[n.toInt()] = false
                        m += step
                    }
                }
            }
        }
        i++
    }
    var cnt = 0L
    var survivors = 0L
    for (n in (N_SKIP + 1)..nInt) {
        if (alive[n]) {
            survivors++
            if (isPrimeDeterministic(f(n.toLong()))) cnt++
        }
    }
    if (verbose) println("  [B] 筛到 $b；幸存者 $survivors；其中素数 $cnt")
    return cnt + countSmallDirect(comp)
}

// ─────────────────────────────── 计时辅助 ───────────────────────────────

private fun bestOf(tag: String, expected: Long, rounds: Int, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${r + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("  $tag：${"%.1f".format(best)} ms（$rounds 轮最优，JIT 预热后）")
    return best
}

// ─────────────────────────────── 主程序 ───────────────────────────────

fun main() {
    val nMax = maxN(LIMIT)
    println("== 0. 关键量 ==")
    println("  f(n) = 2n²+2n+1 = n²+(n+1)²；f(n) 的一切素因子 ≡ 1 (mod 4)（(2n+1)² ≡ −1）")
    println("  f(n) < 5×10^15 ⟺ n ≤ $nMax；f($nMax) = ${f(nMax)}；√f(n_max) = ${isqrt(f(nMax))}")

    println()
    println("== 1. 定义级对拍 ==")
    val compBox = oddCompositeSieve(200_000)
    val box = rawBoxPrimes(XMAX_BOX, compBox)
    var nBox = 1L
    while (constructedX(nBox + 1) <= XMAX_BOX) nBox++
    val boxRef = nFormPrimesUpToN(nBox, compBox)
    println("  1a (x,y) 盒枚举 x ≤ $XMAX_BOX：${box.sorted()}")
    println("     对应 n ≤ $nBox 的 n 形素数：${boxRef.sorted()}")
    println("     对称差 = ${(box - boxRef).sorted()} / ${(boxRef - box).sorted()}")
    check(box == boxRef) { "1a 不一致" }

    val compEnum = oddCompositeSieve(P_SMALL_ENUM.toInt())
    val enumSet = completeEnumPrimes(P_SMALL_ENUM, compEnum)
    val enumRef = nFormPrimesBelow(P_SMALL_ENUM, compEnum)
    println("  1b 完整枚举 (d₁,y₁,k) p < $P_SMALL_ENUM：${enumSet.size} 个；n 形集合：${enumRef.size} 个")
    println("     对称差 = ${(enumSet - enumRef).sorted()} / ${(enumRef - enumSet).sorted()}")
    check(enumSet == enumRef) { "1b 不一致" }

    println()
    println("== 2. 根计算复核 ==")
    val compRoot = oddCompositeSieve(10_000)
    var qCount = 0
    var i = 1
    while (i < compRoot.size) {
        if (!compRoot[i]) {
            val q = 2L * i + 1
            if (q % 4L == 1L) {
                qCount++
                val expect = HashSet<Long>()
                for (nn in 0L until q) if (f(nn) % q == 0L) expect.add(nn)
                val got = HashSet<Long>()
                val step = 2L * q
                for (r in oddRoots(sqrtMinusOne(q), q)) {
                    var m = r
                    while (m <= 2L * q) {
                        val nn = (m - 1) shr 1
                        if (nn < q) got.add(nn)
                        m += step
                    }
                }
                check(got == expect) { "q = $q 的根不一致：$got ≠ $expect" }
            }
        }
        i++
    }
    println("  q ≤ 10^4 的 $qCount 个素数 ≡ 1 (mod 4)：根与 n ∈ [0,q) 全扫逐个一致")

    val compBig = oddCompositeSieve(isqrt(f(nMax)).toInt())
    var sampled = 0
    var acc = 0L
    i = 1
    while (i < compBig.size) {
        if (!compBig[i]) {
            val q = 2L * i + 1
            if (q % 4L == 1L && q > 10_000L) {
                acc++
                if (acc % 32_475L == 0L) {           // 均匀抽样
                    sampled++
                    for (r in oddRoots(sqrtMinusOne(q), q)) {
                        check(r % 2L == 1L && r in 1L until 2L * q) { "根 $r 非法" }
                        check(r * r % q == q - 1) { "q = $q 的根 $r 不满足 r² ≡ −1" }
                    }
                }
            }
        }
        i++
    }
    println("  q ≤ 7.07×10^7 的大素数抽样 $sampled 个：两个根均满足 (2n+1)² ≡ −1 (mod q)")

    println()
    println("== 3. 素性函数自检 ==")
    val compRef = oddCompositeSieve(SIEVE_REF)
    var tested = 0L
    var mismatch = 0L
    var v = 3L
    var t0 = System.nanoTime()
    while (v < SIEVE_REF.toLong()) {
        val bySieve = !compRef[((v - 1) / 2).toInt()]
        if (bySieve != isPrimeDeterministic(v)) mismatch++
        tested++
        v += 2L
    }
    val msMr = (System.nanoTime() - t0) / 1e6
    println("  Miller–Rabin vs 埃氏筛：3 ≤ 奇数 < 10^7 共 $tested 个，失配 $mismatch（${"%.1f".format(msMr)} ms）")
    check(mismatch == 0L) { "MR 自检失配" }

    val rnd = java.util.Random(291)
    var montBad = 0
    repeat(200_000) {
        val bits = 3 + rnd.nextInt(50)
        var m = (rnd.nextLong() and ((1L shl bits) - 1L)) or 3L
        if (m % 2L == 0L) m++
        val a = (rnd.nextLong() and Long.MAX_VALUE) % m
        val b = (rnd.nextLong() and Long.MAX_VALUE) % m
        val mont = Montgomery(m)
        val back = mont.mul(mont.mul(mont.toMont(a), mont.toMont(b)), 1L)
        if (back != slowMulMod(a, b, m)) montBad++
    }
    println("  Montgomery 乘模 vs 128 位慢速乘模：随机 200000 组，失配 $montBad")
    check(montBad == 0) { "Montgomery 失配" }

    println()
    println("== 4. 构造验证（BigInteger 实算定义式）==")
    for (n in 1L..500L) check(verifyConstruction(n)) { "构造失败 n=$n" }
    var ok = 0
    repeat(200) {
        val n = 1L + (rnd.nextLong() and Long.MAX_VALUE) % nMax
        if (verifyConstruction(n)) ok++
    }
    check(ok == 200) { "随机构造验证失败" }
    check(verifyConstruction(nMax))
    println("  n = 1..500 全部 + 200 个随机大 n（含 n_max = $nMax）：(x⁴−y⁴)/(x³+y³) 商 = f(n) 且整除 ✓")

    println()
    println("== 5. 主路径 A：全量二次筛 ==")
    val ansA = countByFullSieve(nMax, verbose = true)
    println("  路径 A 答案 = $ansA")

    println()
    println("== 6. 交叉路径 B：小筛 + 确定性 Miller–Rabin ==")
    val tB0 = System.nanoTime()
    val ansB = countBySmallSievePlusMr(nMax, SMALL_SIEVE_B, verbose = true)
    val msB = (System.nanoTime() - tB0) / 1e6
    println("  路径 B 答案 = $ansB；与 A 一致：${ansA == ansB}（用时 ${"%.1f".format(msB)} ms，单轮）")
    check(ansA == ansB) { "两条路径不一致：$ansA ≠ $ansB" }

    println()
    println("== 7. 计时（best-of-3，JIT 预热后）==")
    val msA = bestOf("路径 A（全量二次筛）", ansA, 3) { countByFullSieve(nMax) }
    val msBox = bestOf("定义级盒枚举 x ≤ 10^4", box.size.toLong(), 3) { rawBoxPrimes(XMAX_BOX, compBox).size.toLong() }
    val msEnum = bestOf("完整枚举 (d₁,y₁,k) p < 10^6", enumSet.size.toLong(), 3) { completeEnumPrimes(P_SMALL_ENUM, compEnum).size.toLong() }

    println()
    println("== 8. 结果 ==")
    println("小于 5×10^15 的 Panaitopol 素数个数 = $ansA")
    println("答案 = $ansA（十进制）")
    println("计时：路径 A ${"%.1f".format(msA)} ms（best-of-3）；路径 B ${"%.1f".format(msB)} ms（单轮）；盒枚举 ${"%.1f".format(msBox)} ms；完整枚举 < 10^6 ${"%.1f".format(msEnum)} ms")
}
