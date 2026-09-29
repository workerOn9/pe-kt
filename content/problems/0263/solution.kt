#!/usr/bin/env kotlin
/**
 * Project Euler 263 — An Engineers' Dream Come True（工程师的天堂）
 *
 * 思路
 * ────
 * 实用数（practical number）的标准判定：设 n 的素因子升序 p₁=2<p₂<…（指数 e_j），
 *   n 实用 ⇔ 每个 j 满足 p_j ≤ 1 + σ(p₁^{e₁}…p_{j-1}^{e_{j-1}})；
 * 等价的「因数贪心」判定：把因数升序排成 d₁=1<d₂<…，逐个要求 d_i ≤ 1 + Σ_{j<i} d_j
 * （可达集始终是连续区间 [0,S]）。两条判定在本题分别用于方法 A 与方法 B。
 *
 * sexy pair = 差 6 的相邻素数对。题设的 triple-pair 要求
 * (n−9,n−3), (n−3,n+3), (n+3,n+9) 三段都是相邻素数对，即 p=n−9 起
 * p, p+6, p+12, p+18 四个素数在素数序列里连续（[p,p+18] 内恰有这四个素数）。
 * 工程师天堂 n ⇔ 上述素数模式成立，且 n−8, n−4, n, n+4, n+8 五个数全为实用数。
 *
 * 候选素数一定是 p ≡ 1 或 11 (mod 30)：p 必须是奇素数；若 p ≡ 2,3,4,0 (mod 5) 则
 * p+6k（k=0..3）中必有一个被 5 整除，故 p ≡ 1 (mod 5)；再排除 3 的倍数（p=3 或 p≡0 (mod 3)
 * 会让 p+6k 中以 3 为因子）即得 p ≡ 1, 11 (mod 30)。
 *
 * 方法 A（主路径）：分段埃氏筛（只存奇数位；段长取 30 的倍数，使窗口扫描下标恰好落在
 *   j ≡ 0,5 (mod 15)，即 p ≡ 1,11 (mod 30)）。段内直接查 [p,p+18] 的 4 个素数位与 6 个空格位；
 *   命中候选后用 σ 链判定 5 个实用数。从 2 起分段推进，找到第 4 个 paradise 立即停止。
 * 方法 B（独立复核）：「模式筛」——完全不生成全区间素数表，而是只在候选空间（密度 2/30）开位图：
 *   对每个小素数 q ≥ 7 与偏移 h ∈ {0,6,12,18}，把 p ≡ −h (mod q) 的候选清掉（同一个 q 多次命中
 *   也只清一次）。存活者必满足四个数全素数，再逐一验证 6 个中间数复合（试除）。实用数改用
 *   按定义的「因数升序贪心」判定。两条路径在完整范围给出完全相同的候选表与前四个 paradise。
 *
 * 复杂度
 * ──────
 * 方法 A：筛到 ≈1.15×10⁹，标记次数 ≈ (N/2)·Σ_{q≤√N}1/q ≈ 1.2×10⁹，实测约 2 s；
 *   候选 4.8×10⁴ 个，每个做 5 次 σ 链判定（试除到 √n），内存 O(段长)。
 * 方法 B：模式筛标记次数 ≈ 0.267·N·Σ_{q≤√N}1/q ≈ 4.8×10⁸，实测约 1–2 s（候选空间位图仅 ~10 MB）。
 * 暴力对照：朴素整段筛 + 定义型实用数判定，10⁸ 规模不到 1 s（brute-force.kt 另跑到 2.5×10⁸）。
 *
 * 验证
 * ────
 * · 题面锚点：6 的分解示例 1..6 全部可表为不同因数之和；首个 sexy pair 为 (23,29)；
 *   实用数小表分别用子集和定义、σ 链、因数贪心三种实现逐一比对；
 * · 双方法互证：候选表（整除 6 间隔连续四素数）与前四个 paradise 完全一致；
 * · 暴力对照：≤10⁸ 的候选表与 A、B 逐一相同；brute-force.kt 独立跑到 2.5×10⁸ 复现第一个 paradise；
 * · 实跑输出（方法 A、B 一致）：前四个 paradise = 219869980, 312501820, 360613700, 1146521020。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0263/solution.kt && java -cp … SolutionKt
 */

private const val FIRST_N = 4                       // 取前四个 paradise
private const val LIMIT_CAP: Long = 1_300_000_000L  // 自适应搜索的安全上限
private const val BRUTE_LIMIT: Long = 100_000_000L  // 内部暴力对照规模

