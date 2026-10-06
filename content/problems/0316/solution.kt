#!/usr/bin/env kotlin
/**
 * Project Euler 316 — Numbers in Decimal Expansions（小红数展开中的数）
 *
 * 题目：随机无限数字序列中，模式串 n 的十进制写法首次出现的【起始下标】期望记作 g(n)。
 *       已知 Σ_{n=2}^{999} g(⌊10^6/n⌋) = 27280188，求 Σ_{n=2}^{999999} g(⌊10^16/n⌋)。
 *
 * 思路推导
 * --------
 * 模式串 s（长度 d）首次出现的【结束时刻】的期望等于 Σ_{k ∈ borders} 10^k，
 * 其中 borders 是「既是前缀又是后缀」的长度集合（含 k = d）。
 * 这是模式匹配的经典结论：等待 s 出现的过程里，s 的每一次「自重叠」都要额外付出一段 10^k 的
 * 期望代价，可由可选停止定理（对赌 / 鞅）严格推出。
 * 题面定义的是起始下标 k，比结束时刻早 d-1，于是
 *
 *       g(n) = ( Σ_{k ∈ borders(s)} 10^k ) - d + 1 。
 *
 * 自检：s = 535 的 border 是 k=1（"5"）与 k=3（整串），g = (10 + 1000) - 3 + 1 = 1008，与题面一致。
 *
 * border 链就是 KMP 前缀函数 pi 的「回溯链」：k = d, pi[d-1], pi[pi[d-1]-1], … 直到 0，
 * 一趟走完全部 border 长度，O(d)。
 *
 * 目标：v = ⌊10^16 / n⌋，n 从 2 到 999999，v 的十进制长度在 11..16 之间。
 *       总和约 5.4e17，超过 Int，全程用 Long。
 *
 * 验证
 * --------
 * 1. 题面样例：g(535) = 1008；
 * 2. 题面给的中间结论：Σ_{n=2}^{999} g(⌊10^6/n⌋) = 27280188；
 * 3. 双方法互证：KMP pi 链 vs 朴素「逐 k 比较前缀 / 后缀」，在 n <= 10^4（两个指数下）全量比较。
 *
 * 复杂度：KMP 版 O(Σ d) ≈ O(1e6 * 16)；朴素版 O(Σ d^2)，每项多一个 d 的因子。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

/** 10 的幂，最多用到 16 位。 */
private val POW10 = LongArray(25).also { var v = 1L; for (i in it.indices) { it[i] = v; v *= 10 } }

private const val POW16 = 10_000_000_000_000_000L
private const val POW6 = 1_000_000L

/** 题面上界 n。 */
private const val N_MAX = 999_999

/** 暴力对照覆盖的 n 个数（与优化法同规模，便于逐项对照耗时）。 */
private const val BRUTE_N = N_MAX

/** 互证覆盖的 n 个数。 */
private const val CROSS_N = 10_000

/** 复用的前缀函数缓冲（v 最多 16 位）。 */
private val PI_BUF = IntArray(25)

/** KMP 前缀函数，写进共享缓冲，零分配。 */
private fun prefixFunction(s: String, pi: IntArray) {
    val n = s.length
    pi[0] = 0
    var j = 0
    for (i in 1 until n) {
        while (j > 0 && s[i] != s[j]) j = pi[j - 1]
        if (s[i] == s[j]) j++
        pi[i] = j
    }
}

/** 优化法：沿 pi 链走完所有 border（含自身长度 d）。 */
private fun gKmp(v: Long): Long {
    val s = v.toString()
    val d = s.length
    val pi = PI_BUF
    prefixFunction(s, pi)
    var sum = 0L
    var k = d
    while (k > 0) {
        sum += POW10[k]
        k = pi[k - 1]
    }
    return sum - d + 1
}

/** 暴力法：朴素地逐个长度 k 比较前缀与后缀是否相同。 */
private fun gNaive(v: Long): Long {
    val s = v.toString()
    val d = s.length
    var sum = 0L
    for (k in 1..d) {
        var same = true
        for (i in 0 until k) if (s[i] != s[d - k + i]) { same = false; break }
        if (same) sum += POW10[k]
    }
    return sum - d + 1
}

/** Σ_{n=2}^{hi} g(⌊basePow/n⌋)。 */
private fun total(basePow: Long, hi: Int, g: (Long) -> Long): Long {
    var sum = 0L
    for (n in 2..hi) sum += g(basePow / n)
    return sum
}

/** border 长度列表（KMP pi 链）。 */
private fun bordersKmp(s: String): IntArray {
    val d = s.length
    val pi = IntArray(d)
    prefixFunction(s, pi)
    val out = IntArray(d)
    var m = 0
    var k = d
    while (k > 0) { out[m++] = k; k = pi[k - 1] }   // 必须另开数组：往 pi 里写会踩掉链上要读的项
    return out.copyOf(m)
}

/** border 长度列表（朴素比较）。 */
private fun bordersNaive(s: String): IntArray {
    val d = s.length
    val out = ArrayList<Int>()
    for (k in 1..d) {
        var same = true
        for (i in 0 until k) if (s[i] != s[d - k + i]) { same = false; break }
        if (same) out.add(k)
    }
    return out.toIntArray()
}

