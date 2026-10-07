package dev.pekt.engine

import dev.pekt.math.modInverse

/**
 * PE 322 — Binomial Coefficients Divisible by 10（被 10 整除的二项式系数）
 *
 * T(m,n) = #{ i : n ≤ i < m 且 C(i,n) 能被 10 整除 }。
 * 已知 T(10⁹, 10⁷−10) = 989697000，求 T(10¹⁸, 10¹²−10)。
 *
 * 推导（详见 content/problems/0322/solution.kt 头部与 0322/analysis.md）：
 *
 *   1. Kummer：v_p(C(i,n)) = 把 n 与 a = i−n 在 p 进制下相加的进位次数。
 *      加法无进位 ⟺ 每位 a_k + n_k ≤ p−1，即 a_k ≤ p−1−n_k。
 *        p=2：无进位 ⟺ (a & n) == 0；
 *        p=5：无进位 ⟺ 五进制每位 a_k ≤ 4 − n_k。
 *   2. C(i,n) 被 10 整除 ⟺ 被 2 整除且被 5 整除 ⟺ 二进制有进位 且 五进制有进位。
 *      令 M = m−n，i∈[n,m) ↔ a=i−n∈[0,M)，由容斥
 *        T = M − N₂ − N₅ + N₂₅，
 *      N₂ / N₅ / N₂₅ 分别是「无 2 进位」「无 5 进位」「两者都无进位」的 a 计数。
 *   3. N₂₅ 用 CRT：取 A = n 的二进制位数、B = n 的五进制位数（必要时加大 A 使
 *      2^A·5^B > M），则 a ↦ (a mod 2^A, a mod 5^B) 在 [0,M) 上单射。
 *        R = { r < 2^A : r & n == 0 }（2^{A−popcount(n)} 个）
 *        L = { l < 5^B : 各位 l_k ≤ 4−n_k }（Π(5−n_k) 个）
 *      a = r + 2^A·t，t ≡ (l−r)·inv_{2^A} (mod 5^B)，a<M ⟺ t ≤ ⌊(M−1−r)/2^A⌋。
 *      把 {l·inv mod 5^B} 排序后对每个 r 二分统计落在 [c, c+T]（模 5^B）内的个数。
 *
 *   本机实测（JIT 预热后）：约 68 ms。最终答案：999998760323313995。
 */
internal fun solve0322Impl(): Long {
    val m = 1_000_000_000_000_000_000L
    val n = 1_000_000_000_000L - 10
    return pe322Solve(m, n)
}

/** 返回 x 的 b 进制数位（低位在前）。 */
private fun pe322Digits(x: Long, b: Int): IntArray {
    var v = x
    val out = ArrayList<Int>()
    while (v > 0) { out.add((v % b).toInt()); v /= b }
    if (out.isEmpty()) out.add(0)
    return out.toIntArray()
}

/** 计算 (a·b) mod m，加倍法避免 64 位溢出。 */
private fun pe322Mulmod(a: Long, b: Long, m: Long): Long {
    var res = 0L
    var x = a % m
    var y = b % m
    while (y > 0) {
        if (y and 1L == 1L) res = if (res >= m - x) res - (m - x) else res + x
        x = if (x >= m - x) x - (m - x) else x + x
        y = y shr 1
    }
    return res
}

/** 有序数组 arr 中 ≤ v 的元素个数。 */
private fun pe322CountLE(arr: LongArray, v: Long): Int {
    var lo = 0
    var hi = arr.size
    while (lo < hi) {
        val mid = (lo + hi) ushr 1
        if (arr[mid] <= v) lo = mid + 1 else hi = mid
    }
    return lo
}

/** T(m,n) = #{ i ∈ [n,m) : C(i,n) 能被 10 整除 }。 */
private fun pe322Solve(m: Long, n: Long): Long {
    val M = m - n

    var A = 64 - java.lang.Long.numberOfLeadingZeros(n)
    if (A == 0) A = 1
    val n5 = pe322Digits(n, 5)
    val B = n5.size
    var p5 = 1L
    repeat(B) { p5 *= 5 }
    while (A < 62 && (1L shl A) <= M / p5) A++
    val p2 = 1L shl A

    // R = { r < 2^A : r & n == 0 }
    val freeBits = ArrayList<Int>()
    for (j in 0 until A) if ((n shr j) and 1L == 0L) freeBits.add(j)
    val rCount = 1 shl freeBits.size
    val rs = LongArray(rCount)
    for (mask in 0 until rCount) {
        var r = 0L
        var mm = mask
        var idx = 0
        while (mm != 0) {
            if (mm and 1 == 1) r = r or (1L shl freeBits[idx])
            mm = mm shr 1
            idx++
        }
        rs[mask] = r
    }

    var n2 = 0L
    for (r in rs) if (r <= M - 1) n2 += (M - 1 - r) / p2 + 1

    // L = { l < 5^B : 各位 l_k ≤ 4−n_k }
    val c5 = IntArray(B) { 4 - n5[it] }
    val digs = IntArray(B)
    val ls = ArrayList<Long>()
    while (true) {
        var l = 0L
        var k = B - 1
        while (k >= 0) { l = l * 5 + digs[k]; k-- }
        ls.add(l)
        var i = 0
        while (i < B && digs[i] == c5[i]) { digs[i] = 0; i++ }
        if (i == B) break
        digs[i]++
    }
    var n5count = 0L
    for (l in ls) if (l <= M - 1) n5count += (M - 1 - l) / p5 + 1

    // N₂₅
    val inv = modInverse(p2, p5)
    val u = LongArray(ls.size) { pe322Mulmod(ls[it], inv, p5) }
    java.util.Arrays.sort(u)

    var n25 = 0L
    for (r in rs) {
        val tmax = (M - 1 - r) / p2
        val c = pe322Mulmod(r, inv, p5)
        val hi = c + tmax
        if (hi < p5) {
            n25 += pe322CountLE(u, hi) - pe322CountLE(u, c - 1)
        } else {
            n25 += (u.size - pe322CountLE(u, c - 1)) + pe322CountLE(u, hi - p5)
        }
    }

    return M - n2 - n5count + n25
}
