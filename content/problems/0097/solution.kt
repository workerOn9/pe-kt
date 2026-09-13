/**
 * Project Euler 097 — Large Non-Mersenne Prime
 *
 * 思路：只要求末十位，就是在模 10^10 下计算 28433 * 2^7830457 + 1。
 * BigInteger.modPow 用平方-乘实现，指数 7830457 只需约 23 次模平方 + 少量模乘。
 * 复杂度：O(log e) 次大数模乘，每次对 34 位数（10^10 以内）操作。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
import java.math.BigInteger

fun solve(): Long {
    val mod = BigInteger.TEN.pow(10)
    val power = BigInteger.TWO.modPow(BigInteger.valueOf(7830457L), mod)
    return power.multiply(BigInteger.valueOf(28433L)).add(BigInteger.ONE).mod(mod).toLong()
}

fun main() {
    println(solve())
}
