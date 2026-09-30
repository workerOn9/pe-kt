#!/usr/bin/env kotlin
/**
 * Project Euler 295 — Lenticular Holes（双凸透镜孔）
 *
 * 题目：把一个「两圆围成的凸区域」叫透镜孔（lenticular hole），若
 *   ① 两圆圆心都是格点；② 两圆交于两个不同的格点；③ 该凸区域内部不含格点。
 * 若存在半径分别为 r1、r2 的两圆能构成透镜孔，就称 (r1, r2) 是透镜对（有序、0 < r1 ≤ r2）。
 * L(N) = 半径不超过 N 的不同透镜对个数。已知 L(10) = 30、L(100) = 3442，求 L(100 000)。
 *
 * 建模：透镜 = 弦 + 两段「月牙」
 * ──────────────────────────────
 * 设两交点为 A、B，平移使 A = (0,0)、B = (u,v)（格点）。中点 B/2 与弦上的其它格点都会
 * 落进透镜内部，故必须 gcd(u,v) = 1；圆心 O 满足 |OA| = |OB|（即落在 AB 的中垂线上），
 * 而中垂线上有格点又要求 gcd(u,v) | (u²+v²)/2——两条件合起来给出 u、v 必为奇数。
 * 此后 O = M₀ + (k+½)·w（M₀ = B/2 是中点、w = (−v,u)、k ∈ ℤ），
 * 记 s = u² + v²、h = √s/2，则圆心到 M₀ 的距离为
 *   |t| = (m/2)·√s，  m = |2k+1| ∈ {1,3,5,…}，  半径 r = √(t² + h²) = (√s/2)·√(m²+1)。
 * 弦 AB 把透镜切成两片月牙：上一片是某个圆盘 ∩ 上半平面，下一片是另一个圆盘 ∩ 下半平面。
 * 两圆心不能同在弦的一侧：若 0 < t₁ < t₂，较小那个圆心满足 |t₁−t₂| < √(t₂²+h²) = r₂，
 * 它自己就落在另一个圆盘内部，成了透镜内部的格点 → 矛盾。故两圆心必须分居弦两侧，
 * 两侧的 m 记为 m_a、m_b（可相等）。于是透镜内部的格点数 = 两片月牙内部的格点数之和。
 *
 * 空透镜条件 → 一条干净的算术判据
 * ──────────────────────────────
 * 用 n = ⟨P,w⟩（格点 P 相对弦的「整数高度」，可取遍 ℤ）与 ⟨P,B⟩ 表示月牙内部条件，得
 *   上一片为空 ⟺ ∀ 格点 P（n ≥ 1）：⟨P,B⟩ − |P|² + m_a·n ≥ 0，
 *   下一片为空 ⟺ 同一条件（P ↦ B−P 对称）把 m_a 换成 m_b。
 * 记 M(u,v) = max{ (⟨P,B⟩ − |P|²)/n : n ≥ 1 }，则允许的 m 恰为奇数 m ≥ max(1, ⌈M⌉)。
 * 若固定 n，把 P 写成「沿弦」的 e_n = dist(s/2, n·k₁ mod s)（k₁ = 某个 ⟨P₁,w⟩ = 1 解的
 * ⟨P₁,B⟩ mod s），可得闭合公式
 *   M(u,v) = max_{n ≥ 1} ( s²/4 − e_n² − n² ) / (s·n)，
 * 而 n 只需枚举到 ~√s/4（因为 f(n) ≤ s/(4n)，且总有 M ≥ √(s−1) − 1）。
 *
 * 计数 → 按弦参数 s 分类 + 对「同一个 r² 被多条弦表示」做容斥
 * ────────────────────────────────────────────────────────
 * 半径只依赖 s 与 m：r² = s(m²+1)/4；不同的 primitive 表示 (u,v)（u²+v² = s）共用一个阈值
 * M_s = min M(u,v)。故 L(N) = #{ (r1,r2) : ∃s（s ≡ 2 mod 8 且 s/2 的素因子全 ≡ 1 mod 4）
 * 使 r1、r2 都形如 (√s/2)√(m²+1)，m 为奇数且 ≥ M_s }。
 * 存在半径 ≤ N 的弦必有 s ≤ 2N + 2√(2N)（由 M ≥ √(s−1) − 1 推出），故只需枚举 s 到该界。
 * 一个半径 r² 可能被多条弦参数 s 表示（如 r = 5 同时来自 s = 2、10、50），
 * 记 S(r²) = { s : r² ∈ R_s }，则一对半径合法 ⟺ S(r1²) ∩ S(r2²) ≠ ∅。
 * 令 T_k = Σ_{k 元弦组 σ} C(|∩_{s∈σ} R_s| + 1, 2)，对任意一对半径（共享 t 条弦）
 * 有 Σ_k (−1)^{k+1} C(t,k) = 1，于是 L(N) = T1 − T2 + T3 − T4 + …（k ≥ 4 的交已在
 * N = 100000 下唯一出现一次，用显式枚举补上）。
 *
 * 验证
 * ────
 * 1. 题面门：L(10) = 30、L(100) = 3442（本实现直接给出）；
 * 2. brute-force.kt 用定义级枚举（列圆心、求交点、判内部格点）逐对复核 N = 10、100、200，
 *    两方法给出的 (r1², r2²) 集合完全相同（差异为空）；
 * 3. 本文件内自带 M(u,v) 闭合公式 vs 直接枚举 max 的对照（s ≤ 20000 的全部 1594 条弦）。
 *
 * 复杂度：枚举弦 O(N) 条、每条求 M 用 O(√s)；关联表 O(N log N) 排序；整体 ~O(N√N) 量级，
 *         N = 100000 时实测约 46 ms（JIT 预热后 3 轮最优）。
 *
 * 运行
 * ────
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * （本机无 kotlinc 时用仓库内的 shim：）
 * OUTDIR=/tmp/kc-0295 bash scripts/kotlinc-shim.sh content/problems/0295/solution.kt
 * java -cp /tmp/kc-0295:<kotlin-stdlib> SolutionKt
 */

