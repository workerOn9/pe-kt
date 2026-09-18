/**
 * Project Euler 112 — Bouncy Numbers（弹跳数）
 *
 * 思路：按位数、前缀的自然顺序访问正整数。一个前缀一旦同时出现上升和下降，
 * 它的所有后缀都构成弹跳数，可以整块跳过。设此前非弹跳数个数为 C，块内 C 不变，
 * 弹跳比例为 (n-C)/n；目标百分比 p 的候选位置满足 (100-p)n=100C。
 * 只在候选是整数且落在当前块内时返回，否则继续。单调前缀才需要展开。
 * 复杂度：O(10 D U) 时间的保守上界，O(D) 递归空间；D 为答案位数，U 为这些位数内
 * 非弹跳数总数。跳过块时不逐个访问其中的数，所有比例比较使用 Long 整数。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun firstBouncyProportion(percent: Int): Long {
    require(percent in 1..99)
    var nonBouncy = 0L
    val powers = LongArray(18) { 1L }
    for (i in 1 until powers.size) powers[i] = powers[i - 1] * 10L

    fun visit(prefix: Long, last: Int, remaining: Int, up: Boolean, down: Boolean): Long {
        if (up && down) {
            val lower = prefix * powers[remaining]
            val upper = lower + powers[remaining] - 1
            val numerator = 100L * nonBouncy
            val denominator = 100 - percent
            val candidate = numerator / denominator
            return if (numerator % denominator == 0L && candidate in lower..upper) candidate else 0L
        }
        if (remaining == 0) {
            nonBouncy++
            return 0L
        }
        for (digit in 0..9) {
            val answer = visit(prefix * 10 + digit, digit, remaining - 1, up || digit > last, down || digit < last)
            if (answer != 0L) return answer
        }
        return 0L
    }

    for (digits in 1..18) {
        for (first in 1..9) {
            val answer = visit(first.toLong(), first, digits - 1, false, false)
            if (answer != 0L) return answer
        }
    }
    error("在支持的 18 位整数范围内未找到目标比例")
}

fun solve(): Long {
    check(firstBouncyProportion(50) == 538L)
    check(firstBouncyProportion(90) == 21780L)
    return firstBouncyProportion(99)
}

fun main() { println(solve()) }
