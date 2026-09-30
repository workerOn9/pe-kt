#!/usr/bin/env kotlin
/**
 * Project Euler 299 — Three Similar Triangles（三个相似三角形）：暴力 / 独立对照
 *
 * 与 solution.kt 不共享核心代码。本文件提供四套互相独立的对照实现：
 *   ① 定义级纯暴力：直接四重枚举 (a,b,d,p)，用精确整数比较三个三角形三边平方的比
 *      （排序后交叉相乘），完全不知道任何推导结论。代价 ~N₀⁴/96，只能做到 N₀≈200。
 *   ② 剪枝定义级暴力：只枚举 (a,p,u)，v 由「ABP~CDP 两种角度对应」给出的两条方程
 *      （v=2pq/u 或 v=pu/q）算出候选，再对每个候选**用 ① 的直接相似判定复核**，
 *      并检查第三个三角形。代价 ~N₀³/12，可以做到 N₀=2000。
 *   ③ 除数枚举：直接用「a=2p 且 uv=2p²」/「u=v=2w 且 pq=2w²」两族条件，
 *      逐个分解 2p²、枚举因数对计数 —— 与 solution.kt 的 (C,x,y) 参数化/网点计数
 *      完全不同的实现路线，可跑到 N=10^6（10^7 视机器而定）。
 *   ④ 小规模互证：① 与 ③ 在建得动的所有 N 上逐一相等。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0299/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

// ───────────────────────── 基础：精确相似判定 ─────────────────────────

private fun sq(x1: Long, y1: Long, x2: Long, y2: Long): Long {
    val dx = x1 - x2
    val dy = y1 - y2
    return dx * dx + dy * dy
}

/** 三边平方排序后交叉相乘判定相似（非退化三角形才可能为真）。 */
private fun similar(
    s1: LongArray, s2: LongArray,
): Boolean {
    if (s1[0] == 0L || s2[0] == 0L) return false
    return s1[0] * s2[1] == s1[1] * s2[0] && s1[1] * s2[2] == s1[2] * s2[1]
}

private fun sortedSides(x1: Long, y1: Long, x2: Long, y2: Long, x3: Long, y3: Long): LongArray {
    val s = longArrayOf(sq(x1, y1, x2, y2), sq(x2, y2, x3, y3), sq(x3, y3, x1, y1))
    if (s[0] > s[1]) { val t = s[0]; s[0] = s[1]; s[1] = t }
    if (s[1] > s[2]) { val t = s[1]; s[1] = s[2]; s[2] = t }
    if (s[0] > s[1]) { val t = s[0]; s[0] = s[1]; s[1] = t }
    return s
}

/** 判定三元组 (a,b,d)（a=c）与给定 P=(p,q)（p+q=a）是否使三个三角形两两相似。 */
private fun threeSimilar(a: Long, b: Long, d: Long, p: Long, q: Long): Boolean {
    val t1 = sortedSides(a, 0, b, 0, p, q)          // ABP
    val t2 = sortedSides(0, a, 0, d, p, q)          // CDP
    val t3 = sortedSides(b, 0, 0, d, p, q)          // BDP
    return similar(t1, t2) && similar(t1, t3) && similar(t2, t3)
}

private fun key(a: Long, b: Long, d: Long) = (a shl 40) or (b shl 20) or d

// ───────────────────────── ① 定义级纯暴力 ─────────────────────────

/** 四重枚举 (a,b,d,p)，直接判定相似；返回不同三元组 (a,b,d) 的个数。 */
private fun countPure(n: Long, collect: MutableSet<Long>? = null): Long {
    val found = collect ?: HashSet<Long>()
    var a = 2L
    while (2 * a + 2 < n) {
        var b = a + 1
        while (b + a + 1 < n) {
            var d = a + 1
            while (b + d < n) {
                var p = 1L
                while (p < a) {
                    val q = a - p
                    if (threeSimilar(a, b, d, p, q)) found.add(key(a, b, d))
                    p++
                }
                d++
            }
            b++
        }
        a++
    }
    return found.size.toLong()
}

/** 同 ①，但 p 放宽到 [lo,hi]（覆盖 P 落在线段 AC 之外的一切可能位置），用于确认「P 必在段内」。 */
private fun countPureWide(n: Long, lo: Long, hi: Long): Long {
    val found = HashSet<Long>()
    var a = 2L
    while (2 * a + 2 < n) {
        var b = a + 1
        while (b + a + 1 < n) {
            var d = a + 1
            while (b + d < n) {
                var p = lo
                while (p <= hi) {
                    val q = a - p
                    if (threeSimilar(a, b, d, p, q)) found.add(key(a, b, d))
                    p++
                }
                d++
            }
            b++
        }
        a++
    }
    return found.size.toLong()
}

