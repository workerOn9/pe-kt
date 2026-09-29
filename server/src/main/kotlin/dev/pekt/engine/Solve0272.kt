package dev.pekt.engine

import dev.pekt.math.primesUpTo

/**
 * PE 272 — Modular Cubes, Part 2（模立方 II）：求所有 n ≤ 10¹¹ 且 C(n) = 242 的 n 之和，
 * C(n) = #{x : 1 < x < n, x³ ≡ 1 (mod n)}。
 *
 * 推导（详见 content/problems/0272/solution.kt 头部与 0272/analysis.md）：
 *   C(n) = R(n) − 1，R(n) = ∏ R(p^e) 是 x³ ≡ 1 (mod n) 的解数；R(p^e) = gcd(3, φ(p^e)) ∈ {1, 3}：
 *   只有 p ≡ 1 (mod 3)（任意指数）与 p = 3, e ≥ 2 贡献 3。记 k(n) = n 的不同素因子中 ≡ 1 (mod 3)
 *   的个数，则 C(n) = 3^{k(n)+[9|n]} − 1，故 C(n) = 242 ⟺ k(n) + [9|n] = 5。
 *
 *   n 唯一分解为 n = s·m：m = 全部 ≡1 (mod 3) 素因子（带指数）之积，s = 其余部分。两种情形：
 *     k = 5, 9 ∤ s（m 恰含 5 个不同素数）；k = 4, 9 | s（写 s = 9s′）。
 *   记 P_all(L) = Σ_{s ≤ L, 素因子 ≢ 1 (mod 3)} s、P₀(L) = P_all(L) − 9·P_all(⌊L/9⌋)，则
 *     答案 = Σ_{m₅ ≤ B} m₅·P₀(⌊B/m₅⌋) + 9·Σ_{m₄ ≤ B/9} m₄·P_all(⌊(B/9)/m₄⌋)，B = 10¹¹。
 *
 *   实现：s 侧用标记筛（把被 ≡1 (mod 3) 素数整除的数剔除）建两套前缀和；m 侧用「升序素数 +
 *   指数 ≥ 1」的 DFS，以「后继最小可用素因子乘积」剪枝，递归携带商 q = ⌊上界/prod⌋，
 *   叶子处用 ⌊q/p^e⌋ O(1) 查表。总和带溢出检查（答案 8495585919506151122 < Long.MAX_VALUE）。
 *
 * 复杂度：O(P log log P + sMax log log sMax + #m)，P ≈ 6.43×10⁶（最大可能三次素因子）、
 *   #m ≈ 4.1×10⁷（m 枚举叶子数），实测 ≈ 0.2 s，远低于 10 s 引擎熔断线。
 *
 * 校验：题面锚点 C(91) = 8；n ≤ 10⁶/10⁷/10⁸ 三个规模与独立筛法逐 n 路径完全一致；
 *   内容侧 solution.kt 另有方法 B（降序枚举 + 直方图 + 递归生成的 s 侧）全量互证。
 *   逻辑与 content/problems/0272/solution.kt 主路径（方法 A）一致。
 */
internal fun solve0272Impl(): Long {
    val bound = 100_000_000_000L

    val primeLimit = maxOf(43L, bound / 9 / (7 * 13 * 19) + 1)
    val p1 = primesUpTo(primeLimit.toInt()).filter { it % 3L == 1L }.toLongArray()

    // s 侧：标记所有「被某个 ≡1 (mod 3) 素数整除」的 s，再做两套前缀和
    val sMax = maxOf(bound / (7 * 13 * 19 * 31), bound / (7 * 13 * 19 * 31 * 37))
    val bad = BooleanArray(sMax.toInt() + 1)
    for (p in p1) {
        if (p > sMax) break
        var m = p
        while (m <= sMax) { bad[m.toInt()] = true; m += p }
    }
    val listAll = ArrayList<Long>()
    val list0 = ArrayList<Long>() // 9 ∤ s 的部分（k = 5 分支）
    for (v in 1..sMax.toInt()) {
        if (!bad[v]) { listAll.add(v.toLong()); if (v % 9 != 0) list0.add(v.toLong()) }
    }
    val denseLimit = minOf(50_424, sMax.toInt()) // 覆盖全部 5-素 m 的查表点 ⌊B/m₅⌋
    val lutAll = buildCondSum0272(listAll.toLongArray(), denseLimit)
    val lut0 = buildCondSum0272(list0.toLongArray(), denseLimit)

    val mt5 = mtTable0272(p1, 5, bound)
    val mt4 = mtTable0272(p1, 4, bound / 9)
    val a = accumulateM0272(p1, 5, bound, lut0, 1L, mt5)
    val b = accumulateM0272(p1, 4, bound / 9, lutAll, 9L, mt4)
    return Math.addExact(a, b)
}

