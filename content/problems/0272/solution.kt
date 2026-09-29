#!/usr/bin/env kotlin
/**
 * Project Euler 272 — Modular Cubes, Part 2（模立方 II）
 *
 * 思路
 * ────
 * 记 R(n) = #{x ∈ [0, n) : x³ ≡ 1 (mod n)}。n > 1 时 x = 0 不是解、x = 1 是解，而 x = n−1 不是解
 *（(−1)³ = −1 ≡ 1 (mod n) 只在 n | 2 时成立），故题面的 C(n) = R(n) − 1。
 *
 * 由 CRT，R 对素因子幂可乘：R(n) = ∏ R(p^e)；(Z/p^e)* 是循环群，x³ = 1 的解数 = gcd(3, φ(p^e))：
 *   · p = 2 或 p ≡ 2 (mod 3)：gcd(3, φ) = 1 → R(p^e) = 1；
 *   · p = 3, e = 1：φ = 2 → 1；p = 3, e ≥ 2：φ = 2·3^{e−1} → 3（解 1, 1+3^{e−1}, 1+2·3^{e−1}）；
 *   · p ≡ 1 (mod 3)：3（与指数无关，因 3 | p−1 | φ(p^e)）。
 * 记 k(n) = n 的不同素因子中 ≡ 1 (mod 3) 的个数，则
 *
 *   C(n) = 3^{k(n) + [9|n]} − 1,   C(n) = 242 = 3⁵ − 1  ⟺  k(n) + [9|n] = 5.
 *
 * 把 n 唯一分解为 n = s·m：m = n 中全部 ≡ 1 (mod 3) 素因子（带指数）之积，s = 其余部分
 *（素因子只有 2、3 与 ≡ 2 (mod 3) 的素数）。于是恰好两种情形：
 *   · k(n) = 5 且 9 ∤ n：m 恰含 5 个不同素数，s 不被 9 整除；
 *   · k(n) = 4 且 9 | n：m 恰含 4 个不同素数，s 被 9 整除；写 s = 9s′ 则 s′ 也是「素因子 ≢ 1 (mod 3)」的数。
 * 故（B = 10¹¹，P_all(L) = Σ_{s ≤ L, 素因子 ≢ 1 (mod 3)} s，P₀(L) = P_all(L) − 9·P_all(⌊L/9⌋)）
 *
 *   答案 = Σ_{m₅ ≤ B} m₅·P₀(⌊B/m₅⌋) + 9·Σ_{m₄ ≤ B/9} m₄·P_all(⌊(B/9)/m₄⌋).
 *
 * 实现要点：
 *   · s 侧：把 sMax = ⌊B/53599⌋ 内「被某个 ≡1 (mod 3) 素数整除」的数标记出来，再求两套前缀和
 *     （P₀ 与 P_all 的稠密小表 + 长尾二分）；
 *   · m 侧：「升序素数 + 指数 ≥ 1、恰含 k 个不同素数」的 DFS，用「后继最小可用素因子乘积」剪枝，
 *     递归里直接携带商 q = ⌊上界/m⌋，叶子处用 ⌊q/p^e⌋ 做 O(1) 查表（不做除法热点、不重不漏）；
 *   · 总和用带溢出检查的 Long 精确累加。
 *
 * 复杂度
 * ──────
 * 筛素数 O(P log log P)（P ≈ 6.43×10⁶ 为最大可能的三次素因子）、s 标记 O(sMax log log sMax)；
 * m 枚举叶子总数约 4.1×10⁷（= 合法 n 的「m 侧」规模），每叶 O(1) 查表，总时间 O(#m + P + sMax)。
 * 对 n ≤ 10¹¹ 逐个数数（暴力）需要 10¹¹ 次判定，不可行。
 *
 * 验证
 * ────
 * 1. 题面锚点：C(91) = 8，解集 {9,16,22,29,53,74,79,81}；
 * 2. 定义域暴力：n ≤ 2000 逐个 x 枚举的 C(n) 与 3^{k+[9|n]} − 1 全部一致；
 * 3. 线性筛法（独立算法）逐 n 累加 n ≤ 10⁷ 的答案，与主路径限同界结果一致（两分支都被覆盖）；
 * 4. 方法 B（独立路径）：m 改用「最大素因子优先」降序枚举 + 按 ⌊B/m⌋ 建直方图，s 侧改用递归生成
 *    （与标记筛独立）并只用 P_all 表示 P₀ = P_all − 9·P_all(⌊L/9⌋)，外层求点积、大整数累加，全量一致；
 * 5. 公开答案表旁证（见 analysis.md）。
 *
 * 答案：8495585919506151122（< Long.MAX_VALUE = 9223372036854775807，实跑无溢出）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0272/solution.kt -d /tmp/kc-0272
 * java -cp /tmp/kc-0272:/Users/samuel/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/2.1.21/97a0975aa19d925e109537af60eb46902920015c/kotlin-stdlib-2.1.21.jar SolutionKt
 */

