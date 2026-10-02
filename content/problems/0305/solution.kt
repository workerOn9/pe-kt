#!/usr/bin/env kotlin
/**
 * Project Euler 305 — Reflexive Position（自反位置）
 *
 * 题目：S = 12345678910111213… 是把所有正整数依次拼接而成的无限串（Champernowne 串）。
 * 记 f(n) 为 n 作为 S 的连续子串第 n 次出现时的起始位置（位置从 1 起）。
 * 求 Σ_{k=1}^{13} f(3^k)。样例：f(1)=1、f(5)=81、f(12)=271、f(7780)=111111365。
 *
 * 思路推导
 * ────────
 * 关键观察：f(n) 只需「起始位置 ≤ X」，窗口内容来自无限串 S、总是完整的，
 * 因此 count(X, n) 统计的是起始位置 ≤ X 的 n 出现次数；对 X 二分即可求 f(n)。
 * 令 totalLen(T) = Σ_{t=1}^{T} len(t)（覆盖到整数 T 末尾的位置），N0 = 最大的 T 使
 * totalLen(T) ≤ X，则 X 落在整数 N = N0+1 的前缀内。出现分为三类：
 *
 * 1. 单整数内部：t 的十进制含 n（窗口在 t 内，偏移 i ∈ [0, len(t)−len(n)]）。
 *    对 (位数 d, 偏移 i) 用模板计数：位数固定、窗口位固定、其余位自由，
 *    上界为 10^d−1 时直接乘积公式，上界为 N0（不满）时用数位 DP 处理「≤ N0」。
 *    所有 t ≤ N0 的窗口起始都 ≤ totalLen(N0) ≤ X，全部计入。
 *
 * 2. 跨边界（窗口覆盖 ≥ 2 个连续整数）：窗口 = t 的末 a0 位 + 中间整数完整
 *    + 最后整数的前 ak 位（a0 + 中间位数和 + ak = len(n)）。起始位置
 *    = totalLen(t) − a0 + 1 ≤ X ⟺ totalLen(t) ≤ X + a0 − 1，与窗口是否读完无关。
 *    · k = 2：t = H·10^{a0} + P（P = n[:a0]），t+1 的前 ak 位必须等于 n 尾段 Q，
 *      即 t+1 ∈ [Q·10^r, Q·10^r + 10^r − 1]（r = len(t+1) − ak），对自由位 H 解整数区间；
 *    · k ≥ 3：中间整数完整在窗口内，其数字被 n 的子串唯一固定 → 每个分割配置
 *      至多一个 t，逐项检查（连续整数、位数、末 a0 位、最后整数前缀、t ≤ 上界）。
 *
 * 3. 最后一个整数 N = N0+1 的部分前缀：窗口起始 ≤ X 且窗口完整在 N 内，
 *    直接对 N 的十进制逐偏移检查。
 *
 * 复杂度：每个 n（≤ 7 位）二分 O(log X) 步（X ~ 10^16，约 54 步）；每步 count 为
 * O(D²·L) 次模板数位 DP（D ≤ 16 为整数位数、L ≤ 7）+ O(L³) 跨边界配置枚举。
 * 13 个 n 全量本机实测约 180 ms（见 analysis）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

import kotlin.system.measureNanoTime

/** Σ_{t=1}^{T} len(t)：覆盖到整数 T 末尾的字符位置。 */
private fun totalLen(T: Long): Long {
    var sum = 0L
    var pow = 1L
    var d = 1L
    while (T >= pow * 10) {
        sum += pow * 9 * d
        pow *= 10
        d++
    }
    return sum + (T - pow + 1) * d
}

private fun pow10(e: Int): Long {
    var r = 1L
    repeat(e) { r *= 10 }
    return r
}

private fun lenOf(v: Long): Int {
    var d = 1
    var x = v
    while (x >= 10) { x /= 10; d++ }
    return d
}

/** 最大的 T 使 totalLen(T) ≤ X。 */
private fun maxTWithTotalLen(X: Long): Long {
    if (X < 0) return 0
    var lo = 0L
    var hi = 1_000_000_000_000_000_000L
    while (lo < hi) {
        val mid = (lo + hi + 1) / 2
        if (totalLen(mid) <= X) lo = mid else hi = mid - 1
    }
    return lo
}

/**
 * 模板计数：d 位整数 t（首位非零）满足 t ≤ upper 且第 i 位起（0-indexed）恰为 n。
 * upper ≥ 10^{d−1}；upper = 10^d − 1 时退化为乘积公式，否则数位 DP。
 */
private fun countFixed(d: Int, i: Int, upper: Long, n: String): Long {
    val L = n.length
    if (upper >= pow10(d) - 1) {
        return if (i == 0) pow10(d - L) else 9L * pow10(i - 1) * pow10(d - i - L)
    }
    val u = upper.toString()
    fun go(pos: Int, tight: Boolean): Long {
        if (pos == d) return 1L
        val up = if (tight) (u[pos] - '0') else 9
        var s = 0L
        if (pos in i until i + L) {
            val fd = n[pos - i] - '0'
            if (fd <= up) s += go(pos + 1, tight && fd == up)
        } else {
            val lo = if (pos == 0) 1 else 0
            for (c in lo..up) s += go(pos + 1, tight && c == up)
        }
        return s
    }
    return go(0, true)
}

