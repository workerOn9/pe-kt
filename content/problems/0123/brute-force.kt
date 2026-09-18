/**
 * Project Euler 123 — 暴力解（教学对比用）
 *
 * 与 solution.kt 有三处刻意不同，确保两条路径几乎不共享失误：
 *   1) 素数不用筛法，用「试除已找到的素数」逐个增量造表（试除到 √candidate）；
 *   2) 余数不做任何代数化简，按题面定义直接算 BigInteger 幂取模
 *      r = ((p−1)^n + (p+1)^n) mod p²，偶数 n 同样老老实实算（不利用「偶数恒为 2」）；
 *   3) 判定「超过阈值」用 BigInteger 比较，不涉及位数、不转 Long。
 * 代价是每次判定要做 2 次模幂，比 solution.kt 的一次乘法取模慢若干数量级。
 *
 * 锚点同样写成断言：n = 3 → 5；超过 10⁹ 的最小 n → 7037；再报超过 10¹⁰ 的下标。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 第 index 个素数（1 起）：按已得素数表试除，逐个候选推进。 */
class TrialDivisionPrimes {
    private val found = ArrayList<Int>()

    init {
        found.add(2)
    }

    private fun isPrime(candidate: Int): Boolean {
        val root = Math.sqrt(candidate.toDouble()).toLong()
        for (p in found) {
            if (p.toLong() > root) break
            if (candidate % p == 0) return false
        }
        return true
    }

    fun nth(index: Int): Int {
        while (found.size < index) {
            var candidate = found.last() + 1
            while (!isPrime(candidate)) candidate++
            found.add(candidate)
        }
        return found[index - 1]
    }
}

/** 题面定义直译：((p−1)^n + (p+1)^n) mod p²，全程多精度整数。 */
fun bruteRemainder(prime: Long, exponent: Int): BigInteger {
    val modulus = BigInteger.valueOf(prime).pow(2)
    val minus = BigInteger.valueOf(prime - 1L).modPow(BigInteger.valueOf(exponent.toLong()), modulus)
    val plus = BigInteger.valueOf(prime + 1L).modPow(BigInteger.valueOf(exponent.toLong()), modulus)
    return minus.add(plus).mod(modulus)
}

/** 使余数首次超过 threshold 的最小 n，用暴力算法从头扫。 */
fun firstIndexExceedingBrute(threshold: Long): Long {
    val limit = BigInteger.valueOf(threshold)
    val primes = TrialDivisionPrimes()
    var index = 1
    while (true) {
        val prime = primes.nth(index).toLong()
        if (bruteRemainder(prime, index) > limit) return index.toLong()
        index++
    }
}

fun main() {
    check(bruteRemainder(5L, 3) == BigInteger.valueOf(5L)) { "题面样例 n = 3 应得余数 5" }
    val sample = firstIndexExceedingBrute(1_000_000_000L)
    check(sample == 7037L) { "题面阈值 10⁹ 的答案应为 7037，实际 $sample" }
    println("暴力解：超过 10⁹ 的最小 n = $sample（题面 7037）")
    println("暴力解：超过 10¹⁰ 的最小 n = ${firstIndexExceedingBrute(10_000_000_000L)}")
}