/** 升序 + 前缀和 + 稠密小表（≤ denseLimit 的查询 O(1)，更长的二分）。 */
private class CondSum0272(val sorted: LongArray, val pref: LongArray, val dense: LongArray) {
    fun at(l: Long): Long {
        if (l < dense.size) return dense[l.toInt()]
        var lo = 0
        var hi = sorted.size
        while (lo < hi) {
            val mid = (lo + hi) ushr 1
            if (sorted[mid] <= l) lo = mid + 1 else hi = mid
        }
        return pref[lo]
    }
}

private fun buildCondSum0272(values: LongArray, denseLimit: Int): CondSum0272 {
    val sorted = values.copyOf().also { it.sort() }
    val pref = LongArray(sorted.size + 1)
    for (i in sorted.indices) pref[i + 1] = pref[i] + sorted[i]
    val dense = LongArray(denseLimit + 1)
    var acc = 0L
    var idx = 0
    for (v in 0..denseLimit) {
        while (idx < sorted.size && sorted[idx] <= v) { acc += sorted[idx]; idx++ }
        dense[v] = acc
    }
    return CondSum0272(sorted, pref, dense)
}

/** mt[j][i] = pᵢ·pᵢ₊₁·…·pᵢ₊ⱼ₋₁（超过 cap 记为 cap+1），用于「后继最小可用素因子」剪枝。 */
private fun mtTable0272(primes: LongArray, k: Int, cap: Long): Array<LongArray> {
    val n = primes.size
    val mt = Array(k + 1) { LongArray(n + 1) { cap + 1 } }
    for (i in 0..n) mt[0][i] = 1L
    for (j in 1..k) for (i in n - 1 downTo 0) {
        val below = mt[j - 1][i + 1]
        mt[j][i] = if (below > cap / primes[i]) cap + 1 else primes[i] * below
    }
    return mt
}

/** Σ (factor·m)·lut.at(⌊boundForM/m⌋)，m 恰含 k 个不同 ≡1 (mod 3) 素数（指数 ≥ 1）。 */
private fun accumulateM0272(
    primes: LongArray, k: Int, boundForM: Long, lut: CondSum0272, factor: Long, mt: Array<LongArray>
): Long {
    var total = 0L
    fun rec(startIdx: Int, chosen: Int, prod: Long, q: Long) {
        val need = k - chosen
        if (need == 1) {
            for (i in startIdx until primes.size) {
                val p = primes[i]
                if (p > q) break
                var v = prod * p
                var qq = q / p
                while (true) {
                    total = Math.addExact(total, Math.multiplyExact(v * factor, lut.at(qq)))
                    if (qq < p) break
                    v *= p
                    qq /= p
                }
            }
            return
        }
        for (i in startIdx until primes.size) {
            if (mt[need][i] > q) break
            val p = primes[i]
            val tail = mt[need - 1][i + 1]
            var v = prod * p
            var qq = q / p
            while (tail <= qq) {
                rec(i + 1, chosen + 1, v, qq)
                if (qq < p) break
                v *= p
                qq /= p
            }
        }
    }
    rec(0, 0, 1L, boundForM)
    return total
}
