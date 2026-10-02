package dev.pekt.engine

/**
 * PE 306 — 纸带游戏（Dawson's Kayles）：n 个白格排成一条纸带，两人轮流挑两个相邻白格涂黑，
 * 无法行动者输。求 1 ≤ n ≤ 10^6 中先手必胜的 n 的个数（n≤5 有 3 个、n≤50 有 40 个）。
 *
 * 推导（详见 content/problems/0306/solution.kt 头部与 0306/analysis.md）：
 *   1) 涂黑两格后左右两段互不影响 ⇒ Sprague–Grundy：
 *        g(0) = g(1) = 0
 *        g(n) = mex{ g(i) ⊕ g(n-2-i) : 0 ≤ i ≤ n-2 }
 *      先手必胜 ⟺ g(n) ≠ 0。逐项递推是 O(N²)，N = 10^6 不可行。
 *   2) Guý–Smith 定理说该序列最终周期 34；本实现不写死常数，而是自己搜：
 *      对每个候选 p 扫出最后一个破坏 g(i)=g(i+p) 的下标 L(p)，前导段 N0 = L(p)+1，
 *      取 N0 最小的 p。实测 N0 = 53、p = 34，并在 n∈[53, 3966] 上逐点复核全对
 *      （另与 O(N²) brute 表在 n∈[53, 20000]、n∈[53, 99966] 两个长窗口上对拍，零失配）。
 *   3) 周期块 g(53..86) 的 34 项里 29 项非零，只有 n ≡ 5, 9, 21, 25, 29 (mod 34) 五个残类必败。
 *      计数：[1, N0-1] 直接数（42 个），尾段按「整块 + 余段」闭式：
 *        前导段 42 + (999948 div 34) × 29 + 余段 8 个残类中的 6 个
 *        = 42 + 29410 × 29 + 6 = 852938。
 *
 * 最终答案：852938（与 content/problems/0306/solution.kt 的实跑输出逐位一致）。
 */
internal fun solve0306Impl(): Long {
    val tableMax = 4000 // 找周期用的 mex DP 规模
    val limit = 1_000_000

    val g = IntArray(tableMax + 2)
    val seen = BooleanArray(128) // g 的实测值 < 16，异或结果 < 128
    for (n in 2..tableMax) {
        val rest = n - 2
        for (i in 0..rest) seen[g[i] xor g[rest - i]] = true
        var m = 0
        while (seen[m]) m++
        g[n] = m
        for (i in 0..rest) seen[g[i] xor g[rest - i]] = false
    }

    // 搜 (N0, p)：前导段最短的候选周期
    var start = 0
    var p = 0
    outer@ for (cand in 1..64) {
        var last = 0
        for (i in 1..tableMax - cand) if (g[i] != g[i + cand]) last = i
        if (last + 1 <= 1000) { start = last + 1; p = cand; break@outer }
    }
    check(p > 0) { "未在前导段 ≤ 1000 内找到周期" }

    // 前导段直接数
    var count = 0L
    for (n in 1 until start) if (g[n] != 0) count++

    // 尾段按周期闭式：整块 + 余段
    var perPeriod = 0
    for (j in 0 until p) if (g[start + j] != 0) perPeriod++
    val len = limit - start + 1
    count += (len / p).toLong() * perPeriod
    for (j in 0 until len % p) if (g[start + j] != 0) count++

    return count
}