/** 一次搜索的结果：候选素数表（升序）、paradise 表、扫描到的位置。 */
private data class Hunt(val candidates: LongArray, val paradises: LongArray, val scannedTo: Long)

// ────────────────────────────── 小工具 ──────────────────────────────

private fun sieveBasePrimes(n: Int): IntArray {
    val comp = BooleanArray(n + 1)
    val out = ArrayList<Int>()
    for (i in 2..n) {
        if (!comp[i]) {
            out.add(i)
            if (i.toLong() * i <= n) {
                var j = i * i
                while (j <= n) { comp[j] = true; j += i }
            }
        }
    }
    return out.toIntArray()
}

private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

private fun gcdInt(a: Int, b: Int): Int {
    var x = if (a < 0) -a else a
    var y = if (b < 0) -b else b
    while (y != 0) { val t = x % y; x = y; y = t }
    return x
}

private fun modInverse(a: Long, m: Long): Long {
    var r0 = ((a % m) + m) % m
    var r1 = m
    var s0 = 1L
    var s1 = 0L
    while (r1 != 0L) {
        val q = r0 / r1
        val t = r0 - q * r1; r0 = r1; r1 = t
        val u = s0 - q * s1; s0 = s1; s1 = u
    }
    return ((s0 % m) + m) % m
}

/** 试除法素性判定（只用给定素数表，调用方保证表覆盖 √x）。 */
private fun isPrimeSmall(x: Long, primes: IntArray): Boolean {
    if (x < 2L) return false
    for (p in primes) {
        val pl = p.toLong()
        if (pl * pl > x) return true
        if (x % pl == 0L) return x == pl
    }
    return true
}

// ────────────────────── 实用数的三种判定 ──────────────────────

/** 方法 A 用：σ 链判定（逐素数检验 p_j ≤ 1+σ(before)）。 */
private fun isPracticalBySigma(n: Long): Boolean {
    if (n == 1L) return true
    if (n and 1L == 1L) return false
    var m = n
    var sigma = 1L
    var p = 2L
    while (p * p <= m) {
        if (m % p == 0L) {
            if (p > sigma + 1) return false
            var pk = 1L
            while (m % p == 0L) { m /= p; pk *= p }
            sigma *= (pk * p - 1) / (p - 1)
        }
        p += if (p == 2L) 1L else 2L
    }
    if (m > 1 && m > sigma + 1) return false
    return true
}

/** 方法 B 用：按定义枚举全部因数，升序贪心检验 d_i ≤ 1+Σ_{j<i} d_j。 */
private fun isPracticalByDivisors(n: Long, primes: IntArray): Boolean {
    if (n == 1L) return true
    if (n and 1L == 1L) return false
    var m = n
    val ps = IntArray(16)
    val es = IntArray(16)
    var k = 0
    for (pr in primes) {
        val p = pr.toLong()
        if (p * p > m) break
        if (m % p == 0L) {
            var e = 0
            while (m % p == 0L) { m /= p; e++ }
            ps[k] = pr
            es[k] = e
            k++
        }
    }
    if (m > 1L) { ps[k] = m.toInt(); es[k] = 1; k++ }
    var divs = LongArray(1) { 1L }
    for (i in 0 until k) {
        val pr = ps[i].toLong()
        val e = es[i]
        val next = LongArray(divs.size * (e + 1))
        var idx = 0
        var pk = 1L
        for (ee in 0..e) {
            for (d in divs) next[idx++] = d * pk
            pk *= pr
        }
        divs = next
    }
    divs.sort()
    var sum = divs[0]
    for (i in 1 until divs.size) {
        if (divs[i] > sum + 1) return false
        sum += divs[i]
    }
    return true
}

/** 暴力/锚点用：直接按定义做子集和可达性（只适合小 n）。 */
private fun isPracticalBySubsetSum(n: Int): Boolean {
    val reach = BooleanArray(n + 1)
    reach[0] = true
    var d = 1
    while (d <= n) {
        if (n % d == 0) {
            for (s in n downTo d) if (reach[s - d]) reach[s] = true
        }
        d++
    }
    for (s in 1..n) if (!reach[s]) return false
    return true
}

// ────────────────────────── 方法 A：分段素数筛 ──────────────────────────

