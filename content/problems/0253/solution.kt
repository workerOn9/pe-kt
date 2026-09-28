#!/usr/bin/env kotlin
/**
 * Project Euler 253 — Tidying Up A（整理毛毛虫）
 *
 * 思路
 * ────
 * 一次随机整理 = 一个均匀随机排列 t = (t_1, …, t_n)：t_p ∈ {1..n} 是第 p 号拼图被放下的时刻。
 * 放下 k 块后，已放集合 S_k = {p : t_p ≤ k} 的「段数」为
 *
 *     b(k) = k − #{ p ∈ [1, n−1] : max(t_p, t_{p+1}) ≤ k } ,
 *
 * 因为每一对相邻且都已放下的拼图恰好把两块并成一段（记 b(0)=0、b(n)=1）。要求的是
 * M = max_k b(k) 在全部 n! 个排列上的平均值，四舍五入到六位小数。
 *
 * b 是一条 Motzkin 型路径：b(k+1) − b(k) ∈ {−1, 0, +1}，三种步恰好对应
 *
 *     生段：新块与任何已放块都不相邻   b → b+1
 *     接边：新块恰与一个已放块相邻     b → b
 *     合段：新块填进长度为 1 的空隙     b → b−1
 *
 * 关键引理（本题核心）
 * ────────────────
 * 固定一条轨迹 b(1..n)（b(1)=1、b(n)=1、b ≥ 1、步差 ∈ {−1,0,1}），恰有
 *
 *     f(b) = Π_{k=1}^{n−1} w(第 k 步, h_k)，  h_k = b(k)，
 *     w(生段, h) = h+1 ，w(接边, h) = 2h ，w(合段, h) = h−1
 *
 * 个排列与之对应。直观：生段 = 把新段插进 h 个段之间的 h+1 个空档；接边 = 选 h 个段的
 * 2h 个端点之一；合段 = 选 h−1 对相邻段之一（「Motzkin 历史」编码）。等价地，若 a_h 为
 * 从高度 h 向上的步数、e_h 为高度 h 上的平步数，则 f(b) = Π_h (h(h+1))^{a_h}·(2h)^{e_h}。
 *
 * 于是 C(n,m) := #{排列 : M ≤ m} = 「高度限制在 1..m 的加权 Motzkin 路径总权」
 * （长度 n−1、起点与终点高度 1）；#{M = m} = C(n,m) − C(n,m−1)；
 * E[M] = (Σ m·#{M = m}) / n! 用精确有理数（BigInteger）算，再四舍五入。
 *
 * 验证（main 内联跑，全部实跑通过）
 * ────────────────────────────
 *   · 暴力对照：n ≤ 9 的全部 n! 个排列按定义模拟，M 分布与路径 DP 逐项一致；
 *   · 独立结构对照：n = 20、m = 1..10 的「子集格 DP」（O(2ⁿ·n)，完全不使用路径公式）
 *     与路径 DP 的 C(n,m) 逐项一致；
 *   · 第二独立路径（完整规模）：转移矩阵 T_m（对角 2h、上副对角 h+1、下副对角 h−1）满足
 *     C(n,m) = e_1ᵀ T_m^{n−1} e_1，由 Cayley–Hamilton 得 m 阶线性递推；特征多项式
 *     χ_m = det(xI − T_m) 用三对角递推 χ_0 = 1、χ_1 = x−2、χ_k = (x−2k)χ_{k−1} − k(k−1)χ_{k−2}
 *     精确求出。两条路径对 m = 1..20、n = 40 的 C(40,m) 完全一致。
 *     （χ_m(x) = Σ_{j=0}^m (−1)^j·m(m+1)(j−1)!·N(m,j)·x^{m−j}，N 为 Narayana 数。）
 *   · 题面样例：n = 10 的分布 512 / 250912 / 1815264 / 1418112 / 144000 与平均值
 *     385643/113400 = 3.400732 复现；n = 40 的众数实算为 11，与题面「最可能是 11」一致。
 *
 * 答案：11.492847（六位小数）。仓库编码惯例（六位小数用 10⁶）：
 *       meta.answer = round(E×10^6)×100 = 1149284700（后两位恒为 0）。
 * 复杂度：路径 DP O(n·m) 次大整数运算（n=40、m ≤ 20）；子集格 DP O(2ⁿ·n)；暴力 O(n!·n)。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

private const val N = 40
private val TWO: BigInteger = BigInteger.TWO

private fun factorial(k: Int): BigInteger {
    var r = BigInteger.ONE
    for (i in 2..k) r = r * BigInteger.valueOf(i.toLong())
    return r
}

// ------------------------------------------------------------------ 方法 A：加权 Motzkin 路径 DP

/**
 * C(n, m) = #{M ≤ m} = 高度限制在 1..m 的加权 Motzkin 路径总权（长度 n−1，起止高度 1）。
 * cur[h] = 当前步数下终点为高度 h 的权和；每步：生段 h→h+1 权 h+1、接边 h→h 权 2h、
 * 合段 h→h−1 权 h−1。
 */
