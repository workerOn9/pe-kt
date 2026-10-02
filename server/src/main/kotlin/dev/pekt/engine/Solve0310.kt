package dev.pekt.engine

/**
 * PE 310 — Nim Square（平方尼姆）：三堆石子 (a,b,c)，每次从一堆取走一个平方数个石子，
 * 取不动者输（普通规则）。求 0 <= a <= b <= c <= 100000 的必败局面个数。
 *
 * 推导（详见 content/problems/0310/solution.kt 头部与 0310/analysis.md）：
 *
 *   1. 由 Sprague–Grundy 定理，三个独立游戏之和必败 <=> g(a) ^ g(b) ^ g(c) = 0，
 *      其中 g(n) = mex{ g(n-k^2) : k^2 <= n }，g(0) = 0。
 *      用「时间戳」数组递推整张表，复杂度 O(M*sqrt(M))（M = 100000 约 3.2e7 次转移）。
 *      实测 M = 100000 时 SG 值域为 0..74（V = 75 个不同取值），|{n : g(n)=0}| = 2781。
 *
 *   2. 直接枚举 O(M^3/6) ≈ 1.7e14 次不可行，改按 SG 值分组计数：
 *      T = sum over 有序 (v1,v2) in V×V, v3 = v1^v2 in V of cnt[v1]*cnt[v2]*cnt[v3]
 *      是满足异或为零的**有序**三元组数（(v1,v2) 有序，故已含全部排列）。
 *      令 Z0 = { n in [0,M] : g(n) = 0 }，则
 *        E = 恰两相等的多重集数 = sum_{c in Z0} c  +  sum_{a in Z0} (M-a)
 *            （形态 (a,a,c) 要求 g(c)=0，形态 (a,b,b) 要求 g(a)=0）
 *        Z = 全相等的多重集数 = |Z0|
 *      T = 6D + 3E + Z 解出互不相同的多重集数 D = (T-3E-Z)/6，答案 = D + E + Z。
 *
 *   3. 自证：题面样例 M = 29 该公式给出 1160（与题面一致）；
 *      M ∈ {5,10,20,60,200,1000} 上与三重循环暴力逐一相等；
 *      M = 20000 上与「逐对 + 每 SG 值后缀计数」第三方法相等。
 *
 * 最终答案：M = 100000 时必败局面数为 2586528661783。
 * 本机实测（JIT 预热后 5 轮取中位数）：约 49 ms。
 */
internal fun solve0310Impl(): Long {
    val m = 100_000

    // 1) SG 表：g(n) = mex{ g(n-k^2) : k^2 <= n }
    val squares = ArrayList<Int>()
    var k = 1
    while (k.toLong() * k <= m) { squares.add(k * k); k++ }
    val g = IntArray(m + 1)
    val stamp = IntArray(squares.size + 2)
    var cur = 0
    for (n in 1..m) {
        cur++
        for (s in squares) {
            if (s > n) break
            stamp[g[n - s]] = cur
        }
        var mex = 0
        while (stamp[mex] == cur) mex++
        g[n] = mex
    }

    // 2) 按 SG 值分组统计有序三元组数 T
    val maxV = g.max()
    val cnt = LongArray(maxV + 1)
    for (v in g) cnt[v]++
    var t = 0L
    for (v1 in 0..maxV) {
        if (cnt[v1] == 0L) continue
        for (v2 in 0..maxV) {
            if (cnt[v2] == 0L) continue
            val v3 = v1 xor v2
            if (v3 <= maxV) t += cnt[v1] * cnt[v2] * cnt[v3]
        }
    }

    // 3) 折算回多重集：T = 6D + 3E + Z，答案 = D + E + Z
    var zCount = 0L
    var sumIdx = 0L
    for (i in g.indices) if (g[i] == 0) { zCount++; sumIdx += i }
    val e = sumIdx + (m.toLong() * zCount - sumIdx)
    val z = zCount
    val d = (t - 3L * e - z) / 6L
    return d + e + z
}
