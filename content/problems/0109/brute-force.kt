/**
 * Project Euler 109 — Darts（checkout 计数，对照实现）
 *
 * 思路：与 solution.kt「按上限聚合」的写法相反，这里逐得分做精确枚举：对每个目标得分
 * s，枚举末镖双倍 d ≤ s，再分别枚举首镖区域（2 镖情形）与下标 i ≤ j 的前两镖区域对
 * （3 镖情形，i ≤ j 天然去掉了前两镖的顺序），只把得分恰为 s 的组合计入。
 * 最后把 s = 1..99 的结果相加，就是「得分小于 100」的 checkout 总数。
 * 两种写法在数据流向上完全不同：solution.kt 用计数数组把同值区域归并成组合数公式，
 * 这里则是一个个区域对硬枚举，二者结果一致才说明对得分的拆分没有算重或算漏。
 * 自检锚点同样取自题面：waysForScore(6) = 11，1..170 全得分求和 = 42336。
 *
 * 复杂度：每个得分 O(D·(1 + R + R²/2))（R = 62 个区域），99 个得分合计约 4×10⁶ 次比较，
 * 时间 O(S·D·R²)、空间 O(1)，仍在毫秒以内。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 62 个计分区域的分值：三档倍率 × 1..20，外加外牛眼 25、内牛眼 50。 */
fun boardDarts(): IntArray {
    val values = IntArray(62)
    var index = 0
    for (multiplier in 1..3) {
        for (number in 1..20) values[index++] = number * multiplier
    }
    values[index++] = 25
    values[index] = 50
    return values
}

/** 21 个可作末镖的双倍区域。 */
fun boardDoubles(): IntArray {
    val values = IntArray(21)
    for (number in 1..20) values[number - 1] = number * 2
    values[20] = 50
    return values
}

/** 得分恰为 score 的 checkout 方式数。 */
fun waysForScore(score: Int, darts: IntArray, doubles: IntArray): Long {
    var ways = 0L
    for (d in doubles) {
        if (d > score) continue
        if (d == score) ways++                                  // 1 镖
        for (x in darts) if (x + d == score) ways++              // 2 镖
        for (i in darts.indices) {                               // 3 镖：i ≤ j 即前两镖不算顺序
            for (j in i until darts.size) {
                if (darts[i] + darts[j] + d == score) ways++
            }
        }
    }
    return ways
}

fun solveBruteForce(): Long {
    val darts = boardDarts()
    val doubles = boardDoubles()
    var total = 0L
    for (score in 1 until 100) total += waysForScore(score, darts, doubles)
    return total
}

fun main() {
    val darts = boardDarts()
    val doubles = boardDoubles()
    val six = waysForScore(6, darts, doubles)
    check(six == 11L) { "题面锚点：得分 6 应有 11 种 checkout，实际 $six" }
    var all = 0L
    for (score in 1..170) all += waysForScore(score, darts, doubles)
    check(all == 42336L) { "题面锚点：checkout 总数应为 42336，实际 $all" }
    println(solveBruteForce())
}
