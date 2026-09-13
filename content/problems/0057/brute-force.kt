/**
 * Project Euler 057 — Square Root Convergents（暴力对照）
 *
 * 暴力解：直接用 BigInteger 递推 (p, q) -> (p + 2q, p + q)，
 * 每步把分子分母都转成字符串，用字符串长度判断数位多少。
 * 算法正确但每次都要做一次十进制格式化，开销远大于数位数组直接读长度。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    var num = BigInteger.valueOf(3)
    var den = BigInteger.valueOf(2)
    var count = 0L
    for (k in 1..1000) {
        if (k > 1) {
            val nextNum = num.add(den.multiply(BigInteger.TWO))
            val nextDen = num.add(den)
            num = nextNum
            den = nextDen
        }
        if (num.toString().length > den.toString().length) count++
    }
    return count
}

fun main() {
    println(solveBruteForce())
}
