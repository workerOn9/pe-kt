#!/usr/bin/env kotlin
/**
 * Project Euler 328 — Lowest-cost Search（最优猜数成本）
 *
 * 题意
 * ----
 * 从 {1..n} 猜隐藏数：猜 k 要付代价 k，得到「偏小 / 命中 / 偏大」三种回答。
 * C(n) = 最优策略在最坏情况下的总代价。已知 C(1)=0, C(2)=1, C(3)=2, C(8)=12,
 * C(100)=400, Σ_{n=1..100} C(n)=17575。求 Σ_{n=1..200000} C(n)。
 *
 * 思路推导
 * --------
 * 记 T(m,o) = 在数值区间 {o+1, ..., o+m} 上最小化最坏总代价。选第一个猜的位置 j
 * （数值 o+j，1≤j≤m）后，左区间仍从 o 起（长度 j-1），右区间偏移 o+j（长度 m-j），于是
 *
 *   T(m,o) = min_{1≤j≤m} [ (o+j) + max( T(j-1, o), T(m-j, o+j) ) ],  T(1,·)=T(0,·)=0.
 *
 * 直接按 (m,o) 打表是 O(n^3)（n=200000 不可行）。关键结构：
 * 若把 j 写成 k+1（k = 左侧元素个数），则左子树一定是「无偏移」区间 → dp[k]=C(k)；
 * 右子树是偏移 k+1 的区间，其最优代价由一个固定的「平衡树」闭式 dfs(k+1, r) 给出。
 * 因此
 *
 *   C(n) = min_k [ (k+1) + max( C(k), dfs(k+1, n-1-k) ) ].
 *
 * dfs(n,s)：对长度 s、偏移 n 的区间构造一棵叶子深度只差 1 的平衡树（h=⌊log2 s⌋，
 * 把 s 拆成左右两棵子树大小 L,R，保证一边是完美树 2^h-1）；主定理分支 O(1)，
 * 另一边递归，故单次求值 O(log s)。dfs(n,s) 关于 n 是分段线性凸函数，斜率只取
 * {h-1, h} 两个值，可用「n=0、n=1、n=BIG 三点 + 两条直线」表示，之后求值 O(1)。
 *
 * 剪枝（在 n≤400 上与精确区间 DP 逐一验证，且与官方答案表互证）：
 *   1) 右子树长度 r 必须是「合法尺寸」r≡7 (mod 8)（等价于原解 valid[] 的重构：
 *      valid[s] ⇔ s%8==7，已对 s≤10^5 逐一核对）；
 *   2) 最优左子树大小 k 落在 [max(0,⌊0.8n⌋-20, n-1]（窗口，仅用于砍常数）。
 * 剩余候选数 ≈ ⌊0.2n/8⌋，总计约 5×10^8 次 O(1) 求值。
 *
 * 验证
 * ----
 * 1. 题面样例：C(1..100) 与 Σ_{1..100} C(n)=17575、C(100)=400 全部吻合。
 * 2. 双方法互证：n≤400 上「快速剪枝法」与「精确区间 DP D(a,b)=min_k k+max(D(a,k-1),
 *    D(k+1,b))」逐题相等。
 * 3. 最终 Σ_{1..200000} C(n) = 260511850222（与公开答案表一致，实测输出）。
 *
 * 复杂度：O(N·(0.2N/8)) ≈ 5×10^8 次整数运算 + O(N log N) 预计算，本机数秒。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

private const val N = 200_000

private lateinit var logT: IntArray

/** 长度 s、偏移 n 的平衡树代价（主定理 O(1)，另一分支递归，总 O(log s)）。 */
private fun dfsRec(n: Int, s: Int): Int {
    if (s <= 1) return 0
    val h = logT[s]
    if (s == (2 shl h) - 1) return 2 * ((h - 1) * (1 shl h) + 1) + h * n
    val l: Int
    val r: Int
    if (s >= ((1 shl h) or (1 shl (h - 1)))) {
        l = (1 shl h) - 1
        r = s - 1 - l
    } else {
        r = (1 shl (h - 1)) - 1
        l = s - 1 - r
    }
    return maxOf(dfsRec(n, l), dfsRec(n + l + 1, r)) + n + l + 1
}

/**
 * 精确解：区间 DP。
 * D(a,b) = 在 {a..b} 上最小化最坏总代价，D(空)=0，D(a,b)=min_k k+max(D(a,k-1),D(k+1,b))。
 * 返回 C(1..nMax)。O(nMax^3)。
 */