// ───────────────────────── ② 剪枝定义级暴力 ─────────────────────────

/**
 * 枚举 (a,p,u)，v 只可能取 2pq/u（ABP~CDP 的 B↔P 对应）或 pu/q（B↔D 对应，会被复核否掉）；
 * 两个候选都用 ① 的直接相似判定复核后才计数。返回不同三元组个数。
 */
private fun countPruned(n: Long): Long {
    val found = HashSet<Long>()
    var a = 2L
    while (2 * a + 2 < n) {
        var p = 1L
        while (p < a) {
            val q = a - p
            val twoPq = 2 * p * q
            var u = 1L
            while (2 * a + u + 1 < n) {
                if (twoPq % u == 0L) {
                    val v = twoPq / u
                    if (2 * a + u + v < n && threeSimilar(a, a + u, a + v, p, q)) {
                        found.add(key(a, a + u, a + v))
                    }
                }
                val pu = p * u
                if (pu % q == 0L) {
                    val v = pu / q
                    if (v >= 1 && 2 * a + u + v < n && threeSimilar(a, a + u, a + v, p, q)) {
                        found.add(key(a, a + u, a + v))
                    }
                }
                u++
            }
            p++
        }
        a++
    }
    return found.size.toLong()
}

// ───────────────────────── ③ 除数枚举（独立全程计数）─────────────────────────

/** 最小素因子筛。 */
private fun spfSieve(limit: Int): IntArray {
    val spf = IntArray(limit + 1) { it }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (spf[i] == i) {
            var j = i.toLong() * i
            while (j <= limit) {
                if (spf[j.toInt()] == j.toInt()) spf[j.toInt()] = i
                j += i
            }
        }
        i++
    }
    return spf
}

/**
 * 2·m² 的全部因数。注意 2 的指数要独立处理：2·m² 中 2 的指数 = 1 + 2·v₂(m)，
 * 若把 [2,1] 直接乘进 m 的分解，m 为偶数时因数会重复计数。
 */
private fun divisorsOf2m2(m: Int, spf: IntArray): LongArray {
    var x = m
    var divs = longArrayOf(1L)
    var e2 = 1
    while (x % 2 == 0) { x /= 2; e2 += 2 }
    run {
        val newDivs = LongArray(divs.size * (e2 + 1))
        var k = 0
        for (d in divs) {
            var t = 1L
            repeat(e2 + 1) { newDivs[k++] = d * t; t *= 2 }
        }
        divs = newDivs
    }
    while (x > 1) {
        val pr = spf[x]
        var e = 0
        while (x % pr == 0) { x /= pr; e++ }
        val newDivs = LongArray(divs.size * (2 * e + 1))
        var k = 0
        for (d in divs) {
            var t = 1L
            repeat(2 * e + 1) { newDivs[k++] = d * t; t *= pr }
        }
        divs = newDivs
    }
    return divs
}

/**
 * 独立全程计数：
 *   情形 A：a=2p，有序 (u,v) 满足 uv=2p² 且 4p+u+v<n；
 *   情形 B：无序 {p,q} 满足 pq=2w²、u=2w=v 且 2(p+q+2w)<n。
 */
private fun countDivisor(n: Long): Long {
    val pmax = (n / 4 + 2).toInt()
    val spf = spfSieve(pmax + 2)
    var total = 0L
    var p = 1L
    while (4 * p + 2 < n) {
        val m = 2 * p * p
        for (u in divisorsOf2m2(p.toInt(), spf)) {
            if (4 * p + u + m / u < n) total++
        }
        p++
    }
    var w = 1L
    while (2 * (2 + 2 * w) < n) {
        val m = 2 * w * w
        for (pp in divisorsOf2m2(w.toInt(), spf)) {
            val qq = m / pp
            if (pp <= qq && 2 * (pp + qq + 2 * w) < n) total++
        }
        w++
    }
    return total
}

// ───────────────────────── 计时与主流程 ─────────────────────────

private fun best(tag: String, expected: Long, rounds: Int = 3, f: () -> Long): Double {
    var bestMs = Double.MAX_VALUE
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${r + 1} 轮漂移：$out ≠ $expected" }
        if (ms < bestMs) bestMs = ms
    }
    println("$tag：${"%.1f".format(bestMs)} ms（$rounds 轮最优）")
    return bestMs
}

