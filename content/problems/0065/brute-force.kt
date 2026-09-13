import java.math.BigInteger

/**
 * Project Euler 065 — 暴力解（教学对比用）
 *
 * 完全不用渐近分数递推：把 [2; 1, 2, 1, 1, 4, …, 2k, 1] 从最内层一项开始逐层还原成分数，
 * 每层都做一次 gcd 约分（其实连分数的渐近分数天然最简，这些约分是白做的功）。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun eCoefB(k: Int): Int = when {
    k == 0 -> 2
    k % 3 == 2 -> 2 * (k + 1) / 3
    else -> 1
}

fun solveBruteForce(): Long {
    val n = 99
    var num = BigInteger.valueOf(eCoefB(n).toLong())
    var den = BigInteger.ONE
    for (k in n - 1 downTo 0) {
        val nn = BigInteger.valueOf(eCoefB(k).toLong()).multiply(num).add(den)
        val dd = num
        val g = nn.gcd(dd)
        num = nn.divide(g)
        den = dd.divide(g)
    }
    var sum = 0L
    var x = num
    while (x.signum() > 0) {
        sum += x.mod(BigInteger.TEN).toLong()
        x = x.divide(BigInteger.TEN)
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