private fun huntBySieve(base: IntArray, limitCap: Long): Hunt {
    val stepNum = 2_097_150L                       // 30 的倍数
    val nbits = (stepNum / 2).toInt()
    val bits = LongArray(nbits / 64 + 1)
    val candidates = ArrayList<Long>()
    val paradises = ArrayList<Long>()
    var lo = 1L
    outer@ while (lo <= limitCap) {
        java.util.Arrays.fill(bits, -1L)
        val hi = minOf(lo + stepNum, limitCap + 1)
        var nbit = ((hi - lo + 1) / 2).toInt()
        if (nbit > nbits) nbit = nbits
        if (lo == 1L) bits[0] = bits[0] and 1L.inv()   // 1 不是素数
        for (q in base) {
            if (q == 2) continue                        // 偶数位不在奇数表里
            val ql = q.toLong()
            if (ql * ql >= hi) break
            val s0 = if (ql * ql >= lo) ql * ql else ((lo + ql - 1) / ql) * ql
            val start = if (s0 and 1L == 0L) s0 + ql else s0
            var idx = ((start - lo) / 2).toInt()
            while (idx < nbit) {
                bits[idx ushr 6] = bits[idx ushr 6] and (1L shl (idx and 63)).inv()
                idx += q
            }
        }
        var j = 0
        var toggle = false
        while (j + 9 < nbit) {
            if (bits[j ushr 6] and (1L shl (j and 63)) != 0L &&
                bits[(j + 3) ushr 6] and (1L shl ((j + 3) and 63)) != 0L &&
                bits[(j + 6) ushr 6] and (1L shl ((j + 6) and 63)) != 0L &&
                bits[(j + 9) ushr 6] and (1L shl ((j + 9) and 63)) != 0L
            ) {
                var clear = true
                for (t in intArrayOf(1, 2, 4, 5, 7, 8)) {
                    val k = j + t
                    if (bits[k ushr 6] and (1L shl (k and 63)) != 0L) { clear = false; break }
                }
                if (clear) {
                    val p = lo + 2L * j
                    candidates.add(p)
                    val n = p + 9
                    if (isPracticalBySigma(n - 8) && isPracticalBySigma(n - 4) &&
                        isPracticalBySigma(n) && isPracticalBySigma(n + 4) &&
                        isPracticalBySigma(n + 8)
                    ) {
                        paradises.add(n)
                        if (paradises.size == FIRST_N) break@outer
                    }
                }
            }
            j += if (toggle) 10 else 5
            toggle = !toggle
        }
        lo += stepNum
    }
    return Hunt(candidates.toLongArray(), paradises.toLongArray(), lo)
}

// ────────────────────────── 方法 B：候选空间模式筛 ──────────────────────────

/** 枚举 [from, to) 内 p ≡ 1,11 (mod 30) 的候选素数，升序。 */
private inline fun forEachCandidateIn(from: Long, to: Long, action: (Long) -> Boolean) {
    var base = from - ((from - 1L) % 30L + 30L) % 30L      // 调成 ≡ 1 (mod 30)
    while (base < to) {
        if (base >= from) {
            if (windowIsCandidate(base) && !action(base)) return
        }
        val second = base + 10L
        if (second >= from && second < to) {
            if (windowIsCandidate(second) && !action(second)) return
        }
        base += 30L
    }
}

/** 模式筛的单段长度/候选位图大小（段长为 30 的倍数）。 */
private const val B_SEG_CAND = 1 shl 17            // 每段候选数
private const val B_SEG_NUM = 30L * (B_SEG_CAND / 2)
private const val PP_LIMIT = 36_100                // 模式筛从 p ≥ 34111 开始

