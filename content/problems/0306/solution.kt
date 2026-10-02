#!/usr/bin/env kotlin
/**
 * Project Euler 306 — Paper-strip Game（纸带游戏）
 *
 * 题目：n 个白格排成一条纸带，两人轮流挑两个相邻白格涂黑，无法行动者输。
 * 求 1 ≤ n ≤ 10^6 中先手必胜的 n 的个数（题面给 n≤5 有 3 个、n≤50 有 40 个）。
 *
 * 思路推导
 * ────────
 * 1) 局面化归：一次走法把长度 n 的连续白格段切成左右两段（长 i 与 n-2-i），
 *    两段此后互不影响（黑格不可跨越），故可用 Sprague–Grundy 定理对两段异或求和：
 *
 *        g(0) = g(1) = 0
 *        g(n) = mex{ g(i) ⊕ g(n-2-i) : 0 ≤ i ≤ n-2 }   (n ≥ 2)
 *
 *    先手必胜 ⟺ g(n) ≠ 0。
 *
 * 2) 直接递推是 O(N²)，10^6 不可行。Dawson's Kayles（八进制游戏 0.07）的
 *    Guý–Smith 周期性定理指出 g 最终以 p = 34 为周期。本文件不引用该结论的数值，
 *    而是自己从递推表里搜出 (N0, p)：对每个候选 p 扫出最后一个不满足
 *    g(i)=g(i+p) 的下标 L(p)，令 N0 = L(p)+1，再取使 N0 最小的那个 p，
 *    并在整段窗口上逐点复核。
 *
 * 3) 计数：[1, N0-1] 直接数掉；尾段 [N0, L] 按 len = L-N0+1、周期 p 闭式计数——
 *    整周期 len div p 个，各贡献 S = Σ_{j<p} [g(N0+j) ≠ 0]，
 *    余数 len mod p 个按前 rem 个残类再贡献一次。整体 O(N0 + p)，与 L 无关。
 *
 * 验证方法
 * ────────
 * 1. 题面样例：n≤5 必胜 3 个、n≤50 必胜 40 个（闭式计数在 limit=5/50 上跑）；
 * 2. 双方法互证：brute 直接 mex DP 到 n=20000（O(N²)），optimized 的
 *    (N0,p) 表按周期把 n 折回 [N0, N0+p-1] 外推，在 n∈[N0, 20000] 上逐点
 *    比对——同时也是「g(n+34)=g(n) 在长窗口上成立」的实测证据（窗口大小见下）；
 * 3. brute 与 optimized 在同一 limit=20000 上互比必胜数一致，
 *    optimized 再独立跑到 limit=10^6 出最终答案。
 *
 * 复杂度：brute O(N²)（N=20000）；optimized O(M²) 找周期（M=4000）+ O(N0+p) 计数。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

private const val BRUTE_MAX = 20000     // brute 对照规模：mex DP 直接到 20000
private const val TABLE_MAX = 4000      // optimized 找周期用的 DP 规模
private const val PREPERIOD_MAX = 1000  // 允许的最大前导段长度 N0
private const val LIMIT = 1_000_000

/** Dawson's Kayles 的 SG 函数表：g(n) = mex{ g(i) xor g(n-2-i) }，g(0)=g(1)=0。 */
private fun grundyTable(maxN: Int): IntArray {
    val g = IntArray(maxN + 2)
    val seen = BooleanArray(128) // g 的实测值 < 16，异或结果 < 128，开够即可
    for (n in 2..maxN) {
        val rest = n - 2
        for (i in 0..rest) seen[g[i] xor g[rest - i]] = true
        var m = 0
        while (seen[m]) m++
        g[n] = m
        for (i in 0..rest) seen[g[i] xor g[rest - i]] = false // 现算现清
    }
    return g
}

/** 最后一个满足 g(i) != g(i+p) 的下标；没有则返回 0（即前导段长度为 1）。 */
private fun lastMismatch(g: IntArray, maxN: Int, p: Int): Int {
    var last = 0
    for (i in 1..maxN - p) if (g[i] != g[i + p]) last = i
    return last
}

/** 找出 (N0, p)：所有候选周期里前导段最短的那个。 */
private fun findPeriod(g: IntArray, maxN: Int): Pair<Int, Int> {
    for (p in 1..64) {
        val start = lastMismatch(g, maxN, p) + 1
        if (start <= PREPERIOD_MAX) return start to p
    }
    error("前导段 ≤ " + PREPERIOD_MAX + " 内未找到周期（表长 " + maxN + " 不足）")
}