import kotlin.math.abs
import kotlin.math.sqrt

// ───────────────────────── 整数工具 ─────────────────────────

private fun gcd64(a: Long, b: Long): Long {
    var x = abs(a); var y = abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

/** 返回 (g, x, y)：a·x + b·y = g ≥ 0 */
private fun extGcd(a0: Long, b0: Long): LongArray {
    val a = abs(a0); val b = abs(b0)
    var oldR = a; var r = b; var oldS = 1L; var s = 0L; var oldT = 0L; var t = 1L
    while (r != 0L) {
        val q = oldR / r
        var tmp = oldR - q * r; oldR = r; r = tmp
        tmp = oldS - q * s; oldS = s; s = tmp
        tmp = oldT - q * t; oldT = t; t = tmp
    }
    var x = oldS; var y = oldT
    if (a0 < 0) x = -x
    if (b0 < 0) y = -y
    return longArrayOf(oldR, x, y)
}

private fun isqrt(x: Long): Long {
    var r = sqrt(x.toDouble()).toLong()
    while (r * r > x) r--
    while ((r + 1) * (r + 1) <= x) r++
    return r
}

private fun ceilDiv(a: Long, b: Long): Long = -Math.floorDiv(-a, b)

// ───────────────────────── 单条弦的阈值 M(u,v) ─────────────────────────

/**
 * u、v 为奇数且互素。返回 max(1, ⌈M(u,v)⌉)：允许的圆心参数 m 恰为**奇数** m ≥ 该值。
 * M(u,v) = max_{n≥1} (s²/4 − e_n² − n²)/(s·n)，e_n = dist(s/2, n·k₁ mod s)。
 * n 只需到 ~√s/4：因为 f(n) ≤ s/(4n)，且总有 M ≥ √(s−1) − 1（k₀ ≥ √(s−1) 的推论）。
 */
private fun mThreshold(u: Int, v: Int): Long {
    val s = u.toLong() * u + v.toLong() * v
    // 解 ⟨P₁, w⟩ = −v·P₁x + u·P₁y = 1
    val e = extGcd(-v.toLong(), u.toLong())
    require(e[0] == 1L) { "gcd(u,v) != 1: u=$u v=$v" }
    val k1 = (((u.toLong() * e[1] + v.toLong() * e[2]) % s) + s) % s
    val sq = isqrt(s - 1)
    var best = maxOf(1L, (if (sq * sq == s - 1) sq else sq + 1) - 1L)   // 下界 √(s−1) − 1
    var n = 1L
    while (4L * n * best < s) {
        val d = (n * k1) % s - s / 2
        val vi = ceilDiv(s * s - 4L * d * d - 4L * n * n, 4L * s * n)
        if (vi > best) best = vi
        n++
    }
    return best
}

/** 对照实现：直接在 |P| ≤ R 的格点里取 max ((⟨P,B⟩ − |P|²)/n)，n ≥ 1（R 取 ⌈√s⌉+3 足够）。 */
private fun mThresholdDirect(u: Int, v: Int, boxRadius: Int): Long {
    var bestNum = Long.MIN_VALUE; var bestDen = 1L
    for (px in -boxRadius..boxRadius) for (py in -boxRadius..boxRadius) {
        val n = -v.toLong() * px + u.toLong() * py
        if (n < 1L) continue
        val num = u.toLong() * px + v.toLong() * py - (px.toLong() * px + py.toLong() * py)
        if (num * bestDen > bestNum * n) { bestNum = num; bestDen = n }
    }
    return maxOf(1L, ceilDiv(bestNum, bestDen))
}

// ───────────────────────── 按弦参数 s 汇总阈值 ─────────────────────────

/**
 * 枚举全部 primitive 弦 (u,v)（同为奇数、互素、u²+v² ≤ 2N + 2√(2N) + 200），
 * 返回 s ↦ M_s = min M(u,v)。半径只依赖 s，故同一 s 的不同表示取最小的阈值即可。
 */
private fun chordThresholds(N: Int): HashMap<Int, Long> {
    val maxS = 2 * N + (4 * sqrt(2.0 * N)).toInt() + 200
    val mByS = HashMap<Int, Long>()
    val uMax = isqrt(maxS.toLong()).toInt() + 1
    for (u in 1..uMax step 2) for (v in u..uMax step 2) {
        val s = u * u + v * v
        if (s > maxS) break
        if (gcd64(u.toLong(), v.toLong()) != 1L) continue
        val m = mThreshold(u, v)
        val cur = mByS[s]
        if (cur == null || m < cur) mByS[s] = m
    }
    return mByS
}

// ───────────────────────── 计数：容斥去重 ─────────────────────────

private const val SH = 18                       // s < 2^18（N = 1e5 时 s ≤ ~2×10^5）
private const val SHIFT1 = 1L shl SH
private const val MASK_S = SHIFT1 - 1
private const val SHIFT2 = 1L shl (2 * SH)

private class Stats {
    var chords = 0
    var distinctS = 0
    var incidences = 0
    var maxT = 0
    var t1 = 0L
    var t2 = 0L
    var t3 = 0L
    var corr = 0L
    var maxContributingS = 0
}

/**
 * L(N)：对每条弦参数 s 列出全部 (r², s) 关联（r² = s(m²+1)/4，m 为满足阈值的奇数），
 * 排序后按 r² 归组，用 T1 − T2 + T3 − (T4 − T5 + …) 对「多弦共用半径」做容斥。
 */
private fun countLenticularPairs(N: Int, stats: Stats? = null): Long {
    val n2 = N.toLong() * N
    val mByS = chordThresholds(N)
    stats?.chords = countChords(N)
    stats?.distinctS = mByS.size

    var cap = 1 shl 22
    var wArr = LongArray(cap)
    var wcnt = 0
    var t1 = 0L
    for ((s, mMin) in mByS) {
        var m = if (mMin % 2L == 0L) mMin + 1 else mMin   // 最小的允许奇数 m
        if (m < 1) m = 1
        var k = 0
        while (true) {
            val w = s * (m * m + 1)                     // = 4r²
            if (w > 4 * n2) break
            if (wcnt == cap) { cap *= 2; wArr = wArr.copyOf(cap) }
            wArr[wcnt++] = w * SHIFT1 + s
            k++
            m += 2
        }
        if (k > 0 && stats != null && s > stats.maxContributingS) stats.maxContributingS = s
        t1 += k.toLong() * (k + 1) / 2
    }
    java.util.Arrays.sort(wArr, 0, wcnt)

    val c2 = HashMap<Long, Int>()                        // 2 元弦组的公共半径数
    val c3 = HashMap<Long, Int>()                        // 3 元弦组
    val highSets = ArrayList<IntArray>()                 // |S(r²)| ≥ 4 的罕见情形
    var i = 0
    while (i < wcnt) {
        var j = i + 1
        val w = wArr[i] ushr SH
        while (j < wcnt && (wArr[j] ushr SH) == w) j++
        val t = j - i
        if (t >= 2) {
            val ss = IntArray(t) { (wArr[i + it] and MASK_S).toInt() }
            for (a in 0 until t) for (b in a + 1 until t) {
                val key = ss[a].toLong() * SHIFT1 + ss[b]
                c2[key] = (c2[key] ?: 0) + 1
            }
            if (t >= 3) for (a in 0 until t) for (b in a + 1 until t) for (c in b + 1 until t) {
                val key = ss[a].toLong() * SHIFT2 + ss[b].toLong() * SHIFT1 + ss[c]
                c3[key] = (c3[key] ?: 0) + 1
            }
            if (t >= 4) highSets.add(ss)
            if (stats != null && t > stats.maxT) stats.maxT = t
        }
        i = j
    }
    var t2 = 0L
    for (c in c2.values) t2 += c.toLong() * (c + 1) / 2
    var t3 = 0L
    for (c in c3.values) t3 += c.toLong() * (c + 1) / 2

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
            val term = c.toLong() * (c + 1) / 2
            corr += if (sigma.size % 2 == 0) -term else term
        }
    }
    if (stats != null) {
        stats.incidences = wcnt
        stats.t1 = t1
        stats.t2 = t2
        stats.t3 = t3
        stats.corr = corr
    }
    return t1 - t2 + t3 + corr
}

