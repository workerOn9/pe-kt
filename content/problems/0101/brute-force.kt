/**
 * Project Euler 101 — 暴力解（教学对比用）
 *
 * 与 solution.kt 同一个数学定义，但实现上完全「照本宣科」：
 * 不利用差分三角的滚动结构，而是对每个 k 单独套 Lagrange 插值公式
 *   OP(k, n) = Σ_i u_i · Π_{j≠i} (n − j) / (i − j)
 * 逐点算出 OP(k, k+1)。插值基函数的系数是有理数，用 BigInteger 分数
 * （分子/分母 + gcd 约分）精确累加，避免任何浮点误差——代价是每个 k 要做
 * O(k²) 次多精度乘除与约分，而 solution.kt 每个 k 只做 O(k) 次 64 位加减。
 * 序列本身也改用 BigInteger 幂生成，让两个文件的数值路径完全独立，
 * 互证时才不会同时错在同一处。
 *
 * 顺带把题面给出的立方数列样例（FIT = 1, 15, 58，和 74）当自检锚点跑一遍：
 * 样例出自题面本身，能同时兜住「公式用错」与「分数算错」两类失误。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 精确有理数：分子/分母，构造时约分并把符号归到分子。 */
private class Frac(num0: BigInteger, den0: BigInteger) {
    val num: BigInteger
    val den: BigInteger

    init {
        val sign = if (den0.signum() < 0) BigInteger.valueOf(-1) else BigInteger.ONE
        val n = num0 * sign
        val d = den0 * sign
        val g = n.gcd(d)
        num = n / g
        den = d / g
    }

    operator fun times(o: Frac) = Frac(num * o.num, den * o.den)
    operator fun plus(o: Frac) = Frac(num * o.den + o.num * den, den * o.den)
}

/** Lagrange 插值：由前 k 项求 OP(k, n)。 */
fun lagrangeOp(terms: LongArray, k: Int, n: Long): BigInteger {
    var total = Frac(BigInteger.ZERO, BigInteger.ONE)
    for (i in 0 until k) {
        var basis = Frac(BigInteger.ONE, BigInteger.ONE)
        for (j in 0 until k) {
            if (j == i) continue
            basis *= Frac(
                BigInteger.valueOf(n - (j + 1)),
                BigInteger.valueOf((i + 1).toLong() - (j + 1)),
            )
        }
        total += basis * Frac(BigInteger.valueOf(terms[i]), BigInteger.ONE)
    }
    check(total.den == BigInteger.ONE) { "OP 在整数点 n 上应为整数，实际 ${total.num}/${total.den}" }
    return total.num
}

/** 每个前缀的下一项预测；下标 i 对应 k = i+1。 */
fun bruteForceFits(terms: LongArray): LongArray =
    LongArray(terms.size) { k -> lagrangeOp(terms, k + 1, k + 2L).longValueExact() }

/** u_n = 1 − n + n² − … + n¹⁰，n = 1..count，逐次 BigInteger 幂精确求值（与 solution.kt 的 Long Horner 互证）。 */
fun exactTenthDegreeTerms(count: Int): LongArray =
    LongArray(count) { index ->
        val n = BigInteger.valueOf(index + 1L)
        (0..10).fold(BigInteger.ZERO) { sum, degree ->
            if (degree % 2 == 0) sum + n.pow(degree) else sum - n.pow(degree)
        }.longValueExact()
    }

fun solveBruteForce(): Long = bruteForceFits(exactTenthDegreeTerms(10)).sum()

fun main() {
    // 自检锚点：立方数列 u_n = n³ 的前 3 项，题面给出 FIT = 1, 15, 58（和 74）
    val cubes = LongArray(3) { val n = (it + 1).toLong(); n * n * n }
    val cubeFits = bruteForceFits(cubes)
    println("立方样例 FIT = ${cubeFits.toList()}，和 = ${cubeFits.sum()}（题面 1, 15, 58 → 74）")
    check(cubeFits.toList() == listOf(1L, 15L, 58L) && cubeFits.sum() == 74L) {
        "立方样例自检失败：${cubeFits.toList()}"
    }

    val tenth = exactTenthDegreeTerms(10)
    println("u_1..u_10 = ${tenth.joinToString(", ")}")
    val fits = bruteForceFits(tenth)
    fits.forEachIndexed { i, fit -> println("OP(${i + 1}, ${i + 2}) = $fit") }
    println("BOP 的 FIT 之和 = ${fits.sum()}")
}
