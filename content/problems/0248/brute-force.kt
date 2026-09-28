#!/usr/bin/env kotlin
/**
 * Project Euler 248 — 暴力 / 朴素对照程序
 *
 * 思路：
 *   本题的真解在 6.2×10⁹ 以上，直接的全区间 φ 筛够不着，所以暴力对照分三层：
 *
 *   A. **朴素全量枚举（基线）**：与 solution.kt 同一个逆欧拉递归骨架，但**不用「剩余因子
 *      的约数表」剪枝**——每个节点都把全部 459 个候选素数（(p−1) | 13!）顺序扫一遍，
 *      只保留「p > 上一个素因子」与「(p−1) | rem」两条基本条件，逻辑更直白、常数更大，
 *      用来全量复核 13! 的每一个解（个数、和、全表哈希、第 15 万个）。
 *
 *   B. **小模数全区间筛法**：筛出 [0, 10⁸] 的全部 φ(n)，对 m = 8! / 9! / 10! 各自列全解。
 *      这是与逆欧拉结构完全无关的路线（只按定义算 φ 再比对），用来给递归引擎在小规模
 *      上钉死正确性；10! 的最大解远小于 10⁸，筛法能完整覆盖。
 *
 *   C. **13! 尺度的分段筛窗口**：用「不大于 √(hi) 的素数逐个划掉窗口内倍数」的分段筛，
 *      在 [6227180929, +2×10⁷) 与 [23507044290, +2×10⁷) 两个真实尺度的窗口内，独立算出
 *      每个 φ(n) 并按定义比对 13!，逐项复核该窗口内的解集合（个数、和、哈希）。
 *      窗口之外够不着，但窗口内是**不依赖逆欧拉逻辑**的完全独立验证。
 *
 *   D. **比值上界（Long 安全性）**：每个解都满足 n = m·R，R = ∏ p/(p−1) 只依赖素因子
 *      集合，且 ∏(p−1) | m。对同一组整除约束做「最大化 R」的 DFS（分数用 BigInteger
 *      精确比较），得到 R_max 与上界 R_max·m；它与枚举出的最大解精确相等，从而证明
 *      搜索全程（含所有中间乘积）都在 Long 内。
 *
 *   运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private const val M13 = 6_227_020_800L

// ---------------------------------------------------------------- 基础工具

private fun primesUpTo(limit: Int): IntArray {
    val comp = BooleanArray(limit + 1)
    val out = ArrayList<Int>()
    for (i in 2..limit) {
        if (!comp[i]) {
            out.add(i)
            if (i.toLong() * i <= limit) {
                var j = i * i
                while (j <= limit) { comp[j] = true; j += i }
            }
        }
    }
    return out.toIntArray()
}

private val SMALL_PRIMES: IntArray by lazy { primesUpTo(300_000) }

/** 试除法素性判定，n ≤ 6.3×10⁹（只用来判定 (p−1)+1 是否为素数） */
private fun isPrimeTrial(n: Long): Boolean {
    if (n < 2) return false
    for (p in SMALL_PRIMES) {
        val pl = p.toLong()
        if (pl * pl > n) return true
        if (n % pl == 0L) return n == pl
    }
    return true
}

private class LongBag(cap: Int = 1 shl 20) {
    var a = LongArray(cap)
    var size = 0
    fun add(x: Long) {
        if (size == a.size) a = a.copyOf(a.size * 2)
        a[size++] = x
    }
    fun sorted(): LongArray = a.copyOf(size).also { it.sort() }
}

/** FNV-1a 64 位哈希（与 solution.kt 用同一算法，压缩全表比对） */
private fun fnv(xs: LongArray): Long {
    var h = -3_750_763_034_362_895_579L // 0xCBF29CE484222325
    for (x in xs) h = (h xor x) * 1_099_511_628_211L
    return h
}

private fun factorize(n0: Long): Map<Long, Int> {
    val out = LinkedHashMap<Long, Int>()
    var n = n0
    for (p in SMALL_PRIMES) {
        val pl = p.toLong()
        if (pl * pl > n) break
        if (n % pl == 0L) {
            var e = 0
            while (n % pl == 0L) { n /= pl; e++ }
            out[pl] = e
        }
    }
    if (n > 1) out[n] = (out[n] ?: 0) + 1
    return out
}

// ---------------------------------------------------------------- A. 朴素全量枚举

/**
 * 候选素数表：p 必须满足 (p−1) | m，故只需对 m 的全部约数 d 试 d + 1 的素性
 * （d = 1 给出 p = 2，承担 2 的幂；d = 2 给出 p = 3；……），按 p 升序返回。
 */
private fun candidatePrimes(m: Long): LongArray {
    var divs = longArrayOf(1L)
    for ((p, e) in factorize(m)) {
        val add = ArrayList<Long>()
        var pk = 1L
        repeat(e) { pk *= p; for (d in divs) add.add(d * pk) }
        divs = (divs.toMutableList() + add).toLongArray()
    }
    divs.sort()
    val out = LongBag()
    for (d in divs) if (isPrimeTrial(d + 1)) out.add(d + 1)
    return out.sorted()
}

