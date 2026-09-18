/**
 * Project Euler 140 — Modified Fibonacci Golden Nuggets（修改版斐波那契金块）
 *
 * 思路推导：
 * 1) 生成函数。G_k = G_{k−1} + G_{k−2}，G_1 = 1、G_2 = 4，于是
 *        A_G(x) = xG_1 + x²G_2 + x³G_3 + ⋯ = (x + 3x²) / (1 − x − x²)。
 * 2) 令 A_G(x) = n（正整数），整理得 (n+3)x² + (n+1)x − n = 0，判别式
 *        D(n) = (n+1)² + 4n(n+3) = 5n² + 14n + 1。
 *    x 有理 ⟺ D(n) 是完全平方数 m²；配方后 5D(n) = (5n+7)² − 44，即
 *        (5n+7)² − 5m² = 44。
 *    记 u = 5n + 7，问题化为：解 Pell 型方程 u² − 5m² = 44，且 u > 7、u ≡ 2 (mod 5)。
 *    （u = 7 给出 n = 0，对应 x = 0、A_G(0) = 0，不是正整数，故排除。）
 * 3) Z[√5] 中范数为 1 的基本单位 ε = 9 + 4√5（9² − 5·4² = 1）。把解 (u, m) 乘 ε：
 *        u' = 9u + 20m，  m' = 4u + 9m，
 *    得到同一「解链」上的下一个解。注意 u' = 9u + 20m ≡ 4u (mod 5)，
 *    而 4·2 ≡ 3、4·3 ≡ 2 (mod 5)，所以 u ≡ 2 与 u ≡ 3 沿链交替出现：
 *    金块落在每条链的每隔一项上。
 * 4) 沿链隔一步的两个解由 ε² = 161 + 72√5 联系；换算到 u 上，同一子列满足二阶线性递推
 *        u_{k+1} = 7u_k − u_{k−1}     （特征根 φ⁴ 与 φ^{−4}，二者之和恰为 7）
 *    记 n = (u − 7)/5，则
 *        n_{k+1} = 7n_k − n_{k−1} + 7。
 *    两条子列的种子是 (u_0, u_1) = (7, 17) 与 (7, 32)，对应 n 列 (0, 2) 与 (0, 5)。
 *    两条子列归并后的前 30 项（n = 2, 5, 21, 42, 152, 296, …）即所求。
 *
 * 复杂度：时间 O(1)（生成 30 余项 + 30 次 BigInteger 完全平方校验），空间 O(1)。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

private const val COUNT = 30
private const val LIMIT = 100_000_000_000_000L   // u ≤ 10¹⁴，远超第 30 个金块（n ≈ 3.2×10¹²）

/** 判别式 D(n) = 5n² + 14n + 1；n ≥ 1.4×10⁹ 时 5n² 溢出 Long，故用 BigInteger。 */
private fun discriminant(n: Long): BigInteger {
    val big = BigInteger.valueOf(n)
    return big * big * BigInteger.valueOf(5) + big * BigInteger.valueOf(14) + BigInteger.ONE
}

/** n 是金块 ⟺ D(n) 为完全平方数；是则返回 m = √D(n)，否则返回 null。 */
private fun goldenRoot(n: Long): BigInteger? {
    val d = discriminant(n)
    val m = d.sqrt()
    return if (m * m == d) m else null
}

/** 沿 u_{k+1} = 7u_k − u_{k−1} 生成一条子列的 n（u = 5n + 7），直到 u 超过 limit。 */
private fun subsequence(first: Long, second: Long, limit: Long): List<Long> {
    val nuggets = ArrayList<Long>()
    var prev = first
    var cur = second
    while (cur <= limit) {
        val n = (cur - 7) / 5
        if (n >= 1) nuggets.add(n)               // n = 0 对应 x = 0，被排除
        val next = 7 * cur - prev
        prev = cur
        cur = next
    }
    return nuggets
}

/** 升序的前 count 个金块：两条子列（种子 u = 7, 17 与 u = 7, 32）归并去重。 */
fun goldenNuggets(count: Int = COUNT, limit: Long = LIMIT): List<Long> =
    (subsequence(7, 17, limit) + subsequence(7, 32, limit)).sorted().take(count)

fun solve(): Long = goldenNuggets().sum()

fun verifySample() {
    // ── 题面表格：A_G(x) = 1…5 时判别式依次为 20, 49, 88, 137, 196，只有后两者中的 49、196 为完全平方
    val discs = (1L..5L).map { discriminant(it).toLong() }
    check(discs == listOf(20L, 49L, 88L, 137L, 196L)) { "判别式应为 20,49,88,137,196，实得 $discs" }
    check(goldenRoot(2) == BigInteger.valueOf(7) && goldenRoot(5) == BigInteger.valueOf(14)) { "2 与 5 应为金块" }
    for (n in listOf(1L, 3L, 4L)) check(goldenRoot(n) == null) { "n = $n 的 x 应是无理数" }

    // ── 题面表格中的 x 反代回 A_G(x) = (x + 3x²)/(1 − x − x²)，应得正整数 n
    val table = listOf(
        1L to (Math.sqrt(5.0) - 1) / 4,
        2L to 2.0 / 5,
        3L to (Math.sqrt(22.0) - 2) / 6,
        4L to (Math.sqrt(137.0) - 5) / 14,
        5L to 1.0 / 2
    )
    for ((n, x) in table) {
        val value = (x + 3 * x * x) / (1 - x - x * x)
        check(Math.abs(value - n) < 1e-9) { "A_G($x) 应为 $n，实算 $value" }
    }
    // 闭式根 x = (√D(n) − (n+1)) / (2(n+3)) 与题面表格逐项吻合
    for ((n, x) in table) {
        val closed = (Math.sqrt(discriminant(n).toDouble()) - (n + 1)) / (2 * (n + 3))
        check(Math.abs(closed - x) < 1e-12) { "n = $n 的闭式根 $closed 与题面 $x 不符" }
    }

    // ── 答案链：第 20 个金块必须等于题面给出的 211345365
    val nuggets = goldenNuggets()
    check(nuggets.size == COUNT) { "应得 $COUNT 个金块，实得 ${nuggets.size}" }
    check(nuggets[19] == 211_345_365L) { "第 20 个金块应为 211345365，实得 ${nuggets[19]}" }
    check(nuggets.zipWithNext().all { (a, b) -> a < b }) { "金块必须严格递增" }

    // ── 逐项回到定义：D(n) = m²，且 (5n+7)² − 5m² = 44
    for (n in nuggets) {
        val m = goldenRoot(n) ?: error("$n 不是金块")
        val u = BigInteger.valueOf(n) * BigInteger.valueOf(5) + BigInteger.valueOf(7)
        check(u * u - m * m * BigInteger.valueOf(5) == BigInteger.valueOf(44)) { "n = $n 不满足 Pell 方程" }
    }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
