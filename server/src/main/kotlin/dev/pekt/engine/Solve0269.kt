package dev.pekt.engine

import kotlin.math.abs

/**
 * PE 269 — Polynomials with at Least One Integer Root（至少有一个整数根的多项式）：
 * 求 n ≤ 10^16 中「十进制数位构成的多项式 P_n 有整数根」的 n 的个数。
 *
 * 推导（详见 content/problems/0269/solution.kt 头部与 0269/analysis.md）：
 *   把 n 写成 L 位数字串（前导零允许，P_n 不受影响）。只需考虑根 r ∈ [-9, 9]：
 *   |r| ≥ 10 时首项压倒其余项之和，r = 1 时 P_n(1) = 数位和 > 0。
 *   有理根定理：r | d_0（末位数字）。于是
 *     · 末位 d_0 = 0：r = 0 恒为根，所有这种数自动计入，[0, 10^L−1] 中共 10^{L−1} 个；
 *     · 末位 d_0 = d ∈ {1..9}：候选根 = d 的约数取 ±（去掉 +1），最多 7 个。
 *   n = 0 与 n = 10^L 的贡献恰好抵消，故 Z(10^L) = 10^L − Σ_{d=1}^{9} N_L(d)，
 *   N_L(d) 是「末位为 d 且没有任何候选根」的 L 位串数。
 *
 * 方法（与 solution.kt 主路径完全一致）：高位优先扫描，对每个候选根维护归一化前缀值
 *   V_r = Σ_{i=k}^{L-1} d_i r^{i-k}；若 r 是根，剩余 k 位最多把 |V_r| 拉到
 *   9k（|r| = 1）或 9/(|r| − 1)（|r| ≥ 2），越界即永久剪掉该根；末态「仍有存活的根
 *   满足 V_r = −d_0/r」即有根。每个末位类一次 DP，9 次覆盖全部，无需容斥。
 *
 * 复杂度：L = 16 时状态数最多 7016，单类毫秒级，内存为单步状态表。
 * 实测与校验：本机 JIT 预热后约 27 ms（远低于 10 s 熔断线）；题面锚点 Z(10^5) = 14696，
 *   暴力（n ≤ 10^6 / 10^7）与低位进位 DP + 容斥的独立实现三方在全部小规模上逐值一致，
 *   完整答案 1311109198529286。
 */
internal fun solve0269Impl(): Long {
    val l = 16
    val shift = 9
    val slotMask = (1L shl shift) - 1
    val base = 256
    val dead = 400

    var limit = 1L
    repeat(l) { limit *= 10L }

    var noRootTotal = 0L
    for (d0 in 1..9) {
        // 候选根：d0 的约数取 ±，去掉 +1
        val roots = ArrayList<Int>(8)
        for (d in 1..9) {
            if (d0 % d != 0) continue
            roots.add(-d)
            if (d != 1) roots.add(d)
        }
        val n = roots.size
        val target = IntArray(n) { -d0 / roots[it] } // 命中值 −d0/r（r | d0，精确整除）

        var cur = HashMap<Long, Long>()
        var init = 0L
        for (i in 0 until n) init = init or (base.toLong() shl (shift * i)) // 所有 V = 0
        cur[init] = 1L

        var pos = l - 1
        while (pos >= 1) {
            val next = HashMap<Long, Long>(cur.size * 3)
            for ((st, cnt) in cur) {
                for (d in 0..9) {
                    var ns = 0L
                    var i = 0
                    while (i < n) {
                        val stored = ((st ushr (shift * i)) and slotMask).toInt()
                        var out = dead
                        if (stored != dead) {
                            val r = roots[i]
                            val nv = (stored - base) * r + d
                            val a = abs(nv)
                            // 剩余 pos 位最多凑出 9·pos（|r| = 1）或 9/(|r|−1)（|r| ≥ 2）
                            val keep = if (r == -1) a <= 9 * pos else a * (abs(r) - 1) <= 9
                            if (keep) out = nv + base
                        }
                        ns = ns or (out.toLong() shl (shift * i))
                        i++
                    }
                    next[ns] = (next[ns] ?: 0L) + cnt
                }
            }
            cur = next
            pos--
        }

        var noRoot = 0L
        for ((st, cnt) in cur) {
            var hit = false
            var i = 0
            while (i < n) {
                val stored = ((st ushr (shift * i)) and slotMask).toInt()
                if (stored != dead && stored - base == target[i]) {
                    hit = true
                    break
                }
                i++
            }
            if (!hit) noRoot += cnt
        }
        noRootTotal += noRoot
    }
    return limit - noRootTotal
}