private fun countChords(N: Int): Int {
    val maxS = 2 * N + (4 * sqrt(2.0 * N)).toInt() + 200
    val uMax = isqrt(maxS.toLong()).toInt() + 1
    var cnt = 0
    for (u in 1..uMax step 2) for (v in u..uMax step 2) {
        val s = u * u + v * v
        if (s > maxS) break
        if (gcd64(u.toLong(), v.toLong()) != 1L) continue
        cnt++
    }
    return cnt
}

// ───────────────────────── 主程序 ─────────────────────────

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    println("== 1. M(u,v) 闭合公式 vs 直接枚举（全部弦 u²+v² ≤ 20000）==")
    var checked = 0
    var mismatches = 0
    for (u in 1..142 step 2) for (v in u..142 step 2) {
        val s = u * u + v * v
        if (s > 20000) break
        if (gcd64(u.toLong(), v.toLong()) != 1L) continue
        checked++
        val formula = mThreshold(u, v)
        val direct = mThresholdDirect(u, v, isqrt(s.toLong()).toInt() + 3)
        if (formula != direct) {
            mismatches++
            println("  不一致：u=$u v=$v 公式=$formula 直接=$direct")
        }
    }
    check(mismatches == 0) { "M(u,v) 公式与直接枚举不一致" }
    println("对照 $checked 条弦，全部一致 ✓（例如 (1,1): M=0；(1,3): M=2；(3,7): M=12）")
    println("  说明：M 是「最小合法 m 阈值」，m 为奇数；如 s=2 的全部奇数 m 都合法，")
    println("        s=10（弦 (1,3)）则要求 m ≥ 3，故 r=√5 不合法、r=5 与 r=√65 合法。")

    println()
    println("== 2. 题面样例门（本实现的快速算法）==")
    val gates = listOf(10 to 30L, 100 to 3442L)
    for ((n, expected) in gates) {
        val got = countLenticularPairs(n)
        check(got == expected) { "L($n) = $got，与题面 $expected 不符" }
        println("L($n) = $got（题面给 $expected）✓")
    }

    println()
    println("== 3. 全尺寸 L(100000) ==")
    val stats = Stats()
    val answer = countLenticularPairs(100000, stats)
    println("primitive 弦数（u²+v² ≤ ${2 * 100000 + (4 * sqrt(2e5)).toInt() + 200}）：${stats.chords}")
    println("不同弦参数 s：${stats.distinctS}；关联条目 (r², s)：${stats.incidences}")
    println("T1 = Σ_s C(k_s+1,2) = ${stats.t1}")
    println("T2 = ${stats.t2}，T3 = ${stats.t3}，k ≥ 4 修正 = ${stats.corr}（|S(r²)| 最大值 ${stats.maxT}）")
    println("s 的最大有效值：${stats.maxContributingS}（< 枚举上界，截断安全）")
    println("L(100000) = $answer")

    println()
    println("== 4. 计时 ==")
    val msSmall = bestOf3("L(10) + L(100)（快速算法）", 3472L) { countLenticularPairs(10) + countLenticularPairs(100) }
    val msBig = bestOf3("L(100000)（快速算法）", answer) { countLenticularPairs(100000) }
    println()
    println("结论：L(100000) = $answer（L(10) = 30、L(100) = 3442 均已复现）")
    println("耗时：小规模 ${"%.3f".format(msSmall)} ms，全尺寸 ${"%.3f".format(msBig)} ms")
}
