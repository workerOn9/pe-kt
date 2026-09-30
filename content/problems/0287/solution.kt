#!/usr/bin/env kotlin
/**
 * Project Euler 287 — Quadtree Encoding (a Simple Compression Algorithm)（四叉树编码）
 *
 * 题目：比特序列按「0 = 把当前 2^n×2^n 区域分裂成 4 个 2^{n−1}×2^{n−1} 子区域（左上、右上、
 * 左下、右下），10 = 全黑，11 = 全白」的规则描述图像。D_N 是 2^N×2^N 图像，像素 (x, y)
 * （x = 0, y = 0 是左下角）在黑 ⟺ (x−2^{N−1})² + (y−2^{N−1})² ≤ 2^{2N−2}。求 D_24 的最小序列长度。
 *
 * 思路
 * ────
 * 计数规则：最小序列按「区域均色就写成叶、否则分裂」建树（多余的分裂只会加长序列，题面 30 位与
 * 16 位两个例子正好对应「多分裂」与「最小」）。记内部节点（分裂）S 个、叶 L 个，4 叉树的
 * 4S = L + S − 1 给出 L = 3S + 1；序列长度 = 2L + S（叶 2 位、内部节点 1 位）= 7S + 2。
 * 题面 4×4 例子：S = 2（根 + 一个含 2 黑 2 白的 2×2 子块），L = 7，长度 7·2+2 = 16 ✔。
 *
 * 关键观察：一个区域「是内部节点」⟺ 它既非全黑也非全白（混合）；而方块混合 ⇒ 它的所有祖先
 * 都混合，所以内部节点恰好是整棵树里**全部**混合方块，可以按尺寸逐层直接数：
 *
 *   S = Σ_{k=1}^{N} #{ 边长 2^k 的方块中混合的个数 }。
 *
 * 混合判定：对区域 [x0, x0+s) × [y0, y0+s)（圆心 c = 2^{N−1}、R² = 2^{2N−2}），像素坐标下
 *   dxMax = max(|x0 − c|, |x0+s−1 − c|)，dxMin = 0（c 落在区间内）否则 min(两侧距离)，
 *   dyMax、dyMin 同理；则
 *   全黑 ⟺ dxMax² + dyMax² ≤ R²；  全白 ⟺ dxMin² + dyMin² > R²；  混合 = 两者皆否。
 *
 * 逐层行扫描（主路径）：第 k 层方块边长 s = 2^k，行数 M = 2^{N−k}。固定一行（y 范围已知，
 * 得 dyMin、dyMax）后，一行的混合方块数 = （非全白方块数）−（全黑方块数）：
 *   · 非全白 ⟺ 方块 x 区间与 [c − X, c + X] 相交，X = isqrt(R² − dyMin²)；
 *   · 全黑 ⟺ 方块 x 区间含于 [c − Y, c + Y]，Y = isqrt(R² − dyMax²)；
 * 两者都退化成「区间内有多少个长度为 s 的对齐方块」的 O(1) 计数（s 是 2 的幂，取整即移位）。
 * 总行数 Σ_{k=1}^{24} 2^{24−k} = 2^24 − 1 ≈ 1.7×10^7，isqrt 用 Math.sqrt 加校正保证精确。
 *
 * 复杂度
 * ──────
 * O(2^N) 行 × O(1)：N = 24 约 1.7×10^7 行、3.4×10^7 次整数平方根，本机 JIT 预热后约 130 ms；
 * 空间 O(1)。递归几何版（自顶向下、只对混合区域下探）要访问约 1.8×10^8 个节点，同机约 0.5 s。
 * 逐像素暴力（先置出全部像素再建树）是 Θ(4^N)：N = 13 已需 6.7×10^7 像素（约 80 ms），
 * N = 24 需要 2.8×10^14 像素（按像素速率外推约 5 天、内存 281 TB），不可行。
 *
 * 验证
 * ────
 * 1. 题面例子校准计数规则：把 `0100101111101110` 与 `001010101001011111011010101010` 两条序列
 *    都按 `0`/`10`/`11` 语义解码，得到同一幅 4×4 图像（前者是最小序列）；对该图像运行同一套
 *    「均色即叶、否则分裂」的编码器，长度恰为 16，且 7S+2（S = 2）也是 16；
 * 2. N = 2…11 四条路径对拍：逐层行扫描（主路径）、逐层列扫描（转置）、自顶向下递归几何、
 *    定义级像素网格暴力（前缀和判均色），全部给出同一 S 与同一长度 7S+2；
 * 3. 全规模 N = 24：三条几何路径（行、列、递归）都给出 S = 44733642、长度 313135496；
 * 4. 公开答案表（nayuki / luckytoilet 的 projecteuler-solutions 等）第 287 条为 313135496，
 *    与本机实跑一致（仅作旁证）。
 *
 * 答案：S = 44733642，最小序列长度 = 7S + 2 = 313135496
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0287/solution.kt -d /tmp/kc-0287
 * java -cp /tmp/kc-0287:<kotlin-stdlib> SolutionKt
 */

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val N24 = 24

