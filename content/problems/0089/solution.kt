/**
 * Project Euler 089 — Roman Numerals
 *
 * 思路：罗马数字到数值是唯一的（左减右加），数值到「最小写法」也唯一——按
 * 1000/900/500/400/…/4/1 这 13 个记号的贪心分解即为最短表示。于是逐行解码再编码，
 * 累加两次长度之差即可。
 *
 * 复杂度：O(行数 × 数值位数)，常数极小，实测 &lt; 1 ms。
 * 需从题目目录运行（读取同目录 roman.txt）；
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

private val p089Table = listOf(
    1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC",
    50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I",
)

private val p089Value = mapOf(
    'I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000,
)

private fun p089Decode(r: String): Int {
    var total = 0
    for (i in r.indices) {
        val v = p089Value.getValue(r[i])
        total += if (i + 1 < r.length && v < p089Value.getValue(r[i + 1])) -v else v
    }
    return total
}

private fun p089Encode(n: Int): String {
    val sb = StringBuilder()
    var v = n
    for ((value, sym) in p089Table) while (v >= value) { sb.append(sym); v -= value }
    return sb.toString()
}

fun solve(path: String = "roman.txt"): Long {
    var saved = 0L
    for (line in File(path).readLines()) {
        val r = line.trim()
        if (r.isEmpty()) continue
        saved += r.length - p089Encode(p089Decode(r)).length
    }
    return saved
}

fun main() {
    println(solve())
}
