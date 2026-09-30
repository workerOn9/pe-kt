package dev.pekt.engine

import kotlin.math.sqrt

/**
 * PE 283 — Integer Sided Triangles with Integral Area/perimeter Ratio（整数边三角形与整数面积周长比）：
 * 求所有整数边三角形中「面积/周长为不超过 1000 的正整数」者的周长之和。
 *
 * 推导（详见 content/problems/0283/solution.kt 头部与 0283/analysis.md）
 * ────────────────────────────────────────────────────────────────
 * 1) 面积 A = r·s（r 内切圆半径、s 半周长）⟹ A/P = r/2，故比值 k 为正整数 ⟺ r = 2k 为偶数，
 *    k ≤ 1000 即 r ∈ {2, 4, …, 2000}。
 * 2) 切线长 x = s−a、y = s−b、z = s−c 给出三边 (y+z, z+x, x+y) 与正整数三元组的一一双射；
 *    Heron + A = r·s 得核心方程 r²(x+y+z) = xyz。
 * 3) 关键恒等式 (xy − r²)(xz − r²) = r²(x² + r²)：记 A = xy − r²、B = xz − r²、M = r²(x²+r²)，
 *    则「A·B = M 且 x | A + r²、x | B + r²」与 (∗) 的解一一对应（y = (A+r²)/x、z = (B+r²)/x），
 *    顺序 x ≤ y ≤ z 等价于 x² − r² ≤ A ≤ B ⟺ A ≤ √M。
 * 4) 界：1 = r²(Σ 1/两两积) 夹出 r² ≤ xy ≤ 3r² ⟹ x ≤ √3·r；p-adic 约束（a = v_p(x)、
 *    b = v_p(r²)）给出 a ≤ b 时 v_p(A) ∈ [a, v_p(M) − a]；a > b 时 v_p(A) = v_p(B) = b 精确。
 *
 * 实现：对每个 (r, x) 用试除分解 M = r²(x²+r²)（x²+r² ≤ 4r² ≤ 1.6×10^7；其中 ≡ 3 (mod 4) 的
 * 素因子必同时整除 x 与 r，故试除表只需 2 与所有 ≡ 1 (mod 4) 的素数，加上 r 的 ≡ 3 (mod 4)
 * 素因子），再用展开最后两/三层的回溯 DFS 枚举 ≤ √M 的约数并施加上述界，叶子处验证同余与顺序。
 * 与 content/problems/0283/solution.kt 的主路径（方法 A）逐行一致。
 *
 * 复杂度：约 2.7×10^8 次整数运算（1.73×10^6 个 (r,x) 对 + 约 2.5×10^8 次 DFS 节点），
 *   实测约 1.1 s（JIT 预热后），远低于 10 s 熔断线。校验：题面样例 6-8-10 / 13-14-15；
 *   窗口暴力（k ≤ 100）与 Heron 定义级暴力（P ≤ 2000）的周长多重集完全一致；全尺寸窗口暴力
 *   （k ≤ 1000，1.15×10^10 次判定）给出同一总和；另有 Python 独立实现与公开答案表旁证。
 */
internal fun solve0283Impl(): Long {
    val kMax = 1000
    val primes = sievePrimes(4000)
    val p1mod4 = ArrayList<Int>()
    for (p in primes) if (p % 4 == 1) p1mod4.add(p)
    val base = IntArray(p1mod4.size + 1)
    base[0] = 2
    for (i in p1mod4.indices) base[i + 1] = p1mod4[i]
    val testPrimes = IntArray(base.size + 8)
    val baseCount = base.size

    val w = DivisorWalk()
    val psR = IntArray(16)
    val esR = IntArray(16)
    var total = 0L

    for (k in 1..kMax) {
        val r = 2L * k
        val r2 = r * r
        val r2i = r2.toInt()
        val cR = mergeFactor(r2, primes, primes.size, psR, esR, 0)
        System.arraycopy(base, 0, testPrimes, 0, baseCount)
        var tc = baseCount
        for (i in 0 until cR) if (psR[i] % 4 == 3) testPrimes[tc++] = psR[i]
        java.util.Arrays.sort(testPrimes, 0, tc)

        var x = 1L
        while (x * x <= 3 * r2) {
            val n = r2 + x * x
            val ps = w.ps
            val hi = w.hi
            val loX = w.lo
            System.arraycopy(psR, 0, ps, 0, cR)
            System.arraycopy(esR, 0, hi, 0, cR)
            val cnt = mergeFactor(n, testPrimes, tc, ps, hi, cR)

            var ok = true
            for (i in 0 until cnt) {
                if (i >= cR) { loX[i] = 0; continue }
                val p = psR[i]
                if (x % p != 0L) { loX[i] = 0; continue }
                var t = x
                var a = 0
                while (t % p == 0L) {
                    t /= p
                    a++
                }
                val b = esR[i]
                if (a <= b) {
                    loX[i] = a
                    val newHi = hi[i] - a
                    if (newHi < a) { ok = false; break }
                    hi[i] = newHi
                } else {
                    if (hi[i] != 2 * b) { ok = false; break }
                    loX[i] = b
                    hi[i] = b
                }
            }
            if (ok) {
                w.cnt = cnt
                w.cap = isqrt(r2 * n)
                w.loBound = if (x > r) x * x - r2 else 1L
                w.m = r2 * n
                w.r2i = r2i
                w.xi = x.toInt()
                when {
                    cnt == 1 -> {
                        val p0 = ps[0]
                        var f = loX[0]
                        var v = 1L
                        repeat(f) { v *= p0 }
                        while (f <= hi[0]) {
                            if (v > w.cap) break
                            if (v >= w.loBound) w.leaf(v)
                            v *= p0
                            f++
                        }
                    }
                    else -> w.dfs(0, 1L)
                }
                total += w.gained
                w.gained = 0L
            }
            x++
        }
    }
    return total
}

