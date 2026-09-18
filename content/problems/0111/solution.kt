/**
 * Project Euler 111 — Primes with Runs（重复数字素数）
 *
 * 思路：把数字 d 的出现次数 m 从 n 向下枚举，首个有素数的层就是 M(n,d)。
 * 逐位递归生成恰含 m 个 d 的数，重复位置不要求相邻；排除首位零、非素数末位
 * 与数位和为 3 的倍数。用底数 2,3,5,7,11,13,17 的确定性 Miller–Rabin 判素性，
 * 其有效范围覆盖本题的 10¹⁰；BigInteger 模幂避免 Long 平方溢出。
 *
 * 复杂度：设 C 为已搜索层的候选数上界 Σ_m C(n,m)·9^(n−m)，
 * 时间 O(C·n + C·log V) 次数位操作与模乘，空间 O(n + log V)，V = 10ⁿ。
 * n = 10 实际只搜索 m = 10,9,8；每个数字各层最多 1、90、3645 个未剪枝候选。
 * 单个候选 < 10¹⁰，至多 10·3736 个候选被统计，总和 < 3.736×10¹⁴，Long 足够。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.math.BigInteger

/** 末位只可能是 1/3/7/9（n ≥ 2 的素数），下标即数字。 */
private val PRIME_LAST_DIGITS = booleanArrayOf(false, true, false, true, false, false, false, true, false, true)

/** 确定性 Miller–Rabin 底数：对一切 n < 3.4×10¹⁴ 无错判。 */
private val MILLER_RABIN_BASES = longArrayOf(2, 3, 5, 7, 11, 13, 17)

fun isPrime(candidate: Long): Boolean {
    if (candidate < 2L) return false
    for (base in MILLER_RABIN_BASES) {
        if (candidate == base) return true
        if (candidate % base == 0L) return false
    }
    var oddPart = candidate - 1L
    var powers = 0
    while (oddPart % 2L == 0L) {
        oddPart /= 2L
        powers++
    }
    val modulus = BigInteger.valueOf(candidate)
    val one = BigInteger.ONE
    val minusOne = modulus - one
    for (base in MILLER_RABIN_BASES) {
        if (base >= candidate) continue
        var power = BigInteger.valueOf(base).modPow(BigInteger.valueOf(oddPart), modulus)
        if (power == one || power == minusOne) continue
        var witnessed = false
        var round = 1
        while (round < powers && !witnessed) {
            power = power * power % modulus
            if (power == minusOne) witnessed = true
            round++
        }
        if (!witnessed) return false
    }
    return true
}

/**
 * 递归生成所有「恰好含 dNeeded 个数字 d」的 n 位数，素数计入 tally[0]（个数）/tally[1]（和）。
 * sumMod3 是已放置数位之和对 3 的余数，用于剔除必然被 3 整除的候选。
 */
private fun placeDigits(
    position: Int,
    n: Int,
    d: Int,
    dNeeded: Int,
    value: Long,
    sumMod3: Int,
    tally: LongArray,
) {
    val slotsLeft = n - position - 1
    for (digit in 0..9) {
        if (position == 0 && digit == 0) continue
        if (slotsLeft == 0 && !PRIME_LAST_DIGITS[digit]) continue
        val remainingD = if (digit == d) dNeeded - 1 else dNeeded
        if (remainingD < 0 || remainingD > slotsLeft) continue
        val nextValue = value * 10L + digit
        val nextMod3 = (sumMod3 + digit) % 3
        if (slotsLeft > 0) {
            placeDigits(position + 1, n, d, remainingD, nextValue, nextMod3, tally)
        } else if (nextMod3 != 0 && isPrime(nextValue)) {
            tally[0]++
            tally[1] += nextValue
        }
    }
}

/** 返回 [M(n, d), N(n, d), S(n, d)]：自 m = n 下降，取第一个有素数的层。 */
fun statsFor(n: Int, d: Int): LongArray {
    for (m in n downTo 1) {
        val tally = LongArray(2)
        placeDigits(0, n, d, m, 0L, 0, tally)
        if (tally[0] > 0L) return longArrayOf(m.toLong(), tally[0], tally[1])
    }
    return longArrayOf(0L, 0L, 0L)
}

fun solve(): Long = (0..9).sumOf { statsFor(10, it)[2] }

fun verifySample() {
    // 自检锚点：题面给出的四位素数表（M/N/S 与总和 273700）
    val expectedM = longArrayOf(2, 3, 3, 3, 3, 3, 3, 3, 3, 3)
    val expectedN = longArrayOf(13, 9, 1, 12, 2, 1, 1, 9, 1, 7)
    val expectedS = longArrayOf(67061, 22275, 2221, 46214, 8888, 5557, 6661, 57863, 8887, 48073)
    var sampleTotal = 0L
    for (d in 0..9) {
        val stats = statsFor(4, d)
        check(stats[0] == expectedM[d] && stats[1] == expectedN[d] && stats[2] == expectedS[d]) {
            "d = $d 的四位素数表不符：实际 M=${stats[0]} N=${stats[1]} S=${stats[2]}"
        }
        sampleTotal += stats[2]
    }
    check(sampleTotal == 273700L) { "四位总和应为 273700，实际 $sampleTotal" }
    println("四位素数表自检通过，总和 = $sampleTotal")
}

fun main() {
    verifySample()
    for (d in 0..9) {
        val stats = statsFor(10, d)
        println("d = $d: M = ${stats[0]}, N = ${stats[1]}, S = ${stats[2]}")
    }
    println(solve())
}
