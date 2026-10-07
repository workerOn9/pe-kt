#!/usr/bin/env kotlin
/**
 * Project Euler 323 — Bitwise-OR Operations on Random Integers（随机整数的按位或）
 *
 * 题目：y₀, y₁, ... 是随机无符号 32 位整数序列。x₀ = 0, xᵢ = xᵢ₋₁ | yᵢ₋₁。
 *      N = 使 xᵢ = 2³²−1 的最小下标。求 E[N]，四舍五入到 10 位小数。
 *
 * 思路推导
 * --------
 * 1. 期望的级数表达：E[N] = Σ_{k≥0} P(N > k)。
 *
 * 2. P(N > k) = P(x_k ≠ 2³²−1) = 1 − P(x_k = 2³²−1)。
 *    x_k = y₀ | y₁ | ... | y_{k−1}。对每个比特位 j，P(位 j 在 k 次 OR 后被置位)
 *    = 1 − P(位 j 在全部 k 个 y 中都为 0) = 1 − 2^{−k}。
 *    32 个比特位独立，故 P(x_k = 2³²−1) = (1 − 2^{−k})³²。
 *    因此 E[N] = Σ_{k≥0} (1 − (1 − 2^{−k})³²)。
 *
 * 3. 级数收敛快：k > 50 后项 < 32·2^{−k} < 10^{−13}，求和到 k=100 足够。
 *
 * 验证
 * --------
 * 1. double 精度求和与 BigDecimal(50 位) 求和对比；
 * 2. Monte Carlo 模拟（10⁶ 次试验）验证数量级。
 *
 * 复杂度
 * --------
 * double 求和 O(K)（K=100，< 1 ms）；
 * BigDecimal 求和 O(K)（K=100，约 10 ms）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/** BigDecimal 精确求和（50 位有效数字）。 */
private fun expectedNBigDecimal(): BigDecimal {
    val mc = MathContext(50)
    var sum = BigDecimal.ZERO
    var twoPowK = BigDecimal.ONE
    for (k in 0..100) {
        val invTwoPowK = BigDecimal.ONE.divide(twoPowK, mc)
        val prob = BigDecimal.ONE.subtract(invTwoPowK, mc).pow(32, mc)
        sum = sum.add(BigDecimal.ONE.subtract(prob, mc), mc)
        twoPowK = twoPowK.multiply(BigDecimal.valueOf(2L), mc)
    }
    return sum
}

/** double 精度求和（主求解路径）。 */
private fun expectedNDouble(): Double {
    var sum = 0.0
    var twoPowK = 1.0
    for (k in 0..100) {
        val oneMinus = 1.0 - 1.0 / twoPowK
        sum += 1.0 - Math.pow(oneMinus, 32.0)
        twoPowK *= 2.0
    }
    return sum
}

/** Monte Carlo 模拟。 */
private fun monteCarlo(trials: Int, seed: Long): Double {
    val rng = java.util.Random(seed)
    var totalN = 0L
    for (t in 0 until trials) {
        var x = 0
        var n = 0
        while (x != -1) {
            x = x or rng.nextInt()
            n++
        }
        totalN += n
    }
    return totalN.toDouble() / trials
}

private inline fun <T> timeOf(runs: Int = 3, body: () -> T): Pair<T, Double> {
    body()
    var best = Double.MAX_VALUE
    var r: T? = null
    for (k in 0 until runs) {
        val st = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return r!! to best
}

fun main() {
    println("== 双方法互证（double vs BigDecimal） ==")
    val d = expectedNDouble()
    val b = expectedNBigDecimal()
    println("  double:     " + String.format("%.15f", d))
    println("  BigDecimal: " + b.toPlainString())
    println("  差值: " + String.format("%.2e", Math.abs(d - b.toDouble())))

    println("== Monte Carlo 验证（10⁶ 次试验） ==")
    val mc = monteCarlo(1_000_000, 42)
    println("  Monte Carlo: " + String.format("%.6f", mc))

    println("== 正式求解 ==")
    val (ans, ms) = timeOf { expectedNDouble() }
    val rounded = BigDecimal.valueOf(ans).setScale(10, RoundingMode.HALF_UP)
    println("E[N] = " + String.format("%.15f", ans))
    println("四舍五入到 10 位小数 = " + rounded.toPlainString())
    println("OPT_MS: " + String.format("%.3f", ms) + "  （double 精度求和，主求解路径）")
    val (b2, bms) = timeOf { expectedNBigDecimal() }
    println("BRUTE_MS: " + String.format("%.3f", bms) + "  （BigDecimal 50 位精确求和，旁证）")
}
