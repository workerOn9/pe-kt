#!/usr/bin/env kotlin
/**
 * Project Euler 285 — Pythagorean Odds（勾股概率）
 *
 * 题目：Albert 先选一个正整数 k，再在 [0,1] 上均匀、独立地取两个实数 a、b；计算
 * √((ka+1)² + (kb+1)²) 并四舍五入到最近的整数，若结果恰等于 k 就得 k 分，否则得 0 分。
 * 题面给出：k = 1..10 时总得分的期望（四舍五入到 5 位小数）是 10.20914。
 * 求 k = 1..10^5 时的总得分期望（四舍五入到 5 位小数）。
 *
 * 答案编码：期望值是小数，按项目惯例编码为整数 round(期望 × 10^5)（题面要求 5 位小数）。
 *
 * 思路
 * ────
 * 令 u = ka + 1、v = kb + 1。a、b 均匀独立地落在 [0,1] 上，故 (u,v) 均匀落在 [1,k+1]² 上，
 * 面积元满足 du dv = k² da db。第 k 轮得分的充要条件是
 *
 *   (k − 1/2)² ≤ u² + v² < (k + 1/2)² .
 *
 * 圆够不到正方形的上/右边界：对 R = k ± 1/2，(k+1)² − R² = k + 3/4 > 0，所以圆盘与直线
 * u = k+1、v = k+1 都不相交，[1,k+1]² 的上界是自动满足的。记
 *
 *   Q(R) = area{u ≥ 1, v ≥ 1, u² + v² ≤ R²}
 *        = ∫_1^{√(R²−1)} (√(R²−u²) − 1) du        （R ≥ √2；R < √2 时 (1,1) 在圆外，Q = 0）
 *        = πR²/4 + 1 − √(R²−1) − R²·asin(1/R) ,
 *
 * 于是单轮得分概率 P(k) = [Q(k+1/2) − Q(k−1/2)]/k²，期望总分
 *
 *   E(N) = Σ_{k=1}^{N} k·P(k) = Σ_{k=1}^{N} [Q(k+1/2) − Q(k−1/2)]/k .
 *
 * 数值细节（避免两个 ~k² 量级的 Q 相减）：k ≥ 2 时把差值改写为
 *
 *   [Q(Ro) − Q(Ri)]/k = π/2 − 2/(√(Ro²−1) + √(Ri²−1)) − [g(Ro) − g(Ri)]/k,
 *   g(R) = R²·asin(1/R),  Ro = k + 1/2,  Ri = k − 1/2,
 *
 * 其中 π/2 来自 (π/4)(Ro² − Ri²)/k = π/2；第二项用了 (Ro²−1) − (Ri²−1) = 2k 的平方差
 * 恒等式，写成 2k/(√+√)/k = 2/(√+√)；第三项里 g 的量级是 k，差值在浮点里只损失约 1 位
 * 有效数字（抵消掉的正好是外层 1/k）。k = 1 时 Q(0.5) = 0，直接取 Q(1.5)。
 *
 * 独立复核
 * ────────
 * B：分部求和。对 S = Σ_{k=1}^{N}[Q(R_k) − Q(R_{k−1})]/k（R_j = j + 1/2）按 k 分部：
 *
 *   E = Σ_{k=1}^{N−1} Q(k+1/2)·(1/k − 1/(k+1)) + Q(N+1/2)/N
 *     = Σ_{k=1}^{N−1} Q(k+1/2)/(k(k+1)) + Q(N+1/2)/N ,
 *
 * 求和结构完全不同（Q 直接用闭式，不做逐项改写）。
 * C：几何复核。Q 的闭式另与极坐标积分
 *    Q(R) = 2∫_{asin(1/R)}^{π/4} (1/2)(R² − csc²θ)dθ = ∫_{asin(1/R)}^{π/4}(R² − csc²θ)dθ
 * 的高斯–勒让德数值积分逐点对照（换推导 + 换数值方法；注意 40 节点在 R ~ 10² 只有 1e-8 精度，
 * 因为被积函数在左端点附近紧邻 csc²θ 于 θ=0 的极点，取 200 节点）。
 * D：蒙特卡洛直接按题面定义模拟若干 k，用 95% 置信区间粗检。
 * E：高精度裁决路径。用 BigDecimal 60 位有效数字、按定义逐项累加（π 用 Machin 公式、
 * asin 用级数），把 5 位小数的四舍五入与真值的距离算清楚。暴力积分路径见 brute-force.kt
 * （复合 Simpson 数值积分每一轮的环形面积，不用解析原函数）。
 *
 * 复杂度
 * ──────
 * 主路径 O(N)，每项 2 次 sqrt、1 次 asin、常数次四则运算；N = 10^5 时约 1 ms 量级。
 * 高精度路径每项是上百次 BigDecimal 乘除（级数项数随 1/R 自适应），慢约三个数量级，
 * 只在最后裁决取整时跑一遍。
 *
 * 答案：E(10^5) = 157055.8099867274424552358238871…，round(×10^5) = 15705580999
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0285/solution.kt -d /tmp/kc-0285
 * java -cp /tmp/kc-0285:<kotlin-stdlib> SolutionKt
 */

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.SplittableRandom
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val NMAX = 100_000
private const val SCALE = 100_000L                       // 编码 ×10^5（题面 5 位小数）
private const val SAMPLE_K = 10                          // 题面样例：k = 1..10
private val SAMPLE_VALUE = BigDecimal("10.20914")        // 题面样例的 5 位小数结果
private val EXPECTED = BigDecimal("157055.809986727442455235823887126538776001084828415246058271")
private val EXPECTED_ENCODED = 15_705_580_999L           // round(E × 10^5)

