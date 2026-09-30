#!/usr/bin/env kotlin
/**
 * Project Euler 285 — Pythagorean Odds（勾股概率）：暴力 / 独立对照
 * 独立实现，与 solution.kt 不共享核心代码（不算 asin 原函数，面积一律用数值积分）。
 *
 * 路径 1（复合 Simpson 数值积分，meta 的 bruteForceBaselineMs 口径）
 * ──────────────────────────────────────────────────────────
 * 令 u = ka+1、v = kb+1。a、b 在 [0,1] 上均匀独立 ⟺ (u,v) 在 [1,k+1]² 上均匀，第 k 轮得分 ⟺
 * (k−1/2)² ≤ u²+v² < (k+1/2)²。记 Ro = k+1/2、Ri = k−1/2，
 *
 *   Q(R) = area{u ≥ 1, v ≥ 1, u²+v² ≤ R²},   I(R) = ∫_0^1 √(R²−u²) du .
 *
 * R ≥ √2 时单位正方形完全落在圆内（最远角 (1,1) 距原点 √2），于是
 *
 *   Q(R) = πR²/4 − 2·I(R) + 1   （圆盘四分之一面积，减去 u<1、v<1 两块弓形，补回单位正方形），
 *
 * 而 I(Ro) − I(Ri) 用平方差恒等式化成没有相消的形式：
 *
 *   I(Ro) − I(Ri) = ∫_0^1 [√(Ro²−u²) − √(Ri²−u²)] du
 *                 = ∫_0^1 2k / (√(Ro²−u²) + √(Ri²−u²)) du   （被积函数 O(1)，无灾难性抵消）。
 *
 * 于是每轮期望得分（k ≥ 2）
 *
 *   k·P(k) = [Q(Ro) − Q(Ri)]/k = π/2 − (2/k)·∫_0^1 2k/(√(Ro²−u²)+√(Ri²−u²)) du,
 *
 * 其中 π/2 来自 (π/4)(Ro²−Ri²)/k = π/2。积分用**固定步长的复合 Simpson**（每轮 256 段，
 * 与 k 无关）算——这是「按定义量面积」的做法，全程只有 sqrt 和四则运算，没有任何解析原函数；
 * 被积函数在 [0,1] 上解析（支点在 u = ±Ri、±Ro，离区间至少 0.5），Simpson 收敛极快。
 * k = 1 时 Q(Ri) = Q(1/2) = 0（圆太小，(1,1) 落在圆外），该项直接用 Q(3/2) = π·(9/4)/4 −
 * 2·∫_0^1 √(9/4−u²)du + 1 算。
 *
 * 路径 2（定义级蒙特卡洛）
 * ──────────────────────
 * 直接按题面模拟：每轮取 a, b ~ U[0,1]，四舍五入 √((ka+1)²+(kb+1)²)，等于 k 就得 k 分。
 * 用 k = 1..10 的「十轮总得分」样本均值对照题面样例 10.20914，并对若干单个 k 给出命中率与
 * 标准误（4σ 判据）。
 *
 * 路径 3（粗网格二重积分）
 * ─────────────────────
 * 对 k = 1, 2, 6 再用最朴素的均匀网格二重积分（把 [1,k+1]² 划成格子数环带内的格点比例）
 * 核一遍数量级——三种做法里最愚笨的一种，只用来确认定义理解没跑偏。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0285/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

import java.util.SplittableRandom
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sqrt

private const val NMAX = 100_000
private const val STEPS = 256                                  // 每轮积分段数（与 k 无关）
/** solution.kt 闭式路径的实跑值（已由 40 位高精度路径复核），此处仅作对照。 */
private const val EXACT_TOTAL = 157055.80998672744
private const val SAMPLE_TOTAL = 10.209139355576836            // E(k = 1..10)
private const val SQRT2 = 1.4142135623730951

// ─────────────────────── 路径 1：复合 Simpson 数值积分 ───────────────────────

/** 复合 Simpson（n 为偶数），区间 [a,b] 上等距 n 段。 */
private fun simpson(f: (Double) -> Double, a: Double, b: Double, n: Int): Double {
    val h = (b - a) / n
    var s = f(a) + f(b)
    for (i in 1 until n) s += if (i % 2 == 1) 4.0 * f(a + i * h) else 2.0 * f(a + i * h)
    return s * h / 3.0
}

/** I(Ro) − I(Ri) = ∫_0^1 2k/(√(Ro²−u²)+√(Ri²−u²)) du（平方差恒等式，被积函数无抵消）。 */
private fun chordDifference(k: Int, n: Int): Double {
    val ro = k + 0.5
    val ri = k - 0.5
    return simpson({ u -> 2.0 * k / (sqrt(ro * ro - u * u) + sqrt(ri * ri - u * u)) }, 0.0, 1.0, n)
}

/** Q(R) = πR²/4 − 2I(R) + 1（R ≥ √2），I 用 Simpson。 */
private fun qNumeric(r: Double, n: Int): Double =
    Math.PI * r * r / 4.0 - 2.0 * simpson({ u -> sqrt(r * r - u * u) }, 0.0, 1.0, n) + 1.0

