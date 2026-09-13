/**
 * Project Euler 092 — Square Digit Chains
 *
 * 暴力解：对 1..10^7−1 的每个起点完整走一遍数字平方和链，直到撞上 1 或 89，
 * 逐个数计数。没有任何预处理与记忆化，是题意的直接翻译。
 *
 * 复杂度：时间 O(10^7 · L)，L 为链长（约 10 量级），空间 O(1)
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun digitSquareSumBrute(n: Int): Int {
    var x = n
    var s = 0
    while (x > 0) {
        val d = x % 10
        s += d * d
        x /= 10
    }
    return s
}

fun solveBruteForce(): Long {
    var count = 0L
    for (n in 1 until 10_000_000) {
        var x = n
        while (x != 1 && x != 89) x = digitSquareSumBrute(x)
        if (x == 89) count++
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