/**
 * 朴素递归：状态 (rem, minP, n)，rem 是还没被消费的 φ 因子，n 是已选素因子幂之积。
 * 每层**不做任何剪枝**，把全部 459 个候选素数顺序扫一遍（只有 p > minP 与 (p−1) | rem
 * 两条基本条件），(p−1) | rem 时逐个指数递归。
 */
private fun naiveInversePhi(m: Long): LongArray {
    val primes = candidatePrimes(m)
    val sol = LongBag()
    var nodes = 0L

    fun dfs(rem: Long, minP: Long, n: Long) {
        nodes++
        if (rem == 1L) { sol.add(n); return }
        for (p in primes) {
            if (p <= minP) continue
            val d = p - 1
            if (rem % d != 0L) continue
            var q = d                      // p^{e−1}(p−1)
            var pe = p                     // p^e
            while (q <= rem && rem % q == 0L) {
                val nr = rem / q
                val nn = n * pe
                if (nn <= 0L || nn > 8L * m) error("中间量越界：n = $nn（不应发生）")
                if (nr == 1L) sol.add(nn) else dfs(nr, p, nn)
                if (q > rem / p) break
                q *= p
                pe *= p
            }
        }
    }

    dfs(m, 1L, 1L)
    println("  [A] 候选素数 ${primes.size} 个，搜索节点 $nodes")
    return sol.sorted()
}

// ---------------------------------------------------------------- B. 小模数线性筛

/** 标准 φ 筛：phi[n] = φ(n)，n ∈ [0, N]，O(N log log N)，空间 O(N) */
private fun phiSieve(N: Int): IntArray {
    val phi = IntArray(N + 1) { it }
    for (p in 2..N) {
        if (phi[p] == p) {                       // p 是素数
            var j = p
            while (j <= N) {
                phi[j] -= phi[j] / p
                j += p
            }
        }
    }
    return phi
}

private fun reportSieve(phi: IntArray, m: Int, name: String) {
    val hits = LongBag(1 shl 10)
    for (n in 1..phi.size - 1) if (phi[n] == m) hits.add(n.toLong())
    val xs = hits.sorted()
    println("  [B] φ(n) = $name 的全解：${xs.size} 个，前 10 个 ${xs.take(10)}，末个 ${xs.last()}，和 ${xs.sum()}")
}

// ---------------------------------------------------------------- C. 分段 φ 筛窗口

/**
 * 在 [lo, hi) 上按定义筛出 φ(n)（不用任何逆欧拉结构）：
 * 对每个不超过 √hi 的素数 p，把窗口内 p 的倍数整体除掉一个 p 因子；
 * 窗口内剩下的未除尽部分 r > 1 必为素数，再补乘 (r−1)/r。空间 O(hi−lo)。
 */
private fun phiWindow(lo: Long, hi: Long): LongArray {
    val w = (hi - lo).toInt()
    val phi = LongArray(w)
    val rem = LongArray(w)
    for (i in 0 until w) {
        val n = lo + i
        phi[i] = n
        rem[i] = n
    }
    for (p in SMALL_PRIMES) {
        val pl = p.toLong()
        if (pl * pl >= hi) break
        var start = ((lo + pl - 1) / pl) * pl
        while (start < hi) {
            val idx = (start - lo).toInt()
            phi[idx] -= phi[idx] / pl
            var r = rem[idx]
            while (r % pl == 0L) r /= pl
            rem[idx] = r
            start += pl
        }
    }
    for (i in 0 until w) {
        val r = rem[i]
        if (r > 1L) phi[i] -= phi[i] / r
    }
    return phi
}

private fun reportWindow(lo: Long, w: Long) {
    val t0 = System.nanoTime()
    val phi = phiWindow(lo, lo + w)
    val hits = LongBag(1 shl 10)
    for (i in phi.indices) if (phi[i] == M13) hits.add(lo + i)
    val xs = hits.sorted()
    val ms = (System.nanoTime() - t0) / 1e6
    println(
        "  [C] 窗口 [$lo, ${lo + w})：φ(n) = 13! 的解 ${xs.size} 个，" +
            "和 ${xs.sum()}，FNV ${fnv(xs)}，" +
            "首 ${if (xs.isEmpty()) "-" else xs.first()}，末 ${if (xs.isEmpty()) "-" else xs.last()}（${"%.0f".format(ms)} ms）",
    )
}

// ---------------------------------------------------------------- D. 比值上界（Long 安全性）

/**
 * 在「素因子集合 S 满足 ∏_{p∈S}(p−1) | m」的约束下最大化 R = ∏ p/(p−1)。
 * 每个解都等于 m·R，故 R_max·m 是全部解与全部搜索中间量的上界。
 * 分数比较用 BigInteger 保证精确，Double 仅用于快速跳过明显更小的分支。
 */
