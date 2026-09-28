package dev.pekt.engine

import dev.pekt.math.primesUpTo

/**
 * PE 256 — Tatami-Free Rooms（无榻榻米铺法的房间）。
 *
 * 规则「内部格点上不许四块垫子的角相遇」等价于：每个内部 2×2 方块至少含一块完整垫子
 * （否则四个格子的垫子都把另一端伸到方块外，各自在该点留下一个角）。
 *
 * 结构刻画（Hickerson 定理的推论，已在 content/problems/0256/brute-force.kt 中用字面
 * 枚举与行轮廓 DP 在 a,b ≤ 16 全量核对）：对 a ≤ b、面积为偶数的房间，记
 * q = ⌊b/a⌋、r = b − qa，则
 *
 *     a×b 是 tatami-free  ⟺  q + 2 ≤ r ≤ a − q − 3。
 *
 * 于是 T(s) = #{a | s : a ≤ √s, 上述对 b = s/a 成立}。
 *
 * 求最小 T(s) = 200 的 s：因无序约数对至多 ⌈τ(s)/2⌉，须 τ(s) ≥ 2·200−1 = 399，且 s 为
 * 偶数。以 τ 的上界做剪枝 DFS 枚举候选（素数严格递增、指数 ≥ 1 保证不重不漏），对每个
 * τ ≥ 399 的候选由分解枚举约数、逐对判定。s ≤ 10^8 时候选仅 5206 个，毫秒级完成。
 * 素因子只需 10^4 以内：若 p² | s 则 p ≤ √s；若 p ‖ s，写 s = p·n 得 τ(n) ≥ 200，而
 * 最小的这样的 n 是 498960（2⁴·3⁴·5·7·11），故 p ≤ 10^8/498960 < 201。
 *
 * 答案 85765680（= 2⁴·3²·5·7²·11·13·17，τ = 720，其中恰 200 个约数对 tatami-free），
 * 与 content/problems/0256/solution.kt 的最优路径一致；该目录另用「偶数分段筛 τ 全范围
 * 扫描」的方法 B 复核出同一值（见 analysis.md 的复杂度对比）。
 */
internal fun solve0256Impl(): Long = pe256SmallestTatamiFreeRoom(100_000_000L, 200)

/** 枚举 s ≤ [sMax] 的全部偶数候选（τ(s) ≥ 2·[target]−1），返回最小 T(s)=[target] 的 s。 */
private fun pe256SmallestTatamiFreeRoom(sMax: Long, target: Int): Long {
    val primes = primesUpTo(10_000)
    val tauMin = 2 * target - 1
    val ps = LongArray(32)
    val es = IntArray(32)
    val memo = HashMap<Long, Int>()

    /** n ≤ lim 且素因子 ≥ primes[pi] 时 τ(n) 的最大值（指数非递增即已达最大，标准上界） */
    fun maxTau(lim: Long, pi: Int): Int {
        if (pi >= primes.size) return 1
        val p = primes[pi]
        if (p > lim) return 1
        val key = pi.toLong() * 1_000_000_000L + lim
        memo[key]?.let { return it }
        var best = 1
        var pe = 1L
        var e = 0
        while (pe <= lim / p) {
            e++
            pe *= p
            val c = (e + 1) * maxTau(lim / pe, pi + 1)
            if (c > best) best = c
        }
        memo[key] = best
        return best
    }

    /** 由当前分解 ps[0..k-1]^es[0..k-1] 精确计算 T(s) */
    fun tatamiFreeCount(s: Long, k: Int): Int {
        var tau = 1
        for (i in 0 until k) tau *= es[i] + 1
        val divs = LongArray(tau)
        divs[0] = 1L
        var size = 1
        for (i in 0 until k) {
            val base = size
            var pe = 1L
            for (e in 1..es[i]) {
                pe *= ps[i]
                for (j in 0 until base) divs[size++] = divs[j] * pe
            }
        }
        var cnt = 0
        for (d in divs) {
            if (d * d > s) continue
            val b = s / d
            val q = b / d
            if (b >= (d + 1) * q + 2 && b <= (d - 1) * (q + 1) - 2) cnt++
        }
        return cnt
    }

    var best = Long.MAX_VALUE
    fun dfs(s: Long, tau: Long, k: Int, pi: Int) {
        if (tau >= tauMin && s < best && tatamiFreeCount(s, k) == target) best = s
        var idx = pi
        while (idx < primes.size) {
            val p = primes[idx]
            if (p > sMax / s) break
            var pe = p
            var e = 1
            while (true) {
                ps[k] = p; es[k] = e
                val ntau = tau * (e + 1)
                if (ntau * maxTau(sMax / (s * pe), idx + 1) >= tauMin) {
                    dfs(s * pe, ntau, k + 1, idx + 1)
                }
                if (pe > (sMax / s) / p) break
                pe *= p; e++
            }
            idx++
        }
    }

    // s 必须为偶数：先取定 2 的幂，再用 ≥ 3 的素数按递增顺序扩展
    var pe = 2L
    var e = 1
    while (true) {
        ps[0] = 2L; es[0] = e
        val tau = (e + 1).toLong()
        if (tau * maxTau(sMax / pe, 1) >= tauMin) dfs(pe, tau, 1, 1)
        if (pe > sMax / 2) break
        pe *= 2; e++
    }
    return best
}
