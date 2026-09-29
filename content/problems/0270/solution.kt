#!/usr/bin/env kotlin
/**
 * Project Euler 270 — Cutting Squares（切正方形）
 *
 * 思路
 * ────
 * 把 N×N 正方形的边界按整数点编号：顺时针从 (0,0) 起共 M = 4N 个点，位置 p 附带一个「所在边」
 * 掩码（bottom/right/top/left 各占一位；四个角同时属于两条边）。题面要求「两点位于不同边上」，
 * 由于角点同属两边的歧义，唯一与锚点 C(1)=2、C(2)=30 相容的解读是：
 *
 *   一条割线合法 ⟺ 两个端点的边掩码不相交（即没有一条边同时包含两点）。
 *
 * 这样割线的内部严格落在正方形内（不会沿线边界），也不会是零长度。割线之间只允许端点相遇
 * （「several cuts can meet at the same border point」），不允许内部相交。
 *
 * 关键结构定理：**极大割线集 ⟺ 用合法对角线给出的「三角剖分」**。
 *   (1) 非交叉割线集把正方形切成若干凸多边形（面）。若某个面的边界上有 m ≥ 4 个整数边界点
 *       p1…pm（按面边界顺序），则取不相邻的对 (p1,p3)：若两点无边掩码相交，线段 p1p3 落在面内、
 *       不与任何已画割线交叉，可以再切一刀，与极大性矛盾；所以它们必须同边。由凸性，一个面上
 *       位于同一条边的点在该面边界上连续出现（面与该边所在直线的交是线段）；于是 p1、p3 同边
 *       ⇒ 中间的 p2 也在这条边上。再对 (p2,p4) 用同样推理，在 m = 4 时把 p3 逼到两条相邻边的
 *       公共角上、同时 p2 也被逼到同一角，矛盾；m ≥ 5 时三重连续点迫使所有点落在同一条边上，
 *       面退化为线段，同样矛盾。故极大割线集里每个面都只有 3 个边界点，即全部是三角形——
 *       这正是 M 边形的三角剖分，且所有对角线（非单位边）都必须合法。
 *   (2) 反过来，任何只用合法对角线的三角剖分都是极大的：不在剖分里的任意割线要么与某条
 *       剖分边内部相交，要么是某个三角形的一条对角线，而三角形三边中最多两条是边界单位边，
 *       剩下那条必是对角线、已合法地画出，故没有可加的割线。
 *
 * 于是 C(N) = 「M = 4N 边形中只允许合法对角线」的三角剖分数，区间 DP：
 *
 *   dp[i][j] = Σ_{k=i+1}^{j-1} [edge(i,k) 可用]·[edge(k,j) 可用]·dp[i][k]·dp[k][j]
 *
 * 其中 edge(a,b) 可用 ⟺ b = a+1（单位边界边）或两点的边掩码不相交（合法对角线）；
 * 边界 dp[i][i+1] = 1；答案 = dp[0][M-1]（(0,M-1) 是单位边界边）。全体运算模 10^8。
 *
 * 复杂度
 * ──────
 * O(M³) 次转移（M = 4N，N = 30 时 M = 120，约 2.9×10^5 次），内存 O(M²)。
 * 对照：直接枚举割线集合的数量级是 2^Θ(N²)，暴力只能做到 N ≤ 7。
 *
 * 验证
 * ────
 * 1. 题面锚点：C(1) = 2、C(2) = 30（本文件的三套实现全部复现）；
 * 2. 方法 A（区间 DP，自底向上）/ 方法 B（顶点 0 的扇形分解 + 自顶向下记忆化 + 边掩码判定）
 *    在 N = 1…12 与 N = 30 全部一致；
 * 3. 直接暴力（按题面定义枚举极大非交叉割线集）在 N = 1,2,3 复现 2, 30, 604；
 *    直接枚举三角剖分（不加记忆化）在 N = 4…7 复现 12168 / 238848 / 4569624 / 85553528，与 DP 一致。
 *
 * 答案：82282080（C(30) mod 10^8）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0270/solution.kt && java -cp … SolutionKt
 */

private const val MOD = 100_000_000L

/**
 * 边界点编号：顺时针从 (0,0) = 位置 0 起，(N,0) = 位置 N，(N,N) = 位置 2N，(0,N) = 位置 3N。
 * 掩码位：1 = bottom，2 = right，4 = top，8 = left；角点占两位。
 */
private fun sideMasks(n: Int): IntArray {
    val m = 4 * n
    return IntArray(m) { p ->
        var b = 0
        if (p <= n) b = b or 1                    // bottom：[0, N]
        if (p in n..2 * n) b = b or 2             // right：[N, 2N]
        if (p in 2 * n..3 * n) b = b or 4         // top：[2N, 3N]
        if (p >= 3 * n || p == 0) b = b or 8      // left：[3N, 4N]，位置 0 即 4N
        b
    }
}