/** 第 k 轮的期望得分贡献 k·P(k)（k ≥ 2 走 Simpson 的差值形式，k = 1 单算）。 */
private fun contribution(k: Int, n: Int): Double {
    if (k == 1) return qNumeric(1.5, n)                        // Q(0.5) = 0
    return Math.PI / 2.0 - 2.0 / k * chordDifference(k, n)
}

/** 路径 1：E = Σ_k k·P(k)，逐项 Simpson，Kahan 累加。 */
private fun expectedByQuadrature(kMax: Int, n: Int): Double {
    var total = 0.0
    var c = 0.0
    for (k in 1..kMax) {
        val term = contribution(k, n)
        val y = term - c
        val t = total + y
        c = (t - total) - y
        total = t
    }
    return total
}

// ─────────────────────── 路径 2：定义级蒙特卡洛 ───────────────────────

/** 题面的「四舍五入到最近整数」判定：√S 四舍五入后等于 k。 */
private fun roundsToK(root: Double, k: Int): Boolean = Math.round(root) == k.toLong()

private fun mcSingleRound(k: Int, samples: Long, seed: Long): Pair<Double, Double> {
    val rng = SplittableRandom(seed)
    var hits = 0L
    for (i in 0 until samples) {
        val u = k * rng.nextDouble() + 1.0
        val v = k * rng.nextDouble() + 1.0
        if (roundsToK(sqrt(u * u + v * v), k)) hits++
    }
    val p = hits.toDouble() / samples
    return p to sqrt(p * (1 - p) / samples)
}

/** k = 1..10 的「十轮总得分」样本均值（题面样例 10.20914 的定义级复核）。 */
private fun mcTenRounds(samples: Long, seed: Long): Pair<Double, Double> {
    val rng = SplittableRandom(seed)
    var sum = 0.0
    var sumSq = 0.0
    for (i in 0 until samples) {
        var score = 0L
        for (k in 1..10) {
            val u = k * rng.nextDouble() + 1.0
            val v = k * rng.nextDouble() + 1.0
            if (roundsToK(sqrt(u * u + v * v), k)) score += k
        }
        sum += score
        sumSq += score.toDouble() * score
    }
    val mean = sum / samples
    val varN = (sumSq / samples - mean * mean) * samples / (samples - 1)
    return mean to sqrt(varN / samples)
}

// ─────────────────────── 路径 3：粗网格二重积分 ───────────────────────

/** 均匀 n×n 网格统计落在环带内的格点比例，乘正方形面积得面积估计。 */
private fun roundAreaByGrid(k: Int, n: Int): Double {
    val lo = 1.0
    val hi = k + 1.0
    val step = (hi - lo) / n
    val inner2 = (k - 0.5) * (k - 0.5)
    val outer2 = (k + 0.5) * (k + 0.5)
    var count = 0L
    for (i in 0 until n) {
        val u = lo + (i + 0.5) * step
        for (j in 0 until n) {
            val v = lo + (j + 0.5) * step
            val r2 = u * u + v * v
            if (r2 >= inner2 && r2 < outer2) count++
        }
    }
    return count.toDouble() / (n.toDouble() * n) * (hi - lo) * (hi - lo)
}

/** 闭式面积（仅作路径 1 的对照物；与 solution.kt 同源但独立抄写）。 */
private fun closedArea(k: Int): Double {
    fun q(r: Double): Double = if (r < SQRT2) 0.0
    else Math.PI * r * r / 4.0 + 1.0 - sqrt(r * r - 1.0) - r * r * asin(1.0 / r)
    return q(k + 0.5) - q(k - 0.5)
}

// ─────────────────────────────── 计时 ───────────────────────────────