/** 给定 (g, N0, p)，用周期闭式数出 1..limit 中先手必胜的个数。 */
private fun countByPeriod(g: IntArray, start: Int, p: Int, limit: Int): Long {
    // limit 小于前导段（如题面样例 n≤5）时，闭式段为空，整段直接数
    val eff = if (limit < start) limit + 1 else start
    var count = 0L
    for (n in 1 until eff) if (g[n] != 0) count++ // 前导段直接数

    var perPeriod = 0
    for (j in 0 until p) if (g[eff + j] != 0) perPeriod++

    val len = limit - eff + 1                       // 尾段长度
    var tail = 0L
    if (len > 0) {
        tail = (len / p).toLong() * perPeriod
        for (j in 0 until len % p) if (g[eff + j] != 0) tail++
    }
    return count + tail
}

/**
 * 统计 1..limit 中先手必胜的 n 个数：先 mex DP 到 TABLE_MAX 搜周期，
 * 再按周期闭式计数。verbose=true 时打印周期、复核窗口与样例核对（计时请传 false）。
 */
private fun solveOptimized(limit: Int, verbose: Boolean = false): Long {
    val g = grundyTable(TABLE_MAX)
    val (start, p) = findPeriod(g, TABLE_MAX)

    // 在窗口 [start, TABLE_MAX-p] 上逐点复核 g(n+p)=g(n)
    var checked = 0
    for (n in start..TABLE_MAX - p) {
        check(g[n] == g[n + p]) { "周期复核失败于 n=" + n }
        checked++
    }

    val ans = countByPeriod(g, start, p, limit)
    var perPeriod = 0
    for (j in 0 until p) if (g[start + j] != 0) perPeriod++

    if (verbose) {
        println("周期：N0=" + start + ", p=" + p +
                "（复核窗口 n∈[" + start + ", " + (TABLE_MAX - p) + "]，" + checked + " 个点全对）")
        println("周期块内先手必胜残类：" + perPeriod + " / " + p)
        println("样例核对 n≤5：" + solveOptimized(5, false) + "（题面 3）")
        println("样例核对 n≤50：" + solveOptimized(50, false) + "（题面 40）")
    }
    return ans
}

/** brute 对照：直接 mex DP 到 maxN，返回该范围内的必胜个数。 */
private fun solveBrute(maxN: Int): Long {
    val g = grundyTable(maxN)
    var c = 0L
    for (n in 1..maxN) if (g[n] != 0) c++
    return c
}

/** 预热一次后连跑若干次取中位数（毫秒）。 */
private fun medianMs(runs: Int, block: () -> Long): Double {
    block()
    val ts = DoubleArray(runs)
    for (i in 0 until runs) {
        val t0 = System.nanoTime()
        block()
        ts[i] = (System.nanoTime() - t0) / 1_000_000.0
    }
    ts.sort()
    return ts[runs / 2]
}

fun main() {
    val answer = solveOptimized(LIMIT, verbose = true)
    val gHead = grundyTable(60)
    val head = StringBuilder("g(0..60) =")
    for (i in 0..60) head.append(' ').append(gHead[i])
    println(head)

    // 双方法互证：brute 的 SG 表 vs optimized 周期外推
    val gBrute = grundyTable(BRUTE_MAX)
    val gSmall = grundyTable(TABLE_MAX)
    val per = findPeriod(gSmall, TABLE_MAX)
    val start = per.first
    val p = per.second
    var window = 0
    for (n in start..BRUTE_MAX) {
        val ext = gSmall[start + (n - start) % p]   // 用周期把 n 折回一个周期内
        check(gBrute[n] == ext) { "外推与 brute 在 n=" + n + " 处不一致" }
        window++
    }
    val bruteCount = solveBrute(BRUTE_MAX)
    val optAtBruteMax = solveOptimized(BRUTE_MAX)
    println("周期外推 vs brute DP：窗口 n∈[" + start + ", " + BRUTE_MAX + "]，" +
            window + " 个点全对")
    println("n≤" + BRUTE_MAX + " 必胜数：brute=" + bruteCount + ", optimized=" + optAtBruteMax +
            " → " + (if (bruteCount == optAtBruteMax) "一致 ✓" else "不一致 ✗"))

    val bruteMs = medianMs(5) { solveBrute(BRUTE_MAX) }
    val optMs = medianMs(5) { solveOptimized(LIMIT) }
    val countMs = medianMs(5) { countByPeriod(gSmall, start, p, LIMIT) }
    println("BRUTE_MS: " + "%.3f".format(bruteMs) + "  (mex DP 到 n=" + BRUTE_MAX + ")")
    println("OPT_MS: " + "%.3f".format(optMs) + "  (周期搜索 DP(4000) + 闭式计数到 n=" + LIMIT + ")")
    println("  其中纯计数部分 " + "%.3f".format(countMs) + " ms，余下均为找周期的一次性 DP")
    println("ANSWER: " + answer)
}
