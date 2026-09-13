/**
 * Project Euler 040 — Champernowne's Constant
 *
 * 优化解：一次性拼出前 100 万个数字的字符串，再直接取第 1、10、…、10^6 位相乘。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val sb = StringBuilder()
    var n = 1
    while (sb.length < 1_000_000) { sb.append(n); n++ }
    var prod = 1L
    var d = 1
    while (d <= 1_000_000) { prod *= (sb[d - 1] - '0'); d *= 10 }
    return prod
}

fun main() {
    println(solve())
}