private fun countLeqByPaths(n: Int, m: Int): BigInteger {
    if (m <= 0) return BigInteger.ZERO
    if (n <= 1) return BigInteger.ONE
    if (n <= 2 * m) return factorial(n)          // 恒有 M ≤ ⌈n/2⌉，无需受限 DP
    var cur = Array(m + 2) { BigInteger.ZERO }
    cur[1] = BigInteger.ONE
    repeat(n - 1) {
        val next = Array(m + 2) { BigInteger.ZERO }
        for (h in 1..m) {
            val w = cur[h]
            if (w.signum() == 0) continue
            if (h + 1 <= m) next[h + 1] += w * BigInteger.valueOf((h + 1).toLong())
            next[h] += w * BigInteger.valueOf(2L * h)
            if (h - 1 >= 1) next[h - 1] += w * BigInteger.valueOf((h - 1).toLong())
        }
        cur = next
    }
    return cur[1]
}

// ------------------------------------------------------------------ 方法 B：特征多项式 + 线性递推

/** χ_m(x) = det(xI − T_m) 的系数（低次在前，c[m] = 1）；三对角递推精确整数运算。 */
private fun charPolyTridiagonal(m: Int): Array<BigInteger> {
    var prev = arrayOf(BigInteger.ONE)                        // χ_0 = 1
    if (m == 0) return prev
    var cur = arrayOf(BigInteger.valueOf(-2L), BigInteger.ONE) // χ_1 = x − 2
    for (k in 2..m) {
        val t = Array(cur.size + 1) { BigInteger.ZERO }
        val twoK = BigInteger.valueOf(2L * k)
        val kk1 = BigInteger.valueOf(k.toLong() * (k - 1))
        for (i in cur.indices) {
            t[i + 1] += cur[i]                // × x
            t[i] -= cur[i] * twoK             // − 2k
        }
        for (i in prev.indices) t[i] -= prev[i] * kk1   // − k(k−1)，低次项
        prev = cur
        cur = t
    }
    return cur
}

/**
 * 方法 B：C(·, m) 满足 χ_m 给出的 m 阶线性递推
 *     a_k = −Σ_{j=1..m} c_{m−j} a_{k−j}     (k > m)，
 * 种子 a_k = k!（k ≤ m，此时限制 1..m 不可能被突破）。
 */
private fun countLeqByRecurrence(n: Int, m: Int): BigInteger {
    if (m <= 0) return BigInteger.ZERO
    if (n <= 1) return BigInteger.ONE
    if (n <= m) return factorial(n)
    val c = charPolyTridiagonal(m)
    val a = Array(n + 1) { BigInteger.ZERO }
    for (k in 1..m) a[k] = factorial(k)
    for (k in m + 1..n) {
        var s = BigInteger.ZERO
        for (j in 1..m) s -= c[m - j] * a[k - j]
        a[k] = s
    }
    return a[n]
}

