package dev.pekt.engine

import dev.pekt.math.gcd

/**
 * PE 292 — 毕达哥拉斯多边形（Pythagorean Polygons）：P(120) = 3600060866。
 *
 * 推导（详见 content/problems/0292/solution.kt 头部与 0292/analysis.md）：
 *   多边形（平移类）⇔「边向量按极角排成的循环序列」：凸 + 无三点共线 ⇔ 边方向沿周长严格递增
 *   （每个方向至多出现一次），而互异方向按极角排列、Σeᵢ = 0 且个数 ≥ 3 时自动闭合为严格凸
 *   多边形（相邻方向夹角必 < π，否则其余方向落在开半平面内、正系数和不可能为 0）。
 *   边长整数 ⇒ 每个可用方向是原始毕达哥拉斯方向：gcd(|u|,|v|) = 1 且 u²+v² = L² 为完全平方
 *   （边 = t·(u,v)，t ≥ 1，边长 = t·L）。n = 120 时共 4 个轴方向 + 19 组原始三元组 × 8 = 156 个方向。
 *
 *   计数：把方向按极角排序（半平面 + 叉积比较，不用浮点 atan2），做「跳过 / 取 t 条（t ≥ 1）」
 *   的背包 DP，状态 (x, y, p) = 已选边向量和与已用周长；dp 是扁平 LongArray，按 p 降序原地更新，
 *   保证每个方向至多用一次。可达性剪枝：任何前缀状态剩余边要闭合回原点，故 |x|,|y| ≤ n−p；
 *   写剪枝同理。终态 (0,0,p)（p = 1..n）求和，再减去退化「二边形」{d,−d}（周长 2tL ≤ n，
 *   共 Σ n/(2L) 个；每个无序方向对在求和中出现两次，故除 2）。
 *
 * 复杂度：状态 (n+1)(2n+1)² ≈ 7.0×10⁶，方向 156 个，转移约 3×10⁸ 次整数加法；
 *   内存一张扁平 LongArray（约 56 MB）。本机实测约 0.2 s。
 *
 * 验证：content/problems/0292/solution.kt 实跑给出 P(4) = 1、P(30) = 3655、P(60) = 891045
 *   三个题给 gate 与 P(120) = 3600060866。
 */
internal fun solve0292Impl(): Long = countP292(120, buildDirections292(120))

/** 一个可用的原始方向 (u,v) 与原始长度 len = √(u²+v²)（整数）。 */
private class Dir292(val u: Int, val v: Int, val len: Int)

/** 半平面标记：v > 0 或 (v = 0 且 u > 0) 记 0（极角 [0, π)），否则记 1（极角 [π, 2π)）。 */
private fun half292(u: Int, v: Int): Int = if (v > 0 || (v == 0 && u > 0)) 0 else 1

/** 整数平方根（向下取整，无浮点误差）。 */
private fun isqrt292(v: Int): Int {
    var r = Math.sqrt(v.toDouble()).toInt()
    while (r > 0 && r * r > v) r--
    while ((r + 1).toLong() * (r + 1) <= v) r++
    return r
}

/**
 * 枚举全部原始毕达哥拉斯方向 (u,v)：gcd(|u|,|v|) = 1 且 u²+v² 为完全平方数 L² ≤ n，
 * 按极角递增排序（叉积比较，精确）。
 */
private fun buildDirections292(n: Int): List<Dir292> {
    val list = ArrayList<Dir292>()
    for (u in -n..n) {
        for (v in -n..n) {
            if (u == 0 && v == 0) continue
            if (gcd(u.toLong(), v.toLong()) != 1L) continue
            val q = u * u + v * v
            val s = isqrt292(q)
            if (s * s != q || s > n) continue
            list.add(Dir292(u, v, s))
        }
    }
    list.sortWith(Comparator { a, b ->
        val ha = half292(a.u, a.v)
        val hb = half292(b.u, b.v)
        if (ha != hb) {
            ha - hb
        } else {
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
 * 对方向按极角顺序做 (x,y,p) 背包 DP，返回周长 ≤ n 的毕达哥拉斯多边形数。
 */
private fun countP292(n: Int, dirs: List<Dir292>): Long {
    val side = 2 * n + 1
    val pStride = side * side
    val dp = LongArray((n + 1) * pStride)
    dp[n * side + n] = 1L                                         // 空集：(0,0)，周长 0
    for (d in dirs) {
        val u = d.u
        val v = d.v
        val l = d.len
        var p = n
        while (p >= 0) {                                          // p 降序原地更新 ⇒ 每个方向至多用一次
            val rem = n - p
            if (rem >= l) {
                val cap = if (p < rem) p else rem                 // |x|,|y| ≤ min(p, n−p)（可达状态界）
                val tMax = rem / l
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
                                val np = p + t * l
                                val ncap = n - np             // 写剪枝：剩余边要闭合，(n−np) 是剩余总长上界
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
    for (p in 1..n) total += dp[p * pStride + n * side + n]       // 排除 p = 0 的空集
    // 减去退化「二边形」{d, −d}：两方向各取 t 条，t·d + t·(−d) = 0，周长 2tL ≤ n。
    // 每个无序方向对在下面的求和里计两次（d 与 −d 各一次），最后除以 2。
    var degenerate = 0L
    for (d in dirs) if (2 * d.len <= n) degenerate += n / (2 * d.len)
    return total - degenerate / 2
}
