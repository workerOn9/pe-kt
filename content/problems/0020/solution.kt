import java.math.BigInteger

/**
 * Project Euler 020 — Factorial Digit Sum
 *
 * 优化解：100! 只有 158 位，BigInteger 逐次相乘后逐位求和。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** [n]! 的十进制数字和 */
fun solve(n: Int = 100): Long {
    var f = BigInteger.ONE
    for (i in 2..n) f = f.multiply(BigInteger.valueOf(i.toLong()))
    return f.toString().fold(0L) { acc, c -> acc + (c - '0') }
}

fun main() {
    println(solve())
}
