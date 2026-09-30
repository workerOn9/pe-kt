package dev.pekt.engine

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * PE 287 — Quadtree Encoding (a Simple Compression Algorithm)（四叉树编码）：求 D_24 的最小序列长度。
 *
 * 题目：序列按「0 = 分裂成 4 个（左上、右上、左下、右下），10 = 全黑，11 = 全白」描述图像；
 *   D_N 为 2^N×2^N 图像，像素 (x, y)（左下角为 (0,0)）为黑 ⟺ (x−2^{N−1})²+(y−2^{N−1})² ≤ 2^{2N−2}。
 *
 * 推导（完整过程见 content/problems/0287/solution.kt 头部与 0287/analysis.md）：
 *   最小序列 = 最小四叉树（均色即叶，否则分裂）：内部节点 S 个、叶 L = 3S+1 个（4S = L+S−1），
 *   长度 = 2L + S = 7S + 2。题面 4×4 例子 S = 2 → 16 位 ✔。
 *   一个区域是内部节点 ⟺ 它混合（既非全黑也非全白）；混合区域的祖先必混合，故内部节点恰好是
 *   整棵树里的全部混合方块，可按尺寸逐层数：S = Σ_{k=1}^{N} #{边长 2^k 的混合方块}。
 *   混合判定用像素坐标下的极值距离：全黑 ⟺ dxMax²+dyMax² ≤ R²，全白 ⟺ dxMin²+dyMin² > R²。
 *   固定一行后，该行的混合方块数 =（非全白方块数）−（全黑方块数），两者都是「区间内对齐方块的
 *   个数」的 O(1) 计数：非全白 ⟺ x 区间与 [c−X, c+X] 相交（X = isqrt(R²−dyMin²)），
 *   全黑 ⟺ x 区间含于 [c−Y, c+Y]（Y = isqrt(R²−dyMax²)）。逐层行数 Σ_{k=1}^{24} 2^{24−k} = 2^24−1。
 *
 * 复杂度：O(2^N) 行 × O(1)（N = 24 约 1.7×10^7 行、3.4×10^7 次整数平方根），实测约 115 ms，
 *   远低于 10 s 熔断线；空间 O(1)。定义级像素网格暴力是 Θ(4^N)，N = 13 已需 6.7×10^7 像素。
 *
 * 校验：题面两条序列解码出同一幅 4×4 图像且最小长度 16（计数规则 7S+2 校准）；N = 2…11 时
 *   逐层行扫描 = 列扫描 = 自顶向下递归几何 = 像素网格暴力，全部给出同一 S 与长度；N = 24 时
 *   三条几何路径一致给出 S = 44733642、长度 313135496，brute-force.kt 的像素暴力在 N = 13
 *   与几何路径逐值一致（公开答案表亦为 313135496，仅作旁证）。本题无素数筛/组合数/gcd 等通用
 *   步骤，未用到 dev.pekt.math 工具。
 */
internal fun solve0287Impl(): Long {
    val n = 24
    val c = 1L shl (n - 1)                       // 圆心（像素坐标）
    val r2 = c * c                               // R² = 2^{2N−2}
    var total = 0L                               // S = 混合方块总数

    for (k in 1..n) {
        val s = 1L shl k                         // 本层方块边长
        val m = 1L shl (n - k)                   // 本层行数（= 每行方块数）
        var j = 0L
        while (j < m) {
            val y0 = j * s
            val y1 = y0 + s - 1L
            val e0 = abs(y0 - c)
            val e1 = abs(y1 - c)
            val dyMin = if (y0 <= c && c <= y1) 0L else min(e0, e1)
            val dyMax = max(e0, e1)
            val a = r2 - dyMin * dyMin
            if (a >= 0L) {                       // 该行存在非全白方块
                val x = isqrt(a)
                val iHi = (c + x) shr k          // floor((c+x)/s)
                val iLo = (c - x) shr k          // ceil((c−x−s+1)/s)，c−x ≥ 0
                var cnt1 = min(iHi, m - 1L) - max(iLo, 0L) + 1L
                if (cnt1 < 0L) cnt1 = 0L
                var cnt2 = 0L
                val b = r2 - dyMax * dyMax
                if (b >= 0L) {                   // 该行存在全黑方块
                    val y = isqrt(b)
                    val hHi = (c + y - s + 1L) shr k     // floor((c+y−s+1)/s)，可为负
                    val hLo = (c - y + s - 1L) shr k     // ceil((c−y)/s)
                    cnt2 = min(hHi, m - 1L) - max(hLo, 0L) + 1L
                    if (cnt2 < 0L) cnt2 = 0L
                }
                total += cnt1 - cnt2
            }
            j++
        }
    }

    return 7L * total + 2L                       // 最小序列长度 = 7S + 2
}

/** 精确整数平方根（0 ≤ v ≤ 2^46）。 */
private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0L && r * r > v) r--
    while ((r + 1L) * (r + 1L) <= v) r++
    return r
}