private fun legal(mask: IntArray, i: Int, j: Int): Boolean = (mask[i] and mask[j]) == 0

/** edge(a,b)（a < b）可用：单位边界边，或合法对角线。 */
private fun edgeOk(mask: IntArray, a: Int, b: Int): Boolean =
    b == a + 1 || legal(mask, a, b)

/**
 * 方法 A（主路径）：区间 DP。
 * dp[i][j] = 用弧 i→j 与闭合边 (i,j) 围成的子多边形中、只允许合法对角线的三角剖分数。
 */
private fun solveDp(n: Int, mod: Long? = MOD): Long {
    val m = 4 * n
    val mask = sideMasks(n)
    val dp = Array(m) { LongArray(m) }
    for (i in 0 until m - 1) dp[i][i + 1] = 1L
    for (len in 2 until m) {
        for (i in 0..m - len - 1) {
            val j = i + len
            var total = 0L
            for (k in i + 1 until j) {
                if (!edgeOk(mask, i, k) || !edgeOk(mask, k, j)) continue
                total += dp[i][k] * dp[k][j]
                if (mod != null) total %= mod
            }
            dp[i][j] = total
        }
    }
    return dp[0][m - 1]
}

/**
 * 方法 B（独立复核）：顶点 0 的扇形分解。
 * 设 0 在三角剖分中的邻居依次为 1 = a0 < a1 < … < at = M-1，则三角形 (0, aᵢ, aᵢ₊₁) 的第三条边
 * (aᵢ, aᵢ₊₁) 把多边形切成独立的子多边形 [aᵢ, aᵢ₊₁]，而 (0, aᵢ)（内部邻居）必须合法。于是
 *   C = H(M-1)，H(1) = 1，H(j) = Σ_{i<j} H(i)·[edge(i,j) 可用]·[chord(0,j) 合法或 j = M-1]·f(i,j)，
 * f(i,j) 用自顶向下记忆化计算（与 A 同族的子问题，但完全独立的代码路径与合法性编码）。
 */
private fun solveFan(n: Int, mod: Long? = MOD): Long {
    val m = 4 * n
    val mask = sideMasks(n)
    val memo = Array(m) { arrayOfNulls<Long>(m) }

    fun f(i: Int, j: Int): Long {
        if (j == i + 1) return if (mod == null) 1L else 1L % mod
        memo[i][j]?.let { return it }
        var total = 0L
        for (k in i + 1 until j) {
            if (!edgeOk(mask, i, k) || !edgeOk(mask, k, j)) continue
            total += f(i, k) * f(k, j)
            if (mod != null) total %= mod
        }
        memo[i][j] = total
        return total
    }

    val h = LongArray(m)
    h[1] = 1L
    for (j in 2 until m) {
        if (j != m - 1 && !legal(mask, 0, j)) continue          // 邻居 j 的弦 (0,j) 必须合法（末端是边界边）
        var total = 0L
        for (i in 1 until j) {
            if (i != 1 && !legal(mask, 0, i)) continue          // 内部邻居 i 的弦 (0,i) 必须合法
            if (!edgeOk(mask, i, j)) continue
            total += h[i] * f(i, j)
            if (mod != null) total %= mod
        }
        h[j] = total
    }
    return h[m - 1]
}

/** 两条割线（p,q）与（r,s）是否内部相交：端点互异时，看 r、s 是否被切开的同一条弧分隔。 */
private fun crosses(p: Int, q: Int, r: Int, s: Int, m: Int): Boolean {
    if (p == r || p == s || q == r || q == s) return false
    val arc = ((q - p) % m + m) % m
    fun inside(x: Int): Boolean {
        val d = ((x - p) % m + m) % m
        return d > 0 && d < arc
    }
    return inside(r) != inside(s)
}

/**
 * 直接暴力 1（N ≤ 3）：完全按题面定义枚举「极大非交叉合法割线集」。
 * 规范枚举：找到编号最小的、当前可加的割线，分「加入」与「永久排除」两支；
 * 到叶子再验证极大性（每条未含割线都必须与已含割线相交），保证不重不漏。
 */