private fun maxRatio(m: Long): Triple<java.math.BigInteger, java.math.BigInteger, LongArray> {
    val primes = candidatePrimes(m)
    var bestNum = java.math.BigInteger.ONE
    var bestDen = java.math.BigInteger.ONE
    var bestDouble = 1.0
    var bestSet = LongArray(0)
    val cur = ArrayList<Long>()
    var nodes = 0L

    fun dfs(rem: Long, idx: Int, num: java.math.BigInteger, den: java.math.BigInteger, dbl: Double) {
        nodes++
        if (dbl >= bestDouble && num.multiply(bestDen).compareTo(bestNum.multiply(den)) > 0) {
            bestNum = num
            bestDen = den
            bestDouble = dbl
            bestSet = cur.toLongArray()
        }
        for (j in idx until primes.size) {
            val p = primes[j]
            val d = p - 1
            if (d > rem) break                     // d 升序，后面的都不可能整除
            if (rem % d == 0L) {
                cur.add(p)
                dfs(
                    rem / d, j + 1,
                    num.multiply(java.math.BigInteger.valueOf(p)),
                    den.multiply(java.math.BigInteger.valueOf(d)),
                    dbl * p / d,
                )
                cur.removeAt(cur.size - 1)
            }
        }
    }

    dfs(m, 0, java.math.BigInteger.ONE, java.math.BigInteger.ONE, 1.0)
    println("  [D] 比值 DFS 节点 $nodes，取最大值的素因子集合 ${bestSet.toList()}")
    return Triple(bestNum, bestDen, bestSet)
}

// ---------------------------------------------------------------- main

private fun main() {
    // ---- A. 朴素全量枚举（预热两次后计时 3 次取中位） ----
    naiveInversePhi(M13)
    naiveInversePhi(M13)
    val times = DoubleArray(3)
    var all = LongArray(0)
    for (k in times.indices) {
        val t0 = System.nanoTime()
        all = naiveInversePhi(M13)
        times[k] = (System.nanoTime() - t0) / 1e6
    }
    val msA = times.sorted()[1]
    check(all.size == 182_752) { "解数不符：${all.size}" }
    check(all.sum() == 3_127_106_224_790_235L) { "解之和不符：${all.sum()}" }
    check(all.first() == 6_227_180_929L) { "最小解不符：${all.first()}" }
    check(all.last() == 37_020_293_310L) { "最大解不符：${all.last()}" }
    println("  [A] 全量解：${all.size} 个，最小 ${all.first()}，最大 ${all.last()}，和 ${all.sum()}")
    println("  [A] 全表 FNV = ${fnv(all)}")
    println("  [A] 第 150000 个 = ${all[149_999]}，φ 复核 = ${phiByDefinition(all[149_999])}")
    println("  [A] 朴素全量枚举耗时：${"%.0f".format(msA)} ms（JIT 预热后 3 次 ${times.joinToString("/") { "%.0f".format(it) }}，取中位；bruteForceBaselineMs）")

    // ---- B. 小模数全区间 φ 筛，给递归引擎在小规模上钉死 ----
    // 10! 的最大解实测 19969950（n ≤ R·m，R = ∏p/(p−1) ≤ 6），远小于 10⁸，故筛法完整覆盖。
    val tB = System.nanoTime()
    val phi = phiSieve(100_000_000)
    for (m in intArrayOf(40_320, 362_880, 3_628_800)) {
        val name = when (m) {
            40_320 -> "8!"
            362_880 -> "9!"
            else -> "10!"
        }
        reportSieve(phi, m, name)
    }
    println("  [B] 小模数筛法（[0, 10⁸]）总耗时：${"%.0f".format((System.nanoTime() - tB) / 1e6)} ms")

    // ---- C. 13! 尺度的分段窗口独立复核 ----
    reportWindow(6_227_180_929L, 20_000_000L)
    reportWindow(23_507_044_290L, 20_000_000L)

    // ---- D. 比值上界：证明搜索全程（含中间乘积）都在 Long 内 ----
    val (ratioNum, ratioDen, ratioSet) = maxRatio(M13)
    val g = ratioNum.gcd(ratioDen)
    val bound = ratioNum.multiply(java.math.BigInteger.valueOf(M13)).divide(ratioDen)
    val exact = ratioNum.multiply(java.math.BigInteger.valueOf(M13))
        .mod(ratioDen) == java.math.BigInteger.ZERO
    println(
        "  [D] R_max = ${ratioNum.divide(g)}/${ratioDen.divide(g)} = ${ratioNum.toDouble() / ratioDen.toDouble()}，" +
            "取自集合 ${ratioSet.toList()}；R_max·m = $bound（精确整除=$exact），最大解 = ${all.last()}",
    )
    check(exact && bound == java.math.BigInteger.valueOf(all.last())) { "比值上界与最大解不符" }

    println("brute-force 全部检查通过")
}

private fun phiByDefinition(n0: Long): Long {
    var n = n0
    var phi = n0
    for (p in SMALL_PRIMES) {
        val pl = p.toLong()
        if (pl * pl > n) break
        if (n % pl == 0L) {
            while (n % pl == 0L) n /= pl
            phi -= phi / pl
        }
    }
    if (n > 1L) phi -= phi / n
    return phi
}
