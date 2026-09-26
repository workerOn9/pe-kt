#!/usr/bin/env kotlin
/**
 * Project Euler 233 — Lattice Points on a Circle（圆上的格点）
 *
 * 思路：
 *   过 (0,0)、(N,0)、(0,N)、(N,N) 的圆圆心 (N/2, N/2)、半径 N/√2，方程 (2x−N)² + (2y−N)² = 2N²。
 *   按 N 的奇偶讨论 (u,v) = (2x−N, 2y−N)（N 偶 ⟹ u、v 同偶；N 奇 ⟹ 同奇）可得双射到
 *   a² + b² = N²（N 奇）或 N²/2（N 偶），于是
 *       f(N) = r₂(N²) = 4·∏_{p ≡ 1 (mod 4), p^e ‖ N} (2e + 1).
 *   题面样例 f(10000) = 36（10000 = 2⁴·5⁴ → 4·(2·4+1) = 36）与之一致。
 *
 *   f(N) = 420 ⟺ ∏(2e+1) = 105 = 3·5·7，1 mod 4 部分的指数模式只能是
 *       {1,2,3}、{7,3}、{10,2}、{17,1}、{52}
 *   （{17,1} 的最小核 5¹⁷ 已超 10¹¹，{52} 更不必说，实际只用到前三种）。
 *   于是 N 有唯一的分解
 *       N = 2^a · M · T，  M = 核（1 mod 4 素数按上述指数模式），T 只含 3 mod 4 的素因子，
 *   从而
 *       Total = Σ_{核 M} Σ_{a ≥ 0, 2^a·M ≤ L} 2^a · M · G(⌊L / (2^a M)⌋),
 *       G(X) = Σ_{T ≤ X, T 只含 3 mod 4 素因子} T.
 *   最小的核是 5³·13²·17 = 359125，所以 T ≤ 10¹¹/359125 = 278455 —— 这个半群可以完整枚举：
 *   用「素因子不减」的 DFS 生成全部 T ≤ 278455（每个数恰好一次），排序后取前缀和，G(X) 一次二分。
 *
 * 枚举的规范形式（避免重复计数）：{1,2,3} 用 A<B<C 的三元组 × 6 种指数分派（每个核恰好一次）；
 *   {7,3}、{10,2} 用 A<B 的有序对 × 2 种指数顺序。旧版用「有序三元组 × 6 分派」把每个核算了 6 遍，
 *   得到 5.26×10¹⁷ 的假答案。
 *
 * 旁证：
 *   1. 与「按定义逐个数算 f(N)」的暴力在 L = 10⁶（8 个解，和 5680350）与 L = 10⁷
 *      （300 个解，和 1722531830）上逐位一致；这两个规模已覆盖全部三种指数模式。
 *   2. 题面样例 f(10000) = 36 由闭式复现。
 *
 * 答案：271204031455541309
 * 复杂度：筛到 5.6×10⁶ 为 O(√L·loglog)，核 5.85×10⁵ 个、每个约 18 次二分，实测约 1.5 s。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val LIMIT = 100_000_000_000L
private const val SIEVE_MAX = 5_600_000
private const val MIN_CORE = 359_125L          // 5³·13²·17

fun main() {
    val t0 = System.currentTimeMillis()
    val isPrime = BooleanArray(SIEVE_MAX + 1) { true }
    isPrime[0] = false
    isPrime[1] = false
    var i = 2
    while (i.toLong() * i <= SIEVE_MAX) {
        if (isPrime[i]) {
            var j = i * i
            while (j <= SIEVE_MAX) { isPrime[j] = false; j += i }
        }
        i++
    }
    val p1 = ArrayList<Int>()                       // 1 mod 4 素数
    for (x in 5..SIEVE_MAX) if (isPrime[x] && x % 4 == 1) p1.add(x)
    val p1a = p1.toIntArray()
    println("primes ≡ 1 (mod 4) ≤ $SIEVE_MAX: ${p1a.size}")

    val xMax = (LIMIT / MIN_CORE).toInt()           // 278455
    val p3 = ArrayList<Int>()                       // 3 mod 4 素数 ≤ xMax
    for (x in 3..xMax) if (isPrime[x] && x % 4 == 3) p3.add(x)
    val sList = ArrayList<Long>()
    genS(0, 1L, p3.toIntArray(), xMax.toLong(), sList)
    val s = sList.toLongArray()
    java.util.Arrays.sort(s)
    val sPref = LongArray(s.size + 1)
    for (j in s.indices) sPref[j + 1] = sPref[j] + s[j]
    println("半群 |{T ≤ $xMax，素因子全为 3 mod 4}| = ${s.size}")

    var total = 0L
    var cores = 0L

    fun addCore(m: Long) {
        if (m < 1L || m > LIMIT) return        // 防御：只在 1 ≤ M ≤ L 时计数
        cores++
        var v = m
        while (v <= LIMIT) {
            total += v * gOf(LIMIT / v, s, sPref)
            v = v shl 1
        }
    }

    // ① 指数模式 {1,2,3}：A<B<C 的三元组 × 6 种分派
    val exps = arrayOf(
        intArrayOf(1, 2, 3), intArrayOf(1, 3, 2), intArrayOf(2, 1, 3),
        intArrayOf(2, 3, 1), intArrayOf(3, 1, 2), intArrayOf(3, 2, 1),
    )
    for (e in exps) {
        val eA = e[0]; val eB = e[1]; val eC = e[2]
        var ia = 0
        while (ia < p1a.size) {
            val aA = pow(p1a[ia].toLong(), eA)
            if (aA > LIMIT) break
            var ib = ia + 1
            while (ib < p1a.size) {
                val bB = aA * pow(p1a[ib].toLong(), eB)
                if (bB > LIMIT) break
                val lim = LIMIT / bB
                if (eC == 1) {
                    var ic = ib + 1
                    while (ic < p1a.size && p1a[ic].toLong() <= lim) {
                        addCore(bB * p1a[ic])
                        ic++
                    }
                } else {
                    val cMax = iroot(lim, eC)
                    var ic = ib + 1
                    while (ic < p1a.size && p1a[ic].toLong() <= cMax) {
                        addCore(bB * pow(p1a[ic].toLong(), eC))
                        ic++
                    }
                }
                ib++
            }
            ia++
        }
    }

    // ② 指数模式 {7,3}、{10,2}：A<B × 两种指数顺序
    for (ee in arrayOf(intArrayOf(7, 3), intArrayOf(3, 7), intArrayOf(10, 2), intArrayOf(2, 10))) {
        val e1 = ee[0]; val e2 = ee[1]
        var ia = 0
        while (ia < p1a.size) {
            val aA = pow(p1a[ia].toLong(), e1)
            if (aA > LIMIT) break
            var ib = ia + 1
            while (ib < p1a.size) {
                val m = aA * pow(p1a[ib].toLong(), e2)
                if (m > LIMIT) break
                addCore(m)
                ib++
            }
            ia++
        }
    }

    println("核数 = $cores")
    println("answer = $total")
    println("elapsed ${System.currentTimeMillis() - t0} ms")
}

/** 生成全部「素因子均为 3 mod 4」且 ≤ xMax 的正整数：素因子不减，每个数恰好一次 */
private fun genS(start: Int, cur: Long, p3: IntArray, xMax: Long, out: ArrayList<Long>) {
    out.add(cur)
    var j = start
    while (j < p3.size) {
        val q = p3[j].toLong()
        if (cur > xMax / q) break
        var v = cur * q
        while (v <= xMax) {
            genS(j + 1, v, p3, xMax, out)
            if (v > xMax / q) break
            v *= q
        }
        j++
    }
}

/** G(x) = Σ_{T ≤ x, T 只含 3 mod 4 素因子} T（排序 + 前缀和 + 二分） */
private fun gOf(x: Long, s: LongArray, pref: LongArray): Long {
    if (x <= 0) return 0L
    var lo = 0
    var hi = s.size
    while (lo < hi) {
        val mid = (lo + hi) ushr 1
        if (s[mid] <= x) lo = mid + 1 else hi = mid
    }
    return pref[lo]
}

private fun pow(base: Long, e: Int): Long {
    var r = 1L
    repeat(e) { r *= base }
    return r
}

private fun iroot(x: Long, k: Int): Long {
    if (k == 1) return x
    if (x <= 0) return 0
    var r = Math.pow(x.toDouble(), 1.0 / k).toLong() + 2
    while (r > 0 && pow(r, k) > x) r--
    while (pow(r + 1, k) <= x) r++
    return r
}
