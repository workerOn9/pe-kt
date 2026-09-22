/**
 * PE 183 — 等分乘积最大值的小数性质。
 *
 * 原理：
 * 设 P(k) = (N/k)^k。取对数得 f(k) = k * (ln N - ln k)。
 * 求导：f'(k) = ln N - ln k - 1 = 0 => ln(N/k) = 1 => k = N / e。
 * 最佳整数分段数 k 必在 ⌊N/e⌋ 或 ⌈N/e⌉ 之中取到。
 * 比较两者取对数后的值以选取最佳 k。
 * 判断 M(N) = (N/k)^k 是否为有限十进制小数：
 *   (N/k)^k 为有限小数 ⟺ N/k 为有限小数 ⟺ 既约分母只含质因子 2 与 5。
 * 计算 d = k / gcd(N, k)，将 d 中所有的 2 与 5 除尽；
 * 若最终 d == 1 则为有限小数（贡献 -N），否则为无限小数（贡献 +N）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import kotlin.math.ln

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun solve183(limit: Int = 10000): Long {
    val e = Math.E
    var total = 0L

    for (n in 5..limit) {
        val nl = n.toLong()
        val k1 = (n / e).toInt()
        val k2 = k1 + 1

        val val1 = if (k1 > 0) k1 * (ln(n.toDouble()) - ln(k1.toDouble())) else Double.NEGATIVE_INFINITY
        val val2 = k2 * (ln(n.toDouble()) - ln(k2.toDouble()))
        val bestK = if (val1 > val2) k1.toLong() else k2.toLong()

        var denom = bestK / gcd(nl, bestK)
        while (denom % 2L == 0L) denom /= 2L
        while (denom % 5L == 0L) denom /= 5L

        if (denom == 1L) {
            total -= nl
        } else {
            total += nl
        }
    }
    return total
}

fun main() {
    // 题面样例：N = 5 到 100 给出 2438
    val testSample = solve183(100)
    check(testSample == 2438L) { "Expected 2438, got $testSample" }

    val answer = solve183(10000)
    println(answer)
}