import java.math.BigInteger

private const val BOUND = 100_000_000_000L // 10^11
private const val TAB = 50_424 // ⌊10^11/(7·13·19·31·37)⌋，覆盖全部 5-素 m 的查表点

// ───────────────────────── 基础工具 ─────────────────────────

/** 埃氏筛：≤ limit 的素数（升序）。 */
private fun primesUpTo(limit: Int): LongArray {
    val isComp = BooleanArray(limit + 1)
    val out = ArrayList<Long>()
    for (i in 2..limit) {
        if (!isComp[i]) {
            out.add(i.toLong())
            if (i.toLong() * i <= limit) {
                var j = i * i
                while (j <= limit) { isComp[j] = true; j += i }
            }
        }
    }
    return out.toLongArray()
}

/** 升序 + 前缀和 + 稠密小表（≤ denseLimit 的和/个数查询 O(1)，更长的走二分）。 */
private class CondSum(
    val sorted: LongArray,
    val pref: LongArray,
    private val denseSum: LongArray,
    private val denseCount: IntArray
) {
    /** 第一个「> l」的元素下标（即 ≤ l 的元素个数）。 */
    private fun upperIndex(l: Long): Int {
        var lo = 0
        var hi = sorted.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (sorted[mid] <= l) lo = mid + 1 else hi = mid
        }
        return lo
    }

    fun at(l: Long): Long = if (l < denseSum.size) denseSum[l.toInt()] else pref[upperIndex(l)]

    fun countUpTo(l: Long): Long =
        if (l < denseCount.size) denseCount[l.toInt()].toLong() else upperIndex(l).toLong()
}

private fun buildCondSum(values: LongArray, denseLimit: Int): CondSum {
    val sorted = values.copyOf().also { it.sort() }
    val pref = LongArray(sorted.size + 1)
    for (i in sorted.indices) pref[i + 1] = pref[i] + sorted[i]
    val denseSum = LongArray(denseLimit + 1)
    val denseCount = IntArray(denseLimit + 1)
    var acc = 0L
    var idx = 0
    for (v in 0..denseLimit) {
        while (idx < sorted.size && sorted[idx] <= v) { acc += sorted[idx]; idx++ }
        denseSum[v] = acc
        denseCount[v] = idx
    }
    return CondSum(sorted, pref, denseSum, denseCount)
}

// ───────────────────────── m 侧枚举 ─────────────────────────

/** mt[j][i] = pᵢ·pᵢ₊₁·…·pᵢ₊ⱼ₋₁（超过 cap 记为 cap+1），用于剪枝。 */
private fun mtTable(primes: LongArray, k: Int, cap: Long): Array<LongArray> {
    val n = primes.size
    val mt = Array(k + 1) { LongArray(n + 1) { cap + 1 } }
    for (i in 0..n) mt[0][i] = 1L
    for (j in 1..k) for (i in n - 1 downTo 0) {
        val below = mt[j - 1][i + 1]
        mt[j][i] = if (below > cap / primes[i]) cap + 1 else primes[i] * below
    }
    return mt
}

/**
 * 主路径：Σ (factor·m)·CondSum.at(⌊boundForM/m⌋)，其中 m 恰含 k 个不同的 ≡1 (mod 3) 素数、
 * 每个指数 ≥ 1、m ≤ boundForM；递归携带商 q = ⌊boundForM/prod⌋，叶子处 qq = ⌊q/p^e⌋ 直接查表。
 * 返回 (加权和, m 的个数)，个数用于核对合法 n 的规模。
 */
private fun accumulateM(
    primes: LongArray, k: Int, boundForM: Long, lut: CondSum, factor: Long, mt: Array<LongArray>
): Pair<Long, Long> {
    var total = 0L
    var count = 0L
    fun rec(startIdx: Int, chosen: Int, prod: Long, q: Long) {
        val need = k - chosen
        if (need == 1) {
            for (i in startIdx until primes.size) {
                val p = primes[i]
                if (p > q) break
                var v = prod * p
                var qq = q / p
                while (true) {
                    total = Math.addExact(total, Math.multiplyExact(v * factor, lut.at(qq)))
                    count++
                    if (qq < p) break
                    v *= p
                    qq /= p
                }
            }
            return
        }
        for (i in startIdx until primes.size) {
            if (mt[need][i] > q) break
            val p = primes[i]
            val tail = mt[need - 1][i + 1]
            var v = prod * p
            var qq = q / p
            while (tail <= qq) {
                rec(i + 1, chosen + 1, v, qq)
                if (qq < p) break
                v *= p
                qq /= p
            }
        }
    }
    rec(0, 0, 1L, boundForM)
    return total to count
}

