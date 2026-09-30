package dev.pekt.engine

import dev.pekt.math.extendedGcd
import dev.pekt.math.gcd
import kotlin.math.sqrt

/**
 * PE 295 — 双凸透镜孔（Lenticular Holes）：L(100 000) = 4884650818。
 *
 * 推导（详见 content/problems/0295/solution.kt 头部与 0295/analysis.md）：
 *   透镜孔由一条弦 AB（A=(0,0)、B=(u,v)，gcd(u,v)=1 且 u、v 同奇）与分居弦两侧的两个圆心确定。
 *   记 s = u² + v²、h = √s/2，圆心到弦中点的距离 |t| = (m/2)√s（m = |2k+1| 为奇数），
 *   半径 r = (√s/2)·√(m²+1)；两片月牙内部无格点 ⟺ 两侧的 m 都 ≥ max(1, ⌈M(u,v)⌉)，其中
 *     M(u,v) = max_{n≥1} (s²/4 − e_n² − n²)/(s·n)， e_n = dist(s/2, n·k₁ mod s)，
 *   k₁ 取自 ⟨P₁,w⟩ = 1（w = (−v,u)）的任一解。由 f(n) ≤ s/(4n) 与 M ≥ √(s−1)−1，
 *   n 只需枚举到 ~√s/4，故每条弦求阈值是 O(√s)。
 *
 *   半径只依赖 s 与 m；同一 s 的多个 primitive 表示 (u,v) 共用阈值 M_s = min M(u,v)，
 *   故每个 s 贡献半径集合 R_s = { (√s/2)·√(m²+1) : m 为奇且 m ≥ M_s }。一对半径 (r1,r2) 合法
 *   ⟺ S(r1²) ∩ S(r2²) ≠ ∅（S(r²) = 所有能表示 r² 的弦参数集合，如 r=5 同时来自 s=2、10、50）。
 *   把所有 (4r², s) 关联打包排序后按 4r² 归组，用 T1 − T2 + T3 + (k ≥ 4 交的修正) 容斥：
 *   对共享 t 条弦的一对半径，Σ_k (−1)^{k+1} C(t,k) = 1，恰好计一次。
 *   存在半径 ≤ N 的弦必有 s ≤ 2N + 2√(2N) + 200（由 M ≥ √(s−1)−1 推出），枚举到该界即可。
 *
 *   验证：题面门 L(10)=30、L(100)=3442；content 侧另有暴力定义级枚举与 M 闭合公式对照。
 *
 * 复杂度：枚举 ~2×10^5 条弦、每条求 M 用 O(√s) → 整体 ~O(N√N) 量级；关联条目排序 + 归组线性。
 *   本机 JIT 预热后全尺寸约 50 ms，远低于 10 s 熔断线。
 */
internal fun solve0295Impl(): Long = countLenticularPairs295(100_000)

// ───────────────────────── 整数工具 ─────────────────────────

/** 整数平方根 ⌊√x⌋（x ≥ 0）。 */
private fun isqrt295(x: Long): Long {
    var r = sqrt(x.toDouble()).toLong()
    while (r * r > x) r--
    while ((r + 1) * (r + 1) <= x) r++
    return r
}

private fun ceilDiv295(a: Long, b: Long): Long = -Math.floorDiv(-a, b)

// ───────────────────────── 单条弦的阈值 M(u,v) ─────────────────────────

/**
 * u、v 为奇数且互素。返回 max(1, ⌈M(u,v)⌉)：允许的圆心参数 m 恰为**奇数** m ≥ 该值。
 * M(u,v) = max_{n≥1} (s²/4 − e_n² − n²)/(s·n)，e_n = dist(s/2, n·k₁ mod s)；
 * n 只需到 ~√s/4：因为 f(n) ≤ s/(4n)，且总有 M ≥ √(s−1) − 1。
 */
private fun mThreshold295(u: Int, v: Int): Long {
    val s = u.toLong() * u + v.toLong() * v
    // 解 ⟨P₁, w⟩ = −v·P₁x + u·P₁y = 1
    val (g, x, y) = extendedGcd(-v.toLong(), u.toLong())
    require(g == 1L) { "gcd(u,v) != 1: u=$u v=$v" }
    val k1 = (((u.toLong() * x + v.toLong() * y) % s) + s) % s
    val sq = isqrt295(s - 1)
    var best = maxOf(1L, (if (sq * sq == s - 1) sq else sq + 1) - 1L)   // 下界 √(s−1) − 1
    var n = 1L
    while (4L * n * best < s) {
        val d = (n * k1) % s - s / 2
        val vi = ceilDiv295(s * s - 4L * d * d - 4L * n * n, 4L * s * n)
        if (vi > best) best = vi
        n++
    }
    return best
}

// ───────────────────────── 按弦参数 s 汇总阈值 ─────────────────────────

