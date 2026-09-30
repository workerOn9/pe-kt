package dev.pekt.engine

/**
 * PE 299 — Three Similar Triangles（三个相似三角形）：b+d < 10^8 的三元组 (a,b,d) 个数 = 549936643。
 *
 * 推导（详见 content/problems/0299/solution.kt 头部与 0299/analysis.md）：
 *   A(a,0)、B(b,0)、C(0,c)、D(0,d)，0<a<b、0<c<d，(a,b,d) 为整数三元组；P 为 AC 上整点，
 *   使 ABP、CDP、BDP 两两相似。可证 a=c，记 P=(p,q)、p+q=a、u=b−a、v=d−a：
 *     u·v = 2pq                                                      … (★)
 *   相似条件进一步只留两种情形：
 *     情形 A（p=q）：{u,v}={C·x², 2C·y²}，每个 (C,x,y) 贡献 2 个三元组，
 *                    b+d = C·(x²+4xy+2y²) = C·f_A；
 *     情形 B（u=v=2w，pq=2w²）：{p,q}={C·x², 2C·y²}，b=d，
 *                    b+d = 2C·(x²+2xy+2y²) = 2C·f_B，
 *   其中 C 取遍奇数无平方因子整数（xy=2z² 的充要参数化的因子分解）。
 *
 *   计数：total = Σ_{C 奇无平方} [2·N_A(⌊(N−1)/C⌋) + N_B(⌊(N−1)/(2C)⌋)]，
 *   N_A(M)、N_B(M) 分别是两个二次型在正象限 ≤ M 的格点数，按 y 逐行用整数平方根解出。
 *   C ≤ SMALL 逐个直接算；C > SMALL 时参数 ⌊(N−1)/C⌋ 只有 O(N/SMALL) 种取值，用直方图归并。
 *
 * 复杂度：格点计数 O(Σ_C √(N/C)) ≈ 10^6 量级 + 筛与扫描 O(N/SMALL)，本机约 30 ms。
 *   另有独立路径 1（(x,y) 直方图 + Möbius 平方因子计数）在 content 中与之互证；
 *   引擎只保留本条路径。
 */
internal fun solve0299Impl(): Long = count299Impl(100_000_000L)

private const val SMALL299 = 20000L   // 直方图阈值：C≤SMALL 直接求和，其余归并

/** 整数平方根（向下取整）。本题范围内参数 ≤ 10^8，Double 初值 + 一步修正足够安全。 */
private fun isqrt299(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

/** 情形 A 的二次型 f_A = x²+4xy+2y²（x,y ≥ 1；最小值 7 在 (1,1)）。 */
private fun fA299(x: Long, y: Long) = x * x + 4 * x * y + 2 * y * y

/** 情形 B 的二次型 f_B = x²+2xy+2y²（x,y ≥ 1；最小值 5 在 (1,1)）。 */
private fun fB299(x: Long, y: Long) = x * x + 2 * x * y + 2 * y * y

/** N_A(M) = #{(x,y)≥1: f_A ≤ M}，按 y 逐行、行内由 isqrt 直接解出最大 x。 */
private fun latticeA299(M: Long): Long {
    if (M < 7) return 0
    var t = 0L
    var y = 1L
    while (2 * y * y + 4 * y + 1 <= M) {              // f_A(1,y) ≤ M ⇒ 该行至少一个点
        var x = isqrt299(M + 2 * y * y) - 2 * y       // 解 (x+2y)² ≤ M+2y²
        while (x >= 1 && fA299(x, y) > M) x--
        while (fA299(x + 1, y) <= M) x++
        if (x >= 1) t += x
        y++
    }
    return t
}

/** N_B(M) = #{(x,y)≥1: f_B ≤ M}（正定型，椭圆型区域）。 */
private fun latticeB299(M: Long): Long {
    if (M < 5) return 0
    var t = 0L
    var y = 1L
    while (1 + 2 * y + 2 * y * y <= M) {              // f_B(1,y) ≤ M
        var x = isqrt299(M - y * y) - y               // 解 (x+y)² ≤ M−y²
        while (x >= 1 && fB299(x, y) > M) x--
        while (fB299(x + 1, y) <= M) x++
        if (x >= 1) t += x
        y++
    }
    return t
}

/**
 * 路径 2：对奇数无平方因子 C 求和：
 *   total = Σ_C [2·N_A(⌊(N−1)/C⌋) + N_B(⌊(N−1)/(2C)⌋)]。
 * C ≤ min(SMALL, cmax) 逐个直接算；C > SMALL 时 M 只取 O(N/SMALL) 种值，用直方图归并。
 */
private fun count299Impl(n: Long): Long {
    val cmax = (n - 1) / 7                            // f_A 最小为 7（x=y=1），并且 2C·f_B ≥ 2C·5 > C·7
    val bad = BooleanArray((cmax + 1).toInt())        // bad[m]：m 被某个 d²（d≥2）整除
    var d = 2L
    while (d * d <= cmax) {
        val dd = (d * d).toInt()
        var m = dd
        while (m <= cmax) { bad[m] = true; m += dd }
        d++
    }

    var total = 0L
    val b = minOf(SMALL299, cmax)
    var c = 1L
    while (c <= b) {
        if (!bad[c.toInt()]) total += 2 * latticeA299((n - 1) / c) + latticeB299((n - 1) / (2 * c))
        c += 2
    }

    val size = (n / (SMALL299 + 1)).toInt() + 2
    val histA = LongArray(size)
    val histB = LongArray(size)
    var c0 = if (b % 2 == 0L) b + 1 else b + 2        // 首个大于 b 的奇数
    while (c0 <= cmax) {
        if (!bad[c0.toInt()]) {
            histA[((n - 1) / c0).toInt()]++
            histB[((n - 1) / (2 * c0)).toInt()]++
        }
        c0 += 2
    }
    for (m in 1 until size) {
        if (histA[m] != 0L) total += 2 * histA[m] * latticeA299(m.toLong())
        if (histB[m] != 0L) total += histB[m] * latticeB299(m.toLong())
    }
    return total
}