/** 方法 B：最大素因子优先的降序枚举，把 m 按 ⌊boundForM/m⌋ 累进直方图（桶数 ≤ boundForM/53599）。 */
private fun histogramM(primes: LongArray, k: Int, boundForM: Long, buckets: LongArray) {
    val minSmall = LongArray(k + 1)
    minSmall[0] = 1L
    for (j in 1..k) minSmall[j] = minSmall[j - 1] * primes[j - 1]
    fun rec(limit: Long, remaining: Int, idxHi: Int, acc: Long) {
        for (i in 0 until idxHi) {
            val p = primes[i]
            if (p > limit / minSmall[remaining - 1]) break
            var v = p
            while (true) {
                if (remaining == 1) {
                    val slot = (boundForM / (acc * v)).toInt()
                    buckets[slot] = Math.addExact(buckets[slot], acc * v)
                } else {
                    rec(limit / v, remaining - 1, i, acc * v)
                }
                if (v > limit / p) break
                v *= p
            }
        }
    }
    rec(boundForM, k, primes.size, 1L)
}

// ───────────────────────── 主路径 ─────────────────────────

/** 主路径（方法 A）：返回 (Σ_{n ≤ bound, C(n)=242} n, 合法 n 的个数)。 */
private fun solveAFull(bound: Long): Pair<Long, Long> {
    val primeLimit = maxOf(43L, bound / 9 / (7 * 13 * 19) + 1)
    val primes = primesUpTo(primeLimit.toInt())
    val p1 = primes.filter { it % 3L == 1L }.toLongArray()

    val sMax = maxOf(bound / (7 * 13 * 19 * 31), bound / (7 * 13 * 19 * 31 * 37))
    val bad = BooleanArray(sMax.toInt() + 1)
    for (p in p1) {
        if (p > sMax) break
        var m = p
        while (m <= sMax) { bad[m.toInt()] = true; m += p }
    }
    val listAll = ArrayList<Long>()
    val list0 = ArrayList<Long>() // 9 ∤ s 的部分（k = 5 分支）
    for (v in 1..sMax.toInt()) {
        if (!bad[v]) { listAll.add(v.toLong()); if (v % 9 != 0) list0.add(v.toLong()) }
    }
    val denseLimit = minOf(TAB, sMax.toInt())
    val lutAll = buildCondSum(listAll.toLongArray(), denseLimit)
    val lut0 = buildCondSum(list0.toLongArray(), denseLimit)

    val mt5 = mtTable(p1, 5, bound)
    val mt4 = mtTable(p1, 4, bound / 9)
    val (a, c5) = accumulateM(p1, 5, bound, lut0, 1L, mt5)
    val (b, c4) = accumulateM(p1, 4, bound / 9, lutAll, 9L, mt4)
    return Math.addExact(a, b) to (c5 + c4)
}

private fun solveA(bound: Long): Long = solveAFull(bound).first

/** 合法 n 的个数 = Σ_m #{s ≤ ⌊B/m⌋ 且 9 条件匹配}（单独一趟，不参与主路径计时）。 */
private fun countValidN(bound: Long): Long {
    val primeLimit = maxOf(43L, bound / 9 / (7 * 13 * 19) + 1)
    val p1 = primesUpTo(primeLimit.toInt()).filter { it % 3L == 1L }.toLongArray()
    val sMax = maxOf(bound / (7 * 13 * 19 * 31), bound / (7 * 13 * 19 * 31 * 37))
    val bad = BooleanArray(sMax.toInt() + 1)
    for (p in p1) {
        if (p > sMax) break
        var m = p
        while (m <= sMax) { bad[m.toInt()] = true; m += p }
    }
    val all = ArrayList<Long>()
    val not9 = ArrayList<Long>()
    for (v in 1..sMax.toInt()) if (!bad[v]) { all.add(v.toLong()); if (v % 9 != 0) not9.add(v.toLong()) }
    val denseLimit = minOf(TAB, sMax.toInt())
    val lutAll = buildCondSum(all.toLongArray(), denseLimit)
    val lut0 = buildCondSum(not9.toLongArray(), denseLimit)

    fun count(k: Int, boundForM: Long, lut: CondSum): Long {
        val mt = mtTable(p1, k, boundForM)
        var cnt = 0L
        fun rec(startIdx: Int, chosen: Int, prod: Long, q: Long) {
            val need = k - chosen
            if (need == 1) {
                for (i in startIdx until p1.size) {
                    val p = p1[i]
                    if (p > q) break
                    var qq = q / p
                    while (true) {
                        cnt += lut.countUpTo(qq)
                        if (qq < p) break
                        qq /= p
                    }
                }
                return
            }
            for (i in startIdx until p1.size) {
                if (mt[need][i] > q) break
                val p = p1[i]
                val tail = mt[need - 1][i + 1]
                var v = prod * p
                var qq = q / p
                while (tail <= qq) {
                    rec(i + 1, chosen + 1, v, qq)
                    if (qq < p) break
                    v *= p
                    qq /= p
                }
            }
        }
        rec(0, 0, 1L, boundForM)
        return cnt
    }
    return count(5, bound, lut0) + count(4, bound / 9, lutAll)
}

