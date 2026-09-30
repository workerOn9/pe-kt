#!/usr/bin/env kotlin
/**
 * Project Euler 292 — Pythagorean Polygons（毕达哥拉斯多边形）：暴力 / 独立对照
 *
 * 与 solution.kt 不共享代码，提供三套真正不同的机制：
 *
 * ① 定义级 DFS 枚举（小 n，最"暴力"的一路）
 *    直接递归枚举「每个方向取 0 条或 t ≥ 1 条」，显式判 闭合 Σ = 0 并且 已用方向数 ≥ 3
 *    （枚举过程中直接数方向个数，不依赖 solution.kt 的「减退化二边形」修正）。
 *    这是对建模本身的独立检验：n = 4、30、60 上分别给出 1、3655、891045，与题给三个值逐一相等。
 *    代价是指数级——节点数 n = 30 约 2.5×10⁶、n = 60 约 1.6×10⁹；n = 120 预估 10¹² 量级，不可行。
 *
 * ② 朴素单扫描 DP（全尺寸 n = 120）
 *    方向按极角逐个扫描、每个方向「跳过 / 取 t 条」，状态 (x, y, p) 用扁平数组（p 降序原地更新）。
 *    但**不做任何剪枝**：状态盒是完整的 (2n+1)×(2n+1)，也没有 |x| ≤ n−p 的可达性限制。
 *    这正是"没想出剪枝之前"的写法，作为暴力基线（meta 的 bruteForceBaselineMs 口径）。
 *
 * ③ 左 / 右半平面 meet-in-the-middle（全尺寸 n = 120）
 *    把方向按第一坐标符号劈成两半：R = {u > 0} ∪ {(0,1)}，L = {u < 0} ∪ {(0,−1)}。
 *    闭合要求两边边向量和为零 ⇒ 每半各跑一份 (x, y, p) 直方图（左半存 −向量 使 x ≥ 0），
 *    再把「R 的部分和 = L 的部分和、周长之和 ≤ n」的项乘起来。
 *    与 ② / solution.kt 的切分方式（两半独立直方图 + 配对）不同，是第二套独立机制。
 *
 * 退化口径：②③ 与 solution.kt 一样要减掉空集与退化二边形 {d, −d}；① 用显式 |D| ≥ 3 过滤。
 *   n = 4..60 的 10 个值上，① 与 ③ 完全一致，说明这个减法口径正确。
 *
 * 三条门（题给值）：P(4) = 1、P(30) = 3655、P(60) = 891045。
 * 全尺寸 n = 120：② 与 ③ 各给出 3600060866（= solution.kt 的输出），本文件末尾 assert。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0292/brute-force.kt -d <目录>
 *      java -Xmx4g -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

private const val N120 = 120

private fun gcdInt(a: Int, b: Int): Int {
    var x = if (a < 0) -a else a
    var y = if (b < 0) -b else b
    while (y != 0) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

private fun isqrt(v: Int): Int {
    var r = Math.sqrt(v.toDouble()).toInt()
    while (r > 0 && r * r > v) r--
    while ((r + 1).toLong() * (r + 1) <= v) r++
    return r
}

private fun half(u: Int, v: Int): Int = if (v > 0 || (v == 0 && u > 0)) 0 else 1

private class Dir(val u: Int, val v: Int, val len: Int)

/** 原始毕达哥拉斯方向（gcd = 1，u²+v² = L² 为完全平方），按极角递增。 */
private fun buildDirections(n: Int): List<Dir> {
    val list = ArrayList<Dir>()
    for (u in -n..n) {
        for (v in -n..n) {
            if (u == 0 && v == 0) continue
            if (gcdInt(u, v) != 1) continue
            val q = u * u + v * v
            val s = isqrt(q)
            if (s * s != q || s > n) continue
            list.add(Dir(u, v, s))
        }
    }
    list.sortWith(Comparator { a, b ->
        val ha = half(a.u, a.v)
        val hb = half(b.u, b.v)
        if (ha != hb) ha - hb
        else {
            val cr = a.u * b.v - a.v * b.u
            when {
                cr > 0 -> -1
                cr < 0 -> 1
                else -> 0
            }
        }
    })
    return list
}

/** 退化口径（②③ 共用）：Σ over 全部方向 floor(n/2L) / 2 = 退化二边形 {d,−d} 的个数。 */
private fun degeneratePairs(n: Int, dirs: List<Dir>): Long {
    var sum = 0L
    for (d in dirs) if (2 * d.len <= n) sum += n / (2 * d.len)
    return sum / 2
}

// ───────────────── ① 定义级 DFS：显式枚举方向与倍数，显式要求 |D| ≥ 3 ─────────────────

private var dfsNodes = 0L