private fun bestOf3(tag: String, expected: Double, tol: Double, f: () -> Double): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(abs(out - expected) < tol) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms，值 = ${"%.9f".format(out)}")
        if (ms < best) best = ms
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 0. 网格法（最愚笨的对照，只确认数量级） ----------
    val gridT0 = System.nanoTime()
    for (k in intArrayOf(1, 2, 6)) {
        val g = roundAreaByGrid(k, 4000) / k
        val q = contribution(k, STEPS)
        check(abs(g - q) < 0.01) { "k=$k 网格法 $g 与 Simpson $q 相差过大" }
        println("粗网格二重积分 k=$k：4000×4000 格点给出单轮期望得分 ≈ ${"%.6f".format(g)}" +
            "（Simpson ${"%.6f".format(q)}）")
    }

    val gridMs = (System.nanoTime() - gridT0) / 1e6
    println("  （粗网格 3 个 k 共 ${"%.0f".format(gridMs)} ms，仅作对照，不计入 baseline）")

    // ---------- 1. 题面例子（k=6, a=0.2, b=0.85） ----------
    val u6 = 6 * 0.2 + 1.0
    val v6 = 6 * 0.85 + 1.0
    val exampleRoot = sqrt(u6 * u6 + v6 * v6)
    check(abs(u6 * u6 + v6 * v6 - 42.05) < 1e-12)
    check(roundsToK(exampleRoot, 6)) { "k=6, a=0.2, b=0.85：√42.05 ≈ $exampleRoot 应判得 6 分" }
    println("题面例子：k=6, a=0.2, b=0.85 → (ka+1)²+(kb+1)² = 42.05，√ = " +
        "${"%.5f".format(exampleRoot)} → 四舍五入 ${Math.round(exampleRoot)} = k ✓")

    // ---------- 2. 路径 2：十轮总得分的定义级模拟 ----------
    val mcT0 = System.nanoTime()
    val (mcMean, mcErr) = mcTenRounds(4_000_000L, 285_000L)
    val lo = mcMean - 1.96 * mcErr
    val hi = mcMean + 1.96 * mcErr
    println("定义级蒙特卡洛（400 万次十轮游戏）：总得分均值 ${"%.5f".format(mcMean)}，" +
        "95% CI [${"%.5f".format(lo)}, ${"%.5f".format(hi)}]（标准误 ${"%.5f".format(mcErr)}）")
    check(lo <= SAMPLE_TOTAL && SAMPLE_TOTAL <= hi) { "题面样例 10.20914 不落在模拟置信区间内" }

    for (k in intArrayOf(1, 6, 100)) {
        val (p, sigma) = mcSingleRound(k, 4_000_000L, 285_000L + k)
        val q = closedArea(k) / (k.toDouble() * k)
        check(abs(p - q) < 4 * sigma + 1e-12) { "k=$k 蒙特卡洛 $p 与闭式 $q 相差超过 4σ" }
        println("定义级蒙特卡洛 k=$k：p̂ = ${"%.6f".format(p)}（闭式 ${"%.6f".format(q)}，" +
            "偏差 ${"%.2f".format(abs(p - q) / sigma)}σ）")
    }

    val mcMs = (System.nanoTime() - mcT0) / 1e6
    println("  （定义级蒙特卡洛合计 ${"%.0f".format(mcMs)} ms，仅作统计旁证，不计入 baseline）")

    // ---------- 3. 路径 1：逐轮积分 vs 闭式 + 收敛性 ----------
    for (k in intArrayOf(1, 2, 5, 10, 1000, 100000)) {
        val quad = contribution(k, STEPS)
        val closed = closedArea(k) / k
        check(abs(closed - quad) < 1e-10 * maxOf(1.0, abs(closed))) { "k=$k：闭式 $closed vs 积分 $quad" }
        println("逐轮期望得分 k=$k：Simpson($STEPS 段) ${"%.12f".format(quad)}，" +
            "闭式 ${"%.12f".format(closed)}（差 ${"%.2e".format(quad - closed)}）")
    }
    // 步长加倍收敛检查（Simpson 误差 ~h⁴，256 → 512 段应几乎不变）
    for (k in intArrayOf(1, 6, 1000)) {
        val a = contribution(k, STEPS)
        val b = contribution(k, 2 * STEPS)
        val c = contribution(k, 4 * STEPS)
        println("步长收敛 k=$k：256 段 ${"%.13f".format(a)}，512 段 ${"%.13f".format(b)}，" +
            "1024 段 ${"%.13f".format(c)}")
        check(abs(a - b) < 1e-10 && abs(b - c) < 1e-10) { "k=$k Simpson 未收敛" }
    }
    val quadTotal = expectedByQuadrature(NMAX, STEPS)
    println("复合 Simpson（$STEPS 段/轮）全量：E = ${"%.12f".format(quadTotal)}")
    println("与闭式路径实跑值 ${"%.12f".format(EXACT_TOTAL)} 相差 ${"%.2e".format(quadTotal - EXACT_TOTAL)}" +
        "（5 位小数取整余量 1.7e-6，足以独立确认取整）")
    check(abs(quadTotal - EXACT_TOTAL) < 1e-7) { "数值积分 $quadTotal 与闭式值 $EXACT_TOTAL 相差超过 1e-7" }

    // ---------- 4. 计时（meta 的 bruteForceBaselineMs 口径） ----------
    contribution(10, STEPS)
    val msQuad = bestOf3("路径 1 复合 Simpson（全量 10^5 轮，每轮 $STEPS 段）", EXACT_TOTAL, 1e-7) {
        expectedByQuadrature(NMAX, STEPS)
    }

    // ---------- 5. 汇总 ----------
    val encoded = Math.round(quadTotal * 1e5)
    println()
    println("数值积分总期望 = ${"%.12f".format(quadTotal)}")
    println("答案 = $encoded（编码：round(期望 × 10^5)，与闭式路径一致）")
    check(encoded == 15_705_580_999L)
    println("汇总：复合 Simpson 全量 ${"%.0f".format(msQuad)} ms（baseline 口径）；" +
        "定义级蒙特卡洛 400 万次十轮游戏 + 3 组单轮各 400 万样本")
    println("check() 全部通过")
}