// ───────────────────────── 方法 B（独立路径） ─────────────────────────

/** 方法 B：m 用降序枚举进直方图；s 侧改用递归生成、P₀ = P_all − 9·P_all(⌊L/9⌋)；大整数点积。 */
private fun solveB(bound: Long): BigInteger {
    val primeLimit = maxOf(43L, bound / 9 / (7 * 13 * 19) + 1)
    val primes = primesUpTo(primeLimit.toInt())
    val p1 = primes.filter { it % 3L == 1L }.toLongArray()
    val p2 = primes.filter { it % 3L == 2L }.toLongArray()

    val sMax = maxOf(bound / (7 * 13 * 19 * 31), bound / (7 * 13 * 19 * 31 * 37))
    val base = ArrayList<Long>(p2.size + 1)
    base.add(3L)
    for (v in p2) base.add(v)
    base.sort()
    val allowedB = ArrayList<Long>()
    fun gen(startIdx: Int, cur: Long) {
        allowedB.add(cur)
        for (i in startIdx until base.size) {
            val p = base[i]
            if (cur > sMax / p) break
            var v = cur * p
            while (true) {
                gen(i + 1, v)
                if (v > sMax / p) break
                v *= p
            }
        }
    }
    gen(0, 1L)
    // P_all 稠密表（全部 allowed s 的前缀和）
    val pall = LongArray(sMax.toInt() + 1)
    val mark = BooleanArray(sMax.toInt() + 1)
    for (s in allowedB) mark[s.toInt()] = true
    var acc = 0L
    for (v in 1..sMax.toInt()) {
        if (mark[v]) acc += v
        pall[v] = acc
    }

    // 直方图：桶的下标就是 ⌊boundForM/m⌋
    val buckets5 = LongArray((bound / (7 * 13 * 19 * 31 * 37)).toInt() + 1)
    val buckets4 = LongArray((bound / 9 / (7 * 13 * 19 * 31)).toInt() + 1)
    histogramM(p1, 5, bound, buckets5)
    histogramM(p1, 4, bound / 9, buckets4)

    var total = BigInteger.ZERO
    for (l in buckets5.indices) {
        if (buckets5[l] == 0L) continue
        val pAll = pall[l]
        val p9 = if (l / 9 >= 1) 9L * pall[l / 9] else 0L
        total = total.add(BigInteger.valueOf(buckets5[l]).multiply(BigInteger.valueOf(pAll - p9)))
    }
    for (l in buckets4.indices) {
        if (buckets4[l] == 0L) continue
        total = total.add(
            BigInteger.valueOf(buckets4[l]).multiply(BigInteger.valueOf(9L * pall[minOf(l, sMax.toInt())]))
        )
    }
    return total
}

// ───────────────────────── 线性筛法（第三条独立算法） ─────────────────────────

/** 对 n ≤ limit 用「标记每个 ≡1 (mod 3) 素数的倍数」逐 n 判定，返回 C(n) = 242 的 n 之和。 */
private fun solveBySieve(limit: Int): Long {
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var p = 2
    while (p.toLong() * p <= limit) {
        if (isPrime[p]) {
            var j = p * p
            while (j <= limit) { isPrime[j] = false; j += p }
        }
        p++
    }
    val k = ByteArray(limit + 1)
    for (q in 2..limit) {
        if (isPrime[q] && q % 3 == 1) {
            var m = q
            while (m <= limit) { k[m] = (k[m] + 1).toByte(); m += q }
        }
    }
    var total = 0L
    for (n in 1..limit) {
        if (k[n] + (if (n % 9 == 0) 1 else 0) == 5) total += n
    }
    return total
}

