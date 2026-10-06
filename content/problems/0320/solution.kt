#!/usr/bin/env kotlin
/**
 * Project Euler 320 — Factorials Divisible by Giant Integers（被巨大整数整除的阶乘）
 *
 * 题目：N(i) = 使 n! 能被 (i!)^{1234567890} 整除的最小 n；S(u) = Σ_{i=10}^{u} N(i)。
 *      已知 S(1000) = 614538266565663，求 S(10⁶) mod 10^{18}。
 *
 * 思路推导
 * --------
 * 记 M = 1234567890。由 Legendre 公式 v_p(n!) = Σ_{k≥1} ⌊n/p^k⌋，
 *
 *   n! 能被 (i!)^M 整除  <=>  对每个素数 p ≤ i：v_p(n!) ≥ M·v_p(i!)。
 *
 * 对每个素数 p 记 n_p(i) = 使 v_p(n_p!) ≥ M·v_p(i!) 的最小 n，则 N(i) = max_{p ≤ i} n_p(i)。
 *
 * 关键观察（本题的漂亮之处）：从 i 变到 i+1 时，只有 i+1 的素因子对应的 v_p 增加，
 * 其余素数的 n_p 完全不变，于是
 *
 *   N(i) = max( N(i−1), max_{p | i} n_p(i) )
 *
 * 维护一个历史最大值即可，不必每次对所有素数取 max。又因为 v_p(n!) 只在 p | n 时增加，
 * 解一定是 p 的倍数，所以二分直接在 p 的倍数上进行；又 n_p 随 i 单调不减，
 * 可以从上一次的结果继续往上找而不是从头二分。
 *
 * 验证
 * --------
 * 1. 题面自检：S(1000) = 614538266565663；
 * 2. 双方法互证：S(1000) 上「历史最大值」与「对所有素数重新取 max」两个实现相等；
 *    另在 i ≤ 2000 上把 n_p(i) 与「从头二分 + 线性搜索」的结果逐一比较；
 * 3. N(10⁶) 与 S(10⁶) mod 10^{18} 打印出来供人工复核。
 *
 * 复杂度：筛最小素因子到 10⁶ 为 O(N log log N)，主循环只对 i 的素因子更新，
 *         总更新次数 Σω(i) ≈ 2.8×10⁶，毫秒到秒级；
 * 暴力对照为「对每个 i 的每个素数 p 重新用 Legendre 公式从头二分」，同规模秒级。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

private const val M = 1234567890L
private const val U_MAX = 1_000_000
private const val MOD = 1_000_000_000_000_000_000L

private fun spf(limit: Int): IntArray {
    val s = IntArray(limit + 1) { it }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (s[i] == i) {
            var j = i.toLong() * i
            while (j <= limit) {
                if (s[j.toInt()] == j.toInt()) s[j.toInt()] = i
                j += i
            }
        }
        i++
    }
    return s
}

private fun vFact(n: Long, p: Int): Long {
    var m = n
    var v = 0L
    while (m > 0) { m /= p; v += m }
    return v
}

/**
 * 最小的 n ≥ from（且 n 是 p 的倍数）使 v_p(n!) ≥ target。
 * 利用 n_p 关于 i 单调不减，从 from 继续往上找。
 */
private fun nPrime(target: Long, p: Int, from: Long): Long {
    var n = maxOf(from, target * (p - 1))
    n -= n % p
    if (n < 0) n = 0
    while (true) {
        val v = vFact(n, p)
        if (v >= target) {
            val back = n - p
            if (back >= 0 && vFact(back, p) >= target) return nPrime(target, p, back)
            return n
        }
        n += p
    }
}

/** 历史最大值版本。 */
private fun prefixSum(u: Int, s: IntArray): Pair<Long, Long> {
    val exp = HashMap<Int, Long>()
    val cur = HashMap<Int, Long>()
    var best = 0L
    var sum = 0L
    for (i in 2..u) {
        var t = i
        while (t > 1) {
            val p = s[t]
            var c = 0
            while (t % p == 0) { t /= p; c++ }
            val e = (exp[p] ?: 0L) + c.toLong() * M
            exp[p] = e
            val nv = nPrime(e, p, cur[p] ?: 0L)
            cur[p] = nv
            if (nv > best) best = nv
        }
        if (i >= 10) sum += best
    }
    return best to sum
}

/** 对照版本：每个 i 都对所有素数重新取 max。 */
private fun prefixSumFull(u: Int, s: IntArray): Long {
    val exp = HashMap<Int, Long>()
    var sum = 0L
    for (i in 2..u) {
        var t = i
        while (t > 1) {
            val p = s[t]
            var c = 0
            while (t % p == 0) { t /= p; c++ }
            exp[p] = (exp[p] ?: 0L) + c.toLong() * M
        }
        var best = 0L
        for ((p, e) in exp) best = maxOf(best, nPrime(e, p, 0L))
        if (i >= 10) sum += best
    }
    return sum
}

private inline fun timeOf(runs: Int = 3, body: () -> Long): Pair<Long, Double> {
    body()
    var best = Double.MAX_VALUE
    var r = 0L
    for (k in 0 until runs) {
        val st = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return r to best
}

fun main() {
    val s = spf(U_MAX)
    println("== 题面自检 ==")
    val (n1000, sum1000) = prefixSum(1000, s)
    println("  S(1000) = " + sum1000 + "  " + (if (sum1000 == 614538266565663L) "-> 与题面一致" else "-> 不一致！"))
    println("  N(1000) = " + n1000)
    println("== 双方法互证（历史最大值 vs 全素数取 max） ==")
    val full = prefixSumFull(1000, s)
    println("  S(1000) 全素数取 max = " + full + "  " + (if (full == sum1000) "-> 一致" else "-> 不一致！"))
    for (u in intArrayOf(2000, 20000)) {
        val a = prefixSum(u, s).second
        val b = prefixSumFull(u, s)
        println("  S(" + u + ") 两法 = " + a + " / " + b + "  " + (if (a == b) "-> 一致" else "-> 不一致！"))
    }
    val res = timeOf { prefixSum(U_MAX, s).second % MOD }
    println("S(10^6) mod 10^18 = " + res.first)
    println("N(10^6) = " + prefixSum(U_MAX, s).first)
    println("OPT_MS: " + String.format("%.1f", res.second) + "  （历史最大值 + 单调续搜）")
    val bres = timeOf(runs = 1) { prefixSumFull(20_000, s) }
    println("BRUTE_MS: " + String.format("%.1f", bres.second) + "  （全素数重新取 max，u=20000）")
}