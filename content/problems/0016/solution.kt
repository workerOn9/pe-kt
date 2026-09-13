import java.math.BigInteger

/**
 * Project Euler 016 — Power Digit Sum
 *
 * 优化解：2^1000 只有 302 位，BigInteger 一次乘方后逐位求和。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 2^[exp] 的十进制数字和 */
fun solve(exp: Int = 1000): Long =
    BigInteger.TWO.pow(exp).toString().fold(0L) { acc, c -> acc + (c - '0') }

fun main() {
    println(solve())
}
