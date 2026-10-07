package dev.pekt.engine

/**
 * PE 328 — Lowest-cost Search（最优猜数成本）：从 {1..n} 猜隐藏数，猜 k 付代价 k，
 * 得到「偏小 / 命中 / 偏大」。C(n) 为最优策略最坏总代价；已知 C(100)=400、
 * Σ_{1..100} C(n)=17575，求 Σ_{1..200000} C(n)。
 *
 * 推导（详见 content/problems/0328/solution.kt 头部与 0328/analysis.md）：
 *
 *   记 T(m,o) 为区间 {o+1..o+m} 的最坏最优代价，第一个猜的位置 j（数值 o+j）：
 *     T(m,o) = min_j [ (o+j) + max( T(j-1, o), T(m-j, o+j) ) ]，T(0,·)=T(1,·)=0。
 *   令 k=j-1：左子树恒为**无偏移**区间 → dp[k]=C(k)；右子树是偏移 k+1 的平衡子树，
 *   其代价 dfs(k+1, r) 有 O(log r) 闭式（主定理分支 O(1)，另一分支递归）。于是
 *     C(n) = min_k [ (k+1) + max( C(k), dfs(k+1, n-1-k) ) ]。
 *   dfs(n,s) 关于 n 是分段线性的凸函数，斜率只取 {h-1,h}（h=⌊log2 s⌋），
 *   用 n=0、n=1、n=BIG 三点即可还原两条直线，之后逐点求值 O(1)。
 *
 * 剪枝（n≤400 与精确区间 DP 逐一相等，并与公开答案表互证）：
 *   右子树长度 r 必为合法尺寸 r≡7 (mod 8)（原解 valid[] 的重构，已对 s≤10^5 核对）；
 *   最优左大小 k 落在 [max(0,⌊0.8n⌋-20, n-1]，仅用于砍常数。
 *   总运算约 5×10^8 次整数操作，本机约 1.3 s。
 *
 * 答案：Σ_{n=1..200000} C(n) = 260511850222。
 */
internal fun solve0328Impl(): Long {
    val n = 200_000
    val logT = IntArray(n + 1)
    for (i in 2..n) logT[i] = logT[i / 2] + 1

    fun dfsRec(off: Int, s: Int): Int {
        if (s <= 1) return 0
        val h = logT[s]
        if (s == (2 shl h) - 1) return 2 * ((h - 1) * (1 shl h) + 1) + h * off
        val l: Int
        val r: Int
        if (s >= ((1 shl h) or (1 shl (h - 1)))) {
            l = (1 shl h) - 1
            r = s - 1 - l
        } else {
            r = (1 shl (h - 1)) - 1
            l = s - 1 - r
        }
        return maxOf(dfsRec(off, l), dfsRec(off + l + 1, r)) + off + l + 1
    }

    // 合法右子树尺寸 r≡7 (mod 8)，预计算 dfs(·,r) 的两条直线（凸包）表示。
    val nValid = (n - 7) / 8 + 1
    val vr = IntArray(nValid)
    val s1 = IntArray(nValid); val c1 = IntArray(nValid)
    val s2 = IntArray(nValid); val c2 = IntArray(nValid)
    var idx = 0
    var r = 7
    while (r <= n) {
        val h = logT[r]
        val v0 = dfsRec(0, r)
        val v1 = dfsRec(1, r)
        val vB = dfsRec(n, r)
        vr[idx] = r
        if (v1 - v0 == h) {
            s1[idx] = h; c1[idx] = v0; s2[idx] = h; c2[idx] = v0
        } else {
            s1[idx] = h - 1; c1[idx] = v0; s2[idx] = h; c2[idx] = vB - h * n
        }
        idx++
        r += 8
    }

    val dp = IntArray(n + 1)
    dp[1] = 0
    var ans = 0L
    for (i in 2..n) {
        var best: Int
        if (i <= 100) {
            best = Int.MAX_VALUE
            for (k in 0 until i) {
                val c = k + 1 + maxOf(dp[k], dfsRec(k + 1, i - 1 - k))
                if (c < best) best = c
            }
        } else {
            val kmin = maxOf(0, (0.8 * i).toInt() - 20)
            val rmax = i - 1 - kmin
            best = Int.MAX_VALUE
            var j = 0
            while (j < nValid && vr[j] <= rmax) {
                val rr = vr[j]
                val nn = i - rr
                val d = maxOf(s1[j] * nn + c1[j], s2[j] * nn + c2[j])
                val c = nn + maxOf(dp[nn - 1], d)
                if (c < best) best = c
                j++
            }
        }
        dp[i] = best
        ans += best
    }
    return ans
}
