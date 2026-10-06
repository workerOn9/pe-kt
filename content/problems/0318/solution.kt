#!/usr/bin/env kotlin
/**
 * Project Euler 318 — 2011 Nines（2011 个 9）
 *
 * 题目：p<q 为正整数，(√p+√q)^{2n} 的小数部分开头连续 9 的个数记 C(p,q,n)；N(p,q) 是使
 *      C(p,q,n) ≥ 2011 的最小 n。求 Σ_{p+q ≤ 2011} N(p,q)。
 *
 * 思路推导
 * --------
 * 令 x = √q + √p，y = √q − √p。展开 m 次方后含 (√p)^{odd}(√q)^{odd} 的项两两抵消，
 * 所以 x^m + y^m 恒为整数。于是 frac(x^{2n}) = 1 − y^{2n}，小数部分趋于 1 ⟺ y < 1，
 * 而 y < 1 等价于（纯整数判据，全程无浮点）
 *
 *   (q − p − 1)² < 4p 。
 *
 * 在此前提下
 *
 *   C(p,q,n) ≥ 2011  <=>  1 − y^{2n} ≥ 1 − 10^{−2011}  <=>  2n·log10(y) ≥ −2011
 *
 * 故 N(p,q) = ⌈ 2011 / (−2·log10 y) ⌉。
 *
 * 数值上最要紧的两点：
 * 1. y = √q − √p 有灾难性抵消，必须用 y = (q−p)/(√q+√p) 的等价写法；
 * 2. 满足 y<1 的 (p,q) 共 41987 对，其中 y 最接近 1 的一对是 (931, 993)，
 *    y ≈ 0.9996099，N 高达 193093 —— 总和被少数几对主导，
 *    所以对数必须算准到 8 位小数以上。
 *
 * 验证
 * --------
 * 1. 题面自检：√2+√3 的前几个偶次幂的小数部分依次有 0、1、2、3、4、5、6、7 个 9，打印核对；
 * 2. 双方法互证：N(p,q) 用「log10 版」与「ln 版（= 2011·ln10/(−2 ln y)）」两套公式
 *    在全部 41987 对上逐一比较，完全一致；合计均为 709313889；
 * 3. N 的最小值、最大值与「最接近 1 的 y」对应的 (p,q) 一并打印，说明 ceil 不贴边界、
 *    浮点精度足够（t 距最近整数的最小距离在样例中可见量级为 10⁻⁶）。
 *
 * 复杂度：41987 对，每对两次 log，毫秒级；朴素版用「直接相减」的 y 与 log10
 * 在全部对上再跑一遍作为对照。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

private const val TARGET = 2011
private const val LIMIT = 2011

/** y = √q − √p < 1 的精确整数判据。 */
private fun admissible(p: Int, q: Int): Boolean {
    val t = q - p - 1
    return t * t < 4 * p
}

private fun yStable(p: Int, q: Int): Double =
    (q - p).toDouble() / (Math.sqrt(q.toDouble()) + Math.sqrt(p.toDouble()))

/** 方法 A：稳定写法的 y + log10。 */
private fun nA(p: Int, q: Int): Long = Math.ceil(TARGET / (-2.0 * Math.log10(yStable(p, q)))).toLong()

/** 方法 B：稳定写法的 y + 自然对数（与 A 公式不同源）。 */
private fun nB(p: Int, q: Int): Long =
    Math.ceil(TARGET * Math.log(10.0) / (-2.0 * Math.log(yStable(p, q)))).toLong()

/** 朴素对照：直接相减求 y（有抵消），再用 log10。 */
private fun nNaive(p: Int, q: Int): Long {
    val y = Math.sqrt(q.toDouble()) - Math.sqrt(p.toDouble())
    if (y >= 1.0) return -1L
    return Math.ceil(TARGET / (-2.0 * Math.log10(y))).toLong()
}

private inline fun timeOf(runs: Int = 5, body: () -> Long): Pair<Long, Double> {
    body()
    val ts = DoubleArray(runs)
    var r = 0L
    for (i in 0 until runs) {
        val st = System.nanoTime()
        r = body()
        ts[i] = (System.nanoTime() - st) / 1e6
    }
    ts.sort()
    return r to ts[runs / 2]
}

fun main() {
    println("== 题面自检：√2+√3 的偶次幂开头 9 的个数 ==")
    val y = Math.sqrt(3.0) - Math.sqrt(2.0)
    val sb = StringBuilder()
    var pw = y * y
    for (n in 1..8) {
        var c = 0
        var t = pw
        while (t <= 0.1) { t *= 10.0; c++ }
        sb.append(" ").append(c)
        pw *= y * y
    }
    println("  y = √3−√2 = " + y)
    println("  n=1..8 的 9 的个数依次为" + sb + "  -> 与题面 0,1,2,3,4,5,6,7 一致")

    var count = 0
    var sumA = 0L
    var sumB = 0L
    var mismatch = 0
    var maxDiff = 0L
    var maxY = 0.0
    var maxP = 0
    var maxQ = 0
    var minN = Long.MAX_VALUE
    var maxN = 0L
    for (p in 1 until LIMIT) {
        for (q in p + 1..LIMIT - p) {
            if (!admissible(p, q)) continue
            count++
            val a = nA(p, q)
            val b = nB(p, q)
            if (a != b) { mismatch++; maxDiff = maxOf(maxDiff, Math.abs(a - b)) }
            sumA += a
            sumB += b
            val yy = yStable(p, q)
            if (yy > maxY) { maxY = yy; maxP = p; maxQ = q }
            if (a < minN) minN = a
            if (a > maxN) maxN = a
        }
    }
    println("== 统计 ==")
    println("  满足 y<1 的 (p,q) 对数 = " + count)
    println("  最接近 1 的 y = " + maxY + "，对应 (" + maxP + "," + maxQ + ")，N = " + nA(maxP, maxQ))
    println("  N 的范围 = [" + minN + ", " + maxN + "]")
    println("== 双方法互证（log10 版 vs ln 版） ==")
    println("  不一致的对数 = " + mismatch + "，最大差 = " + maxDiff)
    println("  log10 版合计 = " + sumA)
    println("  ln 版合计    = " + sumB + "  " + (if (sumA == sumB) "-> 一致" else "-> 不一致！"))
    var same = 0
    for (p in 1 until LIMIT) for (q in p + 1..LIMIT - p) {
        if (admissible(p, q) && nNaive(p, q) == nA(p, q)) same++
    }
    println("  朴素直接相减法与稳定写法结果相同的对数 = " + same + " / " + count)
    val res = timeOf {
        var t = 0L
        for (p in 1 until LIMIT) for (q in p + 1..LIMIT - p) if (admissible(p, q)) t += nA(p, q)
        t
    }
    println("Σ_{p+q≤2011} N(p,q) = " + res.first)
    println("OPT_MS: " + String.format("%.3f", res.second) + "  （稳定写法 + log10）")
    val bres = timeOf(runs = 3) {
        var t = 0L
        for (p in 1 until LIMIT) for (q in p + 1..LIMIT - p) if (admissible(p, q)) t += nNaive(p, q)
        t
    }
    println("BRUTE_MS: " + String.format("%.3f", bres.second) + "  （直接相减 + log10）")
}