/**
 * Project Euler 116 — Red, Green or Blue Tiles（红、绿或蓝瓷砖）
 *
 * 思路：颜色不能混用，于是三种颜色各自独立计数再相加。固定一种彩色砖的长度 m
 * （红 2、绿 3、蓝 4），灰色方砖长度为 1，问题即「用长 1 与长 m 两种砖不重叠地铺满
 * 长 n 的一行」。设 ways[i] 为铺满长度 i 的方案数，按最右端那块砖分类：放灰砖得到
 * ways[i−1]，放彩色砖得到 ways[i−m]，故 ways[i] = ways[i−1] + ways[i−m]，其中
 * ways[0] = 1（长度 0 只有空铺法一种），i < m 时退化为 ways[i] = 1（只能全灰）。
 * ways[n] 里含「一块彩色砖都不用」的全灰方案，而题面要求至少用一块彩色砖，
 * 所以该颜色的方案数是 ways[n] − 1。
 * 题面长度 5 的样例在 main 里作自检：红 8 − 1 = 7、绿 4 − 1 = 3、蓝 3 − 1 = 2，合计 12。
 *
 * 复杂度：O(n) 时间、O(n) 空间（n = 50，三次线性扫描共约 150 次 64 位加法）。
 * 数值上界：三种颜色中红砖最多，ways[50] = 20365011074（即 F(51)）；三者之和 20492570929，
 * 远小于 Long 上限 9.2×10¹⁸，无需 BigInteger，也不涉及位数判断与浮点比较。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 只用一种长度 tileLength 的彩色砖时，铺满长 rowLength 的一行且至少用一块彩色砖的方案数。 */
fun waysWithSingleColour(rowLength: Int, tileLength: Int): Long {
    val ways = LongArray(rowLength + 1)
    ways[0] = 1
    for (i in 1..rowLength) {
        var total = ways[i - 1]                                // 最右端是一块灰砖
        if (i >= tileLength) total += ways[i - tileLength]      // 最右端是一块彩色砖
        ways[i] = total
    }
    return ways[rowLength] - 1                                  // 去掉全灰方案
}

fun solve(): Long = (2..4).sumOf { waysWithSingleColour(50, it) }

fun main() {
    val sample = (2..4).map { waysWithSingleColour(5, it) }
    check(sample == listOf(7L, 3L, 2L)) { "题面长度 5 的样例不符：$sample" }
    check(sample.sum() == 12L) { "题面样例合计应为 12，实际 ${sample.sum()}" }
    println(solve())
}
