#!/usr/bin/env kotlin
/**
 * Project Euler 281 — Pizza Toppings（比萨配料）：暴力 / 独立对照
 * 独立实现，与 solution.kt 不共享核心代码（求和口径、系数算法、通项公式全部另写一套）。
 *
 * 路径 1（定义级暴力，小规模穷举）
 * ─────────────────────────────
 * 完全按题面定义枚举：生成长度 N = mn、每种颜色恰出现 n 次的全部着色（组合式枚举，恰好多项
 * 系数那么多），用 Booth 算法求每个着色的最小旋转、放进 HashSet 归并成项链数。这就是 f(m,n)
 * 的定义实现，不经过任何 Burnside/生成函数。规模受「着色数 × N ≤ 2×10^8」约束，覆盖
 * (m,n) = (2,1..12)、(3,1..5)、(4,1..3)、(5,1..2)、(6,1)、(6,2)、(7,1)、(8,1)、(9,1)、(10,1)
 * 共 28 对。
 *
 * 路径 2（非周期项链分解 / Möbius 反演，全量答案）
 * ────────────────────────────────────────
 * 换一条通项公式：令 A(m,k) = 「最小周期恰为 mk 的非周期项链」个数（m 种颜色各 k 次）。
 * 任意内容为「各 n 次」的着色其最小周期必为 mk（k | n），故
 *      C(mn; n,...,n) = Σ_{k|n} mk·A(m,k)。
 * 对 n 做 Möbius 反演得 A(m,k) = (1/(mk)) Σ_{d|k} μ(k/d)·C(md; d,...,d)，于是
 *      f(m,n) = Σ_{k|n} A(m,k)。
 * 这条路径经过 μ、经过「非周期项链」这个完全不同的组合对象。
 *
 * 路径 3（朴素 Burnside：逐旋转 + 每次从零算阶乘，meta 的 bruteForceBaselineMs 口径）
 * ─────────────────────────────────────────────────────────────
 * f = (1/(mn)) Σ_{r=0}^{mn-1} Fix(r)，Fix(r) 用 g = gcd(r,N)、L = N/g 判定，系数写成
 * (g)!/((n/L)!^m) 并每次现算阶乘；不归并 φ、不缓存——「照着 Burnside 定义直写」的版本。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0281/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

import java.math.BigInteger
import java.util.HashSet

private const val LIMIT = 1_000_000_000_000_000L
private const val ANSWER = 1_485_776_387_445_623L

/** 定义级枚举的规模上限：着色数 × 旋转数 ≤ 2×10^8。 */
private const val ENUM_BUDGET = 200_000_000L

// ───────────────────────── 通用小工具（独立实现） ─────────────────────────

private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

private fun divisorsOf(n: Int): List<Int> = (1..n).filter { n % it == 0 }

private fun phiOf(n: Int): Int {
    var x = n
    var r = n
    var p = 2
    while (p * p <= x) {
        if (x % p == 0) {
            while (x % p == 0) x /= p
            r -= r / p
        }
        p++
    }
    if (x > 1) r -= r / x
    return r
}

private fun mobius(n: Int): Int {
    var x = n
    var primeCount = 0
    var p = 2
    while (p * p <= x) {
        if (x % p == 0) {
            var cnt = 0
            while (x % p == 0) {
                x /= p
                cnt++
            }
            if (cnt > 1) return 0
            primeCount++
        }
        p++
    }
    if (x > 1) primeCount++
    return if (primeCount % 2 == 0) 1 else -1
}

/** 现算阶乘（路径 3 用；不缓存，故意保持朴素）。 */
private fun factorial(n: Int): BigInteger {
    var r = BigInteger.ONE
    for (i in 2..n) r = r.multiply(BigInteger.valueOf(i.toLong()))
    return r
}

// ─────────────────────── 路径 1：定义级项链枚举 ───────────────────────

/** Booth 最小旋转（返回最小旋转的起点）。 */
private fun minRotationStart(d: IntArray): Int {
    val n = d.size
    val s = IntArray(2 * n) { d[it % n] }
    var i = 0
    var j = 1
    var k = 0
    while (i < n && j < n && k < n) {
        val a = s[i + k]
        val b = s[j + k]
        when {
            a == b -> k++
            a > b -> {
                i += k + 1
                if (i == j) i++
                k = 0
            }
            else -> {
                j += k + 1
                if (i == j) j++
                k = 0
            }
        }
    }
    return minOf(i, j)
}

