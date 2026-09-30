package dev.pekt.engine

/**
 * PE 297 — Zeckendorf 表示（Zeckendorf Representation）：Σ z(n)（0 < n < 10^17）= 2252639041804718029。
 *
 * 推导（详见 content/problems/0297/solution.kt 头部与 0297/analysis.md）：
 *   权重 w_0 = 1, w_1 = 2, w_i = w_{i−1}+w_{i−2}；「无相邻 1」的长度 m 位串与 [0, w_m−1]
 *   一一对应。记 C(m) = 合法串数 = w_m，T(m) = 串中 1 的总数 = T(m−1)+T(m−2)+C(m−2)。
 *
 *   路径 B（此处采用）：把 [0, B]（B = X−1）按「第一个小于 B 的位 i」分类，加上 B 自身：
 *     Σ_{n≤B} z(n) = z(B) + Σ_{i: b_i=1} [ onesAbove(i)·C(i) + T(i) ]，
 *   其中 onesAbove(i) 为 B 在 i 位以上 1 的个数。O(L)，L = 82。
 *   与数位 DP 路径（content 里 solution.kt 的路径 A）在 X = 10^17 上同值。
 *
 * 复杂度：O(L) ≈ 微秒级；内存几个 LongArray。
 */
internal fun solve0297Impl(): Long {
    val b = 100_000_000_000_000_000L - 1L

    val w = LongArray(90)
    w[0] = 1L
    w[1] = 2L
    for (i in 2 until 90) w[i] = w[i - 1] + w[i - 2]

    var len = 0
    while (w[len] <= b) len++
    val bits = BooleanArray(len)
    var rem = b
    var j = len - 1
    while (rem > 0L) {
        while (w[j] > rem) j--
        bits[j] = true
        rem -= w[j]
        j--
    }

    val cntFree = LongArray(len + 1)                 // C(m)
    val onesFree = LongArray(len + 1)                // T(m)
    cntFree[0] = 1L
    if (len >= 1) {
        cntFree[1] = 2L
        onesFree[1] = 1L
    }
    for (m in 2..len) {
        cntFree[m] = cntFree[m - 1] + cntFree[m - 2]
        onesFree[m] = onesFree[m - 1] + onesFree[m - 2] + cntFree[m - 2]
    }

    var total = 0L
    var onesAbove = 0
    for (pos in len - 1 downTo 0) {
        if (bits[pos]) {
            total += onesAbove.toLong() * cntFree[pos] + onesFree[pos]
            onesAbove++
        }
    }
    total += onesAbove.toLong()                      // B 自身
    return total
}