// ─────────────────────────── 闭式解（主路径） ───────────────────────────

private val SQRT2 = sqrt(2.0)

/** Q(R) = area{u ≥ 1, v ≥ 1, u²+v² ≤ R²} 的闭式；R < √2 时为 0。 */
private fun qClosed(r: Double): Double {
    if (r < SQRT2) return 0.0
    return Math.PI * r * r / 4.0 + 1.0 - sqrt(r * r - 1.0) - r * r * asin(1.0 / r)
}

/** 第 k 轮的期望得分 k·P(k) = [Q(k+1/2) − Q(k−1/2)]/k（朴素相减，仅用于小规模对照）。 */
private fun contributionNaive(k: Int): Double =
    (qClosed(k + 0.5) - qClosed(k - 0.5)) / k

/** g(R) = R²·asin(1/R)（主路径与 B 路径的辅助函数）。 */
private fun gTerm(r: Double): Double = r * r * asin(1.0 / r)

/** 主路径：改写后的稳定形式，Kahan 累加。 */
private fun expectedStable(kMax: Int): Double {
    val first = qClosed(1.5)                             // k = 1：Q(0.5) = 0
    var sum = first
    var c = 0.0
    for (k in 2..kMax) {
        val ro = k + 0.5
        val ri = k - 0.5
        // 注意：Kotlin 不把行首的一元 `-` 续接到上一行，运算符要写在行尾
        val term = Math.PI / 2.0 -
            2.0 / (sqrt(ro * ro - 1.0) + sqrt(ri * ri - 1.0)) -
            (gTerm(ro) - gTerm(ri)) / k
        val y = term - c
        val t = sum + y
        c = (t - sum) - y
        sum = t
    }
    return sum
}

/** B 路径：分部求和 E = Σ_{k<N} Q(k+1/2)/(k(k+1)) + Q(N+1/2)/N，Kahan 累加。 */
private fun expectedByParts(kMax: Int): Double {
    var sum = qClosed(kMax + 0.5) / kMax
    var c = 0.0
    for (k in 1 until kMax) {
        val term = qClosed(k + 0.5) / (k.toDouble() * (k + 1))
        val y = term - c
        val t = sum + y
        c = (t - sum) - y
        sum = t
    }
    return sum
}

/** 朴素路径：直接按定义相减（无 Kahan、无改写），用来说明改写与补偿的收益。 */
private fun expectedNaive(kMax: Int): Double {
    var sum = 0.0
    for (k in 1..kMax) sum += contributionNaive(k)
    return sum
}

// ─────────────────────── 几何复核：极坐标数值积分 ───────────────────────

/** n 点高斯–勒让德节点与权重（Newton 迭代，节点 ~ 100 个以内足够）。 */
private fun gaussLegendre(n: Int): Pair<DoubleArray, DoubleArray> {
    val x = DoubleArray(n)
    val w = DoubleArray(n)
    val m = (n + 1) / 2
    for (i in 1..m) {
        var z = Math.cos(Math.PI * (i - 0.25) / (n + 0.5))
        var pp = 0.0
        for (iter in 0 until 100) {
            var p0 = 1.0
            var p1 = z
            for (j in 2..n) {
                val p2 = ((2 * j - 1) * z * p1 - (j - 1) * p0) / j
                p0 = p1
                p1 = p2
            }
            pp = n * (z * p1 - p0) / (z * z - 1.0)
            val dz = p1 / pp
            z -= dz
            if (abs(dz) < 1e-15) break
        }
        x[i - 1] = -z
        x[n - i] = z
        w[i - 1] = 2.0 / ((1 - z * z) * pp * pp)
        w[n - i] = w[i - 1]
    }
    return x to w
}