// ───────────────────────── 小规模对照 ─────────────────────────

/** 直接枚举 x ∈ (1, n) 数解：返回 C(n)。 */
private fun countByEnumeration(n: Long): Long {
    var c = 0L
    var x = 2L
    while (x < n) {
        if (x * x % n * x % n == 1L) c++
        x++
    }
    return c
}

/** 试除法求 k(n) = 不同素因子中 ≡ 1 (mod 3) 的个数。 */
private fun kByTrialDivision(n: Long): Int {
    var m = n
    var k = 0
    var d = 2L
    while (d <= m / d) {
        if (m % d == 0L) {
            if (d % 3L == 1L) k++
            while (m % d == 0L) m /= d
        }
        d = if (d == 2L) 3L else d + 2L
    }
    if (m > 1L && m % 3L == 1L) k++
    return k
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
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面锚点 ----------
    val roots91 = (2L until 91L).filter { it * it % 91L * it % 91L == 1L }
    check(roots91 == listOf(9L, 16L, 22L, 29L, 53L, 74L, 79L, 81L)) { "n = 91 的解集不符：$roots91" }
    println("题面锚点：n = 91 的解为 {9,16,22,29,53,74,79,81}，C(91) = ${roots91.size}")

    // ---------- 2. 定义域暴力：n ≤ 2000 ----------
    for (n in 2L..2000L) {
        val c = countByEnumeration(n)
        val k = kByTrialDivision(n)
        var e = 1L
        repeat(k + if (n % 9L == 0L) 1 else 0) { e *= 3L }
        check(c == e - 1L) { "n = $n：枚举 C(n) = $c ≠ 公式 ${e - 1}" }
    }
    println("n = 2…2000：逐个枚举的 C(n) 与 3^{k+[9|n]} − 1 全部一致")

    // ---------- 3. 逐 n 筛法 vs 主路径（独立算法，小规模两个界） ----------
    for (sieveBound in intArrayOf(1_000_000, 10_000_000)) {
        val bySieve = solveBySieve(sieveBound)
        val byA_Small = solveA(sieveBound.toLong())
        val byB_Small = solveB(sieveBound.toLong())
        check(bySieve == byA_Small) { "n ≤ $sieveBound：筛法 $bySieve ≠ 主路径 $byA_Small" }
        check(byB_Small == BigInteger.valueOf(bySieve)) { "n ≤ $sieveBound：方法 B $byB_Small ≠ 筛法 $bySieve" }
        println("n ≤ $sieveBound：筛法 = 主路径 = 方法 B = $bySieve")
    }

    // ---------- 4. 全量：主路径 vs 方法 B ----------
    val (ansA, cntA) = solveAFull(BOUND)
    val ansB = solveB(BOUND)
    check(ansB == BigInteger.valueOf(ansA)) { "方法 B $ansB ≠ 方法 A $ansA" }
    println("n ≤ 10¹¹：主路径 = $ansA，方法 B = $ansB（一致，Long.MAX_VALUE = ${Long.MAX_VALUE}）")
    println("m 侧枚举叶子数 = $cntA；合法 n 的个数 = ${countValidN(BOUND)}")

    // ---------- 5. 计时 ----------
    solveA(10_000_000L); solveB(10_000_000L); solveA(BOUND); solveB(BOUND)
    val msA = bestOf3("主路径（标记筛 s + 升序 m DFS + O(1) 查表，B = 10¹¹）", ansA) { solveA(BOUND) }
    val t0 = System.nanoTime()
    solveB(BOUND)
    val msB = (System.nanoTime() - t0) / 1e6
    println("方法 B（独立路径，单轮）：${"%.1f".format(msB)} ms")

    // ---------- 6. 大界逐 n 筛法复查（放在计时之后，避免大数组分配干扰计时） ----------
    val bySieve8 = solveBySieve(100_000_000)
    val byA8 = solveA(100_000_000L)
    check(bySieve8 == byA8) { "n ≤ 10⁸：筛法 $bySieve8 ≠ 主路径 $byA8" }
    println("n ≤ 10⁸：筛法 = 主路径 = $bySieve8（与 brute-force.kt 的路径 2 一致）")

    println()
    println("答案 = $ansA")
    println("汇总：主路径 ${"%.1f".format(msA)} ms；方法 B ${"%.1f".format(msB)} ms（单轮）")
    println("check() 全部通过")
}
