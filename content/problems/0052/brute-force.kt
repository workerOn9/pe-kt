/**
 * Project Euler 052 — Permuted Multiples（暴力解，教学对比用）
 *
 * 从 1 开始逐个整数检查：把 x 与 2x…6x 的十进制数位排序后比较字符串，
 * 不做 9 的倍数剪枝、不压缩数字计数，直接返回第一个满足条件的 x。
 *
 * 复杂度：O(X · 6 · D log D)，X 为答案、D 为位数（排序与字符串分配是主要开销）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun sortedDigitsOf(n: Int): String = n.toString().toCharArray().sorted().joinToString("")

fun solveBruteForce(): Long {
    var x = 1
    while (true) {
        val sig = sortedDigitsOf(x)
        var ok = true
        for (k in 2..6) {
            if (sortedDigitsOf(k * x) != sig) { ok = false; break }
        }
        if (ok) return x.toLong()
        x++
    }
}

fun main() {
    println(solveBruteForce())
}
