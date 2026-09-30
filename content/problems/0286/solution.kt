#!/usr/bin/env kotlin
/**
 * Project Euler 286 — Scoring Probabilities（投篮得分概率）
 *
 * 题目
 * ────
 * 距离 x 处一投命中的概率是 (1 − x/q)，q > 50 为实常数。Barbara 在 x = 1, 2, …, 50 各投一次，
 * 记录显示「恰好得到 20 分」的概率正好是 2%。求 q，结果保留 10 位小数。
 *
 * 建模
 * ────
 * 各次投篮独立（命中/不中），设 p_x = 1 − x/q 为第 x 投命中概率，则
 *
 *   P(q) = Σ_{|S| = 20} Π_{x∈S} p_x · Π_{x∉S} (1 − p_x)
 *        = Σ_{|S| = 20} Π_{x∈S} (1 − x/q) · Π_{x∉S} (x/q).                  (★)
 *
 * **注意**：不是 e_20(p_1, …, p_50)（初等对称多项式）——那只累加了「S 全中」的因子，
 * 缺了「其余 30 投全部不中」的因子 Π_{x∉S}(x/q)，两者数值差极大（q = 50 时 e_20 ≈ 6.3×10^6
 * 而真概率 ≈ 4.1×10^-2）。这一点在 analysis.md 里有专门说明与对拍证据。
 *
 * 把 (★) 乘上 q^50 得到整系数多项式：令
 *
 *   G(q) = Σ_{|S| = 20} Π_{x∈S} (q − x) · Π_{x∉S} x = [s^20] Π_{x=1}^{50} (x + s(q − x)),
 *
 * 则 P(q) = G(q)/q^50，方程 P(q) = 1/50 等价于
 *
 *   50·G(q) = q^50.                                                        (∗)
 *
 * 单调性：q 增大时每个 p_x 都增大，得分分布整体上移，P(q) 在 q > 50 上单调递减
 * （P(50) ≈ 0.041228 > 0.02，P(100) ≈ 2.5×10^-8 < 0.02），故 (∗) 在 (50, ∞) 上有唯一解。
 *
 * 主路径 A（双精度 DP + 二分）
 * ────────────────────────
 * 直接在「概率尺度」上做卷积：e_t = 打完若干枪后恰得 t 分的概率，
 *
 *   e_t ← e_t·(x/q) + e_{t−1}·(1 − x/q)     (t 降序就地更新，e_0 最后更新)
 *
 * 50 枪 × 20 分 = O(50·20) 次运算，全部非负、无相消，相对误差 ~10^-15。再对
 * F(q) = P(q) − 1/50 在 [50, 200] 上二分（~50 轮），得 q。
 *
 * 主路径 B（精确整数算术，独立复核）
 * ────────────────────────────
 * 用 Python 大整数风格的 BigInteger 递推算出 G 的系数 c_i（i ≤ 20）：
 *
 *   c_t ← x·c_t − x·c_{t−1} + shift(c_{t−1})    （t 降序，c_0 最后乘 x），
 *
 * 再把 q = Q/10^15（Q 为整数）代入 (∗) 并乘 10^{750}：
 *
 *   F(Q) = 50·Σ_{i=0}^{20} c_i·Q^i·10^{15(50−i)} − Q^50,
 *
 * F 在 [50·10^15, 60·10^15] 上单调递减且变号，二分 Q 到相邻整数（F(Q) ≥ 0 > F(Q+1)），
 * 就把根夹在长度 10^{-15} 的区间里。全程整数运算，没有任何浮点误差。
 *
 * 答案编码（项目惯例）：题面要求保留 10 位小数，故返回 round(q × 10^10)。
 * 由 Q ∈ [52649457195309171, 52649457195309172) 得 q·10^10 ∈ [526494571953.09171,
 * 526494571953.09172)，取整为 526494571953（距 .5 边界极远，取证安全）。
 *
 * 复杂度
 * ──────
 * A：50·20 = 1000 次浮点乘加 × ~50 轮二分 ≈ 5×10^4 flops，微秒级；
 * B：21×21 个 BigInteger 系数的递推（最大 ~10^78 量级）+ 53 轮二分，每轮 ~850 位整数
 * 的乘方与求和，毫秒级。
 *
 * 验证
 * ────
 * 1. 题面条件自洽：P(q*) 反算 = 0.02（A、B 两条路径都验到 10^-15 / 精确有理数）；
 * 2. 小规模全子集枚举（n = 6, t = 2/3，2^n 个子集逐项累加）与 DP 在若干 q 上逐位一致，
 *    并对小规模模拟（n = 6、t = 2）用子集枚举 + 二分求根，与 DP 二分的根一致到 10^-13；
 * 3. 两条全尺寸路径（浮点 DP+二分、精确整数+二分）给出的 Q 只差 1（即 10^-15 的区间端点），
 *    提交的 10 位小数两者相同；
 * 4. 蒙特卡洛旁证与被减量补偿：见 brute-force.kt（10^7 样本的 95% 置信区间覆盖 0.02）。
 *
 * 答案：q = 52.6494571953… ⟹ round(q × 10^10) = 526494571953
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0286/solution.kt -d /tmp/kc-0286
 * java -Xmx4g -cp /tmp/kc-0286:<kotlin-stdlib> SolutionKt
 */

