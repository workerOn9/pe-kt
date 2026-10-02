package dev.pekt.engine

/**
 * PE 305 — Reflexive Position（自反位置）：S = 12345678910111213…（Champernowne 串），
 * f(n) = n 第 n 次出现（连续子串）的起始位置，求 Σ_{k=1}^{13} f(3^k)。
 *
 * 推导（详见 content/problems/0305/solution.kt 头部与 0305/analysis.md）：
 *   f(n) 只统计「起始位置 ≤ X」的出现（窗口内容来自无限串、总是完整）⇒ 对 X 二分。
 *   count(X,n) 三类：单整数内部（模板计数 + 数位 DP 处理上界）、跨边界（k=2 对自由位
 *   H 解整数区间；k≥3 中间整数完整在窗口内被 n 子串唯一固定 ⇒ 每配置至多一个 t）、
 *   最后整数部分前缀（逐偏移检查）。跨边界条件 totalLen(t) ≤ X + a0 − 1。
 * 答案 = 18174995535140（28 点公式 vs 定义级暴力全一致、题面 4 样例强断言、公开答案表一致）。
 */
internal fun solve0305Impl(): Long {
    fun totalLen(T: Long): Long {
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

    fun pow10(e: Int): Long {
        var r = 1L
        repeat(e) { r *= 10 }
        return r
    }

    fun lenOf(v: Long): Int {
        var d = 1
        var x = v
        while (x >= 10) { x /= 10; d++ }
        return d
    }

    fun maxTWithTotalLen(X: Long): Long {
        if (X < 0) return 0
        var lo = 0L
        var hi = 1_000_000_000_000_000_000L
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (totalLen(mid) <= X) lo = mid else hi = mid - 1
        }
        return lo
    }

    fun countFixed(d: Int, i: Int, upper: Long, n: String): Long {
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

    fun ceilDiv(a: Long, b: Long) = if (a >= 0) (a + b - 1) / b else a / b
    fun floorDiv(a: Long, b: Long) = if (a >= 0) a / b else (a - b + 1) / b

    fun countCross(X: Long, n: String): Long {
        val L = n.length
        var c = 0L
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
                    if (Q * pow10(r) < pow10(d1 - 1)) continue
                    if (a0 == d0) {
                        if (P in tLo..tHi && P + 1 in Q * pow10(r)..Q * pow10(r) + pow10(r) - 1) c++
                    } else {
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
                        val t = (cur - (parts - 1)) - 1
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

    fun countOcc(X: Long, n: String): Long {
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

    fun fOf(n: Long): Long {
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

    var total = 0L
    var pw = 1L
    for (k in 1..13) {
        pw *= 3
        total += fOf(pw)
    }
    return total
}
