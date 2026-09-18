/**
 * Project Euler 109 — Darts（飞镖 checkout 计数）
 *
 * 思路：一镖盘共 62 个计分区域——S1..S20（值 1..20）、D1..D20（值 2,4,…,40）、
 * T1..T20（值 3,6,…,60）、外牛眼 25、内牛眼（双倍）50。checkout 的规则是：
 * 末镖必须是双倍（含内牛眼）；前三镖里前两镖视作无序多重集（题面 S1 T1 D1 与
 * T1 S1 D1 同一种），但首镖与末镖位置不同所以 2 镖的 checkout 天然有序
 * （D1 D2 ≠ D2 D1）；miss 不计入，因此只统计 1、2、3 镖三种长度。
 * 于是「得分严格小于 limit 的 checkout 数」= 对每个末镖双倍 d：
 *   1 镖：d < limit 记 1 种；
 *   2 镖：首镖区域 x 满足 x + d < limit，逐区域计数；
 *   3 镖：前两镖按得分值分组计数——值 v≠w 时 cnt[v]·cnt[w] 种多重集，
 *         值 v=w 时 cnt[v](cnt[v]+1)/2 种（同一区域可投两次，区域不同但同值时也算不同）。
 * 区域按值归并靠一个长度 61 的计数数组，值域上界只有 60，全程整数运算。
 * 自检锚点：得分恰为 6 的 checkout 有 11 种（= countCheckouts(7) − countCheckouts(6)），
 * 不限得分的 checkout 总数为 42336（= countCheckouts(171)，170 已是最高可能得分），
 * 两个锚点都来自题面。
 *
 * 复杂度：末镖 21 个双倍 × 值域 60² 的配对扫描 ≈ 7.6 万次整数运算，O(D·M²) 时间
 * （D 为双倍个数、M 为单镖分值上界 60）、O(M) 空间，微秒级。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 单镖分值上界：最大区域是三倍 20。 */
const val BOARD_MAX = 60

/** 62 个计分区域的分值：先三档倍率 × 1..20，再外/内牛眼。 */
fun dartValues(): IntArray {
    val values = IntArray(62)
    var index = 0
    for (multiplier in 1..3) {
        for (number in 1..20) values[index++] = number * multiplier
    }
    values[index++] = 25
    values[index] = 50
    return values
}

/** 21 个可作末镖的双倍区域：D1..D20 与内牛眼 D25。 */
fun doubleValues(): IntArray {
    val values = IntArray(21)
    for (number in 1..20) values[number - 1] = number * 2
    values[20] = 50
    return values
}

/** cnt[v] = 分值为 v 的计分区域个数（0 ≤ v ≤ 60）。 */
fun valueCounts(values: IntArray): IntArray {
    val counts = IntArray(BOARD_MAX + 1)
    for (value in values) counts[value]++
    return counts
}

/** 得分严格小于 limit 的 checkout 方式数。 */
fun countCheckouts(limit: Int, darts: IntArray = dartValues(), doubles: IntArray = doubleValues()): Long {
    val counts = valueCounts(darts)
    var total = 0L
    for (d in doubles) {
        if (d >= limit) continue
        total++                                             // 1 镖：末镖即 d
        for (x in darts) if (x + d < limit) total++          // 2 镖：首镖可为任意区域
        for (v in 1..BOARD_MAX) {                            // 3 镖：前两镖的多重集
            if (counts[v] == 0) continue
            val rest = limit - d - v                         // 第二镖的值 w 须满足 w < rest
            for (w in v until minOf(rest, BOARD_MAX + 1)) {
                if (counts[w] == 0) continue
                total += if (w == v) {
                    counts[v].toLong() * (counts[v] + 1) / 2     // 同值区域可重复取
                } else {
                    counts[v].toLong() * counts[w]
                }
            }
        }
    }
    return total
}

fun solve(): Long = countCheckouts(100)

fun main() {
    val exactly6 = countCheckouts(7) - countCheckouts(6)
    check(exactly6 == 11L) { "题面锚点：得分 6 应有 11 种 checkout，实际 $exactly6" }
    val allCheckouts = countCheckouts(171)
    check(allCheckouts == 42336L) { "题面锚点：checkout 总数应为 42336，实际 $allCheckouts" }
    println(solve())
}
