/**
 * Project Euler 080 — Square Root Digital Expansion
 *
 * 优化解：sqrt(n) 前 100 位数字，就是 floor(sqrt(n · 10^198)) 的十进制数字——
 * 因为 sqrt(n·10^198) = sqrt(n)·10^99，取整即把第 100 位之后的尾数截断。
 * 用 BigInteger.sqrt()（库内 Newton 迭代）一次拿到整段精度，再逐位取模求和。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

fun digitSumOf(n: BigInteger): Int {
    var x = n
    var s = 0
    val ten = BigInteger.TEN
    while (x.signum() != 0) {
        s += x.mod(ten).toInt()
        x = x.divide(ten)
    }
    return s
}

fun solve(): Long {
    val scale = BigInteger.TEN.pow(198)
    var total = 0L
    for (n in 1..100) {
        val r = Math.sqrt(n.toDouble()).toInt()
        if (r * r == n) continue
        total += digitSumOf(BigInteger.valueOf(n.toLong()).multiply(scale).sqrt())
    }
    return total
}

fun main() {
    println(solve())
}
