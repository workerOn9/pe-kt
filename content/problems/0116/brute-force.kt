/**
 * Project Euler 116 — 暴力解 / 独立复核（教学对比用）
 *
 * solution.kt 用「按最右端砖分类」的递推 O(n) 求出答案。本文件走两条与之独立的路径：
 *
 *  1) 直接穷举：从左到右逐格决策——放一块灰砖，或放一块长 m 的彩色砖，把能恰好铺满一行的
 *     「砖序」一个个数出来。它数的是可见的排列本身，不含任何递推或组合公式，是最字面的暴力法。
 *     代价是搜索树与铺法数同量级：长度 50 的红砖一项就有 2.04×10¹⁰ 条铺法，本机不可能穷举完，
 *     因此穷举只用来在 n ≤ 20 的整个区间上逐点对照递推结果（n = 20 时红砖 10945 条，仍是全量）。
 *  2) 组合数闭式：铺 k 块长 m 的彩色砖时，把每块彩色砖「压缩」成 1 个单位，与剩下的 n − km 块
 *     灰砖一起共 n − k(m−1) 个位置，从中选 k 个位置放彩色砖，故该颜色的方案数为
 *     Σ_{k≥1} C(n − k(m−1), k)。二项式用 BigInteger 精确计算，与递推路径的数值表示完全独立。
 *
 * 题面长度 5 的样例（7, 3, 2）在 main 里作自检锚点，同时兜住「公式写错」与「递推写错」两类失误。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 穷举所有恰好铺满的砖序，只统计至少含一块彩色砖的铺法（字面暴力法）。 */
fun countByEnumeration(rowLength: Int, tileLength: Int, position: Int, colourUsed: Boolean): Long {
    if (position == rowLength) return if (colourUsed) 1L else 0L
    var count = countByEnumeration(rowLength, tileLength, position + 1, colourUsed)   // 放灰砖
    if (position + tileLength <= rowLength) {                                         // 放彩色砖
        count += countByEnumeration(rowLength, tileLength, position + tileLength, true)
    }
    return count
}

/** solution.kt 的递推，仅用于在 n ≤ 20 区间与穷举逐点对照。 */
fun waysByRecurrence(rowLength: Int, tileLength: Int): Long {
    val ways = LongArray(rowLength + 1)
    ways[0] = 1
    for (i in 1..rowLength) {
        var total = ways[i - 1]
        if (i >= tileLength) total += ways[i - tileLength]
        ways[i] = total
    }
    return ways[rowLength] - 1
}

/** 组合数闭式：Σ_{k≥1} C(n − k(m−1), k)，k 的上界由 n − km ≥ 0 给出。 */
fun countByBinomials(rowLength: Int, tileLength: Int): BigInteger {
    var total = BigInteger.ZERO
    var tiles = 1
    while (rowLength - tiles * (tileLength - 1) >= tiles) {
        total += binomial(rowLength - tiles * (tileLength - 1), tiles)
        tiles++
    }
    return total
}

private fun binomial(n: Int, k: Int): BigInteger {
    var result = BigInteger.ONE
    for (i in 1..k) {
        result = result * BigInteger.valueOf((n - k + i).toLong()) / BigInteger.valueOf(i.toLong())
    }
    return result
}

/** 长度 50 的答案（三条路径中的 BigInteger 组合数闭式路径）。 */
fun solveBruteForce(): Long =
    (2..4).fold(BigInteger.ZERO) { sum, tileLength -> sum + countByBinomials(50, tileLength) }
        .longValueExact()

fun main() {
    // 自检锚点：题面给出长度 5 时红/绿/蓝各 7/3/2 种，合计 12
    val sample = (2..4).map { countByEnumeration(5, it, 0, false) }
    println("长度 5 样例（穷举）= $sample，合计 ${sample.sum()}（题面 7, 3, 2 → 12）")
    check(sample == listOf(7L, 3L, 2L) && sample.sum() == 12L) { "题面样例自检失败：$sample" }

    // 穷举 vs 递推：在 n = 1..20 的整个区间上逐点对照（n = 20 红砖仍有 10945 条铺法，属全量穷举）
    var checked = 0
    for (n in 1..20) {
        for (tileLength in 2..4) {
            val brute = countByEnumeration(n, tileLength, 0, false)
            val recurrence = waysByRecurrence(n, tileLength)
            check(brute == recurrence) { "n = $n、砖长 $tileLength：穷举 $brute != 递推 $recurrence" }
            checked++
        }
    }
    println("穷举与递推在 n ≤ 20 的 $checked 个 (n, 砖长) 组合上完全一致")

    val closed = (2..4).map { countByBinomials(50, it) }
    println("长度 50 各色方案数（组合数闭式）= $closed")
    println("组合数闭式总和 = ${closed.reduce { a, b -> a + b }}")
    println("solveBruteForce() = ${solveBruteForce()}")
}