import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToLong

private const val SHOTS = 50                 // x = 1 … 50 各投一次
private const val TARGET = 20                // 恰好 20 分
private const val P_MEASURED = 0.02          // 题面给定的概率 2%

/** 概率尺度 DP：打完 x = 1…n 枪后恰得 t 分的概率（就地降序更新）。 */
private fun probDp(q: Double, n: Int = SHOTS, t: Int = TARGET): Double {
    val e = DoubleArray(t + 1)
    e[0] = 1.0
    for (x in 1..n) {
        val p = 1.0 - x / q                  // 命中
        val m = x / q                        // 不中
        for (k in min(x, t) downTo 1) e[k] = e[k] * m + e[k - 1] * p
        e[0] *= m
    }
    return e[t]
}

/** 主路径 A：对 P(q) − 0.02 二分（P 单调递减）。 */
private fun solveByBisection(): Double {
    var lo = 50.0
    var hi = 200.0
    require(probDp(lo) > P_MEASURED && probDp(hi) < P_MEASURED) { "二分区间端点未变号" }
    repeat(200) {
        val mid = (lo + hi) / 2
        if (probDp(mid) > P_MEASURED) lo = mid else hi = mid
    }
    return (lo + hi) / 2
}

// ───────────────────── 主路径 B：精确整数（BigInteger） ─────────────────────

/**
 * G(q) = Σ_{|S|=20} Π_{x∈S}(q−x)·Π_{x∉S}x 的系数（下标 = q 的幂次）。
 * 递推（对每个 x 乘上 (x + s(q−x))）：
 *   c_new[t] = x·c_old[t] + (q − x)·c_old[t−1]，t 降序就地更新，最后 c_0 *= x。
 */
private fun gCoefficients(n: Int = SHOTS, t: Int = TARGET): Array<BigInteger> {
    val c = Array(t + 1) { arrayOfNulls<BigInteger>(t + 1) }
    val zero = BigInteger.ZERO
    for (a in 0..t) for (b in 0..t) c[a][b] = zero
    c[0][0] = BigInteger.ONE
    for (x in 1..n) {
        val bx = BigInteger.valueOf(x.toLong())
        for (tt in min(x, t) downTo 1) {
            for (i in tt downTo 0) {
                var v = c[tt][i]!!.multiply(bx)              // x·c_old[t][i]
                v = v.subtract(c[tt - 1][i]!!.multiply(bx))  // − x·c_old[t−1][i]
                if (i - 1 >= 0) v = v.add(c[tt - 1][i - 1]!!) // + (q 部分) c_old[t−1][i−1]
                c[tt][i] = v
            }
        }
        c[0][0] = c[0][0]!!.multiply(bx)                     // 30 投全不中的权重 Π x
    }
    return Array(t + 1) { c[t][it]!! }
}

/**
 * F(Q) = 50·G(Q/N)·N^50 − Q^50 的符号与 50·P(Q/N) − 1 相同（N = 10^15，缩放为正）。
 * 用 BigInteger 精确计算。
 */
