#!/usr/bin/env kotlin
/**
 * Project Euler 309 — Integer Ladders（整数梯子）
 *
 * 题目：巷宽 w，两架梯子长 x、y（0 < x < y），交叉点离地 h；当 x、y、h、w 都是正整数时，
 * 求 0 < x < y < 1,000,000 中使 w 为整数的 (x, y, h) 组数。
 *
 * 思路推导
 * ────────
 * 梯子 x 的上端高度 a = √(x² − w²)，梯子 y 的上端高度 b = √(y² − w²)（巷子两侧各有一堵墙，
 * 两梯在高度 h 处相交）。由相似三角形：
 *     1/h = 1/a + 1/b  ⟺  h = ab/(a + b)  ⟺  (a − h)(b − h) = h²，
 * 而 h < a、h < b 恒成立，所以只需 ab/(a+b) 整除为整数。
 *
 * 关键一步：a、b 必为整数（题面只要求 x、y、h、w 为整数）。令 A = a²、B = b² ∈ ℤ，
 * 由 ab = h(a+b) 整理得
 *     √(AB) = ab = [AB − h²(A + B)] / (2h²) ∈ ℚ ⟹ ab = K ∈ ℤ。
 * 于是 A、B 的平方自由部分相同，设 A = d·u²、B = d·v²（d 无平方因子），则
 * a = u√d、b = v√d，代入 h = ab/(a+b) = uv√d/(u+v) ∈ ℚ 迫使 d = 1。故 a, b ∈ ℤ。
 *
 * 问题于是化为：(a, w, x) 与 (b, w, y) 是两个共享直角边 w 的整数勾股数（x < y ⟺ a < b），
 * 且 (a+b) | ab。
 *
 * 算法：用欧几里得参数化枚举所有斜边 < 10⁶ 的整数勾股数（m > n ≥ 1、gcd(m,n) = 1、
 * m 与 n 奇偶不同，c = m²+n² < 10⁶，故 m ≤ 999），再乘倍数 s（c·s < 10⁶）——共约
 * 4.0×10⁶ 条「w = 某条直角边、a = 另一条直角边、斜边 = c」的记录，按 w 分桶。
 * 对每个 w 取桶内所有无序对 a < b，判断 (a + b) | ab，成立即得一组 (x, y, h)，
 * 其中 h = ab/(a+b) < b < y，h < y 与 w < x 自动满足，无需再判。
 *
 * 验证
 * ────
 * 1. y < 200 时恰好 5 组，且与题面给出的 (70,119,30)、(74,182,21)、(87,105,35)、
 *    (100,116,35)、(119,175,40) 逐条吻合；
 * 2. 三种互不依赖的实现在 y < 1000 上给出完全相同的 (x, y, h) 集合（各 76 组）：
 *    · 勾股分桶法（欧几里得参数化 + 倍数，主方法）；
 *    · 平方扫描法：O(N²) 枚举 (w, x) 判 x² − w² 是否平方，不用欧几里得参数化；
 *    · 朴素法：O(N³) 三重枚举 (x, y, w)，只判平方，不含任何数论结构；
 *    y < 20,000 上另比对「平方扫描法 vs 勾股分桶法」的完整三元组集合（各 3165 组）。
 *
 * 复杂度：勾股分桶法建桶 O(N log N)、桶内配对合计约 6.1×10⁷ 次整除判定，空间 O(N)；
 *         朴素法 O(N³)、平方扫描法 O(N²)。计时对照的规模不同：brute 用 y < 1000，
 *         opt 用 y < 10⁶（搜索空间大 10⁶ 倍）。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

private fun gcd(a: Int, b: Int): Int {
    var x = a
    var y = b
    while (y != 0) { val t = x % y; x = y; y = t }
    return x
}

/** 向下取整的整数平方根（入参 v ≥ 0）。 */
private fun isqrt(v: Long): Long {
    if (v <= 0L) return 0L
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

/**
 * 按巷宽 w 分桶的勾股数表：桶 w 里存所有 a² + w² = c²（a > 0, c < limit）的 (a, c)。
 * 用链式前向星（head / next）实现，省掉 10⁶ 个 List 的对象开销。
 */
private class LegsBuckets(val limit: Int) {
    val head = IntArray(limit + 1) { -1 }
    var a = IntArray(1 shl 16)
    var c = IntArray(1 shl 16)
    var next = IntArray(1 shl 16)
    var n = 0

    fun push(w: Int, av: Int, cv: Int) {
        if (n == a.size) { a = a.copyOf(n * 2); c = c.copyOf(n * 2); next = next.copyOf(n * 2) }
        a[n] = av; c[n] = cv; next[n] = head[w]; head[w] = n; n++
    }
}

/**
 * 建桶。euclid = true 走欧几里得参数化（主方法）；false 则 O(N²) 枚举 (w, x) 判平方（对照法）。
 */
private fun buildBuckets(limit: Int, euclid: Boolean): LegsBuckets {
    val bk = LegsBuckets(limit)
    if (euclid) {
        for (m in 2..isqrt(limit.toLong()).toInt()) {
            val m2 = m * m
            for (n in 1 until m) {
                if (gcd(m, n) != 1) continue
                if (((m + n) and 1) == 0) continue
                val cc = m2 + n * n
                if (cc >= limit) continue
                val p = m2 - n * n
                val q = 2 * m * n
                var s = 1
                while (cc * s < limit) { bk.push(p * s, q * s, cc * s); bk.push(q * s, p * s, cc * s); s++ }
            }
        }
    } else {
        for (w in 1 until limit) {
            val w2 = w.toLong() * w
            for (x in w + 1 until limit) {
                val t = x.toLong() * x - w2
                val r = isqrt(t)
                if (r * r == t) bk.push(w, r.toInt(), x)
            }
        }
    }
    return bk
}

/** 逐个吐出非空桶：先按 a 升序的竖边 sa，再是对应的斜边 sc（a 升序即 x 升序，保证 a < b ⟺ x < y）。 */
private fun forEachBucket(bk: LegsBuckets, body: (IntArray, IntArray) -> Unit) {
    var ba = IntArray(1024)
    var bc = IntArray(1024)
    for (w in 1..bk.limit) {
        var k = 0
        var i = bk.head[w]
        while (i != -1) {
            if (k == ba.size) { ba = ba.copyOf(k * 2); bc = bc.copyOf(k * 2) }
            ba[k] = bk.a[i]; bc[k] = bk.c[i]; k++; i = bk.next[i]
        }
        if (k < 2) continue
        val ord = IntArray(k) { it }.sortedBy { ba[it] }
        body(IntArray(k) { ba[ord[it]] }, IntArray(k) { bc[ord[it]] })
    }
}

/** 桶内配对：所有 a < b 且 (a+b) | ab 的组数（只要计数，不物化结果）。 */
private fun countPairs(bk: LegsBuckets): Long {
    var count = 0L
    forEachBucket(bk) { sa, _ ->
        for (p in 0 until sa.size) {
            val a1 = sa[p].toLong()
            for (q in p + 1 until sa.size) {
                val b1 = sa[q].toLong()
                if (a1 * b1 % (a1 + b1) == 0L) count++
            }
        }
    }
    return count
}

/** 同上，但把每一组 (x, y, h) 物化出来（小阈值互证用）。 */
private fun listPairs(bk: LegsBuckets): List<Triple<Int, Int, Int>> {
    val out = ArrayList<Triple<Int, Int, Int>>()
    forEachBucket(bk) { sa, sc ->
        for (p in 0 until sa.size) {
            val a1 = sa[p].toLong()
            for (q in p + 1 until sa.size) {
                val b1 = sa[q].toLong()
                if (a1 * b1 % (a1 + b1) == 0L) out.add(Triple(sc[p], sc[q], (a1 * b1 / (a1 + b1)).toInt()))
            }
        }
    }
    return out
}

/** 对照法：O(N³) 朴素枚举 (x, y, w)，只判平方，不含任何数论结构。 */
private fun solveNaive(limit: Int): List<Triple<Int, Int, Int>> {
    val out = ArrayList<Triple<Int, Int, Int>>()
    for (y in 2 until limit) {
        for (x in 1 until y) {
            for (w in 1 until x) {
                val ta = x.toLong() * x - w.toLong() * w
                val a = isqrt(ta)
                if (a * a != ta) continue
                val tb = y.toLong() * y - w.toLong() * w
                val b = isqrt(tb)
                if (b * b != tb) continue
                if (a * b % (a + b) == 0L) out.add(Triple(x, y, (a * b / (a + b)).toInt()))
            }
        }
    }
    return out
}

private fun median(xs: DoubleArray): Double = xs.sorted()[xs.size / 2]
private fun ms(v: Double): String = "%.3f".format(v)

fun main() {
    val fullLimit = 1_000_000
    val bruteLimit = 1_000
    val crossLimit = 20_000

    // ① 题面样例：y < 200 恰有 5 组，且逐条吻合
    val sample = listPairs(buildBuckets(200, true))
    val expected = listOf(
        Triple(70, 119, 30), Triple(74, 182, 21), Triple(87, 105, 35),
        Triple(100, 116, 35), Triple(119, 175, 40)
    )
    println("sample y<200: got=" + sample.size + " " + (if (sample.size == 5) "[OK 5 groups]" else "[FAIL]") +
        " matches statement: " + (if (sample.toSet() == expected.toSet()) "[OK]" else "[FAIL] " + sample))

    // ② 计时：各「预热 1 次 + 5 次取中位数」
    solveNaive(bruteLimit)
    val bruteTimes = DoubleArray(5) {
        val t0 = System.nanoTime(); solveNaive(bruteLimit)
        (System.nanoTime() - t0) / 1_000_000.0
    }
    countPairs(buildBuckets(fullLimit, true))
    val optTimes = DoubleArray(5) {
        val t0 = System.nanoTime(); countPairs(buildBuckets(fullLimit, true))
        (System.nanoTime() - t0) / 1_000_000.0
    }
    val bruteMs = median(bruteTimes)
    val optMs = median(optTimes)

    // ③ 互证：小阈值上三种实现的 (x, y, h) 集合完全相同
    val b1 = listPairs(buildBuckets(bruteLimit, true)).toSet()
    val s1 = listPairs(buildBuckets(bruteLimit, false)).toSet()
    val n1 = solveNaive(bruteLimit).toSet()
    println("cross-check y<1000: buckets=" + b1.size + " scan=" + s1.size + " naive=" + n1.size +
        " -> identical set: " + (if (b1 == s1 && b1 == n1) "[OK]" else "[FAIL]"))
    val b2 = listPairs(buildBuckets(crossLimit, true)).toSet()
    val s2 = listPairs(buildBuckets(crossLimit, false)).toSet()
    println("cross-check y<20000: buckets=" + b2.size + " scan=" + s2.size +
        " -> identical set: " + (if (b2 == s2) "[OK]" else "[FAIL]"))
    val scanT0 = System.nanoTime()
    listPairs(buildBuckets(crossLimit, false))
    println("scan y<20000 single run (ms): " + ms((System.nanoTime() - scanT0) / 1_000_000.0))
    println("scales: naive/scan use y<" + bruteLimit + ", bucketed method uses y<" + fullLimit)
    println("brute 5 runs (ms): " + bruteTimes.joinToString(", ") { ms(it) })
    println("opt   5 runs (ms): " + optTimes.joinToString(", ") { ms(it) })

    val answer = countPairs(buildBuckets(fullLimit, true))
    println("BRUTE_MS: " + ms(bruteMs))
    println("OPT_MS: " + ms(optMs))
    println("ANSWER: " + answer)
}