// ------------------------------------------------------------------ 独立结构对照：子集格 DP（n 小）

/**
 * 完全按定义：C(n, m) = 满足「链上每个前缀的段数 ≤ m」的极大链数。
 * G[S] = [blocks(S) ≤ m] · Σ_{p∈S} G[S∖{p}]，G[∅] = 1，答案 = G[全集]；
 * blocks(S) = |S| − #相邻对 = popcount(S) − popcount(S & (S≫1))。n ≤ 20 时用 Long 足够。
 */
private fun countLeqBySubsetDP(n: Int, m: Int): Long {
    val size = 1 shl n
    val g = LongArray(size)
    val pop = IntArray(size)
    for (s in 1 until size) pop[s] = pop[s shr 1] + (s and 1)
    g[0] = 1L
    for (s in 1 until size) {
        val blocks = pop[s] - pop[s and (s shr 1)]
        if (blocks > m) continue
        var acc = 0L
        var t = s
        while (t != 0) {
            val p = t and (-t)
            acc += g[s xor p]
            t = t xor p
        }
        g[s] = acc
    }
    return g[size - 1]
}

// ------------------------------------------------------------------ 暴力对照：全排列按定义模拟

/** 枚举 n! 个排列，按定义模拟每个排列的段数轨迹，返回 M 的分布（下标即 M）。 */
private fun bruteDistribution(n: Int): LongArray {
    val dist = LongArray(n + 2)
    val perm = IntArray(n)
    val used = BooleanArray(n + 1)
    fun rec(k: Int) {
        if (k == n) {
            var bits = 0
            var blocks = 0
            var mx = 0
            for (t in 0 until n) {
                val p = perm[t] - 1
                val left = if (p > 0) (bits shr (p - 1)) and 1 else 0
                val right = if (p + 1 < n) (bits shr (p + 1)) and 1 else 0
                when (left + right) {
                    0 -> blocks += 1
                    2 -> blocks -= 1
                }
                bits = bits or (1 shl p)
                if (blocks > mx) mx = blocks
            }
            dist[mx]++
            return
        }
        for (v in 1..n) {
            if (used[v]) continue
            used[v] = true
            perm[k] = v
            rec(k + 1)
            used[v] = false
        }
    }
    rec(0)
    return dist
}

// ------------------------------------------------------------------ 期望与编码

/** 用给定的 C(n,m) 计数器算 n=40 的期望并编码：round(E×10⁶)×100。 */
private fun expectationEncoded(count: (Int, Int) -> BigInteger): BigInteger {
    val mMax = (N + 1) / 2
    val total = factorial(N)
    var prev = BigInteger.ZERO
    var weighted = BigInteger.ZERO
    for (m in 1..mMax) {
        val c = count(N, m)
        weighted += (c - prev) * BigInteger.valueOf(m.toLong())
        prev = c
    }
    check(prev == total) { "Σ #{M=m} 应等于 40!" }
    val scaled = weighted * BigInteger.valueOf(1_000_000L)
    val (q, r) = scaled.divideAndRemainder(total)
    val micro = if (r * TWO >= total) q + BigInteger.ONE else q   // 四舍五入
    return micro * BigInteger.valueOf(100L)
}

private fun timeBest(label: String, block: () -> BigInteger): Double {
    block()                                   // 预热
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        val v = block()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
        check(v.signum() >= 0)
    }
    println("$label：${"%.3f".format(best)} ms（JIT 预热后 3 轮最优）")
    return best
}

// ------------------------------------------------------------------ main

