/**
 * Project Euler 048 — 暴力解（教学对比用）
 *
 * 老老实实算出每个 i^i 的完整大整数（1000^1000 约 3000 位）再求和取末十位。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    var sum = BigInteger.ZERO
    for (i in 1..1000) sum = sum.add(BigInteger.valueOf(i.toLong()).pow(i))
    return sum.mod(BigInteger.TEN.pow(10)).toLong()
}

fun main() {
    println(solveBruteForce())
}
