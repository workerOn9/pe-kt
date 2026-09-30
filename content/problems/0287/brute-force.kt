#!/usr/bin/env kotlin
/**
 * Project Euler 287 — Quadtree Encoding（四叉树编码）：暴力 / 对照
 * 独立实现，与 solution.kt 不共享核心代码。
 *
 * 路径 1（定义级像素网格暴力，meta 的 bruteForceBaselineMs 口径）
 * ──────────────────────────────────────────────────────────
 * 完全照题面定义办：先把 D_N 的 2^N × 2^N 个像素逐一置成黑/白（判据
 * (x−2^{N−1})² + (y−2^{N−1})² ≤ 2^{2N−2}），再用二维前缀和 O(1) 判断任意区域是否均色，
 * 自顶向下建最小四叉树（均色 → 叶 2 位，否则 1 位分裂 + 4 个子描述）。代价 Θ(4^N)：
 * N = 13 需 6.7×10⁷ 像素与 2.7×10⁸ 字节的前缀和（本机约 80 ms），N = 14 内存翻到 1 GB，
 * N = 24 需要 2.8×10^14 个像素（按本机像素速率外推约 5 天、内存 281 TB）——不可行。
 * 因此它把主路径钉到 N = 13；N = 24 的答案由 solution.kt 的三条几何路径互证。
 *
 * 路径 2（最朴素扫描版，验证路径 1 的前缀和机制）
 * ────────────────────────────────────────────
 * 不建前缀和，直接对每个区域逐像素数黑点数（代价 O(4^N·N)，只跑到 N ≤ 6），与路径 1 全对。
 *
 * 路径 3（几何行扫描，和 solution.kt 主路径同构的独立副本）
 * ────────────────────────────────────────────────────────
 * 只为在 N = 2…13 上与暴力逐值对拍；不参与计时。
 *
 * 题面例子：两条给定序列都按「0 分裂 / 10 黑 / 11 白」解码，验证得到同一幅 4×4 图像，
 * 且该图像的最小序列长度恰为 16（与题面一致）。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0287/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 */

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0L && r * r > v) r--
    while ((r + 1L) * (r + 1L) <= v) r++
    return r
}

// ─────────────── 路径 1：像素网格 + 前缀和（暴力基线） ───────────────

/** 返回 (S, 最小序列长度)；S = 内部节点数（混合区域）。 */
private fun brutePrefix(N: Int): Pair<Long, Long> {
    val m = 1 shl N
    val c = m shr 1
    val r2 = c.toLong() * c
    val pre = IntArray((m + 1) * (m + 1))
    for (y in 0 until m) {
        var acc = 0
        val base = (y + 1) * (m + 1)
        val prev = y * (m + 1)
        for (x in 0 until m) {
            val dx = x - c
            val dy = y - c
            if (dx.toLong() * dx + dy.toLong() * dy <= r2) acc++
            pre[base + x + 1] = pre[prev + x + 1] + acc
        }
    }
    fun blk(x0: Int, y0: Int, s: Int): Int =
        pre[(y0 + s) * (m + 1) + x0 + s] - pre[y0 * (m + 1) + x0 + s] -
            pre[(y0 + s) * (m + 1) + x0] + pre[y0 * (m + 1) + x0]

    var internal = 0L
    var bits = 0L
    fun rec(x0: Int, y0: Int, s: Int) {
        val b = blk(x0, y0, s)
        if (b == 0 || b == s * s) { bits += 2; return }
        check(s > 1)
        internal++
        bits += 1
        val h = s shr 1
        rec(x0, y0 + h, h); rec(x0 + h, y0 + h, h); rec(x0, y0, h); rec(x0 + h, y0, h)
    }
    rec(0, 0, m)
    return internal to bits
}

// ─────────────── 路径 2：逐像素扫描（不建前缀和，最朴素） ───────────────

private fun bruteNaive(N: Int): Pair<Long, Long> {
    val m = 1 shl N
    val c = m shr 1
    val r2 = c.toLong() * c
    val g = Array(m) { y -> BooleanArray(m) { x -> (x - c).toLong() * (x - c) + (y - c).toLong() * (y - c) <= r2 } }
    var internal = 0L
    var bits = 0L
    fun rec(x0: Int, y0: Int, s: Int) {
        var black = 0
        for (y in y0 until y0 + s) for (x in x0 until x0 + s) if (g[y][x]) black++
        if (black == 0 || black == s * s) { bits += 2; return }
        check(s > 1)
        internal++
        bits += 1
        val h = s shr 1
        rec(x0, y0 + h, h); rec(x0 + h, y0 + h, h); rec(x0, y0, h); rec(x0 + h, y0, h)
    }
    rec(0, 0, m)
    return internal to bits
}

// ─────────────── 路径 3：几何行扫描（对拍用） ───────────────