fun main() {
    println("== ① 定义级纯暴力（四重枚举 + 直接相似判定）==")
    val sols100 = HashSet<Long>()
    val c100 = countPure(100, sols100)
    println("b+d < 100：不同三元组 = $c100（题面样例 92）")
    check(c100 == 92L)
    println("  样例三元组在解集中：(2,3,4)=${key(2, 3, 4) in sols100}，(2,4,3)=${key(2, 4, 3) in sols100}，(3,5,5)=${key(3, 5, 5) in sols100}")
    val c150 = countPure(150)
    val c200 = countPure(200)
    println("b+d < 150：$c150；b+d < 200：$c200")
    val cWide = countPureWide(100, -100, 200)   // p 放宽到 [-100,200]：覆盖段外全部可能性
    check(cWide == 92L) { "放宽 p 后解集改变：$cWide" }
    println("把 p 放宽到 [-100,200]（P 可取直线 AC 上线段外的整点）：仍是 92 —— P 必在段内")

    println()
    println("== ② 剪枝定义级暴力（角度候选 + 直接复核）==")
    val p100 = countPruned(100)
    val p200 = countPruned(200)
    check(p100 == c100 && p200 == c200) { "剪枝暴力与纯暴力不一致：$p100/$p200 vs $c100/$c200" }
    println("b+d < 100：$p100；b+d < 200：$p200（与纯暴力一致）")
    val p500 = countPruned(500)
    val p1000 = countPruned(1000)
    val p2000 = countPruned(2000)
    println("b+d < 500：$p500；b+d < 1000：$p1000；b+d < 2000：$p2000")

    println()
    println("== ③ 除数枚举（独立全程计数，与 solution.kt 参数化路线不同）==")
    val d100 = countDivisor(100)
    val d1e5 = countDivisor(100_000)
    val d1e6 = countDivisor(1_000_000)
    println("b+d < 100：$d100（样例 92）；b+d < 10^5：$d1e5（样例 320471）；b+d < 10^6：$d1e6")
    check(d100 == 92L && d1e5 == 320471L && d1e6 == 3969774L)
    val t1e7 = System.nanoTime()
    val d1e7 = countDivisor(10_000_000)
    println("b+d < 10^7：$d1e7（${"%.0f".format((System.nanoTime() - t1e7) / 1e6)} ms，与 solution.kt 两路径一致 ⇒ 第三套实现独立复现 10^7 级）")
    check(d1e7 == 47345573L)
    // 与 ② 的中等规模逐个对齐
    check(countDivisor(1000) == p1000 && countDivisor(2000) == p2000) { "除数法与剪枝法不一致" }
    println("b+d < 1000：$p1000；b+d < 2000：$p2000（除数法与剪枝暴力一致）")

    println()
    println("== ④ 计时（JIT 预热后 3 轮最优）==")
    val msPure = best("纯暴力 b+d<200（定义级四重枚举）", c200) { countPure(200) }
    val msPruned = best("剪枝暴力 b+d<2000（角度候选 + 直接复核）", p2000) { countPruned(2000) }
    val msDiv = best("除数枚举 b+d<10^6", d1e6) { countDivisor(1_000_000) }

    println()
    println("== ⑤ 外推参照（为什么全尺寸只能靠 solution.kt）==")
    val j200 = 200.0 * 200.0 * 200.0 * 200.0 / 96      // 纯暴力判定次数 ~N₀⁴/96
    val perJudge = msPure / j200                        // ms / 次判定
    val ratio2000 = Math.pow(2000.0 / 200.0, 4.0)
    val ratioHuge = Math.pow(1e8 / 200.0, 4.0)
    println("纯暴力 b+d<200 实测判定 ~${"%.2e".format(j200)} 次（${"%.1f".format(msPure)} ms）")
    println("外推 b+d<2000：~${"%.2e".format(j200 * ratio2000)} 次 ≈ ${"%.1f".format(perJudge * j200 * ratio2000 / 1000 / 60)} 分钟")
    println("外推 b+d<10^8：~${"%.1e".format(j200 * ratioHuge)} 次 ≈ ${"%.1f".format(perJudge * j200 * ratioHuge / 1000 / 60 / 60 / 24 / 365)} 年，物理不可行")
    println("本机实测：纯暴力 ${"%.1f".format(msPure)} ms、剪枝暴力 ${"%.1f".format(msPruned)} ms、除数枚举 ${"%.1f".format(msDiv)} ms")
}
