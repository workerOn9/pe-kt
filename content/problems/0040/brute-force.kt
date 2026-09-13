/**
 * Project Euler 040 — 暴力解（教学对比用）
 *
 * 不预先拼接字符串，每次需要第 n 位时现场从 1 开始逐数推进数位（重复扫描）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun champernowneDigit(idx: Int): Int {
    var n = 1; var pos = 0
    while (true) {
        val s = n.toString()
        if (idx - pos <= s.length) return s[idx - pos - 1] - '0'
        pos += s.length; n++
    }
}

fun solveBruteForce(): Long {
    var prod = 1L
    var d = 1
    while (d <= 1_000_000) { prod *= champernowneDigit(d); d *= 10 }
    return prod
}

fun main() {
    println(solveBruteForce())
}
