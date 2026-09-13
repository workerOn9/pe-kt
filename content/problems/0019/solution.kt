/**
 * Project Euler 019 — Counting Sundays
 *
 * 优化解：从 1900-01-01（周一）出发逐年逐月推进星期，O(年数×12)。
 * 平年 +365 ≡ +1 (mod 7)，闰年 +2；每月只需累加当月天数 mod 7。
 * 星期编码：0=周日, 1=周一, ..., 6=周六。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isLeap(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

fun solve(): Long {
    var dow = 1                    // 1900-01-01 是周一
    var count = 0L
    for (year in 1900..2000) {
        val daysInMonth = intArrayOf(31, if (isLeap(year)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        for (days in daysInMonth) {
            if (year >= 1901 && dow == 0) count++                  // 每月 1 日是周日
            dow = (dow + days) % 7                                 // 推进到下月 1 日
        }
    }
    return count
}

fun main() {
    println(solve())
}