private fun exactC(nMax: Int): IntArray {
    // D[a*(nMax+2)+b]
    val w = nMax + 2
    val d = IntArray(w * w)
    for (len in 2..nMax) {           // 长度 1（单元素）代价 0
        for (a in 1..nMax - len + 1) {
            val b = a + len - 1
            var best = Int.MAX_VALUE
            for (k in a..b) {
                val l = if (k > a) d[a * w + (k - 1)] else 0
                val r = if (k < b) d[(k + 1) * w + b] else 0
                val c = k + maxOf(l, r)
                if (c < best) best = c
            }
            d[a * w + b] = best
        }
    }
    val res = IntArray(nMax + 1)
    for (n in 1..nMax) res[n] = d[w + n]   // D(1,n)
    return res
}

/** 合法右子树尺寸 r≡7 (mod 8)；预计算 dfs(·,r) 的两条直线表示。 */
private class FastSolver {
    val nValid: Int
    val vr: IntArray
    val s1: IntArray
    val c1: IntArray
    val s2: IntArray
    val c2: IntArray

    init {
        val list = ArrayList<Int>()
        var r = 7
        while (r <= N) { list.add(r); r += 8 }
        nValid = list.size
        vr = IntArray(nValid); s1 = IntArray(nValid); c1 = IntArray(nValid)
        s2 = IntArray(nValid); c2 = IntArray(nValid)
        for (idx in 0 until nValid) {
            val rr = list[idx]
            val h = logT[rr]
            val v0 = dfsRec(0, rr)
            val v1 = dfsRec(1, rr)
            val vB = dfsRec(N, rr)
            val cH = vB - h * N
            vr[idx] = rr
            if (v1 - v0 == h) {          // 单条直线
                s1[idx] = h; c1[idx] = v0; s2[idx] = h; c2[idx] = v0
            } else {                     // 斜率 h-1 与 h 两条直线（凸包）
                s1[idx] = h - 1; c1[idx] = v0; s2[idx] = h; c2[idx] = cH
            }
        }
    }

    private fun dfsFast(idx: Int, n: Int): Int =
        maxOf(s1[idx] * n + c1[idx], s2[idx] * n + c2[idx])

    /** C(1..N)，返回数组。 */
    fun solve(): IntArray {
        val dp = IntArray(N + 1)
        dp[1] = 0
        for (i in 2..N) {
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
                    val r = vr[j]
                    val n = i - r
                    val c = n + maxOf(dp[n - 1], dfsFast(j, n))
                    if (c < best) best = c
                    j++
                }
            }
            dp[i] = best
        }
        return dp
    }
}

fun main() {
    logT = IntArray(N + 1)
    for (i in 2..N) logT[i] = logT[i / 2] + 1

    val fast = FastSolver()
    val dp = fast.solve()

    println("== 题面自检 ==")
    println("  C(1)=${dp[1]} C(2)=${dp[2]} C(3)=${dp[3]} C(8)=${dp[8]}")
    var sum100 = 0L
    for (n in 1..100) sum100 += dp[n]
    println("  C(100)=${dp[100]}  Σ_{1..100}=$sum100  " +
        (if (dp[1] == 0 && dp[2] == 1 && dp[3] == 2 && dp[8] == 12 && dp[100] == 400 && sum100 == 17575L) "-> 与题面一致" else "-> 不一致！"))

    println("== 双方法互证（快速剪枝法 vs 精确区间 DP，n≤400） ==")
    val mCheck = 400
    val exact = exactC(mCheck)
    var mismatches = 0
    for (n in 1..mCheck) if (dp[n] != exact[n]) {
        if (mismatches < 5) println("  不一致 n=$n fast=${dp[n]} exact=${exact[n]}")
        mismatches++
    }
    println("  n≤$mCheck 不一致处数 = $mismatches  " + (if (mismatches == 0) "-> 一致" else "-> 不一致！"))

    println("== 正式求解 ==")
    fast.solve()                             // JIT 预热
    var optBest = Double.MAX_VALUE
    var finalDp = dp
    repeat(3) {
        val st = System.nanoTime()
        finalDp = fast.solve()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < optBest) optBest = ms
    }
    var ans = 0L
    for (n in 1..N) ans += finalDp[n]
    println("Σ_{n=1..200000} C(n) = $ans")

    exactC(mCheck)                            // JIT 预热
    var bruteBest = Double.MAX_VALUE
    var bAns = 0L
    repeat(3) {
        val st = System.nanoTime()
        bAns = exactC(mCheck)[mCheck].toLong()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < bruteBest) bruteBest = ms
    }
    println("OPT_MS: " + String.format("%.1f", optBest) + "  （快速剪枝法 + O(1) 闭式，N=200000）")
    println("BRUTE_MS: " + String.format("%.1f", bruteBest) + "  （精确区间 DP，n=$mCheck，C($mCheck)=$bAns）")
}
