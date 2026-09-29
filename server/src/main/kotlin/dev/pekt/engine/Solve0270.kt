package dev.pekt.engine

/**
 * PE 270 — Cutting Squares（切正方形）：N×N 正方形边界有 4N 个整点，割线连接「边掩码不相交」的
 * 两点（即不共边的两条边上的点），割线之间只允许端点相遇；不断切到不能再切。求切法数 C(30) mod 10^8。
 *
 * 推导（详见 content/problems/0270/solution.kt 头部与 0270/analysis.md）：
 *   把边界点顺时针编号 0…4N−1，位置 p 记边掩码（bottom/right/top/left 各一位，角点两位）。
 *   结构定理：极大非交叉割线集 ⟺ 用「合法对角线」（端点掩码不相交）给出的 M = 4N 边形三角剖分。
 *   证明要点：非交叉割线把正方形分成凸多边形（面）；若某面边界上有 ≥ 4 个整点 p1…pm，由凸性，
 *   落在同一条边上的点在该面边界上连续；取不相邻点对 (p1,p3)，若它们掩码不相交则线段 p1p3 落在
 *   面内且不与已有割线相交（可加，矛盾），故必须共边，于是 p2 也在该边上；对 (p2,p4) 同理，
 *   m = 4 时把 p2、p3 同时逼到相邻两边唯一公共角上而矛盾，m ≥ 5 时所有点被迫共线退化，矛盾。
 *   故极大集里每个面都是三角形，即三角剖分；反之只有合法对角线的三角剖分显然极大。
 *
 * 计数用区间 DP：dp[i][j] = 弧 i→j 与闭合边 (i,j) 围成的子多边形中合法三角剖分数，
 *   dp[i][j] = Σ_{k} [edge(i,k) 可用]·[edge(k,j) 可用]·dp[i][k]·dp[k][j]（模 10^8），
 *   edge(a,b) 可用 ⟺ b = a+1 或端点掩码不相交；答案 = dp[0][M−1]。
 *
 * 复杂度：O(M³) 时间（M = 120，实测 < 1 ms）、O(M²) 内存。校验：题面锚点 C(1)=2、C(2)=30 复现；
 *   按题面定义直接枚举极大割线集在 N=1,2,3 得 2/30/604；直接枚举三角剖分在 N=4…7 得
 *   12168/238848/4569624/85553528，与 DP 一致；方法 B（顶点 0 扇形分解 + 记忆化）在 N=1…12
 *   与 N=30 结果一致。逻辑与 content/problems/0270/solution.kt 的主路径（方法 A）一致。
 */
internal fun solve0270Impl(): Long {
    val n = 30
    val m = 4 * n
    val mask = sideMasks0270(n)

    fun edgeOk(a: Int, b: Int): Boolean = b == a + 1 || (mask[a] and mask[b]) == 0

    val dp = Array(m) { LongArray(m) }
    for (i in 0 until m - 1) dp[i][i + 1] = 1L
    for (len in 2 until m) {
        for (i in 0..m - len - 1) {
            val j = i + len
            var total = 0L
            for (k in i + 1 until j) {
                if (!edgeOk(i, k) || !edgeOk(k, j)) continue
                total += dp[i][k] * dp[k][j]
                total %= MOD_0270
            }
            dp[i][j] = total
        }
    }
    return dp[0][m - 1]
}

private const val MOD_0270 = 100_000_000L

/** 边界点边掩码：1 = bottom，2 = right，4 = top，8 = left；位置 0 同时属于 bottom 与 left。 */
private fun sideMasks0270(n: Int): IntArray {
    val m = 4 * n
    return IntArray(m) { p ->
        var b = 0
        if (p <= n) b = b or 1
        if (p in n..2 * n) b = b or 2
        if (p in 2 * n..3 * n) b = b or 4
        if (p >= 3 * n || p == 0) b = b or 8
        b
    }
}
