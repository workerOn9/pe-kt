package dev.pekt.problems

import java.math.BigDecimal
import java.math.MathContext

/**
 * Problem 192: Best Approximations
 *
 * 思路：连分数展开的收敛子 + 半收敛子扫描
 *
 * 经典定理：分母 ≤ d 的最接近 x 的有理数，必是 x 的连分数收敛子或半收敛子
 * （intermediate fraction: (p_{k-2} + t p_{k-1}) / (q_{k-2} + t q_{k-1}), 1 ≤ t ≤ a_k）。
 *
 * 对每个非完全平方 n (2..100000)：
 *  - 展开 sqrt(n) 连分数（sqrt 型周期以 2*a0 结尾）；
 *  - 逐块生成 s ≤ 10^12 的收敛子/半收敛子；
 *  - 用 60 位 BigDecimal 比较 |p/q - sqrt(n)|，取最小误差的 s。
 *
 * 复杂度：O(n × 周期)，n = 1e5 时 ~1 s 量级（Python 参考实现实测）。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
fun solve0192(): Long {
    val D = 10L.toBigInteger().pow(12)
    val MC = MathContext(60)
    val total = java.util.stream.LongStream.rangeClosed(2L, 100000L).parallel()
        .filter { nn -> { val r = Math.sqrt(nn.toDouble()).toLong(); r * r != nn }() }
        .mapToLong { nn: Long -> bestDenominator(nn, D, MC) }
        .sum()
    return total
}

/** 返回 sqrt(n) 在分母界 D 下最佳逼近的分母；n 非完全平方。 */
fun bestDenominator(n: Long, d: java.math.BigInteger, mc: MathContext): Long {
    val a0 = Math.sqrt(n.toDouble()).toLong().let { a ->
        // 修正浮点 isqrt 边界
        var v = a
        while (v * v > n) v--
        while ((v + 1) * (v + 1) <= n) v++
        v
    }
    if (a0 * a0 == n) error("n must not be a perfect square")
    val x = BigDecimal(n).sqrt(mc)
    var m = 0L; var dd = 1L; var a: Long = a0
    // 收敛子递推：p_{-1}=1, p_0=a0; q_{-1}=0, q_0=1
    var pm1 = 1L; var pm = a0
    var qm1 = 0L; var qm = 1L
    var bestS = 1L; var bestR = a0
    var bestErr = BigDecimal(a0).subtract(x).abs()
    while (true) {
        m = dd * a - m
        dd = (n - m * m) / dd
        a = (a0 + m) / dd
        if (java.math.BigInteger.valueOf(qm) > d) break
        // 半收敛子：t = 1..a，q(t) = t*qm + qm1
        val qmB = java.math.BigInteger.valueOf(qm)
        val qm1B = java.math.BigInteger.valueOf(qm1)
        val dIfTrue = qm1B.add(qmB) // q(t=1)
        var t = 1
        while (t <= a) {
            val q = qm1B.add(qmB.multiply(java.math.BigInteger.valueOf(t.toLong())))
            if (q > d) break
            val p = pm1.toLong() * t + pm
            val err = BigDecimal(p).divide(BigDecimal(q), mc).subtract(x).abs()
            if (err < bestErr) { bestErr = err; bestR = p; bestS = q.toLong() }
            t++
        }
        // 推进收敛子
        val npm = a.toLong() * pm + pm1
        val nqm = a.toLong() * qm + qm1
        pm1 = pm; pm = npm
        qm1 = qm; qm = nqm
        if (pm > Long.MAX_VALUE / 10000) {
            // p、q 用 Long 会溢出前必然已超过 d
            if (nqm.toBigInteger() > d) break
        }
    }
    return bestS
}

private fun Long.toBigInteger() = java.math.BigInteger.valueOf(this)

fun main() { println(solve0192()) }
