#!/usr/bin/env kotlin
/**
 * Project Euler 292 — Pythagorean Polygons（毕达哥拉斯多边形）
 *
 * 题目
 * ────
 * pythagorean polygon：凸多边形，满足顶点数 ≥ 3、无三点共线、顶点均为整点、每条边长均为整数。
 * P(n) = 周长 ≤ n 的这类多边形个数（仅平移视为同一个）。
 * 已知 P(4) = 1、P(30) = 3655、P(60) = 891045，求 P(120)。
 *
 * 建模：边向量 → 方向集合
 * ──────────────────────
 * 1. 多边形（平移类）与「边向量按极角排成的循环序列」一一对应：给定边向量就还原出形状，
 *    起点无关即平移类。
 * 2. 凸 + 无三点共线 ⇔ 边方向沿周长严格递增（每个方向至多出现一次）。理由：相邻两边同向
 *    就是三个相邻顶点共线；而凸多边形边界上的三个共线顶点必是相邻边共线的情形。
 *    反过来，一组互异方向按极角排列、若 Σe_i = 0 且个数 ≥ 3：由于任意相邻两方向夹角
 *    < π（否则其余方向全落在一个开半平面内，正系数和为 0 不可能），该闭合折线自动是
 *    严格凸的、无三点共线的多边形。
 * 3. 边长整数 ⇒ 每个原始方向必须是「毕达哥拉斯方向」：边 = t·(u,v)，t ≥ 1 整数，
 *    gcd(|u|,|v|) = 1（原始向量），边长 = t·√(u²+v²) 为整数 ⇔ u²+v² 是完全平方
 *    （若不然 √(u²+v²) 无理，整数倍仍无理）。轴方向 (±1,0),(0,±1) 是 u²+v² = 1 的特例。
 *    于是 n = 120 时的可用方向 = 4 个轴方向 + 19 组原始毕达哥拉斯三元组 × 8 = 156 个方向。
 *
 * 计数 DP
 * ───────
 * 把可用方向按极角排序（半平面 + 叉积比较，不用浮点 atan2），再对方向逐个做
 * 「跳过 / 取 t 条（t ≥ 1）」的背包式 DP。状态 (x, y, p)：已选边向量之和、已用周长。
 * dp 是一张扁平 LongArray，按 p 降序原地更新——这样写进新格的计数不会被本次方向重复读到，
 * 等价于「每个方向至多用一次」。
 * 可达性剪枝：任何前缀状态剩下的边要闭合回原点，故 |x|, |y| ≤ n − p（剩余总长 ≤ n−p）。
 * 终态 (0,0,p)（p = 1..n）求和，再减去退化情形：两条反向边的「二边形」{d,−d}（t 相同，
 * 周长 2tL ≤ n，共 Σ floor(n/2L) 个，每个方向对在求和里出现两次故除 2）。
 *
 * 验证
 * ────
 * 1. 题给的三个值 P(4) = 1、P(30) = 3655、P(60) = 891045 全部由本程序复现（硬 gate）；
 * 2. brute-force.kt 给出两套独立实现：定义级 DFS 枚举（小 n，显式要求 |D| ≥ 3）与
 *    左/右半平面 meet-in-the-middle DP（全尺寸 n = 120），与本文件互证；
 * 3. 方向排序用叉积、计数用扁平数组、剪枝条件都各自做过量级与边界检查。
 *
 * 复杂度
 * ──────
 * 状态 (n+1)(2n+1)² ≈ 7.0×10⁶，方向 156 个，转移约 3×10⁸ 次整数加法；本机秒级。
 * 内存一个扁平 LongArray（n = 120 时约 56 MB），无其它大结构。
 *
 * 运行
 * ────
 * OUTDIR=/tmp/kc-0292-sol bash scripts/kotlinc-shim.sh content/problems/0292/solution.kt
 * java -Xmx4g -cp /tmp/kc-0292-sol:<kotlin-stdlib> SolutionKt
 */

private const val N120 = 120

/** 一个可用的原始方向 (u,v) 与其原始长度 len = √(u²+v²)（整数）。 */
private class Dir(val u: Int, val v: Int, val len: Int)

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

/** 半平面标记：v > 0 或 (v = 0 且 u > 0) 记 0（极角 [0, π)），否则记 1（极角 [π, 2π)）。 */
private fun half(u: Int, v: Int): Int = if (v > 0 || (v == 0 && u > 0)) 0 else 1

/**
 * 枚举全部原始毕达哥拉斯方向 (u,v)：gcd(|u|,|v|) = 1 且 u²+v² 为完全平方数 L² ≤ n，
 * 按极角递增排序（叉积比较，精确）。
 */
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
            val cr = a.u * b.v - a.v * b.u   // cr > 0 ⇒ a 在 b 的顺时针侧，先出现
            when {
                cr > 0 -> -1
                cr < 0 -> 1
                else -> 0
            }
        }
    })
    return list
}

