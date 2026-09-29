package dev.pekt.engine

import dev.pekt.math.primesUpTo

/**
 * PE 263 — An Engineers' Dream Come True（工程师的天堂）：求前四个 engineers' paradise 之和。
 *
 * 推导（详见 content/problems/0263/solution.kt 头部与 0263/analysis.md）：
 *   实用数判定（Stewart）：素因子升序 p₁=2<p₂<… 时，n 实用 ⇔ 每个 p_j ≤ 1 + σ(前面的素数幂)。
 *   题设的 triple-pair 等价于 p = n−9 起四个素数 p, p+6, p+12, p+18 在素数序列中连续。
 *   候选只可能 p ≡ 1, 11 (mod 30)：p 为奇素数；若 p ≢ 1 (mod 5)，则 p+6k（k=0..3）中必有
 *   一个被 5 整除；再排除 3 的倍数。于是只需在 2/30 的候选上扫描 [p, p+18] 的 10 个奇数位。
 *
 * 方法（与 solution.kt 方法 A 完全一致）：分段埃氏筛（只存奇数位，段长取 30 的倍数，
 *   扫描下标恰好落在 j ≡ 0,5 (mod 15)），段内检查 4 个素数位与 6 个空格位；命中候选后对
 *   n−8, n−4, n, n+4, n+8 做 σ 链实用数判定。从 2 向上推进，找到第 4 个 paradise 立即停止。
 *
 * 复杂度：筛到 ≈1.1466×10⁹，标记约 1.2×10⁹ 次；候选约 4.8×10⁴ 个。
 * 实测与校验：本机 JIT 预热后约 2.2 s（低于 10 s 熔断线）；题面锚点「首个 sexy pair (23,29)」
 *   与「6 的因数可表 1..6」成立；解法中的方法 B（候选空间模式筛 + 因数贪心判定）与
 *   暴力（≤10⁸）候选表逐一一致；完整答案 2039506520（前四个 paradise 之和）。
 */
internal fun solve0263Impl(): Long {
    val limitCap = 1_300_000_000L
    val base = primesUpTo(isqrt263(limitCap + 100).toInt() + 2)
    val stepNum = 2_097_150L                 // 30 的倍数
    val nbits = (stepNum / 2).toInt()
    val bits = LongArray(nbits / 64 + 1)
    var found = 0
    var sum = 0L
    var lo = 1L
    outer@ while (lo <= limitCap) {
        java.util.Arrays.fill(bits, -1L)
        val hi = minOf(lo + stepNum, limitCap + 1)
        var nbit = ((hi - lo + 1) / 2).toInt()
        if (nbit > nbits) nbit = nbits
        if (lo == 1L) bits[0] = bits[0] and 1L.inv()          // 1 不是素数
        for (pp in base) {
            if (pp == 2L) continue                             // 偶数位不在奇数表里
            if (pp * pp >= hi) break
            val s0 = if (pp * pp >= lo) pp * pp else ((lo + pp - 1) / pp) * pp
            val start = if (s0 and 1L == 0L) s0 + pp else s0
            var idx = ((start - lo) / 2).toInt()
            val step = pp.toInt()
            while (idx < nbit) {
                bits[idx ushr 6] = bits[idx ushr 6] and (1L shl (idx and 63)).inv()
                idx += step
            }
        }
        var j = 0
        var toggle = false
        while (j + 9 < nbit) {
            if (bits[j ushr 6] and (1L shl (j and 63)) != 0L &&
                bits[(j + 3) ushr 6] and (1L shl ((j + 3) and 63)) != 0L &&
                bits[(j + 6) ushr 6] and (1L shl ((j + 6) and 63)) != 0L &&
                bits[(j + 9) ushr 6] and (1L shl ((j + 9) and 63)) != 0L
            ) {
                var clear = true
                for (t in intArrayOf(1, 2, 4, 5, 7, 8)) {
                    val k = j + t
                    if (bits[k ushr 6] and (1L shl (k and 63)) != 0L) { clear = false; break }
                }
                if (clear) {
                    val n = lo + 2L * j + 9
                    if (isPractical263(n - 8) && isPractical263(n - 4) && isPractical263(n) &&
                        isPractical263(n + 4) && isPractical263(n + 8)
                    ) {
                        sum += n
                        found++
                        if (found == 4) break@outer
                    }
                }
            }
            j += if (toggle) 10 else 5
            toggle = !toggle
        }
        lo += stepNum
    }
    check(found == 4) { "只找到 $found 个 engineers' paradise" }
    return sum
}

/** σ 链实用数判定（n 的素因子升序，逐个要求 p ≤ 1 + σ(前面部分)）。 */
private fun isPractical263(n: Long): Boolean {
    if (n == 1L) return true
    if (n and 1L == 1L) return false
    var m = n
    var sigma = 1L
    var p = 2L
    while (p * p <= m) {
        if (m % p == 0L) {
            if (p > sigma + 1) return false
            var pk = 1L
            while (m % p == 0L) { m /= p; pk *= p }
            sigma *= (pk * p - 1) / (p - 1)
        }
        p += if (p == 2L) 1L else 2L
    }
    if (m > 1 && m > sigma + 1) return false
    return true
}

private fun isqrt263(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}