/**
 * 枚举全部 primitive 弦 (u,v)（同为奇数、互素、u²+v² ≤ 2N + 2√(2N) + 200），
 * 返回 s ↦ M_s = min M(u,v)。半径只依赖 s，故同一 s 的不同表示取最小阈值即可。
 */
private fun chordThresholds295(N: Int): HashMap<Int, Long> {
    val maxS = 2 * N + (4 * sqrt(2.0 * N)).toInt() + 200
    val mByS = HashMap<Int, Long>()
    val uMax = isqrt295(maxS.toLong()).toInt() + 1
    for (u in 1..uMax step 2) for (v in u..uMax step 2) {
        val s = u * u + v * v
        if (s > maxS) break
        if (gcd(u.toLong(), v.toLong()) != 1L) continue
        val m = mThreshold295(u, v)
        val cur = mByS[s]
        if (cur == null || m < cur) mByS[s] = m
    }
    return mByS
}

// ───────────────────────── 计数：容斥去重 ─────────────────────────

/**
 * L(N)：对每条弦参数 s 列出全部 (4r², s) 关联（r² = s(m²+1)/4，m 为满足阈值的奇数），
 * 排序后按 4r² 归组，用 T1 − T2 + T3 + (k ≥ 4 的修正) 对「多弦共用半径」做容斥。
 */
private fun countLenticularPairs295(N: Int): Long {
    val sh = 18                                        // s < 2^18（N = 1e5 时 s ≤ ~2×10^5）
    val shift1 = 1L shl sh
    val maskS = shift1 - 1
    val shift2 = 1L shl (2 * sh)
    val n2 = N.toLong() * N                            // r ≤ N ⇔ 4r² ≤ 4N²
    val mByS = chordThresholds295(N)

    // ── 每条弦的 (4r², s) 关联打包进一个 Long，便于按 4r² 排序 ──
    var cap = 1 shl 22
    var wArr = LongArray(cap)
    var wcnt = 0
    var t1 = 0L
    for ((s, mMin) in mByS) {
        var m = if (mMin % 2L == 0L) mMin + 1L else mMin   // 最小的允许奇数 m
        if (m < 1L) m = 1L
        var k = 0
        while (true) {
            val w = s * (m * m + 1L)                        // = 4r²
            if (w > 4L * n2) break
            if (wcnt == cap) { cap *= 2; wArr = wArr.copyOf(cap) }
            wArr[wcnt++] = w * shift1 + s
            k++
            m += 2L
        }
        t1 += k.toLong() * (k + 1L) / 2L                    // 单弦内部两两组合 C(k+1, 2)
    }
    java.util.Arrays.sort(wArr, 0, wcnt)

    // ── 按 4r² 归组，统计 2 元 / 3 元弦组各自的公共半径数 ──
    val c2 = HashMap<Long, Int>()
    val c3 = HashMap<Long, Int>()
    val highSets = ArrayList<IntArray>()                   // |S(r²)| ≥ 4 的罕见情形
    var i = 0
    while (i < wcnt) {
        var j = i + 1
        val w = wArr[i] ushr sh
        while (j < wcnt && (wArr[j] ushr sh) == w) j++
        val t = j - i
        if (t >= 2) {
            val ss = IntArray(t) { (wArr[i + it] and maskS).toInt() }
            for (a in 0 until t) for (b in a + 1 until t) {
                val key = ss[a].toLong() * shift1 + ss[b]
                c2[key] = (c2[key] ?: 0) + 1
            }
            if (t >= 3) for (a in 0 until t) for (b in a + 1 until t) for (c in b + 1 until t) {
                val key = ss[a].toLong() * shift2 + ss[b].toLong() * shift1 + ss[c]
                c3[key] = (c3[key] ?: 0) + 1
            }
            if (t >= 4) highSets.add(ss)
        }
        i = j
    }
    var t2 = 0L
    for (c in c2.values) t2 += c.toLong() * (c + 1L) / 2L
    var t3 = 0L
    for (c in c3.values) t3 += c.toLong() * (c + 1L) / 2L

    // k ≥ 4 的修正项：−T4 + T5 − …（只涉及 |S(r²)| ≥ 4 的半径，逐个子集显式统计）
    var corr = 0L
    if (highSets.isNotEmpty()) {
        val subsets = HashMap<String, IntArray>()
        for (set in highSets) {
            val chosen = IntArray(set.size)
            fun rec(start: Int, size: Int) {
                if (size >= 4) subsets[chosen.copyOf(size).joinToString(",")] = chosen.copyOf(size)
                if (size == set.size) return
                for (t in start until set.size) { chosen[size] = set[t]; rec(t + 1, size + 1) }
            }
            rec(0, 0)
        }
        for (sigma in subsets.values) {
            var c = 0
            for (set in highSets) if (sigma.all { set.contains(it) }) c++
            val term = c.toLong() * (c + 1L) / 2L
            corr += if (sigma.size % 2 == 0) -term else term
        }
    }
    return t1 - t2 + t3 + corr
}