private fun dfsCount(n: Int): Long {
    val dirs = buildDirections(n)
    val m = dirs.size
    val us = IntArray(m)
    val vs = IntArray(m)
    val ls = IntArray(m)
    for (i in 0 until m) {
        us[i] = dirs[i].u
        vs[i] = dirs[i].v
        ls[i] = dirs[i].len
    }
    var count = 0L
    dfsNodes = 0L
    fun rec(i: Int, x: Int, y: Int, p: Int, used: Int) {
        dfsNodes++
        val rem = n - p
        if (x.toLong() * x + y.toLong() * y > rem.toLong() * rem) return   // 剩下的总长不足以闭合
        if (used + (m - i) < 3) return                                    // 凑不满 3 条边
        if (i == m) {
            if (x == 0 && y == 0 && used >= 3) count++
            return
        }
        rec(i + 1, x, y, p, used)                                         // 跳过方向 i
        var t = 1
        while (t * ls[i] <= rem) {                                        // 取 t 条（t ≥ 1）
            rec(i + 1, x + t * us[i], y + t * vs[i], p + t * ls[i], used + 1)
            t++
        }
    }
    rec(0, 0, 0, 0, 0)
    return count
}

// ───────────────── ② 朴素单扫描 DP（无剪枝，完整状态盒） ─────────────────

private fun naiveCount(n: Int, dirs: List<Dir>): Long {
    val side = 2 * n + 1
    val pStride = side * side
    val dp = LongArray((n + 1) * pStride)
    dp[0 * pStride + n * side + n] = 1L                                       // 空集：(0,0)，周长 0
    for (d in dirs) {
        val u = d.u
        val v = d.v
        val L = d.len
        var p = n
        while (p >= 0) {
            val rem = n - p
            if (rem >= L) {
                val tMax = rem / L
                val pBase = p * pStride
                var x = -n
                while (x <= n) {
                    val rowBase = pBase + (x + n) * side
                    var y = -n
                    while (y <= n) {
                        val c = dp[rowBase + y + n]
                        if (c != 0L) {
                            var t = 1
                            while (t <= tMax) {
                                dp[(p + t * L) * pStride + (x + t * u + n) * side + (y + t * v + n)] += c
                                t++
                            }
                        }
                        y++
                    }
                    x++
                }
            }
            p--
        }
    }
    var total = 0L
    for (p in 1..n) total += dp[p * pStride + n * side + n]                   // 排除 p = 0 的空集
    return total - degeneratePairs(n, dirs)
}

// ───────────────── ③ 左右半平面 meet-in-the-middle（全尺寸） ─────────────────

/**
 * 半平面直方图：对半个方向集合做「跳过 / 取 t 条」DP。
 * x 坐标恒为非负（左半传入的已是 −向量），x ≤ p，|y| ≤ p，精确到每个 p。
 * 返回扁平数组 idx = p·(n+1)(2n+1) + x·(2n+1) + (y+n)。
 */
private fun halfHistogram(n: Int, ds: List<Dir>): LongArray {
    val sx = n + 1
    val sy = 2 * n + 1
    val pStride = sx * sy
    val dp = LongArray((n + 1) * pStride)
    dp[0 * pStride + 0 * sy + n] = 1L                                        // 空集：(0,0)，周长 0
    for (d in ds) {
        val u = d.u
        val v = d.v
        val L = d.len
        var p = n
        while (p >= 0) {
            val rem = n - p
            if (rem >= L) {
                val tMax = rem / L
                val pBase = p * pStride
                var x = 0
                while (x <= p) {
                    val rowBase = pBase + x * sy
                    var y = -p
                    while (y <= p) {
                        val c = dp[rowBase + y + n]
                        if (c != 0L) {
                            var t = 1
                            while (t <= tMax) {
                                val np = p + t * L
                                // nx ≤ np ≤ n 与 |ny| ≤ np ≤ n 自动成立，无需写剪枝
                                dp[np * pStride + (x + t * u) * sy + (y + t * v + n)] += c
                                t++
                            }
                        }
                        y++
                    }
                    x++
                }
            }
            p--
        }
    }
    return dp
}