private fun huntByPatternSieve(base: IntArray, limitCap: Long): Hunt {
    val candidates = ArrayList<Long>()
    val paradises = ArrayList<Long>()

    fun consider(p: Long) {
        // p 的窗口已确认：模式筛/小范围试除都保证四个素数 + 六个复合中间数
        val n = p + 9
        if (isPracticalByDivisors(n - 8, base) && isPracticalByDivisors(n - 4, base) &&
            isPracticalByDivisors(n, base) && isPracticalByDivisors(n + 4, base) &&
            isPracticalByDivisors(n + 8, base)
        ) paradises.add(n)
    }

    // 1) p < 34111：直接试除验证窗口
    forEachCandidateIn(11L, 34_111L) { p ->
        candidates.add(p)
        consider(p)
        paradises.size < FIRST_N
    }
    if (paradises.size >= FIRST_N) {
        return Hunt(candidates.toLongArray(), paradises.toLongArray(), 34_111L)
    }

    // 2) 模式筛：候选空间中，任何 p+h（h=0,6,12,18）含小素数因子者被清掉
    val inv30 = HashMap<Int, Long>()
    var lo = 34_111L
    val words = B_SEG_CAND / 64
    val bits = LongArray(words)
    outer@ while (lo <= limitCap) {
        java.util.Arrays.fill(bits, -1L)
        for (q in base) {
            if (q < 7) continue
            val ql = q.toLong()
            if (ql * ql > lo + B_SEG_NUM + 18L) break
            val inv = inv30.getOrPut(q) { modInverse(30L, ql) }
            for (h in intArrayOf(0, 6, 12, 18)) {
                for (parity in 0..1) {
                    val delta = if (parity == 0) 0L else 10L
                    val r = (((-h - delta - lo) % ql) + ql) % ql
                    val t0 = (r * inv) % ql
                    var i = (2 * t0 + parity).toInt()
                    val step = 2 * q
                    while (i < B_SEG_CAND) {
                        bits[i ushr 6] = bits[i ushr 6] and (1L shl (i and 63)).inv()
                        i += step
                    }
                }
            }
        }
        var i = 0
        while (i < B_SEG_CAND) {
            if (bits[i ushr 6] and (1L shl (i and 63)) != 0L) {
                val p = lo + 30L * (i shr 1) + (if (i and 1 == 0) 0L else 10L)
                if (p + 18L > limitCap) break
                var midComposite = true
                for (t in longArrayOf(2, 4, 8, 10, 14, 16)) {
                    if (isPrimeSmall(p + t, base)) { midComposite = false; break }
                }
                if (midComposite) {
                    candidates.add(p)
                    consider(p)
                    if (paradises.size == FIRST_N) break@outer
                }
            }
            i++
        }
        lo += B_SEG_NUM
    }
    return Hunt(candidates.toLongArray(), paradises.toLongArray(), lo)
}

/** 试除检查 p,p+6,p+12,p+18 为素数且中间 6 个数复合（用于小范围直接判定）。 */
private fun windowIsCandidate(p: Long): Boolean {
    for (h in longArrayOf(0, 6, 12, 18)) {
        if (!isPrimeSmall(p + h, SMALL_PRIMES)) return false
    }
    for (h in longArrayOf(2, 4, 8, 10, 14, 16)) {
        if (isPrimeSmall(p + h, SMALL_PRIMES)) return false
    }
    return true
}

private val SMALL_PRIMES: IntArray by lazy { sieveBasePrimes(7000) }

// ────────────────────────── 暴力对照（朴素整段筛） ──────────────────────────

private fun bruteSmall(limit: Long, base: IntArray): Hunt {
    val nbits = (limit / 2).toInt() + 1
    val bits = LongArray(nbits / 64 + 1) { -1L }
    bits[0] = bits[0] and 1L.inv()                     // 数 1
    for (q in base) {
        if (q == 2) continue
        val ql = q.toLong()
        if (ql * ql > limit) break
        var idx = ((ql * ql - 1) / 2).toInt()
        while (idx < nbits) {
            bits[idx ushr 6] = bits[idx ushr 6] and (1L shl (idx and 63)).inv()
            idx += q
        }
    }
    fun prime(x: Long): Boolean {
        if (x == 2L) return true
        if (x < 2L || x and 1L == 0L) return false
        val idx = ((x - 1) / 2).toInt()
        return bits[idx ushr 6] and (1L shl (idx and 63)) != 0L
    }
    val candidates = ArrayList<Long>()
    val paradises = ArrayList<Long>()
    // 老老实实扫全部奇数 p（不加 p ≡ 1,11 (mod 30) 之类先验），逐个查窗口
    var p = 3L
    while (p + 18 <= limit) {
        if (prime(p) && prime(p + 6) && prime(p + 12) && prime(p + 18) &&
            !prime(p + 2) && !prime(p + 4) && !prime(p + 8) &&
            !prime(p + 10) && !prime(p + 14) && !prime(p + 16)
        ) {
            candidates.add(p)
            val n = p + 9
            if (isPracticalBySigma(n - 8) && isPracticalBySigma(n - 4) &&
                isPracticalBySigma(n) && isPracticalBySigma(n + 4) &&
                isPracticalBySigma(n + 8)
            ) paradises.add(n)
        }
        p += 2
    }
    return Hunt(candidates.toLongArray(), paradises.toLongArray(), limit)
}

// ────────────────────────────── 主流程 ──────────────────────────────