private fun scaledResidual(coeffs: Array<BigInteger>, q: BigInteger, n: Int = SHOTS): BigInteger {
    // Σ_i c_i · Q^i · N^{n−i}
    var sum = BigInteger.ZERO
    var qPow = BigInteger.ONE
    val nPowCache = ArrayList<BigInteger>(n + 1)
    var acc = BigInteger.ONE
    for (i in 0 until n + 1) {
        nPowCache.add(acc)
        acc = acc.multiply(SCALE)
    }
    for (i in coeffs.indices) {
        if (coeffs[i].signum() == 0) {
            qPow = qPow.multiply(q)
            continue
        }
        sum = sum.add(coeffs[i].multiply(qPow).multiply(nPowCache[n - i]))
        qPow = qPow.multiply(q)
    }
    return sum.multiply(BigInteger.valueOf(50L)).subtract(q.pow(n))
}

private val SCALE: BigInteger = BigInteger.valueOf(1_000_000_000_000_000L)  // 10^15

/** 主路径 B：精确二分，返回 Q 使 F(Q) ≥ 0 > F(Q+1)（Q 的单位是 10^-15）。 */
private fun solveByExactBisection(coeffs: Array<BigInteger>): BigInteger {
    var lo = SCALE.multiply(BigInteger.valueOf(50))
    var hi = SCALE.multiply(BigInteger.valueOf(60))
    require(scaledResidual(coeffs, lo).signum() > 0 && scaledResidual(coeffs, hi).signum() < 0) {
        "精确二分的区间端点未变号"
    }
    while (hi.subtract(lo).bitLength() > 1) {          // 相邻整数时停止
        val mid = lo.add(hi).shiftRight(1)
        if (scaledResidual(coeffs, mid).signum() > 0) lo = mid else hi = mid
    }
    return lo
}

// ───────────────────── 小规模全子集枚举（对拍用） ─────────────────────

/** 直接枚举 2^n 个子集：n 枪、恰得 t 分的概率。只用于小 n。 */
private fun subsetProb(q: Double, n: Int, t: Int): Double {
    var total = 0.0
    for (mask in 0 until (1 shl n)) {
        if (Integer.bitCount(mask) != t) continue
        var pr = 1.0
        for (x in 1..n) {
            pr *= if ((mask shr (x - 1)) and 1 == 1) 1.0 - x / q else x / q
        }
        total += pr
    }
    return total
}

/** 小规模模拟的根（子集枚举 + 二分），用于与 DP 的根互证。 */
private fun smallAnalogRoot(n: Int, t: Int, target: Double): Double {
    fun f(q: Double) = subsetProb(q, n, t) - target
    var lo = n.toDouble() + 1e-9                       // q > n 才有正的命中概率
    var hi = 10.0 * n
    require(f(lo) > 0 && f(hi) < 0) { "小规模区间未变号（n=$n t=$t）" }
    repeat(200) {
        val mid = (lo + hi) / 2
        if (f(mid) > 0) lo = mid else hi = mid
    }
    return (lo + hi) / 2
}

private fun dpAnalogRoot(n: Int, t: Int, target: Double): Double {
    fun f(q: Double) = probDp(q, n, t) - target
    var lo = n.toDouble() + 1e-9
    var hi = 10.0 * n
    require(f(lo) > 0 && f(hi) < 0)
    repeat(200) {
        val mid = (lo + hi) / 2
        if (f(mid) > 0) lo = mid else hi = mid
    }
    return (lo + hi) / 2
}

