package dev.pekt.problems

/**
 * Problem 197: A Recursively Defined Sequence
 *
 * 思路：迭代收敛到 2-cycle
 *
 * 函数 f(x) = floor(2^(30.403243784 - x^2)) * 10^(-9)
 *
 * 从 u_0 = -1 开始迭代，序列会快速收敛到一个 2-cycle。
 * 约 500 步后达到稳定，对 n = 10^12 直接取极限值即可。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve197(): Long {
    var u = -1.0
    var v = f(u)

    // 迭代直到收敛到 2-cycle
    var step = 0
    while (step < 2000) {
        val next = f(v)
        if (Math.abs(next - u) < 1e-12) break
        u = v
        v = next
        step++
    }

    // 返回 u_n + u_{n+1} 的 9 位小数表示（放大 10^9 倍后取整）
    val sum = u + v
    return Math.round(sum * 1e9).toLong()
}

private fun f(x: Double): Double {
    return Math.floor(Math.pow(2.0, 30.403243784 - x * x)) * 1e-9
}

fun main() {
    val ans = solve197()
    val intPart = ans / 1000000000L
    val fracPart = ans % 1000000000L
    println("$intPart.${fracPart.toString().padStart(9, '0')}")
}