private fun bestOf3(tag: String, f: () -> Hunt): Double {
    var best = Double.MAX_VALUE
    var result: Hunt? = null
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val h = f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) { best = ms; result = h }
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms（候选 ${h.candidates.size}，" +
            "paradise ${h.paradises.size}）")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 定义锚点 ----------
    check(isPracticalBySubsetSum(6)) { "6 应为实用数" }
    run {
        val div6 = listOf(1, 2, 3, 6)
        val reachable = BooleanArray(7)
        reachable[0] = true
        for (d in div6) for (v in 6 downTo d) if (reachable[v - d]) reachable[v] = true
        for (v in 1..6) check(reachable[v]) { "$v 不能表示为 6 的不同因数之和" }
    }
    println("定义锚点：6 的因数 1,2,3,6 可表示 1..6；三种实用数判定在 1..2000 上逐一一致：")
    for (n in 1..2000) {
        val a = isPracticalBySubsetSum(n)
        val b = isPracticalBySigma(n.toLong())
        val c = isPracticalByDivisors(n.toLong(), SMALL_PRIMES)
        check(a == b && b == c) { "n=$n：subset=$a sigma=$b divisors=$c" }
    }
    println("  （子集和定义 / σ 链 / 因数贪心 全部一致）")

    // 首个 sexy pair
    var firstSexy = 0L
    var q = 2L
    while (firstSexy == 0L) {
        if (isPrimeSmall(q, SMALL_PRIMES) && isPrimeSmall(q + 6, SMALL_PRIMES) &&
            !isPrimeSmall(q + 2, SMALL_PRIMES) && !isPrimeSmall(q + 4, SMALL_PRIMES)
        ) firstSexy = q
        q++
    }
    check(firstSexy == 23L) { "首个 sexy pair 应为 (23,29)，实得 ($firstSexy,${firstSexy + 6})" }
    println("题面锚点：首个 sexy pair = (23, 29)")

    // ---------- 完整规模的两种独立方法 ----------
    val base = sieveBasePrimes(isqrt(LIMIT_CAP + 100).toInt() + 2)
    val a = huntBySieve(base, LIMIT_CAP)
    val b = huntByPatternSieve(base, LIMIT_CAP)
    check(a.candidates.contentEquals(b.candidates)) {
        "候选表不一致：A=${a.candidates.size} B=${b.candidates.size}"
    }
    check(a.paradises.contentEquals(b.paradises)) {
        "paradise 不一致：A=${a.paradises.toList()} B=${b.paradises.toList()}"
    }
    check(a.paradises.size == FIRST_N) { "只找到 ${a.paradises.size} 个 paradise" }
    println("方法 A 与方法 B 的候选表完全一致（共 ${a.candidates.size} 个候选，扫描到 ${a.scannedTo}）")
    println("前四个工程师天堂：${a.paradises.toList()}")

    // ---------- 暴力对照（缩小规模） ----------
    val brute = bruteSmall(BRUTE_LIMIT, base)
    val aSmall = a.candidates.filter { it <= BRUTE_LIMIT }
    val bSmall = b.candidates.filter { it <= BRUTE_LIMIT }
    check(brute.candidates.toList() == aSmall && brute.candidates.toList() == bSmall) {
        "≤$BRUTE_LIMIT 的候选表不一致：brute=${brute.candidates.size} A=${aSmall.size} B=${bSmall.size}"
    }
    check(brute.paradises.isEmpty()) { "≤$BRUTE_LIMIT 不应有 paradise" }
    println("暴力对照：≤$BRUTE_LIMIT 候选 ${brute.candidates.size} 个，与 A、B 逐一相同（该范围内无 paradise）")

    // ---------- 答案 ----------
    val answer = a.paradises.sum()
    check(answer == 2_039_506_520L) { "答案漂移：$answer" }
    println()
    println("前四个 paradise = ${a.paradises.toList()}")
    println("答案 = $answer")

    // ---------- 计时 ----------
    huntBySieve(base, LIMIT_CAP)
    bruteSmall(10_000_000L, base)
    val msA = bestOf3("方法 A（完整规模）") { huntBySieve(base, LIMIT_CAP) }
    val msB = bestOf3("方法 B（完整规模）") { huntByPatternSieve(base, LIMIT_CAP) }
    val msBrute = bestOf3("暴力 ≤$BRUTE_LIMIT") { bruteSmall(BRUTE_LIMIT, base) }
    println()
    println("汇总：方法 A ${"%.1f".format(msA)} ms；方法 B ${"%.1f".format(msB)} ms；" +
        "暴力 ≤$BRUTE_LIMIT ${"%.1f".format(msBrute)} ms")
    println("check() 全部通过")
}