/**
 * 主路径：对方向按极角顺序做 (x,y,p) 背包 DP，返回周长 ≤ n 的毕达哥拉斯多边形数。
 */
private fun countP(n: Int, dirs: List<Dir>, trace: Boolean = false): Long {
    val side = 2 * n + 1
    val pStride = side * side
    val dp = LongArray((n + 1) * pStride)
    dp[0 * pStride + n * side + n] = 1L                       // 空集：(0,0)，周长 0
    for (d in dirs) {
        val u = d.u
        val v = d.v
        val L = d.len
        var p = n
        while (p >= 0) {
            val rem = n - p
            if (rem >= L) {
                val cap = if (p < rem) p else rem             // |x|,|y| ≤ min(p, n−p)（可达状态界）
                val tMax = rem / L
                val pBase = p * pStride
                var x = -cap
                while (x <= cap) {
                    val rowBase = pBase + (x + n) * side
                    var y = -cap
                    while (y <= cap) {
                        val c = dp[rowBase + y + n]
                        if (c != 0L) {
                            var t = 1
                            while (t <= tMax) {
                                val np = p + t * L
                                val ncap = n - np            // 写剪枝：剩余边要闭合，(n−p) 是剩余总长上界
                                val nx = x + t * u
                                val ny = y + t * v
                                if (nx >= -ncap && nx <= ncap && ny >= -ncap && ny <= ncap) {
                                    dp[np * pStride + (nx + n) * side + (ny + n)] += c
                                }
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
    for (p in 1..n) total += dp[p * pStride + n * side + n]   // 排除 p = 0 的空集
    // 减去退化「二边形」{d, −d}：两方向各取 t 条，t·d + t·(−d) = 0，周长 2tL ≤ n。
    // 每个无序方向对在下面的求和里计两次（d 与 −d 各一次），最后除以 2。
    var degenerate = 0L
    for (d in dirs) if (2 * d.len <= n) degenerate += n / (2 * d.len)

    if (trace) {
        var axis = 0
        for (d in dirs) if (d.len == 1) axis++
        println("  n = $n：可用方向 ${dirs.size} 个（其中轴方向 $axis 个）；" +
            "闭合配置（含退化二边形，不含空集）$total 个，其中退化二边形 ${degenerate / 2} 个 → 多边形 ${total - degenerate / 2} 个")
    }
    return total - degenerate / 2
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    println("== 0. 方向枚举（n = 120）==")
    val dirs120 = buildDirections(N120)
    println("原始毕达哥拉斯方向数 = ${dirs120.size}")
    println("前 6 个（极角序）: " + dirs120.take(6).joinToString(" ") { "(${it.u},${it.v})/L${it.len}" })
    println("后 3 个（极角序）: " + dirs120.takeLast(3).joinToString(" ") { "(${it.u},${it.v})/L${it.len}" })

    println()
    println("== 1. 题给样例 gate：P(4) = 1、P(30) = 3655、P(60) = 891045 ==")
    val gates = listOf(4 to 1L, 30 to 3655L, 60 to 891045L)
    val gateTimes = ArrayList<Pair<Int, Double>>()
    for ((n, expected) in gates) {
        val dirs = buildDirections(n)
        val t0 = System.nanoTime()
        val got = countP(n, dirs, trace = true)
        val ms = (System.nanoTime() - t0) / 1e6
        check(got == expected) { "P($n) = $got ≠ 题给 $expected" }
        println("  P($n) = $got ✓（题给 $expected，${"%.1f".format(ms)} ms）")
        gateTimes.add(n to ms)
    }

    println()
    println("== 2. 全尺寸：P(120) ==")
    val ans = countP(N120, dirs120, trace = true)
    println("P(120) = $ans")

    println()
    println("== 3. 计时 ==")
    bestOf3("P(120) 主路径（极角序 + (x,y,p) 背包 DP，p 降序原地更新）", ans) {
        countP(N120, dirs120)
    }
    for ((n, _) in gateTimes) {
        val dirs = buildDirections(n)
        val expected = when (n) {
            4 -> 1L
            30 -> 3655L
            else -> 891045L
        }
        bestOf3("P($n)（同一实现，缩规模 gate）", expected) { countP(n, dirs) }
    }
    bestOf3("方向枚举（n = 120，含排序）", dirs120.size.toLong()) { buildDirections(N120).size.toLong() }

    println()
    println("== 4. 结果 ==")
    println("P(4) = 1 ✓，P(30) = 3655 ✓，P(60) = 891045 ✓，P(120) = $ans")
}