/** 枚举全部「每种颜色恰 n 次」的着色（在当前空闲位置里做组合，数量 = 多项系数，无重复）。 */
private fun forEachColoring(m: Int, n: Int, action: (IntArray) -> Unit) {
    val N = m * n
    val digits = IntArray(N)
    val used = BooleanArray(N)
    fun choose(color: Int) {
        val pick = IntArray(n)                       // 每层独立，避免递归回溯互相踩踏
        if (color == m - 1) {                        // 最后一种颜色占满剩余位置
            var cnt = 0
            for (i in 0 until N) if (!used[i]) {
                digits[i] = color
                cnt++
            }
            check(cnt == n) { "最后一种颜色位置数 $cnt ≠ $n" }
            action(digits)
            for (i in 0 until N) if (!used[i]) digits[i] = 0
            return
        }
        val free = ArrayList<Int>(N)                 // 本层枚举期间 used 不变，free 固定
        for (i in 0 until N) if (!used[i]) free.add(i)
        fun comb(i: Int, idx: Int) {
            if (i == n) {
                for (p in pick) {
                    digits[p] = color
                    used[p] = true
                }
                choose(color + 1)
                for (p in pick) {
                    used[p] = false
                    digits[p] = 0
                }
                return
            }
            var j = idx
            while (j <= free.size - (n - i)) {
                pick[i] = free[j]
                comb(i + 1, j + 1)
                j++
            }
        }
        comb(0, 0)
    }
    choose(0)
}

/** 定义级暴力：返回项链数（旋转同一，反射不同）。 */
private fun bruteNecklaces(m: Int, n: Int): Long {
    val N = m * n
    check(N <= 62) { "编码需要 m^N 放进 Long：m=$m n=$n" }
    val seen = HashSet<Long>()
    forEachColoring(m, n) { digits ->
        val start = minRotationStart(digits)
        var code = 0L
        for (i in 0 until N) code = code * m + digits[(start + i) % N]
        seen.add(code)
    }
    return seen.size.toLong()
}

/** 定义级枚举的可覆盖范围：着色数（多项系数）× 旋转数 ≤ ENUM_BUDGET。 */
private fun enumNecklaceCountUpper(m: Int, n: Int): BigInteger {
    // 着色数 = (mn)!/(n!^m) 的上界用「逐次 C」估计；这里直接算精确值
    var r = BigInteger.ONE
    var rest = m * n
    for (left in m downTo 2) {
        val k = n
        val kk = minOf(k, rest - k)
        var bin = BigInteger.ONE
        for (i in 1..kk) {
            bin = bin.multiply(BigInteger.valueOf((rest - kk + i).toLong())).divide(BigInteger.valueOf(i.toLong()))
        }
        r = r.multiply(bin)
        rest -= k
        if (r > BigInteger.valueOf(ENUM_BUDGET)) return r
    }
    return r
}

// ───────────── 路径 2：非周期项链分解 + Möbius 反演（全量） ─────────────

/** 多项系数 C(mk; k,...,k) = (mk)!/(k!^m)（独立写法：三次二项系数连乘）。 */
private fun contentWords(m: Int, k: Int): BigInteger {
    var r = BigInteger.ONE
    var rest = m * k
    var left = m
    val cache = HashMap<Int, BigInteger>()
    fun bin(n: Int, kk0: Int): BigInteger {
        val key = n * 100 + kk0
        return cache.getOrPut(key) {
            val k2 = minOf(kk0, n - kk0)
            var b = BigInteger.ONE
            for (i in 1..k2) b = b.multiply(BigInteger.valueOf((n - k2 + i).toLong())).divide(BigInteger.valueOf(i.toLong()))
            b
        }
    }
    while (left > 1) {
        r = r.multiply(bin(rest, k))
        rest -= k
        left--
    }
    return r
}

/** A(m,k) = 「最小周期恰为 mk」的非周期项链数 = (1/(mk)) Σ_{d|k} μ(k/d)·C(md; d,...,d)。 */
private fun aperiodicNecklaces(m: Int, k: Int): BigInteger {
    var s = BigInteger.ZERO
    for (d in divisorsOf(k)) {
        val mu = mobius(k / d)
        if (mu == 0) continue
        val term = contentWords(m, d).multiply(BigInteger.valueOf(mu.toLong()))
        s = s.add(term)
    }
    val (q, r) = s.divideAndRemainder(BigInteger.valueOf((m.toLong()) * k))
    check(r.signum() == 0) { "A($m,$k) 非整数" }
    return q
}

/** f(m,n) = Σ_{k|n} A(m,k)（每个着色按最小周期归类，非周期项链互不重叠）。 */
private fun fByMobius(m: Int, n: Int): BigInteger {
    var s = BigInteger.ZERO
    for (k in divisorsOf(n)) s = s.add(aperiodicNecklaces(m, k))
    return s
}