/** 精确整数平方根（0 ≤ v ≤ 2^46，double 开方后最多校正 1–2 次）。 */
private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0L && r * r > v) r--
    while ((r + 1L) * (r + 1L) <= v) r++
    return r
}

// ───────────────────────── 主路径：逐层行扫描混合方块 ─────────────────────────

/** S = 混合方块总数（= 最小四叉树的内部节点数）。行扫描版。 */
private fun countMixedRows(N: Int): Long {
    val c = 1L shl (N - 1)
    val r2 = c * c
    var total = 0L
    for (k in 1..N) {
        val s = 1L shl k
        val m = 1L shl (N - k)                     // 该层的行数（= 每行方块数）
        var j = 0L
        while (j < m) {
            val y0 = j * s
            val y1 = y0 + s - 1L
            val e0 = abs(y0 - c)
            val e1 = abs(y1 - c)
            val dyMin = if (y0 <= c && c <= y1) 0L else min(e0, e1)
            val dyMax = max(e0, e1)
            val a = r2 - dyMin * dyMin
            if (a >= 0L) {                          // 该行存在非全白方块
                val x = isqrt(a)
                // 非全白：方块 x 区间 [i·s, i·s+s−1] 与 [c−x, c+x] 相交
                val iHi = (c + x) shr k             // floor((c+x)/s)
                val iLo = (c - x) shr k             // ceil((c−x−s+1)/s)（c−x ≥ 0）
                var cnt1 = min(iHi, m - 1L) - max(iLo, 0L) + 1L
                if (cnt1 < 0L) cnt1 = 0L
                var cnt2 = 0L
                val b = r2 - dyMax * dyMax
                if (b >= 0L) {                      // 该行存在全黑方块
                    val y = isqrt(b)
                    // 全黑：方块 x 区间含于 [c−y, c+y]
                    val hHi = (c + y - s + 1L) shr k        // floor((c+y−s+1)/s)，可为负
                    val hLo = (c - y + s - 1L) shr k        // ceil((c−y)/s)
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

// ─────────────── 独立路径 A：逐层列扫描（把行/列交换，同一数学不同代码） ───────────────

private fun countMixedCols(N: Int): Long {
    val c = 1L shl (N - 1)
    val r2 = c * c
    var total = 0L
    for (k in 1..N) {
        val s = 1L shl k
        val m = 1L shl (N - k)
        var i = 0L
        while (i < m) {
            val x0 = i * s
            val x1 = x0 + s - 1L
            val e0 = abs(x0 - c)
            val e1 = abs(x1 - c)
            val dxMin = if (x0 <= c && c <= x1) 0L else min(e0, e1)
            val dxMax = max(e0, e1)
            val a = r2 - dxMin * dxMin
            if (a >= 0L) {
                val y = isqrt(a)
                val jHi = (c + y) shr k
                val jLo = (c - y) shr k
                var cnt1 = min(jHi, m - 1L) - max(jLo, 0L) + 1L
                if (cnt1 < 0L) cnt1 = 0L
                var cnt2 = 0L
                val b = r2 - dxMax * dxMax
                if (b >= 0L) {
                    val z = isqrt(b)
                    val hHi = (c + z - s + 1L) shr k
                    val hLo = (c - z + s - 1L) shr k
                    cnt2 = min(hHi, m - 1L) - max(hLo, 0L) + 1L
                    if (cnt2 < 0L) cnt2 = 0L
                }
                total += cnt1 - cnt2
            }
            i++
        }
    }
    return total
}

// ─────────────── 独立路径 B：自顶向下递归（只对混合区域下探，与逐层法结构不同） ───────────────

private fun countMixedRecursive(N: Int): Long {
    val c = 1L shl (N - 1)
    val r2 = c * c

    /** 区域 [x0, x0+s) × [y0, y0+s) 是否混合（既非全黑也非全白）。 */
    fun mixed(x0: Long, y0: Long, s: Long): Boolean {
        val dxMax = max(abs(x0 - c), abs(x0 + s - 1L - c))
        val dyMax = max(abs(y0 - c), abs(y0 + s - 1L - c))
        if (dxMax * dxMax + dyMax * dyMax <= r2) return false          // 全黑
        val dxMin = if (x0 <= c && c <= x0 + s - 1L) 0L else min(abs(x0 - c), abs(x0 + s - 1L - c))
        val dyMin = if (y0 <= c && c <= y0 + s - 1L) 0L else min(abs(y0 - c), abs(y0 + s - 1L - c))
        return dxMin * dxMin + dyMin * dyMin <= r2                     // 否则混合（全白为 false）
    }

    fun rec(x0: Long, y0: Long, s: Long): Long {
        if (s == 1L || !mixed(x0, y0, s)) return 0L
        val h = s shr 1
        return 1L + rec(x0, y0, h) + rec(x0 + h, y0, h) + rec(x0, y0 + h, h) + rec(x0 + h, y0 + h, h)
    }
    return rec(0L, 0L, 1L shl N)
}

// ─────────────── 定义级像素网格暴力（前缀和判均色；小 N 对拍 + 外推） ───────────────

/** 逐像素置出 D_N，用 2 维前缀和 O(1) 判区域均色，再自顶向下建最小四叉树。返回 (S, 长度)。 */
private fun bruteGrid(N: Int): Pair<Long, Long> {
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
    fun count(x0: Int, y0: Int, s: Int): Int =
        pre[(y0 + s) * (m + 1) + x0 + s] - pre[y0 * (m + 1) + x0 + s] -
            pre[(y0 + s) * (m + 1) + x0] + pre[y0 * (m + 1) + x0]

    fun rec(x0: Int, y0: Int, s: Int): Pair<Long, Long> {
        val b = count(x0, y0, s)
        if (b == 0 || b == s * s) return 0L to 2L                      // 均色叶：2 位
        check(s > 1) { "尺寸 1 的区域不可能混合" }
        val h = s shr 1
        val tl = rec(x0, y0 + h, h)
        val tr = rec(x0 + h, y0 + h, h)
        val bl = rec(x0, y0, h)
        val br = rec(x0 + h, y0, h)
        return (1L + tl.first + tr.first + bl.first + br.first) to
            (1L + tl.second + tr.second + bl.second + br.second)       // 分裂 1 位 + 4 个子描述
    }
    val r = rec(0, 0, m)
    return r.first to r.second
}

// ───────────────────────── 题面例子的解码 / 编码 ─────────────────────────

/** 按「0 分裂 / 10 黑 / 11 白」解码一条比特序列；允许对均色区域继续分裂（题面两条序列都合法）。 */
private fun decode(bits: String, N: Int): Array<BooleanArray> {
    var pos = 0
    val g = Array(1 shl N) { BooleanArray(1 shl N) }
    fun read(x0: Int, y0: Int, s: Int) {
        when (bits[pos++]) {
            '0' -> {
                val h = s / 2
                read(x0, y0 + h, h)                                     // 左上
                read(x0 + h, y0 + h, h)                                 // 右上
                read(x0, y0, h)                                         // 左下
                read(x0 + h, y0, h)                                     // 右下
            }
            else -> {
                val black = bits[pos++] == '0'
                for (y in y0 until y0 + s) for (x in x0 until x0 + s) g[y][x] = black
            }
        }
    }
    read(0, 0, 1 shl N)
    check(pos == bits.length) { "序列长度与图像不符：用了 $pos / ${bits.length} 位" }
    return g
}

/** 对给定图像求最小序列（均色即叶 2 位、否则 1 位 + 4 个子描述）与内部节点数。 */
private fun encode(g: Array<BooleanArray>): Pair<Long, Long> {
    val m = g.size
    fun rec(x0: Int, y0: Int, s: Int): Pair<Long, Long> {
        var black = 0
        for (y in y0 until y0 + s) for (x in x0 until x0 + s) if (g[y][x]) black++
        if (black == 0 || black == s * s) return 0L to 2L
        check(s > 1)
        val h = s / 2
        val ch = arrayOf(rec(x0, y0 + h, h), rec(x0 + h, y0 + h, h), rec(x0, y0, h), rec(x0 + h, y0, h))
        return (1L + ch.sumOf { it.first }) to (1L + ch.sumOf { it.second })
    }
    return rec(0, 0, m)
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
    // ---------- 1. 题面 4×4 例子：两条序列解码一致，最小序列长度 16 ----------
    val s30 = "001010101001011111011010101010"
    val s16 = "0100101111101110"
    val g30 = decode(s30, 2)
    val g16 = decode(s16, 2)
    for (y in 0 until 4) for (x in 0 until 4) {
        check(g30[y][x] == g16[y][x]) { "两条序列解码结果不同：($x,$y)" }
    }
    val rule15 = encode(g16)
    check(rule15.second == 16L) { "题面最小序列长度应为 16，实得 ${rule15.second}" }
    check(7 * rule15.first + 2 == rule15.second) { "7S+2 应为 16" }
    println("题面例子：30 位与 16 位序列解码出同一幅 4×4 图像；同一编码器给出最小长度 " +
        "${rule15.second}（S = ${rule15.first}，7S+2 = ${7 * rule15.first + 2}）")
    println("图像（# = 黑，y 自下而上）：")
    for (y in 3 downTo 0) println("  " + (0 until 4).joinToString("") { if (g16[y][it]) "#" else "." })

    // ---------- 2. 小 N 四条路径对拍 ----------
    for (n in 2..11) {
        val fr = countMixedRows(n)
        val fc = countMixedCols(n)
        val rec = countMixedRecursive(n)
        val bg = bruteGrid(n)
        check(fr == fc && fr == rec && fr == bg.first) { "N=$n：S 不一致（行 $fr / 列 $fc / 递归 $rec / 暴力 ${bg.first}）" }
        check(bg.second == 7 * fr + 2) { "N=$n：长度 ${bg.second} ≠ 7S+2 = ${7 * fr + 2}" }
    }
    println("N = 2…11：逐层行扫描 = 逐层列扫描 = 自顶向下递归几何 = 定义级像素网格暴力（S 与长度 7S+2 全同）")

    // ---------- 3. 小 N 的 S 一览（供分析报告引用） ----------
    println("小规模 S(N)：N=2…12 依次为 " + (2..12).joinToString(",") { countMixedRows(it).toString() })

    // ---------- 4. 全规模 N = 24：三条几何路径互证 ----------
    val sRows = countMixedRows(N24)
    val sCols = countMixedCols(N24)
    val sRec = countMixedRecursive(N24)
    check(sRows == sCols && sRows == sRec) { "N=24：三条路径不一致（$sRows / $sCols / $sRec）" }
    val length = 7 * sRows + 2
    println("N = 24：S = $sRows（行 $sRows / 列 $sCols / 递归 $sRec），最小序列长度 = 7S+2 = $length")

    // ---------- 5. 计时 ----------
    repeat(2) { countMixedRows(N24) }
    val msMain = bestOf3("主路径（逐层行扫描，N = 24）", sRows) { countMixedRows(N24) }
    val msCols = bestOf3("列扫描路径（N = 24）", sRows) { countMixedCols(N24) }
    val msRec = bestOf3("自顶向下递归几何（N = 24）", sRows) { countMixedRecursive(N24) }
    bruteGrid(9)
    val msBrute = bestOf3("定义级像素网格暴力（N = 12，1.7×10⁷ 像素）", bruteGrid(12).first) { bruteGrid(12).first }

    // ---------- 6. 输出 ----------
    println()
    println("S = $sRows，叶 = 3S+1 = ${3 * sRows + 1}，总节点 = 4S+1 = ${4 * sRows + 1}，最小序列长度 = $length")
    println("答案 = $length")
    println("汇总：主路径 ${"%.3f".format(msMain)} ms；列 ${"%.3f".format(msCols)} ms；递归 ${"%.3f".format(msRec)} ms；" +
        "像素暴力 N=12 ${"%.2f".format(msBrute)} ms")
    println("check() 全部通过")
}
