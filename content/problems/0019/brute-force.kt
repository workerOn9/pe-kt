/**
 * Project Euler 019 — 暴力解（教学对比用）
 *
 * 对 1901..2000 的每个月 1 日独立用蔡勒公式（Zeller's congruence）算星期，
 * 1200 次独立计算，不复用任何前序状态。
 */

/**
 * 蔡勒公式（公历）：返回 h = 0 周六, 1 周日, 2 周一, ..., 6 周五。
 * 月份按 3=三月 .. 14=二月（一、二月视作上一年的 13、14 月）。
 */
fun zeller(year: Int, month: Int, day: Int): Int {
    val m = if (month <= 2) month + 12 else month
    val y = if (month <= 2) year - 1 else year
    val k = y % 100          // 年内世纪年
    val j = y / 100          // 世纪
    return (day + 13 * (m + 1) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
}

fun solveBruteForce(): Long {
    var count = 0L
    for (year in 1901..2000) {
        for (month in 1..12) {
            if (zeller(year, month, 1) == 1) count++               // h=1 即周日
        }
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
