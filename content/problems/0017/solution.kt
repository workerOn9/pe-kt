/**
 * Project Euler 017 — Number Letter Counts
 *
 * 优化解：不逐数拼写，直接按出现次数做闭式统计（O(1)）。
 * - 1..9 的字母数在 1..99 中出现：个位 10 次（每段十位内一轮）+ 十位后缀 8 次；
 * - 20/30/.../90 的十位词各出现 10 次；
 * - 100..999 中，百位词出现 100 次/个，"hundred" 100 次/百段，"and" 99 次/百段；
 * - 1000 单独加 "onethousand"。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private val ONES = intArrayOf(3, 3, 5, 4, 4, 3, 5, 5, 4)            // one..nine
private val TEENS = intArrayOf(3, 6, 6, 8, 8, 7, 7, 9, 8, 8)        // ten..nineteen
private val TENS = intArrayOf(6, 6, 5, 5, 5, 7, 6, 6)               // twenty..ninety
private const val HUNDRED = 7
private const val AND = 3
private const val ONE_THOUSAND = 11                                  // "onethousand"

fun solve(): Long {
    val onesSum = ONES.sum()     // 36
    val teensSum = TEENS.sum()   // 70
    val tensSum = TENS.sum()     // 46

    // 1..99：1..19 各一次；十位词各 10 次；个位词作为后缀各出现 8 次
    val sum1to99 = onesSum + teensSum + 10 * tensSum + 8 * onesSum   // 854

    // 100..999：9 个百段，每段 = 百位词×100 + "hundred"×100 + "and"×99 + 1..99 整段
    val sum100to999 = 9L * sum1to99 + 100L * onesSum + 900L * HUNDRED + 9L * 99 * AND

    return sum1to99 + sum100to999 + ONE_THOUSAND                     // 21124
}

fun main() {
    println(solve())
}