/**
 * Q 的另一条推导：极坐标下区域 {u ≥ 1, v ≥ 1, u²+v² ≤ R²} 由两条射线 θ = asin(1/R) 与
 * θ = π/2 − asin(1/R) 夹住，θ 固定时径向内界 ρ(θ) = max(1/cosθ, 1/sinθ)。利用 π/4 两侧的
 * 对称性 (∫ 1/2·(R² − ρ²) 的两半相等) 得
 *
 *   Q(R) = 2∫_{asin(1/R)}^{π/4} (1/2)(R² − csc²θ) dθ = ∫_{asin(1/R)}^{π/4} (R² − csc²θ) dθ .
 *
 * 用高斯–勒让德积分算它（被积函数在左端点取 0，解析、无奇点）。
 */
private fun qByPolarQuadrature(r: Double, nodes: Int = 40): Double {
    if (r < SQRT2) return 0.0
    val theta0 = asin(1.0 / r)
    val a = theta0
    val b = Math.PI / 4.0
    val (xs, ws) = gaussLegendre(nodes)
    val half = (b - a) / 2.0
    val mid = (a + b) / 2.0
    var s = 0.0
    for (i in nodes - 1 downTo 0) {
        val t = mid + half * xs[i]
        val st = sin(t)
        s += ws[i] * (r * r - 1.0 / (st * st))
    }
    return s * half
}

// ─────────────────────────── 蒙特卡洛直接模拟 ───────────────────────────

private class McResult(val hits: Long, val samples: Long, val p: Double) {
    val stdErr: Double get() = sqrt(p * (1 - p) / samples)
}

/** 按题面定义模拟：a, b ~ U[0,1]，四舍五入 √((ka+1)²+(kb+1)²) 看是否等于 k。 */
private fun monteCarlo(k: Int, samples: Long, seed: Long): McResult {
    val rng = SplittableRandom(seed)
    var hits = 0L
    for (i in 0 until samples) {
        val a = rng.nextDouble()
        val b = rng.nextDouble()
        val u = k * a + 1.0
        val v = k * b + 1.0
        val root = sqrt(u * u + v * v)
        if (Math.round(root) == k.toLong()) hits++
    }
    return McResult(hits, samples, hits.toDouble() / samples)
}

// ─────────────────── 高精度裁决路径（BigDecimal，按定义累加） ───────────────────

private val MC60 = MathContext(60, RoundingMode.HALF_EVEN)   // 60 位有效数字（远超 5 位小数所需）

/** π 的 Machin 公式（60 位）：π/4 = 4·atan(1/5) − atan(1/239)。 */
private fun bigPi(): BigDecimal {
    fun atanInv(x: Long): BigDecimal {
        val xd = BigDecimal.valueOf(x)
        val x2 = xd.pow(2)
        var power = BigDecimal.ONE.divide(xd, MC60)
        var total = BigDecimal.ZERO
        var j = 0L
        val eps = BigDecimal.ONE.scaleByPowerOfTen(-65)
        while (true) {
            val term = power.divide(BigDecimal.valueOf(2 * j + 1), MC60)
            total = if (j % 2 == 0L) total.add(term, MC60) else total.subtract(term, MC60)
            if (term.abs() < eps) break
            power = power.divide(x2, MC60)
            j++
        }
        return total
    }
    return atanInv(5).multiply(BigDecimal.valueOf(16), MC60).subtract(atanInv(239).multiply(BigDecimal.valueOf(4), MC60), MC60)
}

private val BIG_PI = bigPi()

/** asin(x) 级数（|x| ≤ 2/3 时收敛快）：Σ c_j x^{2j+1}，c_j = ((2j−1)!!/(2j)!!)/(2j+1)。 */
private fun bigAsin(x: BigDecimal): BigDecimal {
    val x2 = x.multiply(x, MC60)
    var power = x
    var c = BigDecimal.ONE
    var total = BigDecimal.ZERO
    var j = 0L
    val eps = BigDecimal.ONE.scaleByPowerOfTen(-65)
    while (true) {
        val term = c.multiply(power, MC60)
        total = total.add(term, MC60)
        if (term.abs() < eps) break
        j++
        val num = BigDecimal.valueOf(2 * j - 1)
        c = c.multiply(num, MC60).multiply(num, MC60)
            .divide(BigDecimal.valueOf(2 * j), MC60)
            .divide(BigDecimal.valueOf(2 * j + 1), MC60)
        power = power.multiply(x2, MC60)
    }
    return total
}