// ───────────── 路径 3：朴素 Burnside（逐旋转 + 现算阶乘，baseline） ─────────────

private fun fNaiveBurnside(m: Int, n: Int): BigInteger {
    val N = m * n
    var s = BigInteger.ZERO
    for (r in 0 until N) {
        val g = gcd(r.toLong(), N.toLong()).toInt()
        val L = N / g
        if (n % L != 0) continue
        val per = n / L
        s = s.add(factorial(g).divide(factorial(per).pow(m)))   // g!/(per!^m)
    }
    val (q, r2) = s.divideAndRemainder(BigInteger.valueOf(N.toLong()))
    check(r2.signum() == 0) { "朴素 Burnside 不能被 N 整除：m=$m n=$n" }
    return q
}

// ─────────────────────────────── 求和窗口 ───────────────────────────────

/** 主路径的枚举窗口（与 solution.kt 相同结论：m ≤ 18；每个 m 只到第一个超限的 n）。 */
private fun collect(f: (Int, Int) -> BigInteger): List<Triple<Int, Int, Long>> {
    val out = ArrayList<Triple<Int, Int, Long>>()
    var m = 2
    while (m <= 18) {
        var n = 1
        while (true) {
            val v = f(m, n)
            if (v > BigInteger.valueOf(LIMIT)) break
            out.add(Triple(m, n, v.toLong()))
            n++
        }
        m++
    }
    return out
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 定义级暴力 vs 非周期项链公式：小规模逐对 ----------
    val enumPairs = ArrayList<Pair<Int, Int>>()
    for (m in 2..12) {
        var n = 1
        while (true) {
            val words = enumNecklaceCountUpper(m, n)
            if (words.multiply(BigInteger.valueOf((m.toLong()) * n)) > BigInteger.valueOf(ENUM_BUDGET)) break
            enumPairs.add(m to n)
            if (m * n >= 62) break
            n++
        }
    }
    var enumTotal = 0L
    for ((m, n) in enumPairs) {
        val brute = bruteNecklaces(m, n)
        val mob = fByMobius(m, n).toLong()
        val naive = fNaiveBurnside(m, n).toLong()
        check(brute == mob && brute == naive) { "($m,$n)：枚举 $brute / Möbius $mob / 朴素 $naive 不一致" }
        enumTotal += brute
    }
    println("定义级枚举覆盖 ${enumPairs.size} 个 (m,n) 对：" +
        enumPairs.joinToString(" ", prefix = "[", postfix = "]") { "(${it.first},${it.second})" })
    println("每一对都与非周期项链公式（μ 反演）和朴素 Burnside 一致；这些对的 f 之和 = $enumTotal")

    // ---------- 2. 全量答案：Möbius 路径 vs 朴素 Burnside ----------
    val pairsMob = collect { m, n -> fByMobius(m, n) }
    val pairsNaive = collect { m, n -> fNaiveBurnside(m, n) }
    check(pairsMob == pairsNaive) { "Möbius 路径与朴素 Burnside 的配对清单不一致" }
    val sumMob = pairsMob.sumOf { it.third }
    val sumNaive = pairsNaive.sumOf { it.third }
    check(sumMob == ANSWER && sumNaive == ANSWER) { "全量 $sumMob / $sumNaive ≠ $ANSWER" }
    println("全量 ${pairsMob.size} 对：非周期项链分解（路径 2）总和 = $sumMob")
    println("全量 ${pairsNaive.size} 对：朴素逐旋转 Burnside（路径 3）总和 = $sumNaive")

    // ---------- 3. 计时 ----------
    fByMobius(2, 29)
    fNaiveBurnside(2, 29)
    val msMob = bestOf3("路径 2 非周期项链分解（全量）", ANSWER) { collect { m, n -> fByMobius(m, n) }.sumOf { it.third } }
    val msNaive = bestOf3("路径 3 朴素 Burnside（全量，逐旋转 + 现算阶乘）", ANSWER) {
        collect { m, n -> fNaiveBurnside(m, n) }.sumOf { it.third }
    }
    val msEnum = bestOf3("路径 1 定义级枚举（全部可行小规模对，共 ${enumPairs.size} 对）", enumTotal) {
        enumPairs.sumOf { (m, n) -> bruteNecklaces(m, n) }
    }

    // ---------- 4. 汇合 ----------
    println()
    println("小规模枚举 + 非周期分解 + 朴素 Burnside 给出同一答案 = $sumMob")
    println("汇总：路径 2 ${"%.3f".format(msMob)} ms；路径 3（baseline）${"%.3f".format(msNaive)} ms；" +
        "路径 1 枚举 ${"%.1f".format(msEnum)} ms")
    println("check() 全部通过")
}
