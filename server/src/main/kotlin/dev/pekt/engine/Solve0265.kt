package dev.pekt.engine

/**
 * PE 265 — Binary Circles（二进制圆圈）：$2^N$ 个二进制位排成圆，使全部 $N$ 位顺时针窗口互不相同；
 * 以全零窗口开头拼接数值，求所有「忽略旋转」的圆排列编码之和 $S(5)$。
 *
 * 推导（详见 content/problems/0265/solution.kt 头部与 0265/analysis.md）：
 *   窗口 = de Bruijn 图 B(2,N) 的有向边（顶点是 (N-1) 位串，边是 N 位串），
 *   圆排列 ⇔ 欧拉回路。全零边 0^N 在回路中恰好出现一次，因此把 0^N 钉为首边、
 *   从顶点 0^{N-1} 出发枚举欧拉回路，就与「以全零窗口开头的编码」一一对应，无需去重：
 *   S(N) = Σ_{以 0^N 为首边的欧拉回路} (该回路的 2^N 位二进制值)。
 *   DFS 每步在两条未用出边中分支，用满 2^N 条边且回到 0^{N-1} 即得一条回路；
 *   位串下标达到 2^N 之后的回绕比特只可能全为 0（终点回到起点），故不参与拼接。
 *
 * 复杂度：搜索状态数远小于候选空间 2^{2^N}（N=5 实测 92 636 个状态），内存 O(2^N)；
 *   本机实测约 0.5 ms。与 solution.kt 的方法 A 完全一致（方法 B 旋到全零边去重、
 *   直接暴力全量 2^27 候选均给出同一答案 209110240768，题面锚点 S(3)=23+29=52）。
 */
internal fun solve0265Impl(): Long {
    val n = 5
    val edgeCount = 1 shl n                  // 2^N 条边
    val nodeMask = (1 shl (n - 1)) - 1       // (N-1) 位顶点掩码
    val used = BooleanArray(edgeCount)
    var sum = 0L
    var count = 0                            // 回路条数（应为 2^{2^{N-1}-N} = 2048）

    fun dfs(depth: Int, node: Int, value: Long) {
        if (depth == edgeCount) {
            require(node == 0) { "欧拉回路没有回到起点" }
            count++
            sum += value
            return
        }
        for (b in 0..1) {
            val e = (node shl 1) or b
            if (used[e]) continue
            used[e] = true
            // 第 depth 条边贡献位串下标 depth+n-1；回绕比特（下标 ≥ 2^N）只在终点回到
            // 起点时为 0，故不参与拼接
            val next = if (depth + n - 1 < edgeCount) (value shl 1) or b.toLong() else value
            dfs(depth + 1, e and nodeMask, next)
            used[e] = false
        }
    }

    used[0] = true                           // 首边固定为 0^N（全零窗口）
    dfs(1, 0, 0L)
    require(count == 2048) { "圆排列数应为 2048，实得 $count" }
    return sum
}