/** 预热 1 次后跑 runs 轮，返回毫秒中位数。 */
private inline fun medianMs(runs: Int = 5, body: () -> Long): Double {
    body()
    val ts = DoubleArray(runs)
    for (i in 0 until runs) {
        val s = System.nanoTime()
        body()
        ts[i] = (System.nanoTime() - s) / 1_000_000.0
    }
    ts.sort()
    return ts[runs / 2]
}

fun main() {
    // 1) 题面样例 g(535)
    val g535 = gKmp(535)
    println("g(535) = " + g535 + (if (g535 == 1008L) " -> 与题面 1008 一致" else " -> 与题面不一致!"))
    println("  535 的 border：k=3（整串 535）与 k=1（前缀 5 = 后缀 5）-> (10^1 + 10^3) - 3 + 1 = 1008")
    val g1231 = gKmp(1231)
    println("g(1231) = " + g1231 + "  (border: k=1 与 k=4 -> (10 + 10000) - 4 + 1)")
    val g100 = gKmp(100)
    println("g(100)  = " + g100 + "  (只有 k=3 整串是 border -> 10^3 - 3 + 1 = 998)")

    // 2) 题面给的中间结论
    val known = total(POW6, 999) { gKmp(it) }
    println()
    println("Σ_{n=2}^{999} g(⌊10^6/n⌋) = " + known + (if (known == 27280188L) " -> 与题面 27280188 一致" else " -> 与题面不一致!"))

    // 3) 双方法互证
    println()
    println("双方法互证（KMP pi 链 vs 朴素前缀 / 后缀比较）：")
    var ok6 = true
    for (n in 2..CROSS_N) {
        val v = POW6 / n
        if (gKmp(v) != gNaive(v)) { ok6 = false; println("  10^6 不一致 n=" + n + " kmp=" + gKmp(v) + " naive=" + gNaive(v)) }
    }
    println("  指数 6、n<=10^4：全部一致 = " + ok6)
    var ok16 = true
    for (n in 2..CROSS_N) {
        val v = POW16 / n
        if (gKmp(v) != gNaive(v)) { ok16 = false; println("  10^16 不一致 n=" + n + " kmp=" + gKmp(v) + " naive=" + gNaive(v)) }
    }
    println("  指数 16、n<=10^4：全部一致 = " + ok16)
    val k999 = total(POW16, 999, ::gKmp)
    val n999 = total(POW16, 999, ::gNaive)
    println("  10^16、n<=999 的合计：KMP=" + k999 + "，朴素=" + n999 + (if (k999 == n999) " -> 一致" else " -> 不一致!"))

    // 数位长度分布（人工复核用）
    val lenHist = IntArray(20)
    for (n in 2..N_MAX) lenHist[(POW16 / n).toString().length]++
    println("  ⌊10^16/n⌋ 的十进制长度分布：" + (2..19).filter { lenHist[it] > 0 }.joinToString { it.toString() + " 位:" + lenHist[it] })

    // 4) 计时
    val bruteMs = medianMs { total(POW16, BRUTE_N, ::gNaive) }
    val optMs = medianMs { total(POW16, N_MAX, ::gKmp) }
    println()
    println("BRUTE_MS: " + "%.3f".format(bruteMs) + "  (朴素前后缀比较，n<= " + BRUTE_N + ")")
    println("OPT_MS: " + "%.3f".format(optMs) + "  (KMP pi 链，n<= " + N_MAX + ")")
    println("同规模加速比 = " + "%.2f".format(bruteMs / optMs) + "x")

    // d<=16 时朴素法的「失配即退出」把 O(d^2) 压得很扁。用 border 最密的串把两个规模拉开看：
    // s = ("12" 重复 k 次) 的 border 是全部偶数长度，朴素法必须比满 d^2/2 次，KMP 仍是 O(d)。
    println()
    println("规模标度探针（串 = \"12\" 重复 k 次，border 数为 k，两法结果逐元素比较）：")
    val probeKs = intArrayOf(250, 500, 1_000, 2_000, 4_000, 8_000)
    val probeSs = probeKs.map { kk -> buildString { for (i in 0 until kk) append("12") } }
    for (i in probeSs.indices) {                    // 先统一预热，避免首个规模被 JIT 编译吃掉
        repeat(3) { bordersKmp(probeSs[i]); bordersNaive(probeSs[i]) }
        check(bordersKmp(probeSs[i]).contentEquals(bordersNaive(probeSs[i]).reversedArray())) { "border 列表不一致" }
    }
    for (i in probeSs.indices) {
        val s = probeSs[i]
        val nb = bordersKmp(s).size
        val tk = medianMs { bordersKmp(s).size.toLong() }
        val tn = medianMs { bordersNaive(s).size.toLong() }
        println("  d=" + s.length + "：KMP=" + "%.3f".format(tk) + " ms，朴素=" + "%.3f".format(tn)
            + " ms，加速比=" + "%.1f".format(tn / tk) + "x，border 数一致=" + nb)
    }

    // 5) 答案
    val ans = total(POW16, N_MAX, ::gKmp)
    println()
    println("Σ_{n=2}^{999999} g(⌊10^16/n⌋) = " + ans)
    println("ANSWER: " + ans)
}