/** Q(R) 的 60 位高精度版本（R < √2 时为 0）。 */
private fun bigQ(r: BigDecimal): BigDecimal {
    if (r.multiply(r, MC60) < BigDecimal.valueOf(2)) return BigDecimal.ZERO
    val r2 = r.multiply(r, MC60)
    val sqrtPart = r2.subtract(BigDecimal.ONE, MC60).sqrt(MC60)
    val asinPart = r2.multiply(bigAsin(BigDecimal.ONE.divide(r, MC60)), MC60)
    return BIG_PI.multiply(r2, MC60).divide(BigDecimal.valueOf(4), MC60)
        .add(BigDecimal.ONE, MC60)
        .subtract(sqrtPart, MC60)
        .subtract(asinPart, MC60)
}

/** 高精度按定义累加：E(N) = Σ_{k=1}^{N} [Q(k+1/2) − Q(k−1/2)]/k。 */
private fun bigExpected(kMax: Int): BigDecimal {
    var qPrev = BigDecimal.ZERO                            // Q(1/2) = 0
    var total = BigDecimal.ZERO
    for (k in 1..kMax) {
        val qNext = bigQ(BigDecimal.valueOf(2L * k + 1).divide(BigDecimal.valueOf(2), MC60))
        total = total.add(qNext.subtract(qPrev, MC60).divide(BigDecimal.valueOf(k.toLong()), MC60), MC60)
        qPrev = qNext
    }
    return total
}

// ─────────────────────────────── 计时 ───────────────────────────────