fun main() {
    println("Project Euler 253 — Tidying Up A（整理毛毛虫）")

    // 1) 题面样例：n = 10 的 M 分布与平均值
    val expected10 = longArrayOf(512, 250912, 1815264, 1418112, 144000)
    var prev = BigInteger.ZERO
    var sum10 = BigInteger.ZERO
    val cnt10 = ArrayList<BigInteger>()
    for (m in 1..5) {
        val c = countLeqByPaths(10, m)
        val cnt = c - prev
        cnt10.add(cnt)
        sum10 += cnt * BigInteger.valueOf(m.toLong())
        prev = c
    }
    check(cnt10.map { it.toLong() } == expected10.toList()) { "n=10 分布与题面不符：$cnt10" }
    val avg10 = BigDecimal(sum10).divide(BigDecimal(factorial(10)), 6, RoundingMode.HALF_UP)
    println("题面样例 n=10：分布 ${cnt10.joinToString(", ")}；平均 $avg10（题面 3.400732）✓")

    // 2) 暴力对照：n ≤ 9 全排列按定义模拟（不共用路径公式）
    for (n in intArrayOf(8, 9)) {
        val brute = bruteDistribution(n)
        var pre = BigInteger.ZERO
        for (m in 1..(n + 1) / 2) {
            val c = countLeqByPaths(n, m)
            check(brute[m].toLong() == (c - pre).toLong()) { "n=$n m=$m 暴力与 DP 不一致" }
            pre = c
        }
        println("暴力对照 n=$n：全部 ${n}! 个排列的 M 分布与路径 DP 一致 ✓")
    }

    // 3) 独立结构对照：n = 20 子集格 DP（O(2ⁿ·n)）vs 路径 DP
    for (m in 1..10) {
        val s = countLeqBySubsetDP(20, m)
        check(BigInteger.valueOf(s) == countLeqByPaths(20, m)) { "n=20 m=$m 子集 DP 与路径 DP 不一致" }
    }
    var subsetMs = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        countLeqBySubsetDP(20, 10)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < subsetMs) subsetMs = ms
    }
    println(
        "独立对照 n=20：子集格 DP（m=1..10）与路径 DP 逐项一致 ✓" +
            "（m=10 单次 %.0f ms，JIT 预热后 3 轮最优）".format(subsetMs),
    )

    // 4) 双方法互证 + n = 40 的完整分布
    val mMax = (N + 1) / 2
    val total = factorial(N)
    var prevC = BigInteger.ZERO
    var weighted = BigInteger.ZERO
    val counts = ArrayList<BigInteger>()
    for (m in 1..mMax) {
        val a = countLeqByPaths(N, m)
        val b = countLeqByRecurrence(N, m)
        check(a == b) { "m=$m：路径 DP $a ≠ 特征多项式递推 $b" }
        counts.add(a - prevC)
        weighted += (a - prevC) * BigInteger.valueOf(m.toLong())
        prevC = a
    }
    check(prevC == total) { "Σ #{M=m} ≠ 40!" }
    val mode = counts.indices.maxByOrNull { counts[it] }!! + 1
    println("n=40：M 的分布共 ${counts.size} 项、总和 = 40! ✓；众数 M = $mode（题面称最可能是 11）")

    // 5) 精确期望与六位小数编码
    val avg = BigDecimal(weighted).divide(BigDecimal(total), 20, RoundingMode.HALF_UP)
    val encodedA = expectationEncoded(::countLeqByPaths)
    val encodedB = expectationEncoded(::countLeqByRecurrence)
    check(encodedA == encodedB) { "两条路径的编码答案不一致：$encodedA vs $encodedB" }
    check(encodedA == BigInteger.valueOf(1_149_284_700L)) { "答案与预期不符：$encodedA" }
    val six = BigDecimal(weighted).divide(BigDecimal(total), 6, RoundingMode.HALF_UP)
    println("E[M] = ${avg.toPlainString()} …")
    println("六位小数 = ${six.toPlainString()}；编码答案 round(E×10⁶)×100 = $encodedA")

    // 6) 计时（JIT 预热后 3 轮取最优）
    timeBest("方法 A 路径 DP（n=40，全部 m + 精确期望）") { expectationEncoded(::countLeqByPaths) }
    timeBest("方法 B 特征多项式递推（n=40，全部 m + 精确期望）") {
        expectationEncoded(::countLeqByRecurrence)
    }
    println("check() 全部通过")
}