// ─────────────────────────────── 计时与输出 ───────────────────────────────

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("  $tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面条件：P(50) > 0.02 > P(100)，单调性 ----------
    val p50 = probDp(50.0)
    val p100 = probDp(100.0)
    check(p50 > P_MEASURED && p100 < P_MEASURED) { "P 的单调性/端点异常：P(50)=$p50 P(100)=$p100" }
    var prev = Double.MAX_VALUE
    var q = 50.0
    while (q <= 120.0) {                               // 网格上验证 P 单调递减
        val v = probDp(q)
        check(v < prev + 1e-15) { "P(q) 在 q=$q 不单调" }
        prev = v
        q += 0.5
    }
    println("单调性：P(50) = ${"%.9f".format(p50)}，P(100) = ${"%.3e".format(p100)}，网格上严格递减 ✓")

    // ---------- 2. 小规模全子集枚举 vs DP（n = 6、t = 2/3） ----------
    var maxDiff = 0.0
    for ((n, t) in listOf(6 to 2, 6 to 3, 10 to 4)) {
        for (qq in listOf(7.0, 12.5, 30.0, 100.0)) {
            val a = subsetProb(qq, n, t)
            val b = probDp(qq, n, t)
            maxDiff = maxOf(maxDiff, abs(a - b))
        }
    }
    check(maxDiff < 1e-14) { "子集枚举与 DP 最大差 $maxDiff" }
    println("对拍 1（子集枚举 vs DP，n = 6/10 的 12 组）：最大差 ${"%.2e".format(maxDiff)} ✓")

    // 小规模求根：子集枚举 + 二分 vs DP + 二分
    for ((n, t, target) in listOf(Triple(6, 2, 0.05), Triple(6, 3, 0.1), Triple(10, 4, 0.02))) {
        val ra = smallAnalogRoot(n, t, target)
        val rb = dpAnalogRoot(n, t, target)
        check(abs(ra - rb) < 1e-12) { "小规模根不一致：n=$n t=$t $ra vs $rb" }
        println("对拍 2（n=$n、t=$t、目标 $target 的根）：子集枚举 ${"%.12f".format(ra)}，" +
            "DP ${"%.12f".format(rb)} ✓")
    }

    // ---------- 3. 主路径 A：双精度 DP + 二分 ----------
    val t0 = System.nanoTime()
    val qDouble = solveByBisection()
    val msA0 = (System.nanoTime() - t0) / 1e6
    val pAtRoot = probDp(qDouble)
    println("主路径 A（双精度 DP + 二分）：q = ${"%.15f".format(qDouble)}，反算 P(q) = ${"%.17f".format(pAtRoot)}" +
        "（首次 ${"%.1f".format(msA0)} ms）")
    check(abs(pAtRoot - P_MEASURED) < 5e-16) { "A 的反算概率偏大：$pAtRoot" }

    // ---------- 4. 主路径 B：精确整数 ----------
    val t1 = System.nanoTime()
    val coeffs = gCoefficients()
    val qExact = solveByExactBisection(coeffs)
    val msB0 = (System.nanoTime() - t1) / 1e6
    val qLo = qExact.toDouble() / 1e15
    val qHi = (qExact + BigInteger.ONE).toDouble() / 1e15
    println("主路径 B（精确整数）：Q = $qExact ⟹ q ∈ [${"%.15f".format(qLo)}, ${"%.15f".format(qHi)})" +
        "（首次 ${"%.1f".format(msB0)} ms）")
    check(qLo <= qDouble && qDouble <= qHi + 1e-15) { "A 的根不在 B 的夹逼区间内：$qDouble" }

    // ---------- 5. 编码：round(q × 10^10) ----------
    val encoded = qExact.add(BigInteger.valueOf(50_000)).divide(BigInteger.valueOf(100_000))
    val encodedDouble = (qDouble * 1e10).roundToLong()
    check(encoded == BigInteger.valueOf(encodedDouble)) {
        "两条路径的编码不一致：$encoded vs $encodedDouble"
    }
    println("编码：q·10^10 = ${"%.9f".format(qExact.toDouble() / 1e5)} ⟹ round = $encoded" +
        "（两条路径一致）")

    // ---------- 6. 计时 ----------
    solveByBisection()
    val msA = bestOf3("主路径 A 双精度 DP + 二分", encoded.toLong()) {
        (solveByBisection() * 1e10).roundToLong()
    }
    gCoefficients()
    val msB = bestOf3("主路径 B 精确整数（系数 + 精确二分）", encoded.toLong()) {
        solveByExactBisection(gCoefficients())
            .add(BigInteger.valueOf(50_000)).divide(BigInteger.valueOf(100_000)).toLong()
    }

    // ---------- 7. 输出 ----------
    println()
    println("q = ${"%.15f".format(qExact.toDouble() / 1e15)}…（保留 10 位小数 = ${"%.10f".format(qDouble)}）")
    println("答案 = $encoded（编码：round(q × 10^10)）")
    println("汇总：A ${"%.3f".format(msA)} ms；B ${"%.3f".format(msB)} ms")
    println("check() 全部通过")
}
