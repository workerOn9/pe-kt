/**
 * Project Euler 025 — 1000-digit Fibonacci Number
 *
 * 优化解：BigInteger 迭代生成斐波那契数列，与阈值 10^999 直接比较，
 * 找到首个达到 1000 位的项。O(n²) 位运算，n=4782 毫秒级出结果。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

fun solve(digits: Int = 1000): Int {
    val threshold = BigInteger.TEN.pow(digits - 1)  // 1000 位数的下界
    var prev = BigInteger.ONE                       // F_1
    var curr = BigInteger.ONE                       // F_2
    var index = 2
    while (curr < threshold) {
        val next = prev + curr
        prev = curr
        curr = next
        index++
    }
    return index
}

fun main() {
    println(solve())
}