/** 埃氏筛（≤ 4000）。 */
private fun sievePrimes(limit: Int): IntArray {
    val composite = BooleanArray(limit + 1)
    val out = ArrayList<Int>()
    for (i in 2..limit) {
        if (!composite[i]) {
            out.add(i)
            var j = i.toLong() * i
            while (j <= limit) {
                composite[j.toInt()] = true
                j += i
            }
        }
    }
    return out.toIntArray()
}

private fun isqrt(n: Long): Long {
    var s = sqrt(n.toDouble()).toLong()
    while (s > 0 && s * s > n) s--
    while ((s + 1) * (s + 1) <= n) s++
    return s
}

/** 把 n 的质因数合并进 (ps, es) 前缀（相同素数累加指数）。 */
private fun mergeFactor(n0: Long, tp: IntArray, tcount: Int, ps: IntArray, es: IntArray, cnt0: Int): Int {
    var n = n0
    var cnt = cnt0
    for (i in 0 until tcount) {
        val p = tp[i]
        if (p.toLong() * p > n) break
        if (n % p == 0L) {
            var e = 0
            while (n % p == 0L) {
                n /= p
                e++
            }
            var idx = -1
            for (j in 0 until cnt) if (ps[j] == p) { idx = j; break }
            if (idx >= 0) es[idx] += e else { ps[cnt] = p; es[cnt] = e; cnt++ }
        }
    }
    if (n > 1) {
        val p = n.toInt()
        var idx = -1
        for (j in 0 until cnt) if (ps[j] == p) { idx = j; break }
        if (idx >= 0) es[idx] += 1 else { ps[cnt] = p; es[cnt] = 1; cnt++ }
    }
    return cnt
}

/** 单个 (r, x) 的约数枚举状态机（最后两层展开成双重循环，叶子测试内联）。 */
private class DivisorWalk {
    val ps = IntArray(32)
    val hi = IntArray(32)
    val lo = IntArray(32)
    var cnt = 0
    var cap = 0L
    var loBound = 1L
    var m = 0L
    var r2i = 0
    var xi = 1
    var gained = 0L

    fun leaf(a: Long) {
        val ai = a.toInt()
        if ((ai + r2i) % xi != 0) return
        val b = m / a
        if ((b + r2i) % xi != 0L) return
        val y = (a + r2i) / xi
        val z = (b + r2i) / xi
        if (y < xi || z < y) return
        gained += 2 * (xi + y + z)
    }

    fun dfs(idx: Int, cur: Long) {
        val ps = ps
        val hi = hi
        val loX = lo
        if (idx == cnt - 2) {
            val p1 = ps[idx]
            val p2 = ps[idx + 1]
            var f1 = loX[idx]
            var v1 = if (f1 > 0) cur * pow(p1, f1) else cur
            while (f1 <= hi[idx]) {
                if (v1 > cap) return
                var f2 = loX[idx + 1]
                var v2 = if (f2 > 0) v1 * pow(p2, f2) else v1
                while (f2 <= hi[idx + 1]) {
                    if (v2 > cap) break
                    if (v2 >= loBound) leaf(v2)
                    v2 *= p2
                    f2++
                }
                v1 *= p1
                f1++
            }
            return
        }
        if (idx == cnt - 3) {
            val p1 = ps[idx]
            val p2 = ps[idx + 1]
            val p3 = ps[idx + 2]
            var f1 = loX[idx]
            var v1 = if (f1 > 0) cur * pow(p1, f1) else cur
            while (f1 <= hi[idx]) {
                if (v1 > cap) return
                var f2 = loX[idx + 1]
                var v2 = if (f2 > 0) v1 * pow(p2, f2) else v1
                while (f2 <= hi[idx + 1]) {
                    if (v2 > cap) break
                    var f3 = loX[idx + 2]
                    var v3 = if (f3 > 0) v2 * pow(p3, f3) else v2
                    while (f3 <= hi[idx + 2]) {
                        if (v3 > cap) break
                        if (v3 >= loBound) leaf(v3)
                        v3 *= p3
                        f3++
                    }
                    v2 *= p2
                    f2++
                }
                v1 *= p1
                f1++
            }
            return
        }
        val p = ps[idx]
        var f = loX[idx]
        var v = if (f > 0) cur * pow(p, f) else cur
        while (f <= hi[idx]) {
            if (v > cap) return
            dfs(idx + 1, v)
            v *= p
            f++
        }
    }

    private fun pow(p: Int, e: Int): Long {
        var v = 1L
        repeat(e) { v *= p }
        return v
    }
}