/** meet-in-the-middle：R/L 两半直方图配对，减去空集与退化二边形。 */
private fun mitmCount(n: Int): Long {
    val dirs = buildDirections(n)
    val right = ArrayList<Dir>()              // u > 0，外加 (0,1)
    val leftNeg = ArrayList<Dir>()            // u < 0 取反，外加 (0,1)
    for (d in dirs) {
        if (d.u > 0) right.add(d)
        else if (d.u < 0) leftNeg.add(Dir(-d.u, -d.v, d.len))
        else if (d.v > 0) right.add(d)
        else leftNeg.add(Dir(-d.u, -d.v, d.len))
    }
    val nR = halfHistogram(n, right)          // 精确到每个 p：配对时取 p1
    val nL = halfHistogram(n, leftNeg)
    val sx = n + 1
    val sy = 2 * n + 1
    val pStride = sx * sy
    // 左半沿 p 做前缀累加：nL[p][x][y] ← Σ_{p' ≤ p}
    for (p in 1..n) {
        val cur = p * pStride
        val prev = cur - pStride
        for (i in 0 until pStride) nL[cur + i] += nL[prev + i]
    }
    var total = 0L
    // —— 配对：R 的部分和 (x,y,p1) 与 L 的部分和（存的是 −向量）必须相同，p1 ≥ 1 排除空集
    for (p1 in 1..n) {
        val rem = n - p1
        val rBase = p1 * pStride
        val lBase = rem * pStride
        for (x in 0..p1) {
            val rRow = rBase + x * sy
            val lRow = lBase + x * sy
            for (y in -p1..p1) {
                val a = nR[rRow + y + n]
                if (a != 0L) {
                    val b = nL[lRow + y + n]
                    if (b != 0L) total += a * b
                }
            }
        }
    }
    return total - degeneratePairs(n, dirs)
}

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
    val gates = mapOf(4 to 1L, 30 to 3655L, 60 to 891045L)

    println("== ① 定义级 DFS 枚举（显式 |D| ≥ 3，指数级）==")
    val dfsNs = listOf(4, 8, 12, 16, 20, 24, 30, 40, 50, 60)
    val dfsRes = HashMap<Int, Long>()
    var dfsTotalMs = 0.0
    for (n in dfsNs) {
        val t0 = System.nanoTime()
        val c = dfsCount(n)
        val ms = (System.nanoTime() - t0) / 1e6
        dfsTotalMs += ms
        dfsRes[n] = c
        val gate = if (gates.containsKey(n)) {
            check(c == gates[n]) { "DFS P($n) = $c ≠ 题给 ${gates[n]}" }
            " ← 题给值 ✓"
        } else ""
        println("  n = $n：DFS = $c（节点 ${"%,d".format(dfsNodes)}，${"%.1f".format(ms)} ms）$gate")
    }
    println("  DFS 合计 ${"%.1f".format(dfsTotalMs)} ms；节点数按 30→60 的倍率（×640）外推，n = 120 约 10¹² 节点，不可行")

    println()
    println("== ② 朴素单扫描 DP（全尺寸，无剪枝、完整 (2n+1)² 状态盒）==")
    val dirs120 = buildDirections(N120)
    val fullNaive = naiveCount(N120, dirs120)
    println("  n = 120：朴素 DP = $fullNaive")

    println()
    println("== ③ 左右半平面 meet-in-the-middle（全尺寸 + 小规模对拍）==")
    for (n in dfsNs) {
        val c = mitmCount(n)
        check(c == dfsRes[n]) { "MITM P($n) = $c ≠ DFS ${dfsRes[n]}" }
        println("  n = $n：MITM = DFS = $c ✓")
    }
    val fullMitm = mitmCount(N120)
    println("  n = 120：MITM = $fullMitm")

    println()
    println("== ④ 计时（n = 120 全尺寸；缩规模的另标）==")
    val msNaive = best("朴素单扫描 DP（无剪枝）n = 120", fullNaive) { naiveCount(N120, dirs120) }
    val msMitm = best("MITM n = 120（两半直方图 + 配对）", fullMitm) { mitmCount(N120) }
    val msDfs30 = best("DFS n = 30（定义级，缩规模）", dfsRes[30]!!, rounds = 3) { dfsCount(30) }
    val msDfs60 = best("DFS n = 60（定义级，缩规模）", dfsRes[60]!!, rounds = 1) { dfsCount(60) }

    // 全尺寸答案由 solution.kt 与 ②③ 三套独立实现给出同一个数
    check(fullNaive == 3600060866L && fullMitm == 3600060866L) {
        "全尺寸不一致：naive = $fullNaive，MITM = $fullMitm"
    }

    println()
    println("== ⑤ 结果 ==")
    println("P(4) = 1 ✓，P(30) = 3655 ✓，P(60) = 891045 ✓（DFS 与 MITM 双路复现题给值）")
    println("P(120) = $fullMitm = $fullNaive（MITM 与朴素 DP 一致；与 solution.kt 的极角序剪枝 DP 也一致）")
    println("耗时：朴素 DP n=120 ${"%.1f".format(msNaive)} ms；MITM n=120 ${"%.1f".format(msMitm)} ms；" +
        "DFS n=30 ${"%.1f".format(msDfs30)} ms；DFS n=60 ${"%.1f".format(msDfs60)} ms")
}