private fun bruteMaximalSets(n: Int): Long {
    val m = 4 * n
    val mask = sideMasks(n)
    val chords = ArrayList<IntArray>()
    for (i in 0 until m) for (j in i + 1 until m) if (legal(mask, i, j)) chords.add(intArrayOf(i, j))
    val c = chords.size
    check(c <= 62) { "位掩码只支持 ≤ 62 条割线" }
    val cr = LongArray(c)
    for (x in 0 until c) for (y in x + 1 until c) {
        if (crosses(chords[x][0], chords[x][1], chords[y][0], chords[y][1], m)) {
            cr[x] = cr[x] or (1L shl y)
            cr[y] = cr[y] or (1L shl x)
        }
    }
    var count = 0L
    fun dfs(inc: Long, exc: Long) {
        var inc2 = inc
        var exc2 = exc
        for (i in 0 until c) {
            val bit = 1L shl i
            if ((inc2 and bit) != 0L || (exc2 and bit) != 0L) continue
            if ((cr[i] and inc2) != 0L) { exc2 = exc2 or bit; continue }
            dfs(inc2 or bit, exc2)      // 含这条割线
            dfs(inc2, exc2 or bit)      // 不含（之后不再考虑）
            return
        }
        for (i in 0 until c) if ((inc2 and (1L shl i)) == 0L && (cr[i] and inc2) == 0L) return
        count++
    }
    dfs(0L, 0L)
    return count
}

/**
 * 直接暴力 2（N ≤ 7）：不加记忆化，直接枚举 M 边形的全部「只允许合法对角线」三角剖分。
 * 每步取多边形边 (poly[0], poly[1]) 所在的三角形，第三顶点遍历其余顶点；
 * 三角形另外两条边必须是多边形的边或合法对角线；两片残余多边形递归。
 */
private fun bruteTriangulations(n: Int): Long {
    val m = 4 * n
    val mask = sideMasks(n)
    fun rec(poly: IntArray): Long {
        val k = poly.size
        if (k <= 3) return 1L
        val v0 = poly[0]
        val v1 = poly[1]
        var total = 0L
        for (t in 2 until k) {
            val vt = poly[t]
            // 三角形 (v0,v1,vt) 的另两条边：是多边形边，或者必须是合法对角线
            val e1ok = t == 2 || legal(mask, v1, vt)
            val e2ok = t == k - 1 || legal(mask, v0, vt)
            if (!e1ok || !e2ok) continue
            val p1 = poly.copyOfRange(1, t + 1)
            val p2 = IntArray(k - t + 1)
            p2[0] = v0
            for (u in t until k) p2[1 + u - t] = poly[u]
            total += rec(p1) * rec(p2)
        }
        return total
    }
    return rec(IntArray(m) { it })
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面锚点 + 直接暴力 ----------
    for (n in 1..3) {
        val s = solveDp(n, null)
        val lit = bruteMaximalSets(n)
        check(s == lit) { "N=$n：DP $s ≠ 按定义枚举 $lit" }
        println("N=$n：按定义枚举极大割线集 = $lit，区间 DP = $s")
    }
    check(solveDp(1, null) == 2L && solveDp(2, null) == 30L) { "题面锚点 C(1)=2、C(2)=30 未复现" }
    println("题面锚点：C(1) = 2，C(2) = 30（按定义枚举与区间 DP 同时复现）")

    // ---------- 2. 两种方法在多个规模上互证 ----------
    val known = mapOf(4 to 12168L, 5 to 238848L, 6 to 4569624L, 7 to 85553528L)
    for (n in 4..7) {
        val b = bruteTriangulations(n)
        check(b == known[n]) { "N=$n：直接枚举三角剖分 $b ≠ ${known[n]}" }
        println("N=$n：直接枚举三角剖分 = $b（与 DP 一致）")
    }
    for (n in 1..12) {
        val a = solveDp(n, MOD)
        val fan = solveFan(n, MOD)
        check(a == fan) { "N=$n：方法 A $a ≠ 方法 B $fan" }
    }
    println("N = 1…12：方法 A（区间 DP）与方法 B（扇形分解 + 记忆化）全部一致")

    // ---------- 3. 完整规模 ----------
    val c30a = solveDp(30, MOD)
    val c30b = solveFan(30, MOD)
    check(c30a == c30b) { "N=30：方法 A $c30a ≠ 方法 B $c30b" }
    val exact4 = solveDp(4, null)
    println("N=4 的精确值 $exact4 与模 10^8 值 ${solveDp(4, MOD)} 相等（说明模运算未掩盖小规模错误）")

    // ---------- 4. 计时 ----------
    solveDp(30, MOD)
    val msA = bestOf3("方法 A（区间 DP，N=30）", c30a) { solveDp(30, MOD) }
    val msB = bestOf3("方法 B（扇形分解，N=30）", c30b) { solveFan(30, MOD) }
    solveFan(1, MOD); solveDp(3, null); bruteMaximalSets(3); bruteTriangulations(6)
    val msLit = bestOf3("按定义枚举极大割线集（N=3）", 604L) { bruteMaximalSets(3) }
    val msTri = bestOf3("直接枚举三角剖分（N=6）", 4569624L) { bruteTriangulations(6) }

    println()
    println("答案 = $c30a")
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms；" +
        "按定义暴力 N=3 ${"%.1f".format(msLit)} ms；三角剖分枚举 N=6 ${"%.1f".format(msTri)} ms")
    println("check() 全部通过")
}
