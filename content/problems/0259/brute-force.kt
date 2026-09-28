#!/usr/bin/env kotlin
/**
 * Project Euler 259 — Reachable Numbers（可达数）暴力对照
 *
 * 思路：与 solution.kt 的区间 DP 完全不同——直接按语法逐棵枚举并求值所有表达式树：
 *   · 数字串切成连续块（每块是一个拼接整数，恰好 2^{k−1} 种切法）；
 *   · 块序列上任意括号化（m 块有 Catalan(m−1) 种树形）；
 *   · 每个内部节点任选 + − × ÷ 之一（4^{m−1} 种）。
 *   枚举出的每棵树恰好被求值一次、且只被求值一次：没有任何「子串取值集合」中间缓存，
 *   只有最后在根节点收集去重的正整数——这正是与 DP 的分水岭。
 *
 *   表达式树总数（k 位前缀）= Σ_j C(k−1,j−1)·Catalan(j−1)·4^{j−1}：
 *   k = 6 时 64,469 棵；k = 7 时 859,385 棵；k = 8 时 11,853,949 棵；k = 9 时 167,763,361 棵。
 *   本文件默认跑到 k = 7（基线 91.8 ms）；k = 8 单次约 1.7 s；k = 9 单次约 46 s，
 *   可以手动开启作为「全规模暴力对照」（k = 9 跑一次即得与 DP 相同的最终答案 20101196798）。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar [k]（默认 7）
 */

import kotlin.math.abs

private fun gcdL(a: Long, b: Long): Long {
    var x = abs(a)
    var y = abs(b)
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

/** 根节点收集：既约分数 (num, den)，分子分母都塞进一个 Long 键去重。 */
private fun keyOf(num0: Long, den0: Long): Long {
    var num = num0
    var den = den0
    if (den < 0L) { num = -num; den = -den }
    if (num == 0L) den = 1L else { val g = gcdL(num, den); num /= g; den /= g }
    return (num shl 32) xor den
}

private fun sumPositiveIntegers(keys: HashSet<Long>): Pair<Long, Long> {
    var sum = 0L
    var count = 0L
    for (k in keys) {
        if ((k and 0xFFFFFFFFL) == 1L) {
            val num = k shr 32
            if (num > 0L) { sum += num; count++ }
        }
    }
    return sum to count
}

/** 规范化（既约、分母为正）后交给 out。 */
private inline fun emit(out: (Long, Long) -> Unit, num0: Long, den0: Long) {
    var num = num0
    var den = den0
    if (den < 0L) { num = -num; den = -den }
    if (num == 0L) den = 1L else { val g = gcdL(num, den); num /= g; den /= g }
    out(num, den)
}

/**
 * 枚举 s[lo,hi) 的全部表达式树，把根节点取值交给 out（num, den 已既约、分母为正）。
 * 第一支是「整段拼接成一个数」，其余支是「在某处切成左右两半，各枚举子树再组合」。
 */
private fun enumerateTrees(s: String, lo: Int, hi: Int, out: (Long, Long) -> Unit) {
    var chunk = 0L
    for (i in lo until hi) chunk = chunk * 10 + (s[i] - '0')
    out(chunk, 1L)
    if (hi - lo == 1) return
    for (m in lo + 1 until hi) {
        enumerateTrees(s, lo, m) { an, ad ->
            enumerateTrees(s, m, hi) { bn, bd ->
                emit(out, an * bd + bn * ad, ad * bd)       // +
                emit(out, an * bd - bn * ad, ad * bd)       // −
                emit(out, an * bn, ad * bd)                 // ×
                if (bn != 0L) emit(out, an * bd, ad * bn)   // ÷（除零分支直接剪掉）
            }
        }
    }
}

private fun bruteStats(k: Int): Pair<Long, Long> {
    val keys = HashSet<Long>()
    enumerateTrees("123456789".substring(0, k), 0, k) { n, d -> keys.add(keyOf(n, d)) }
    return sumPositiveIntegers(keys)
}

/** k 位前缀的表达式树总数。 */
private fun expressionTreeCount(k: Int): Long {
    val catalan = longArrayOf(1, 1, 2, 5, 14, 42, 132, 429, 1430)
    var total = 0L
    for (blocks in 1..k) {
        var binom = 1L
        for (t in 1..blocks - 1) binom = binom * (k - t) / t
        var pow4 = 1L
        repeat(blocks - 1) { pow4 *= 4 }
        total += binom * catalan[blocks - 1] * pow4
    }
    return total
}

fun main(args: Array<String>) {
    val kMax = if (args.isNotEmpty()) args[0].toInt() else 7

    println("brute-force（逐棵枚举全部表达式树），前缀长度 ≤ $kMax")
    for (k in 1..kMax) {
        val (sum, count) = bruteStats(k)
        println("  前缀长度 $k：表达式树 ${expressionTreeCount(k)} 棵，正可达整数 $count 个，和 = $sum")
    }

    // 计时：k ≤ 8 时 JIT 预热后 3 轮取最优；k = 9 单次即数十秒，只测一轮
    val rounds = if (kMax >= 9) 1 else 3
    if (rounds > 1) bruteStats(kMax)
    var best = Double.MAX_VALUE
    repeat(rounds) {
        val t0 = System.nanoTime()
        bruteStats(kMax)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
    }
    val tag = if (rounds == 1) "单次" else "${rounds} 次最优"
    println(
        "brute-force：${"%.1f".format(best)} ms" +
            "（k = $kMax，${"%,d".format(expressionTreeCount(kMax))} 棵表达式树，$tag）",
    )
}