private fun bestOf3(tag: String, expected: Double, tol: Double, f: () -> Double): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(abs(out - expected) < tol) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 题面样例：k = 1..10 的期望总分 = 10.20914 ----------
    val e10a = expectedStable(SAMPLE_K)
    val e10b = expectedByParts(SAMPLE_K)
    val e10c = expectedNaive(SAMPLE_K)
    check(abs(e10a - e10b) < 1e-12 && abs(e10a - e10c) < 1e-9) { "k=1..10 三路径不一致" }
    val e10Rounded = BigDecimal(e10a.toString()).setScale(5, RoundingMode.HALF_UP)
    check(e10Rounded == SAMPLE_VALUE) { "k=1..10 期望 $e10a 的 5 位小数应为 10.20914，得到 $e10Rounded" }
    println("题面样例：E(1..10) = ${"%.12f".format(e10a)}，5 位小数 = $e10Rounded ✓" +
        "（分部求和 ${"%.12f".format(e10b)}，朴素相减 ${"%.12f".format(e10c)}）")

    // ---------- 2. 题面例子的算术复核：k=6, a=0.2, b=0.85 ----------
    val u6 = 6 * 0.2 + 1.0
    val v6 = 6 * 0.85 + 1.0
    val s6 = u6 * u6 + v6 * v6
    check(abs(s6 - 42.05) < 1e-12) { "(6a+1)²+(6b+1)² 应为 42.05，得到 $s6" }
    val root6 = sqrt(s6)
    check(Math.round(root6) == 6L) { "√42.05 ≈ $root6 应四舍五入为 6" }
    println("题面例子：k=6, a=0.2, b=0.85 → (ka+1)²+(kb+1)² = ${"%.2f".format(s6)}，" +
        "√ = ${"%.5f".format(root6)} → 四舍五入 = ${Math.round(root6)} = k，得 6 分 ✓")

    // ---------- 3. 几何复核：闭式 Q vs 极坐标高斯–勒让德积分 ----------
    var maxDiff = 0.0
    // 200 节点：极坐标积分被积函数在最左端紧邻 csc²θ 的极点 θ=0，40 节点对 R ~ 10^2 只有 ~1e-8 精度
    for (r in doubleArrayOf(1.5, 1.6, 2.5, 3.5, 10.5, 100.5, 1000.5)) {
        val a = qClosed(r)
        val b = qByPolarQuadrature(r, 200)
        maxDiff = maxOf(maxDiff, abs(a - b) / maxOf(1.0, abs(a)))
    }
    check(maxDiff < 1e-12) { "Q 闭式与极坐标数值积分最大相对差 $maxDiff" }
    println("几何复核：Q 闭式 vs 极坐标高斯–勒让德积分（200 节点）在 R = 1.5…1000.5 上" +
        "最大相对差 ${"%.2e".format(maxDiff)}")

    // ---------- 4. 三条精确路径在全尺寸上互证 ----------
    val n = NMAX
    val mainValue = expectedStable(n)
    val partsValue = expectedByParts(n)
    val naiveValue = expectedNaive(n)
    check(abs(mainValue - partsValue) < 1e-6) { "主路径 $mainValue 与分部求和 $partsValue 相差过大" }
    println("全尺寸 N=10^5：")
    println("  主路径（改写 + Kahan）  = ${"%.12f".format(mainValue)}")
    println("  分部求和（Kahan）       = ${"%.12f".format(partsValue)}")
    println("  朴素相减（无补偿）      = ${"%.12f".format(naiveValue)}")
    println("  主路径 − 分部求和 = ${"%.2e".format(mainValue - partsValue)}；" +
        "朴素相减偏差 = ${"%.2e".format(naiveValue - mainValue)}")

    // ---------- 5. 高精度裁决：BigDecimal 40 位按定义累加 ----------
    val hpT0 = System.nanoTime()
    val bigValue = bigExpected(n)
    val hpMs = (System.nanoTime() - hpT0) / 1e6
    val hpRounded = bigValue.setScale(5, RoundingMode.HALF_UP)
    println("高精度（BigDecimal 60 位，${"%.0f".format(hpMs)} ms）：E = $bigValue")
    println("  5 位小数 = $hpRounded；与主路径相差 ${"%.2e".format(mainValue - bigValue.toDouble())}")
    check(hpRounded.toPlainString() == EXPECTED.setScale(5, RoundingMode.HALF_UP).toPlainString()) {
        "高精度取整 $hpRounded 与预期不符"
    }
    // 到取整分界点的距离（分界点 = .809985 与 .810005 的中点两侧）
    val boundary = BigDecimal("157055.809985")
    println("  真值离 5 位小数分界点 $boundary 的距离 = ${bigValue.subtract(boundary)}" +
        "（远大于所有浮点路径的偏差）")

    // ---------- 6. 蒙特卡洛直接模拟（粗检若干 k） ----------
    val mcKs = intArrayOf(1, 2, 6, 100)
    val mcSamples = 2_000_000L
    for (k in mcKs) {
        val mc = monteCarlo(k, mcSamples, 285_000L + k)
        val exactP = (qClosed(k + 0.5) - qClosed(k - 0.5)) / (k.toDouble() * k)
        val sigma = mc.stdErr
        check(abs(mc.p - exactP) < 4 * sigma + 1e-9) {
            "k=$k 蒙特卡洛 ${mc.p} 与精确值 $exactP 相差超过 4σ（σ=$sigma）"
        }
        println("蒙特卡洛 k=$k：$mcSamples 样本命中 ${mc.hits} 次，p̂ = ${"%.6f".format(mc.p)}" +
            "（精确 ${"%.6f".format(exactP)}，偏差 ${"%.2f".format(abs(mc.p - exactP) / sigma)}σ）")
    }

    // ---------- 7. 编码与计时 ----------
    val encoded = bigValue.setScale(5, RoundingMode.HALF_UP).movePointRight(5).toLong()
    check(encoded == EXPECTED_ENCODED) { "编码 $encoded ≠ $EXPECTED_ENCODED" }
    val checkValue = Math.round(mainValue * SCALE)
    check(checkValue == EXPECTED_ENCODED) { "主路径编码 $checkValue ≠ $EXPECTED_ENCODED" }

    expectedStable(1000)
    val msMain = bestOf3("主路径（闭式 + Kahan，N=10^5）", mainValue, 1e-9) { expectedStable(n) }
    val msParts = bestOf3("路径 B（分部求和，N=10^5）", partsValue, 1e-6) { expectedByParts(n) }

    println()
    println("期望总分 = $bigValue")
    println("5 位小数 = $hpRounded")
    println("答案 = $encoded（编码：round(期望 × 10^5)）")
    println("汇总：主路径 ${"%.3f".format(msMain)} ms；分部求和 ${"%.3f".format(msParts)} ms；" +
        "高精度 ${"%.0f".format(hpMs)} ms")
    println("check() 全部通过")
}
