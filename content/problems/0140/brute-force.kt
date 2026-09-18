/**
 * Project Euler 140 — 独立对照解（定义级扫描 + 解链枚举，不做任何递推化简）
 *
 * 与 solution.kt 的差别：
 *   · solution.kt 把解链化简成闭式递推 n_{k+1} = 7n_k − n_{k−1} + 7，一步一跳，只剩常数项运算。
 *   · 本解完全不用那条递推。先用最朴素的定义级扫描（n 从小到大逐个判 5n²+14n+1 是否为完全平方）
 *     在金块的实际数量级上验证答案的前 20 项，再回到 Z[√5] 里**枚举** Pell 解：
 *     小解靠直接搜索 u² − 5v² = 44 得到（u ≤ 1000 的窗口），随后对每个小解反复乘以基本单位
 *     ε = 9 + 4√5：(u, v) → (9u + 20v, 4u + 9v)，把解链放大，收集所有 u ≡ 2 (mod 5)、u > 7 的项。
 *     不同小解会重复生成同一解，交给有序集合去重，不做任何「约简」预处理。
 *
 * 为什么「扫小解 + 乘 ε」就够了：同一链上相邻解的 u 之比约 9 + 20/√5 ≈ 17.94，而逆向一步
 * u → 9u − 20v ≈ 0.056u 严格变小；因此任一 u > 1000 的正当解的祖先链必然经过 u ≤ 1000 的窗口，
 * 一定会被小解搜索捕获。verifySample 里把窗口从 10³ 放大到 10⁵ 复跑，结果逐项相同，佐证没有漏解。
 *
 * 复杂度：定义级扫描 O(limit) 次判定（limit = 2.2×10⁸，覆盖到题面给出的第 20 个金块），
 * 其中约 1/4 触发整数开方；解链枚举 O(#小解 × 每链长度) ≈ 常数（窗口 u ≤ 10³ 内 12 个小解，
 * 共 122 个链元素），空间 O(答案个数)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

private const val COUNT = 30
private const val CHAIN_LIMIT = 100_000_000_000_000L   // 解链枚举的 u 上界，覆盖前 30 个金块
private const val SEED_BOUND = 1_000L                  // 小解搜索窗口：u ≤ 10³
private const val WIDE_SEED_BOUND = 100_000L           // 复跑用的宽窗口
private const val SCAN_LIMIT = 220_000_000L            // 定义级扫描规模：恰好覆盖到第 20 个金块 211345365

/** 平方数模 16 只可能是 0、1、4、9，用它先筛掉 3/4 的候选，再开方。 */
private val SQUARE_MOD_16 = booleanArrayOf(
    true, true, false, false, true, false, false, false,
    false, true, false, false, false, false, false, false
)

/**
 * 定义级朴素扫描：n = 1, 2, 3, … 逐个检查 5n² + 14n + 1 是否为完全平方数，
 * 这就是「x 有理」的原始定义（判别式为平方），不涉及任何 Pell 结构。
 */
fun naiveScan(limit: Long): List<Long> {
    val found = ArrayList<Long>()
    var n = 1L
    var d = 5L * n * n + 14 * n + 1                 // d(n)，n ≤ 2.2×10⁸ 时 < 2.5×10¹⁷，Long 安全
    var delta = 10L * n + 19L                       // d(n+1) − d(n)；其自身每步 +10，故 d 可增量维护，省去每次乘法
    while (n <= limit) {
        if (SQUARE_MOD_16[(d and 15L).toInt()]) {
            var s = Math.sqrt(d.toDouble()).toLong()
            while (s * s > d) s--
            while ((s + 1) * (s + 1) <= d) s++
            if (s * s == d) found.add(n)
        }
        n++
        d += delta
        delta += 10L
    }
    return found
}

/** 小解搜索：直接扫描 u ≤ bound，找 u² − 5v² = 44 的正解 (u, v)。 */
private fun smallSolutions(bound: Long): List<Pair<Long, Long>> {
    val seeds = ArrayList<Pair<Long, Long>>()
    var u = 7L
    while (u <= bound) {
        val t = u * u - 44
        if (t % 5 == 0L) {
            val q = t / 5
            var v = Math.sqrt(q.toDouble()).toLong()
            while (v * v > q) v--
            while ((v + 1) * (v + 1) <= q) v++
            if (v > 0 && v * v == q) seeds.add(u to v)
        }
        u++
    }
    return seeds
}

/**
 * 解链枚举：对每个小解反复乘 ε = 9 + 4√5 放大，收集 u ≡ 2 (mod 5)、u > 7 的项，
 * 换算 n = (u − 7)/5，用有序集合去重（不同种子会覆盖同一链的不同位置）。
 */
fun chainNuggets(limit: Long = CHAIN_LIMIT, seedBound: Long = SEED_BOUND): List<Long> {
    val nuggets = sortedSetOf<Long>()
    for ((su, sv) in smallSolutions(seedBound)) {
        var u = su
        var v = sv
        while (u <= limit) {
            if (u > 7 && u % 5 == 2L) nuggets.add((u - 7) / 5)
            val nextU = 9 * u + 20 * v
            val nextV = 4 * u + 9 * v
            u = nextU
            v = nextV
        }
    }
    return nuggets.toList()
}

fun solveBruteForce(count: Int = COUNT): Long {
    val nuggets = chainNuggets()
    check(nuggets.size >= count) { "解链只找到 ${nuggets.size} 个金块" }
    return nuggets.take(count).sum()
}

fun verifySample() {
    // 先跑一小段把扫描循环预热，再按定义扫到 2.2×10⁸（题面给出的第 20 个金块就在这一段里）
    naiveScan(1_000_000L)
    val scanStart = System.nanoTime()
    val scan = naiveScan(SCAN_LIMIT)
    System.err.printf("definition-scan: %.4f ms%n", (System.nanoTime() - scanStart) / 1e6)
    // 题面表格：只有 2 与 5 给出有理的 x（判别式 49、196 是完全平方），1、3、4 是无理数
    check(scan.take(5) == listOf(2L, 5L, 21L, 42L, 152L)) { "前五个金块应为 2,5,21,42,152，实得 ${scan.take(5)}" }
    check(scan.size == 20) { "n ≤ 2.2×10⁸ 应恰有 20 个金块，实得 ${scan.size}" }
    check(scan[19] == 211_345_365L) { "第 20 个金块应为题面给出的 211345365，实得 ${scan[19]}" }
    check(!scan.contains(1L) && !scan.contains(3L) && !scan.contains(4L)) { "1、3、4 的 x 应是无理数，不能入选" }

    // ── 解链枚举的前 20 项必须与定义级扫描逐项吻合
    val chained = chainNuggets()
    check(chained.take(20) == scan) { "解链枚举与定义级扫描不一致：${chained.take(20)}" }

    // ── 小解窗口稳定性：10³ → 10⁵ 复跑，结果不得变化
    check(chainNuggets(seedBound = WIDE_SEED_BOUND) == chained) { "放大种子窗口后结果变化，说明窗口漏解" }

    // ── 逐项回到定义（用 BigInteger，避免大 n 溢出）：5n² + 14n + 1 = m²
    for (n in chained.take(COUNT)) {
        val big = BigInteger.valueOf(n)
        val d = big * big * BigInteger.valueOf(5) + big * BigInteger.valueOf(14) + BigInteger.ONE
        val m = d.sqrt()
        check(m * m == d) { "n = $n 的判别式不是完全平方数" }
    }
}

fun main() {
    verifySample()
    repeat(3) { solveBruteForce() }              // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
