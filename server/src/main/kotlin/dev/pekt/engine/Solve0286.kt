package dev.pekt.engine

import java.math.BigInteger
import kotlin.math.min

/**
 * PE 286 — Scoring Probabilities（投篮得分概率）：距离 x 处命中概率为 (1 − x/q)（q > 50），
 * x = 1…50 各投一次，「恰好得 20 分」的概率为 2%，求 q（保留 10 位小数）。
 *
 * 编码：题面要求 10 位小数，按项目惯例返回 round(q × 10^10) = 526494571953。
 *
 * 推导（详见 content/problems/0286/solution.kt 头部与 0286/analysis.md）
 * ────────────────────────────────────────────────────────────────
 * 1) P(q) = Σ_{|S|=20} Π_{x∈S}(1 − x/q)·Π_{x∉S}(x/q)。注意不是 e_20(1 − x/q)：各次投篮是独立
 *    Bernoulli，必须带上「其余 30 投全部不中」的因子（q = 50 时 e_20 ≈ 6.3×10^6，真值 ≈ 0.0412）。
 * 2) 乘 q^50 化成整系数多项式：G(q) = [s^20] Π_{x=1}^{50}(x + s(q − x))，P(q) = G(q)/q^50，
 *    方程 P(q) = 1/50 ⟺ 50·G(q) = q^50。
 * 3) q 增大时每个 p_x 增大、得分分布整体上移 ⟹ P(q) 在 q > 50 上严格递减；P(50) ≈ 0.0412 > 0.02
 *    > P(100) ≈ 2.5×10^-8，故根唯一，二分即可。
 *
 * 实现：主路径 A = 概率尺度 DP（e_t ← e_t·(x/q) + e_{t−1}·(1 − x/q)，t 降序就地更新、e_0 最后
 * 更新）+ 200 轮二分；路径 B = BigInteger 精确系数 + 把根夹在相邻 10^-15 网格点之间（精确复核）。
 * 两者编码一致（且与 content/problems/0286/solution.kt 的两条路径一致）。
 *
 * 复杂度：A 为 50·20 = 1000 次浮点乘加 × 200 轮二分，实测 ~0.1 ms；B 为 21×21 个 BigInteger
 * 系数递推 + 53 轮 850 位整数运算，实测 ~3 ms。远低于 10 s 熔断线。
 * 校验：小规模全子集枚举（n = 6/10）与 DP 逐位一致（最大差 1.7×10^-16）、小规模求根一致；
 * 蒙特卡洛 10^7 样本在 q* 处给出 0.019944 ± 8.7×10^-5，覆盖题面要求的 0.02。
 */
internal fun solve0286Impl(): Long {
    val qDouble = solveByBisection()
    val encodedA = Math.round(qDouble * 1e10)

    val qExact = solveByExactBisection()
    val encodedB = qExact.add(BigInteger.valueOf(50_000)).divide(BigInteger.valueOf(100_000)).toLong()
    check(encodedA == encodedB) { "两条路径编码不一致：$encodedA vs $encodedB" }
    return encodedA
}

/** 概率尺度 DP：打完 x = 1…n 枪后恰得 t 分的概率。 */
private fun probDp(q: Double, n: Int = 50, t: Int = 20): Double {
    val e = DoubleArray(t + 1)
    e[0] = 1.0
    for (x in 1..n) {
        val p = 1.0 - x / q
        val m = x / q
        for (k in min(x, t) downTo 1) e[k] = e[k] * m + e[k - 1] * p
        e[0] *= m
    }
    return e[t]
}

/** 主路径 A：对 P(q) − 0.02 二分（P 在 q > 50 上严格递减）。 */
private fun solveByBisection(): Double {
    var lo = 50.0
    var hi = 200.0
    check(probDp(lo) > 0.02 && probDp(hi) < 0.02) { "二分区间端点未变号" }
    repeat(200) {
        val mid = (lo + hi) / 2
        if (probDp(mid) > 0.02) lo = mid else hi = mid
    }
    return (lo + hi) / 2
}

/** G(q) = Σ_{|S|=20} Π_{x∈S}(q−x)·Π_{x∉S}x 的系数（下标 = q 的幂次）。 */
private fun gCoefficients(n: Int = 50, t: Int = 20): Array<BigInteger> {
    val c = Array(t + 1) { Array(t + 1) { BigInteger.ZERO } }
    c[0][0] = BigInteger.ONE
    for (x in 1..n) {
        val bx = BigInteger.valueOf(x.toLong())
        for (tt in min(x, t) downTo 1) {
            for (i in tt downTo 0) {
                var v = c[tt][i].multiply(bx)
                v = v.subtract(c[tt - 1][i].multiply(bx))
                if (i - 1 >= 0) v = v.add(c[tt - 1][i - 1])
                c[tt][i] = v
            }
        }
        c[0][0] = c[0][0].multiply(bx)
    }
    return Array(t + 1) { c[t][it] }
}

/** F(Q) = 50·G(Q/10^15)·10^750 − Q^50（符号同 50·P(Q/10^15) − 1）。 */
private fun scaledResidual(coeffs: Array<BigInteger>, q: BigInteger): BigInteger {
    val n = 50
    val scale = BigInteger.valueOf(1_000_000_000_000_000L)
    val nPow = ArrayList<BigInteger>(n + 1)
    var acc = BigInteger.ONE
    for (i in 0..n) {
        nPow.add(acc)
        acc = acc.multiply(scale)
    }
    var sum = BigInteger.ZERO
    var qPow = BigInteger.ONE
    for (i in coeffs.indices) {
        if (coeffs[i].signum() != 0) sum = sum.add(coeffs[i].multiply(qPow).multiply(nPow[n - i]))
        qPow = qPow.multiply(q)
    }
    return sum.multiply(BigInteger.valueOf(50L)).subtract(q.pow(n))
}

/** 路径 B：精确二分，返回 Q 使 F(Q) ≥ 0 > F(Q+1)（Q 的单位为 10^-15）。 */
private fun solveByExactBisection(): BigInteger {
    val scale = BigInteger.valueOf(1_000_000_000_000_000L)
    val coeffs = gCoefficients()
    var lo = scale.multiply(BigInteger.valueOf(50))
    var hi = scale.multiply(BigInteger.valueOf(60))
    check(scaledResidual(coeffs, lo).signum() > 0 && scaledResidual(coeffs, hi).signum() < 0) {
        "精确二分区间端点未变号"
    }
    while (hi.subtract(lo).bitLength() > 1) {
        val mid = lo.add(hi).shiftRight(1)
        if (scaledResidual(coeffs, mid).signum() > 0) lo = mid else hi = mid
    }
    return lo
}
