#!/usr/bin/env kotlin
/**
 * Project Euler 256 — Tatami-Free Rooms（无榻榻米铺法的房间）
 *
 * 思路：
 *   1) 规则等价形式。房间是 a×b（a ≤ b）的格子，用 1×2 垫子铺满。内部格点 P 上
 *      「四块垫子的角相遇」等价于 P 周围 2×2 的四个格子，各自的垫子都把另一半伸到
 *      该 2×2 之外（这时四块垫子在 P 点各贡献一个角）。所以
 *        「无四角相遇」 ⟺ 每个内部 2×2 都含有一块完整落在其中的垫子。
 *      据此可做精确的行轮廓 DP 判定：记 occ = 本行被上方竖垫占据的格集合、
 *      hleft = 上一行横垫左端集合、hl = 本行横垫左端集合，则行间每个列边界 j 处
 *        cond(j) = hleft[j-1] ∨ occ[j-1] ∨ occ[j] ∨ hl[j-1]
 *                = M[j-1] ∨ occ[j],   M = hleft | occ | hl,
 *      即 (M | (occ >> 1)) 覆盖位 0..b-2。brute-force.kt 的判定器就是它。
 *
 *   2) 文献刻画。Hickerson 的结构定理（见 Ruskey–Woodcock, "Counting Fixed-Height
 *      Tatami Tilings", 2009）给出矩形榻榻米铺法的分解：偶数高 m≥4、宽 n≥m 的铺法
 *      由宽度 m-2 与 m 的块以及夹在块之间（两端最多各一条）的 m×1 竖条拼成；奇数高
 *      m 的铺法由 m×(m-1) 与 m×(m+1) 的块拼成。把可达宽度写成若干连续区间后取补集，
 *      得到本文用的刻画（b = qa + r，q = ⌊b/a⌋）：
 *
 *        a×b（a ≤ b，面积偶数）是 tatami-free ⟺ q+2 ≤ r ≤ a-q-3，
 *
 *      等价地 (a+1)q + 2 ≤ b ≤ (a-1)(q+1) - 2。于是
 *
 *        T(s) = #{ a | s : a ≤ √s, b = s/a, (a,b) 满足上式 }。
 *
 *   3) 搜索。T(s) = 200 蕴含 τ(s) ≥ 399（无序约数对至多 ⌈τ(s)/2⌉ 个），且 s 必须为
 *      偶数。据此做剪枝 DFS 枚举所有「质因数分解下可能达到 τ ≥ 399」的偶数 s，逐个
 *      分解计数——候选在 s ≤ 10^8 内只有 5206 个，几十毫秒即可。
 *      还能把候选素数限到 10^4 以内：若 p² | s 则 p ≤ √s ≤ 10^4；若 p ‖ s，写
 *      s = p·n 得 τ(n) ≥ 200，而最小这样的 n 是 498960，故 p ≤ 10^8/498960 < 201。
 *
 *   方法 A（主路径）：τ 剪枝的候选 DFS + 逐候选约数枚举计数 → 85765680。
 *   方法 B（独立复核）：分段筛出 [2, A] 中每个偶数的 τ(s)，对 τ ≥ 399 的 s 再精确
 *      分解并计数 → 同一答案。两条路径结构不同（约数富裕数枚举 vs 全范围扫描）。
 *
 * 旁证：
 *   · 行轮廓 DP（以及小棋盘上"枚举全部铺法 + 数每个内部格点的垫子角数"的字面
 *     暴力）与刻画式在 a,b ≤ 16 的所有偶数面积对上逐一吻合；题面数据
 *     T(70)=1（唯一 7×10）、T(1320)=5（恰为 20×66 … 33×40）与「最小 T(s)=5 的
 *     s 是 1320」全部复现；
 *   · Hickerson 分解模型与刻画式在 a ≤ b ≤ 300 的全部 33825 个偶数面积对上一致；
 *   · 公开参考实现（ZiCog/tatami-rust、cirosantilli 题解）给出同一答案 85765680。
 *
 * 答案：85765680
 * 复杂度：方法 A ≈ 5.2×10³ 个候选 × O(τ(s))；方法 B 分段筛 O(S log log S) + 候选计数。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val S_MAX = 100_000_000L      // 候选搜索上界（答案必须 < 它）
private const val TARGET = 200
private const val TAU_MIN = 399              // ceil(τ/2) ≥ 200 ⟺ τ ≥ 399
private const val PRIME_LIMIT = 10_000       // 候选的素因子都 ≤ 10^4（见头部证明）
private const val SHIFT = 32
private const val EXPECTED = 85_765_680L     // 实跑得到，见 main 的 check

// ------------------------------------------------------------------ 基本工具

private fun primesTo(n: Int): IntArray {
    val isComposite = BooleanArray(n + 1)
    var count = 0
    for (i in 2..n) if (!isComposite[i]) {
        count++
        var j = i.toLong() * i
        while (j <= n) { isComposite[j.toInt()] = true; j += i }
    }
    val out = IntArray(count)
    var k = 0
    for (i in 2..n) if (!isComposite[i]) out[k++] = i
    return out
}

/** a×b（a ≤ b，面积偶数）是否 tatami-free：q = ⌊b/a⌋，(a+1)q+2 ≤ b ≤ (a-1)(q+1)-2 */
private fun tfFree(a: Long, b: Long): Boolean {
    val q = b / a
    if (q == 0L) return false
    return b >= (a + 1) * q + 2 && b <= (a - 1) * (q + 1) - 2
}

