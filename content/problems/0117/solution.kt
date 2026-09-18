/**
 * Project Euler 117 — Red, Green, and Blue Tiles（红、绿和蓝瓷砖）
 *
 * 思路：与 116 的区别是颜色可以混用，于是「一行的铺法」就是长度集合 {1, 2, 3, 4} 的一个
 * 有序拆分（composition）：灰砖长 1，红/绿/蓝分别长 2/3/4。设 ways[i] 为铺满长度 i 的方案数，
 * 按最右端那块砖分类即可得到四步递推
 *     ways[i] = ways[i−1] + ways[i−2] + ways[i−3] + ways[i−4]，ways[0] = 1，i < 0 时 ways = 0。
 * 四种砖长对应四项相加，正是「四那契数」（tetranacci）。与 116 不同，本题题面把「整行全灰」
 * 也算作一种铺法（长度 5 的样例 15 就是 {1,2,3,4}-拆分数，含全灰那一种），因此不必减 1。
 *
 * 复杂度：O(n) 时间、O(n) 空间（n = 50，49 次四项求和）。
 * 数值上界：ways[50] = 100808458960497 ≈ 1.01×10¹⁴，远小于 Long 上限 9.2×10¹⁸，
 * 递推全程用 64 位整数即可，不需要 BigInteger，也不涉及位数判断与浮点比较。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 用长 1（灰）、2（红）、3（绿）、4（蓝）四种砖铺满长度 rowLength 的方案数（含全灰）。 */
fun tilingWays(rowLength: Int): Long {
    val ways = LongArray(rowLength + 1)
    ways[0] = 1
    for (i in 1..rowLength) {
        var total = 0L
        for (length in 1..4) {
            if (i >= length) total += ways[i - length]
        }
        ways[i] = total
    }
    return ways[rowLength]
}

fun solve(): Long = tilingWays(50)

fun main() {
    // 题面样例锚点：长度 5 恰有 15 种铺法
    val sample = tilingWays(5)
    check(sample == 15L) { "题面长度 5 的样例不符：$sample" }
    println(solve())
}
