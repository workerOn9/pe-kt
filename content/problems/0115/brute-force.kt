/**
 * Project Euler 115 — Counting Block Combinations II（独立对照解）
 *
 * 与 solution.kt 求同一个 n，但完全不走「首格分类 + 长度递推」那条路：对每个候选 n，
 * 按红块个数 k 分类，用隔板法直接给出闭式（组合数用 BigInteger 逐步精确乘除）
 *
 *   F(m, n) = Σ_{k≥0} C(n − (m−1)k + 1, 2k)，  k = 0..⌊(n+1)/(m+1)⌋。
 *
 * 推导：k 块红，块长 L_i = m + a_i（a_i ≥ 0），块间黑格 gap_i = 1 + b_i（b_i ≥ 0），
 * 行首行尾黑格 g_0, g_k ≥ 0。约束 Σ L + Σ gap + g_0 + g_k = n 等价于 2k+1 个非负变量
 * 之和等于 n − (m+1)k + 1，隔板法给出 C(n − (m+1)k + 1 + 2k, 2k)；k = 0 就是全黑那一种。
 *
 * 于是「求最小 n」退化为一件事：从 n = m 起逐个把 F(m,n) 用闭式算出来，第一个超过 10⁶ 的
 * 就是答案。这条路径没有任何递推状态，每个 n 都是独立算的，与 solution.kt 的 64 位线性
 * 递推互不相干，两者一致才算互证成立。
 *
 * 复杂度：闭式对每个 n 求 ⌊(n+1)/(m+1)⌋ 个组合数，逐个组合数是 O(k) 次多精度乘除，
 * 扫到 n = 168 共约 350 次多精度乘除；空间 O(1) 个多精度变量。这里不做逐布局穷举：
 * n = 168 时合法布局已过百万，穷举的教学价值不如「与递推毫无共享状态」这一条。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** C(a, b)，b 取对称的较小侧；逐步乘除，每一步的部分积都是整数（无舍入）。 */
private fun binomial(a: Int, b: Int): BigInteger {
    val choose = minOf(b.toLong(), (a - b).toLong()).toInt()
    require(choose >= 0) { "组合数下标非法：C($a, $b)" }
    var term = BigInteger.ONE
    for (i in 1..choose) {
        term = term.multiply(BigInteger.valueOf((a - choose + i).toLong()))
            .divide(BigInteger.valueOf(i.toLong()))
    }
    return term
}

/** 按红块个数分类的闭式：F(m, n) = Σ_k C(n − (m−1)k + 1, 2k)。 */
fun fillCountByFormula(rowLength: Int, minLength: Int): Long {
    var total = BigInteger.ONE                      // k = 0：全黑
    for (blocks in 1..(rowLength + 1) / (minLength + 1)) {
        total = total.add(binomial(rowLength - (minLength - 1) * blocks + 1, 2 * blocks))
    }
    return total.longValueExact()
}

/** 逐 n 独立套用闭式，找第一个使 F(m,n) > threshold 的 n。 */
fun leastRowLength(minLength: Int, threshold: Long): Int {
    var n = minLength                               // n < m 时恒为 1，不必试
    while (true) {
        if (fillCountByFormula(n, minLength) > threshold) return n
        n++
        check(n < 100_000) { "阈值 $threshold 在 m = $minLength 下迟迟未突破，搜索已越界" }
    }
}

fun solveBruteForce(): Long = leastRowLength(50, 1_000_000L).toLong()

fun main() {
    // 锚点：题面直接给出的四组数，外加 114 的 F(3,7) = 17
    check(fillCountByFormula(29, 3) == 673_135L) { "F(3,29) = ${fillCountByFormula(29, 3)}" }
    check(fillCountByFormula(30, 3) == 1_089_155L) { "F(3,30) = ${fillCountByFormula(30, 3)}" }
    check(fillCountByFormula(56, 10) == 880_711L) { "F(10,56) = ${fillCountByFormula(56, 10)}" }
    check(fillCountByFormula(57, 10) == 1_148_904L) { "F(10,57) = ${fillCountByFormula(57, 10)}" }
    check(fillCountByFormula(7, 3) == 17L) { "F(3,7) = ${fillCountByFormula(7, 3)}" }
    check(leastRowLength(3, 1_000_000L) == 30 && leastRowLength(10, 1_000_000L) == 57) {
        "题面给定的 m = 3 → 30、m = 10 → 57 未复现"
    }
    println("m = 3 → n = ${leastRowLength(3, 1_000_000L)}，m = 10 → n = ${leastRowLength(10, 1_000_000L)}（题面锚点）")
    println("F(50,167) = ${fillCountByFormula(167, 50)}，F(50,168) = ${fillCountByFormula(168, 50)}")
    println(solveBruteForce())
}