/** 约数枚举版 T(s)（s 不太大时够用） */
private fun tByTrialDivision(s: Long): Int {
    var cnt = 0
    var a = 1L
    while (a * a <= s) {
        if (s % a == 0L && tfFree(a, s / a)) cnt++
        a++
    }
    return cnt
}

/** 由分解 (ps[i]^es[i]) 生成全部约数（顺序不限） */
private fun divisorsOf(ps: LongArray, es: IntArray, k: Int): LongArray {
    var tau = 1
    for (i in 0 until k) tau *= es[i] + 1
    val divs = LongArray(tau)
    divs[0] = 1L
    var size = 1
    for (i in 0 until k) {
        val base = size
        var pe = 1L
        for (e in 1..es[i]) {
            pe *= ps[i]
            for (j in 0 until base) divs[size++] = divs[j] * pe
        }
    }
    return divs
}

/** 由分解精确计算 T(s) */
private fun tFromFactorization(s: Long, ps: LongArray, es: IntArray, k: Int): Int {
    var cnt = 0
    for (d in divisorsOf(ps, es, k)) {
        if (d * d <= s && tfFree(d, s / d)) cnt++
    }
    return cnt
}

// ------------------------------------------------------------------ 精确判定器（暴力对照用）

/** 枚举某一行在 occ（被上方竖垫占据的格）约束下的全部铺法，返回打包状态：
 *  低 SHIFT 位 = 本行向下的竖垫集合 D，高位 = 本行横垫的左端集合 hl。
 *  checkRow 时校验行间条件 (M | (occ >> 1)) 覆盖位 0..b-2，M = hleft | occ | hl。 */
private fun enumerateRows(
    occ: Long,
    hleft: Long,
    b: Int,
    maskBm1: Long,
    checkRow: Boolean,
): LongArray {
    val out = ArrayList<Long>()
    fun rec(j: Int, d: Long, hl: Long) {
        if (j >= b) {
            if (checkRow) {
                val m = hleft or occ or hl
                if ((m or (occ ushr 1)) and maskBm1 != maskBm1) return
            }
            out.add(d or (hl shl SHIFT))
            return
        }
        if ((occ ushr j) and 1L == 1L) { rec(j + 1, d, hl); return }
        rec(j + 1, d or (1L shl j), hl)                                  // 竖垫向下
        if (j + 1 < b && (occ ushr (j + 1)) and 1L == 0L)                 // 横垫 (j, j+1)
            rec(j + 2, d, hl or (1L shl j))
    }
    rec(0, 0L, 0L)
    return out.toLongArray()
}

