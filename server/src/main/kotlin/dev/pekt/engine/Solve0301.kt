package dev.pekt.engine

/**
 * PE 301 — Nim（尼姆游戏）：三堆普通尼姆，X(n1,n2,n3) 为轮到行动方胜负（0 = 必败），
 * 求 1 ≤ n ≤ 2^30 中 X(n, 2n, 3n) = 0 的 n 个数。
 *
 * 推导（详见 content/problems/0301/solution.kt 头部与 0301/analysis.md）：
 *   X(n,2n,3n) = 0 ⟺ n⊕2n⊕3n = 0（Bouton 定理）⟺ n⊕2n = 3n
 *   ⟺ 二进制加法不进位 ⟺ n & (n<<1) == 0 ⟺ n 无相邻 1。
 * 四状态数位 DP 统计 1..2^30 的无相邻 1 数（含 n=0 再减 1），答案 = F_{32} = 2178309。
 */
internal fun solve0301Impl(): Long {
    fun countNoAdjacentUpTo(n: Long): Long {
        val bits = ArrayList<Int>()
        var m = n
        while (m > 0) { bits.add((m and 1L).toInt()); m = m shr 1 }
        if (bits.isEmpty()) bits.add(0)
        bits.reverse()
        // t0/t1: 前缀与 n 相等、上一位 0/1；f0/f1: 已小于 n、上一位 0/1
        var t0 = 1L; var t1 = 0L; var f0 = 0L; var f1 = 0L
        for (b in bits) {
            var nt0: Long; var nt1: Long; var nf0: Long; var nf1: Long
            if (b == 0) {
                nt0 = t0 + t1; nt1 = 0L; nf0 = f0 + f1; nf1 = f0
            } else {
                nt0 = 0L; nt1 = t0; nf0 = t0 + t1; nf1 = 0L
                nf0 += f0 + f1; nf1 += f0
            }
            t0 = nt0; t1 = nt1; f0 = nf0; f1 = nf1
        }
        return t0 + t1 + f0 + f1
    }
    return countNoAdjacentUpTo(1L shl 30) - 1L
}
