package dev.pekt.engine

/**
 * PE 258 — A Lagged Fibonacci Sequence（滞后斐波那契数列）。
 *
 * g_k = g_{k−2000} + g_{k−1999}（k ≥ 2000，g_0 = … = g_1999 = 1）是 2000 阶常系数线性递推：
 * 平移后 g_{n+2000} = g_n + g_{n+1}，特征多项式 f(x) = x^2000 − x − 1，即 x^2000 ≡ x + 1 (mod f)。
 * Kitamasa 定理：若 x^k ≡ Σ c_i x^i (mod f)，则 g_k = Σ c_i g_i；本题初值全为 1，故 g_k ≡ Σ c_i。
 *
 * 实现（与 content/problems/0258/solution.kt 的方法 A 一致）：
 *   系数向量长度 2000，乘法 = 惰性取模的长度 2000 卷积（系数 < 2^25，2000 项乘积之和 < 2^61，
 *   Long 累加安全）+ 一次线性归约 x^t → x^{t−1999} + x^{t−2000}（t ≥ 2000）；平方按 i ≤ j 的
 *   半三角用 2·a_i·a_j 一次算两项。k = 10^18 共 59 次平方 + 24 次乘法，实测约 55 ms，远低于
 *   10 s 熔断线。模数 20092010 = 2 × 5 × 2009201 是合数，但全程只有加法与乘法，无需除法/逆元
 *   （本题也没有素数筛、gcd、组合数等可换成 dev.pekt.math 的通用步骤）。
 *
 * 双方法互证（见 content/problems/0258/analysis.md）：solution.kt 中另用 Bostan–Mori 生成函数
 * 折半给出同一答案，O(k) 直接迭代在 k = 10^4 / 10^6 / 10^8 三方对照，公开答案表亦为 12747994。
 */
internal fun solve0258Impl(): Long {
    val c = pe258PolyPowX(1_000_000_000_000_000_000L)
    var sum = 0L
    for (v in c) sum += v
    return sum % PE258_MOD
}

private const val PE258_DIM = 2000
private const val PE258_MOD = 20092010L

/** x^k mod f 的系数向量（二进制快速幂）。 */
private fun pe258PolyPowX(k: Long): LongArray {
    var result = LongArray(PE258_DIM); result[0] = 1L
    var base = LongArray(PE258_DIM); base[1] = 1L
    var e = k
    while (e > 0L) {
        if (e and 1L == 1L) result = pe258PolyMul(result, base)
        e = e ushr 1
        if (e > 0L) base = pe258PolySquare(base)
    }
    return result
}

/** 多项式乘法：卷积 + 归约 x^t → x^{t−1999} + x^{t−2000}（t ≥ 2000）。 */
private fun pe258PolyMul(a: LongArray, b: LongArray): LongArray {
    val tmp = LongArray(2 * PE258_DIM - 1)
    for (i in 0 until PE258_DIM) {
        val ai = a[i]
        if (ai == 0L) continue
        var idx = i
        for (j in 0 until PE258_DIM) {
            tmp[idx] += ai * b[j]
            idx++
        }
    }
    return pe258PolyReduce(tmp)
}

/** 平方：只枚举 i ≤ j，tmp[i+j] += 2·a_i·a_j（i < j）/ a_i²（i = j）。 */
private fun pe258PolySquare(a: LongArray): LongArray {
    val tmp = LongArray(2 * PE258_DIM - 1)
    for (i in 0 until PE258_DIM) {
        val ai = a[i]
        if (ai == 0L) continue
        tmp[2 * i] += ai * ai
        val twice = ai shl 1
        var idx = 2 * i + 1
        for (j in i + 1 until PE258_DIM) {
            tmp[idx] += twice * a[j]
            idx++
        }
    }
    return pe258PolyReduce(tmp)
}

/** 把 3999 个系数折回 2000 个：res[j] = tmp[j] + tmp[j+1999]（j ≥ 1）+ tmp[j+2000]（j ≤ 1998）。 */
private fun pe258PolyReduce(tmp: LongArray): LongArray {
    val res = LongArray(PE258_DIM)
    for (j in 0 until PE258_DIM) {
        var v = tmp[j] % PE258_MOD
        if (j >= 1) v += tmp[j + PE258_DIM - 1] % PE258_MOD
        if (j <= PE258_DIM - 2) v += tmp[j + PE258_DIM] % PE258_MOD
        res[j] = v % PE258_MOD
    }
    return res
}