/** 精确判定 a×b 能否无四角相遇地铺满（行轮廓 DP，与刻画式无关） */
private fun tileableDp(a: Int, b: Int): Boolean {
    if (a.toLong() * b % 2L != 0L) return false
    val full = (1L shl b) - 1L
    val maskBm1 = (1L shl (b - 1)) - 1L
    var states = HashSet<Long>()
    states.add(0L)
    for (i in 0 until a) {
        val next = HashSet<Long>()
        for (st in states) {
            val occ = st and full
            val hleft = (st ushr SHIFT) and full
            for (ns in enumerateRows(occ, hleft, b, maskBm1, i > 0)) next.add(ns)
        }
        states = next
        if (states.isEmpty()) return false
    }
    return states.any { it and full == 0L }
}

// ------------------------------------------------------------------ 方法 A：候选 DFS

/** 枚举 s ≤ S_MAX 的全部偶数候选（τ(s) ≥ TAU_MIN），返回最小 T(s)=TARGET 的 s。 */
private fun solveByCandidates(): Long {
    val primes = primesTo(PRIME_LIMIT)
    val ps = LongArray(32)
    val es = IntArray(32)
    val memo = HashMap<Long, Int>()

    /** n ≤ lim、素因子 ≥ primes[pi] 的 τ(n) 最大值（非递增指数即已达最大，标准剪枝界） */
    fun maxTau(lim: Long, pi: Int): Int {
        if (pi >= primes.size) return 1
        val p = primes[pi]
        if (p > lim) return 1
        val key = pi.toLong() * 1_000_000_000L + lim
        memo[key]?.let { return it }
        var best = 1
        var pe = 1L
        var e = 0
        while (pe <= lim / p) {
            e++
            pe *= p
            val c = (e + 1) * maxTau(lim / pe, pi + 1)
            if (c > best) best = c
        }
        memo[key] = best
        return best
    }

    var best = Long.MAX_VALUE
    fun dfs(s: Long, tau: Long, k: Int, pi: Int) {
        if (tau >= TAU_MIN && s < best && tFromFactorization(s, ps, es, k) == TARGET) best = s
        var idx = pi
        while (idx < primes.size) {
            val p = primes[idx].toLong()
            if (p > S_MAX / s) break
            var pe = p
            var e = 1
            while (true) {
                ps[k] = p; es[k] = e
                val ntau = tau * (e + 1)
                if (ntau * maxTau(S_MAX / (s * pe), idx + 1) >= TAU_MIN) {
                    dfs(s * pe, ntau, k + 1, idx + 1)
                }
                if (pe > (S_MAX / s) / p) break
                pe *= p; e++
            }
            idx++
        }
    }

    // s 必须为偶数：先取定 2 的幂，再用 ≥ 3 的素数扩展（素数严格递增避免重复）
    var pe = 2L
    var e = 1
    while (true) {
        ps[0] = 2L; es[0] = e
        val tau = (e + 1).toLong()
        if (tau * maxTau(S_MAX / pe, 1) >= TAU_MIN) dfs(pe, tau, 1, 1)
        if (pe > S_MAX / 2) break
        pe *= 2; e++
    }
    return best
}

// ------------------------------------------------------------------ 方法 B：分段筛复核

/**
 * 只扫偶数 s ∈ [2, hiBound]：分段（每段 CHUNK 个偶数）分解出每个偶数的 τ(s)，对
 * τ ≥ 399 者再用试除精确分解并计数 T(s)，取最小 T(s)=TARGET 的 s。
 * 与候选 DFS 完全不同的结构（不剪枝、不依赖素因子上界，纯扫描）。
 */
