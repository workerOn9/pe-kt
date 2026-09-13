import java.math.BigInteger

/**
 * Project Euler 065 — Convergents of e
 *
 * 思路：e 的连分数系数有闭式——a₀ = 2；k ≥ 1 时若 k ≡ 2 (mod 3) 取 2(k+1)/3，否则取 1。
 * 渐近分数满足 pₙ = aₙ·pₙ₋₁ + pₙ₋₂（p₋₂ = 0，p₋₁ = 1），第 100 个分子的数字约 60 位，
 * 用 BigInteger 精确计算后逐位累加数位和。
 * 复杂度：O(n²) 位运算（n = 100 个系数）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun eCoefficient(k: Int): Int = when {
    k == 0 -> 2
    k % 3 == 2 -> 2 * (k + 1) / 3
    else -> 1
}

fun solve(): Long {
    var pPrev = BigInteger.ZERO // p_{n-2}
    var p = BigInteger.ONE      // p_{n-1}
    for (k in 0..99) {
        val a = BigInteger.valueOf(eCoefficient(k).toLong())
        val next = a.multiply(p).add(pPrev)
        pPrev = p
        p = next
    }
    var sum = 0L
    var x = p
    while (x.signum() > 0) {
        sum += x.mod(BigInteger.TEN).toLong()
        x = x.divide(BigInteger.TEN)
    }
    return sum
}

fun main() {
    println(solve())
}
