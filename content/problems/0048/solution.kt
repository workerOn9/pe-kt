/**
 * Project Euler 048 — Self Powers
 *
 * 优化解：只需末十位：对 10^10 取模，用 BigInteger.modPow 做模幂，边算边累加。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

fun solve(): Long {
    val mod = BigInteger.TEN.pow(10)
    var sum = BigInteger.ZERO
    for (i in 1..1000) sum = sum.add(BigInteger.valueOf(i.toLong()).modPow(BigInteger.valueOf(i.toLong()), mod))
    return sum.mod(mod).toLong()
}

fun main() {
    println(solve())
}