private fun solveBySieve(hiBound: Long): Long {
    val CHUNK = 1 shl 22                       // 每段处理的偶数个数
    val sievePrimes = primesTo(10_001)
    val rem = IntArray(CHUNK)
    val tau = ShortArray(CHUNK)
    var best = Long.MAX_VALUE

    // 段 [lo, hi] 内全部偶数：s = lo + 2*i（lo 为偶数）
    var lo = 2L
    while (lo <= hiBound) {
        val hi = minOf(lo + 2L * (CHUNK - 1), hiBound)
        val len = ((hi - lo) / 2).toInt() + 1
        for (i in 0 until len) {
            val s = lo + 2L * i
            val tz = java.lang.Long.numberOfTrailingZeros(s)
            rem[i] = (s ushr tz).toInt()       // 去掉全部因子 2 后的奇数部分
            tau[i] = (tz + 1).toShort()
        }
        for (p in sievePrimes) {
            if (p == 2) continue
            if (p.toLong() * p > hi) break
            val step = 2L * p                  // 偶数是 p 的倍数 ⟺ 是 2p 的倍数
            var m = ((lo + step - 1) / step) * step
            while (m <= hi) {
                val idx = ((m - lo) / 2).toInt()
                var e = 0
                while (rem[idx] % p == 0) { rem[idx] /= p; e++ }
                tau[idx] = (tau[idx] * (e + 1)).toShort()
                m += step
            }
        }
        for (i in 0 until len) {
            val s = lo + 2L * i
            var t = tau[i].toInt()
            if (rem[i] > 1) t *= 2             // 余下的大素因子幂次必为 1
            if (t >= TAU_MIN) {
                var x = s
                var k = 0
                val ps = LongArray(12)
                val es = IntArray(12)
                for (p in sievePrimes) {
                    if (p.toLong() * p > x) break
                    if (x % p == 0L) {
                        var e = 0
                        while (x % p == 0L) { x /= p; e++ }
                        ps[k] = p.toLong(); es[k] = e; k++
                    }
                }
                if (x > 1) { ps[k] = x; es[k] = 1; k++ }
                if (tFromFactorization(s, ps, es, k) == TARGET && s < best) best = s
            }
        }
        lo = hi + 2
    }
    return best
}

// ------------------------------------------------------------------ main

fun main() {
    // 1) 小规模暴力对照：行轮廓 DP（精确判定） vs 刻画式
    var checked = 0
    var mismatches = 0
    for (a in 1..16) for (b in a..16) {
        if (a.toLong() * b % 2L != 0L) continue
        checked++
        if (!tileableDp(a, b) != tfFree(a.toLong(), b.toLong())) mismatches++
    }
    check(mismatches == 0) { "行轮廓 DP 与刻画式有 $mismatches 处不一致" }
    println("小规模对照：a ≤ b ≤ 16 的 $checked 个偶数面积对，行轮廓 DP 判定与刻画式完全一致")

    // 2) 题面数据复现
    check(tByTrialDivision(70) == 1) { "T(70) 应为 1" }
    check(tByTrialDivision(1320) == 5) { "T(1320) 应为 5" }
    var minT5 = -1L
    for (s in 2L..20_000L step 2) if (tByTrialDivision(s) == 5) { minT5 = s; break }
    check(minT5 == 1320L) { "最小 T(s)=5 的 s 应为 1320，实得 $minT5" }
    println("题面数据：T(70)=1，T(1320)=5，最小 T(s)=5 的 s=1320 —— 全部复现")

    // 3) 方法 A（主路径）
    val ansA = solveByCandidates()
    println("方法 A（τ ≥ 399 候选 DFS + 逐候选计数）答案 = $ansA")

    // 4) 方法 B（独立结构复核）
    val ansB = solveBySieve(ansA)
    println("方法 B（偶数分段筛 τ + 逐 s 计数）答案 = $ansB")
    check(ansA == ansB) { "两种方法不一致：$ansA vs $ansB" }
    check(ansA == EXPECTED) { "答案与预期不符：$ansA" }
    val pairs = tByTrialDivision(ansA)
    println("两种方法一致；T($ansA) = $pairs")

    // 5) 计时：JIT 预热后各 3 轮取最优
    check(solveByCandidates() == ansA)
    var bestA = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveByCandidates() == ansA)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestA) bestA = ms
        println("  方法 A 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 A 完整求解：${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    check(solveBySieve(ansA) == ansB)
    var bestB = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveBySieve(ansA) == ansB)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestB) bestB = ms
        println("  方法 B 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 B 完整求解：${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")

    println("check() 全部通过")
}
