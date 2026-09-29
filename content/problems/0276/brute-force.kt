#!/usr/bin/env kotlin
/**
 * Project Euler 276 — Primitive Triangles：暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 三条路径：
 *
 *   路径 0（完全按定义，n ≤ 500）：三重循环枚举 a ≤ b ≤ c、a + b > c、周长 ≤ n，
 *     用欧几里得算法判 gcd(a,b,c) = 1 计数；顺带打印全部三角形数 F(500)（不要求本原）。
 *
 *   路径 1（半朴素，baseline 取 n = 20000）：枚举 (a,b)（a ≤ b，a + 2b ≤ n），
 *     c 的可行区间是 [b, min(a+b−1, n−a−b)]；对 g = gcd(a,b) 的相异素因子做容斥，
 *     直接数出区间内与 g 互素的 c。不使用 Möbius 求和，也不使用 T/F 的闭式公式。
 *
 *   路径 2（规模外推）：路径 1 的代价约 0.22 n² 次 (a,b) 枚举；n = 10^7 时约 2.2×10^13 次，
 *     是 n = 20000（约 8.8×10^7 次）的 ~2.5×10^5 倍，直接暴力不可行——这就是本题需要
 *     Möbius 反演（solution.kt 的方法 A/B）的原因。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

/** 路径 0/1 共用的欧几里得 gcd。 */
private fun gcd(a: Int, b: Int): Int {
    var x = a
    var y = b
    while (y != 0) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** 路径 0：完全按定义枚举本原三角形（a ≤ b ≤ c，周长 ≤ n）。 */
private fun bruteDefinition(n: Int): Long {
    var count = 0L
    for (c in 1..n) for (b in 1..c) for (a in 1..b) {
        if (a + b <= c || a + b + c > n) continue
        if (gcd(gcd(a, b), c) == 1) count++
    }
    return count
}

/** 路径 0 附带：全部整数边三角形数（不过滤 gcd）。 */
private fun allTriangles(n: Int): Long {
    var count = 0L
    for (c in 1..n) for (b in 1..c) for (a in 1..b) {
        if (a + b > c && a + b + c <= n) count++
    }
    return count
}

/**
 * 路径 1：枚举 (a,b)，对 c 用「与 gcd(a,b) 互素」的容斥计数。
 * 复杂度 ~Σ_{a ≤ n/3} (n−a)/2 ≈ 0.22 n² 次枚举。
 */
private fun brutePairCount(n: Int): Long {
    // 最小素因子表（用于分解 g = gcd(a,b) ≤ n）
    val spf = IntArray(n + 1)
    var i = 2
    while (i <= n) {
        if (spf[i] == 0) {
            var j = i
            while (j <= n) {
                if (spf[j] == 0) spf[j] = i
                j += i
            }
        }
        i++
    }
    val ps = IntArray(8)
    var count = 0L
    var a = 1
    while (3 * a <= n) {
        var b = a
        while (a + 2 * b <= n) {
            val lo = b
            val hi = minOf(a + b - 1, n - a - b)
            if (lo <= hi) {
                var g = gcd(a, b)
                var m = 0
                while (g > 1 && m < ps.size) {
                    val p = spf[g]
                    ps[m++] = p
                    while (g % p == 0) g /= p
                }
                var cnt = 0L
                for (mask in 0 until (1 shl m)) {
                    var d = 1
                    var bits = 0
                    for (t in 0 until m) if ((mask shr t) and 1 == 1) { d *= ps[t]; bits++ }
                    val c = hi / d - (lo - 1) / d
                    cnt += if (bits % 2 == 0) c else -c
                }
                count += cnt
            }
            b++
        }
        a++
    }
    return count
}

private fun timeIt(f: () -> Long): Pair<Long, Double> {
    val t0 = System.nanoTime()
    val v = f()
    return v to (System.nanoTime() - t0) / 1e6
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val (out, ms) = timeIt(f)
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 路径 0：完全按定义 ----------
    val small = intArrayOf(100, 200, 300, 400, 500)
    val smallVals = LongArray(small.size)
    for (idx in small.indices) {
        val (v, ms) = timeIt { bruteDefinition(small[idx]) }
        smallVals[idx] = v
        println("路径 0（按定义三重循环）n = ${small[idx]}：P = $v（${"%.1f".format(ms)} ms）")
    }
    check(smallVals[4] == 728878L) { "n=500 的定义式枚举 = ${smallVals[4]} ≠ 728878" }
    println("路径 0 附带：n = 500 的全部三角形数 F(500) = ${allTriangles(500)}")

    // ---------- 路径 1：半朴素（逐 (a,b) + 容斥），先与路径 0 对拍 ----------
    for (idx in small.indices) {
        val v = brutePairCount(small[idx])
        check(v == smallVals[idx]) { "n=${small[idx]}：半朴素 $v ≠ 定义式 ${smallVals[idx]}" }
    }
    println("路径 1 与路径 0 在 n = 100…500 全部一致")

    // ---------- 路径 1 的规模与耗时 ----------
    val sizes = intArrayOf(1000, 2000, 4000, 8000)
    val vals = LongArray(sizes.size)
    for (idx in sizes.indices) {
        val (v, ms) = timeIt { brutePairCount(sizes[idx]) }
        vals[idx] = v
        println("路径 1：n = ${sizes[idx]} → P = $v（${"%.1f".format(ms)} ms，${"%.1f".format(sizes[idx] * sizes[idx] * 0.22 / 1e6)}×10⁶ 次枚举）")
    }
    val (v20000, ms20000) = timeIt { brutePairCount(20_000) }
    println("路径 1：n = 20000 → P = $v20000（${"%.1f".format(ms20000)} ms，~8.8×10⁷ 次枚举）")

    // ---------- baseline 计时（meta 的 bruteForceBaselineMs 口径） ----------
    brutePairCount(20_000)
    val msBaseline = bestOf3("路径 1：n = 20000（baseline 口径）", v20000) { brutePairCount(20_000) }

    // ---------- 外推 ----------
    val pairs1e7 = 1e7 * 1e7 * 0.22
    println()
    println("外推：路径 1 在 n = 10^7 需 ~${"%.1e".format(pairs1e7)} 次枚举，")
    println("比 baseline（n = 20000，~8.8×10⁷ 次）大约 ${"%.0e".format(pairs1e7 / 8.8e7)} 倍，直接暴力不可行")
    println("汇总：baseline（路径 1，n = 20000）= ${"%.1f".format(msBaseline)} ms（3 轮最优，JIT 预热后）")
    println("check() 全部通过")
}
