/**
 * Project Euler 074 — Digit Factorial Chains（暴力对照解）
 *
 * 暴力解：对每个起点用一个 HashSet 现场走链，直到遇到重复项为止，集合大小即
 * 「不重复项个数」。每个起点独立计算，不共享任何缓存——但正因如此要反复重走
 * 相同的尾链，代价随起点数近似线性于链长。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

fun solveBruteForce(): Long {
    val fact = intArrayOf(1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880)
    var total = 0L
    for (start in 1 until 1_000_000) {
        val seen = HashSet<Int>()
        var x = start
        while (seen.add(x)) x = p074DigitFactSum(x, fact)
        if (seen.size == 60) total++
    }
    return total
}

fun p074DigitFactSum(n: Int, fact: IntArray): Int {
    var x = n
    var s = 0
    while (x > 0) {
        s += fact[x % 10]
        x /= 10
    }
    return s
}

fun main() {
    println(solveBruteForce())
}