private fun geometryRows(N: Int): Long {
    val c = 1L shl (N - 1)
    val r2 = c * c
    var total = 0L
    for (k in 1..N) {
        val s = 1L shl k
        val m = 1L shl (N - k)
        var j = 0L
        while (j < m) {
            val y0 = j * s
            val y1 = y0 + s - 1L
            val e0 = abs(y0 - c)
            val e1 = abs(y1 - c)
            val dyMin = if (y0 <= c && c <= y1) 0L else min(e0, e1)
            val dyMax = max(e0, e1)
            val a = r2 - dyMin * dyMin
            if (a >= 0L) {
                val x = isqrt(a)
                val iHi = (c + x) shr k
                val iLo = (c - x) shr k
                var cnt1 = min(iHi, m - 1L) - max(iLo, 0L) + 1L
                if (cnt1 < 0L) cnt1 = 0L
                var cnt2 = 0L
                val b = r2 - dyMax * dyMax
                if (b >= 0L) {
                    val y = isqrt(b)
                    val hHi = (c + y - s + 1L) shr k
                    val hLo = (c - y + s - 1L) shr k
                    cnt2 = min(hHi, m - 1L) - max(hLo, 0L) + 1L
                    if (cnt2 < 0L) cnt2 = 0L
                }
                total += cnt1 - cnt2
            }
            j++
        }
    }
    return total
}

// ─────────────── 题面例子 ───────────────

private fun decode(bits: String, N: Int): Array<BooleanArray> {
    var pos = 0
    val g = Array(1 shl N) { BooleanArray(1 shl N) }
    fun read(x0: Int, y0: Int, s: Int) {
        when (bits[pos++]) {
            '0' -> {
                val h = s / 2
                read(x0, y0 + h, h); read(x0 + h, y0 + h, h); read(x0, y0, h); read(x0 + h, y0, h)
            }
            else -> {
                val black = bits[pos++] == '0'
                for (y in y0 until y0 + s) for (x in x0 until x0 + s) g[y][x] = black
            }
        }
    }
    read(0, 0, 1 shl N)
    check(pos == bits.length) { "序列未正好用尽：$pos / ${bits.length}" }
    return g
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---- 题面例子 ----
    val g30 = decode("001010101001011111011010101010", 2)
    val g16 = decode("0100101111101110", 2)
    var same = true
    for (y in 0 until 4) for (x in 0 until 4) if (g30[y][x] != g16[y][x]) same = false
    check(same)

    // 对解码出的图像独立编码：均色即叶（2 位），否则 1 位分裂 + 4 个子描述
    fun encodeGrid(g: Array<BooleanArray>): Long {
        fun rec(x0: Int, y0: Int, s: Int): Long {
            var black = 0
            for (y in y0 until y0 + s) for (x in x0 until x0 + s) if (g[y][x]) black++
            if (black == 0 || black == s * s) return 2L
            check(s > 1)
            val h = s / 2
            return 1L + rec(x0, y0 + h, h) + rec(x0 + h, y0 + h, h) + rec(x0, y0, h) + rec(x0 + h, y0, h)
        }
        return rec(0, 0, g.size)
    }
    check(encodeGrid(g16) == 16L) { "题面例子的最小序列长度应为 16" }
    println("题面例子：两条给定序列（30 位与 16 位）解码一致，独立编码器对该图像给出最小长度 ${encodeGrid(g16)}" +
        "（16 位，与题面一致；30 位那条是多分裂的冗余写法）")

    // ---- 小 N：朴素扫描 vs 前缀和 vs 几何 ----
    for (n in 2..6) {
        val p = brutePrefix(n)
        val q = bruteNaive(n)
        check(p == q) { "N=$n：前缀和 $p ≠ 朴素扫描 $q" }
        check(p.first == geometryRows(n) && p.second == 7 * p.first + 2) { "N=$n：与几何路径/7S+2 不符" }
    }
    println("N = 2…6：前缀和暴力 = 朴素逐像素扫描 = 几何行扫描（S 与长度全同）")

    // ---- 大一点的暴力：N = 2…13 逐值 ----
    val values = ArrayList<String>()
    for (n in 2..13) {
        val p = brutePrefix(n)
        check(p.first == geometryRows(n)) { "N=$n：前缀和 S=${p.first} ≠ 几何 S=${geometryRows(n)}" }
        check(p.second == 7 * p.first + 2)
        values.add("N=$n: S=${p.first}, len=${p.second}")
    }
    println("定义级暴力 N = 2…13 与几何行扫描逐值一致：")
    values.forEach { println("  $it") }

    // ---- 计时 ----
    brutePrefix(11)
    val ms13 = bestOf3("路径 1（定义级像素网格暴力，N = 13，6.7×10⁷ 像素）", brutePrefix(13).first) { brutePrefix(13).first }

    // ---- 外推（用实测的 N = 13 墙钟时间） ----
    val factor = 1L shl 22                                  // 4^11：N = 13 → 24 的像素数增长
    println("外推：N = 24 的像素数是 N = 13（2^26 = ${1 shl 26}）的 4^11 = $factor 倍，" +
        "按 N = 13 实测 ${"%.1f".format(ms13)} ms 线性外推约 ${"%.1f".format(ms13 * factor / 86_400_000.0)} 天" +
        "（内存 2^48 字节 = 281 TB），不可行")

    println()
    val p13 = brutePrefix(13)
    println("N = 13：S = ${p13.first}，长度 = ${p13.second}（与几何路径一致）")
    println("汇总：定义级暴力 N = 13 ${"%.3f".format(ms13)} ms；solution.kt 的几何主路径以约 115 ms 完成 N = 24")
    println("check() 全部通过")
}
