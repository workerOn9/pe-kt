/**
 * Project Euler 056 — Powerful Digit Sum
 *
 * 优化解：固定底数 a，用「逐次乘 a」在 O(1) 次乘法内从 a^(b-1) 递推出 a^b，
 * 而不是对每个指数重新做一次幂运算，乘法的总次数从 O(n^2 log n) 降到 O(n^2)。
 * 每得到一个幂就累加它的十进制数位和，取全局最大值。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

fun p056DigitSum(n: BigInteger): Int {
    var s = 0
    for (c in n.toString()) s += c - '0'
    return s
}

fun solve(): Long {
    var best = 0
    for (a in 2..99) {
        val base = BigInteger.valueOf(a.toLong())
        var power = BigInteger.ONE
        for (b in 1..99) {
            power = power.multiply(base)
            val s = p056DigitSum(power)
            if (s > best) best = s
        }
    }
    return best.toLong()
}

fun main() {
    println(solve())
}