/** 跨边界计数：窗口覆盖 ≥ 2 个连续整数；起始位置 = totalLen(t) − a0 + 1 ≤ X。 */
private fun countCross(X: Long, n: String): Long {
    val L = n.length
    var c = 0L

    // k = 2：t | t+1
    for (a0 in 1 until L) {
        val tMax = maxTWithTotalLen(X + a0 - 1)
        if (tMax < 1) continue
        val ak = L - a0
        val P = n.substring(0, a0).toLong()
        val Q = n.substring(L - ak).toLong()
        val dMax = lenOf(tMax)
        for (d0 in a0..dMax) {
            val tHi = minOf(pow10(d0) - 1, tMax)
            val tLo = if (d0 == 1) 1L else pow10(d0 - 1)
            if (tLo > tHi) continue
            for (d1 in d0..d0 + 1) {
                val r = d1 - ak
                if (r < 0) continue
                if (Q * pow10(r) < pow10(d1 - 1)) continue  // t+1 的前缀不足 d1 位
                if (a0 == d0) {
                    // t 固定 = P
                    if (P in tLo..tHi && P + 1 in Q * pow10(r)..Q * pow10(r) + pow10(r) - 1) c++
                } else {
                    // t = H·10^{a0} + P，H 为 d0−a0 位自由数；t+1 前缀约束解 H 的整数区间
                    val hLoRaw = ceilDiv(Q * pow10(r) - P - 1, pow10(a0))
                    val hHiRaw = floorDiv(Q * pow10(r) + pow10(r) - P - 2, pow10(a0))
                    val hLo = maxOf(hLoRaw, if (d0 - a0 == 1) 1L else pow10(d0 - a0 - 1))
                    val hHi = minOf(hHiRaw, pow10(d0 - a0) - 1)
                    val hHiT = if (tHi >= P) (tHi - P) / pow10(a0) else -1L
                    val hHiFinal = minOf(hHi, hHiT)
                    if (hLo <= hHiFinal) c += hHiFinal - hLo + 1
                }
            }
        }
    }

    // k ≥ 3：t | mid1 … mid_m | last（中间整数完整在窗口内 → 每个分割至多一个 t）
    for (a0 in 1..L - 2) {
        val tMax = maxTWithTotalLen(X + a0 - 1)
        if (tMax < 1) continue
        for (ak in 1..L - a0 - 1) {
            val mid = L - a0 - ak
            val tailQ = n.substring(L - ak).toLong()
            val headP = n.substring(0, a0).toLong()
            fun split(rem: Int, parts: Int, prefix: List<Int>) {
                if (rem == 0) {
                    var ok = true
                    var pos = a0
                    var cur = -1L
                    for (j in 0 until parts) {
                        val dj = prefix[j]
                        val seg = n.substring(pos, pos + dj)
                        if (seg[0] == '0' || lenOf(seg.toLong()) != dj) { ok = false; break }
                        val v = seg.toLong()
                        if (j > 0 && v != cur + 1) { ok = false; break }
                        cur = v
                        pos += dj
                    }
                    if (!ok) return
                    val t = (cur - (parts - 1)) - 1        // mid1 − 1
                    val last = cur + 1
                    if (t < 1 || t > tMax) return
                    if (lenOf(t) < a0 || t % pow10(a0) != headP) return
                    val r = lenOf(last) - ak
                    if (r < 0 || last / pow10(r) != tailQ) return
                    c++
                    return
                }
                for (len1 in 1..rem) split(rem - len1, parts + 1, prefix + len1)
            }
            for (m in 1..mid) split(mid, 0, emptyList())
        }
    }
    return c
}

private fun ceilDiv(a: Long, b: Long) = if (a >= 0) (a + b - 1) / b else a / b
private fun floorDiv(a: Long, b: Long) = if (a >= 0) a / b else (a - b + 1) / b

/** count(X, n)：S 中起始位置 ≤ X 的 n 出现次数。 */
private fun countOcc(X: Long, n: String): Long {
    val L = n.length
    val N0 = maxTWithTotalLen(X)
    var c = 0L
    val dMax = lenOf(N0)
    for (d in L..dMax) {
        val upper = if (d < dMax) pow10(d) - 1 else N0
        if (upper < pow10(d - 1)) continue
        for (i in 0..d - L) c += countFixed(d, i, upper, n)
    }
    val fullN0 = totalLen(N0)
    if (fullN0 < X) {
        val N = N0 + 1
        val sN = N.toString()
        val iMax = minOf((X - fullN0).toInt() - 1, sN.length - L)
        for (i in 0..iMax) if (sN.substring(i, i + L) == n) c++
    }
    return c + countCross(X, n)
}

/** f(n)：n 第 n 次出现的起始位置（对 countOcc 二分）。 */
private fun fOf(n: Long): Long {
    val s = n.toString()
    var lo = 1L
    var hi = 1L
    while (countOcc(hi, s) < n) hi *= 2
    while (lo < hi) {
        val mid = (lo + hi) / 2
        if (countOcc(mid, s) >= n) hi = mid else lo = mid + 1
    }
    return lo
}

fun main() {
    // 题面样例强断言
    val samples = listOf(1L to 1L, 5L to 81L, 12L to 271L, 7780L to 111111365L)
    for ((n, expect) in samples) {
        val got = fOf(n)
        check(got == expect) { "样例不符 f($n) = $got ≠ $expect" }
        println("f($n) = $got  ✓（样例 $expect）")
    }

    val t0 = System.nanoTime()
    var total = 0L
    var pw = 1L
    for (k in 1..13) {
        pw *= 3
        val f = fOf(pw)
        println("f($pw) = $f")
        total += f
    }
    val ms = (System.nanoTime() - t0) / 1_000_000.0
    println("SUM = $total，耗时 ${"%.1f".format(ms)} ms")
    println("ANSWER: $total")
}
