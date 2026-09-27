#!/usr/bin/env kotlin
/**
 * Project Euler 239 — Twenty-two Foolish Primes（二十二个鲁莽的素数）
 *
 * 思路：
 *   100 个盘随机排一行 = {1..100} 上均匀随机的排列，共 100! 种。
 *   1..100 内有 25 个素数；「恰有 22 个素数盘不在原位」= 「恰有 3 个素数盘在原位」。
 *   题面括号明确：**非素数盘的位置不受任何约束**。于是可以分解计数：
 *
 *     1. 选 3 个留在原位的素数盘：      C(25, 3)
 *     2. 这 3 个位置被占住，剩下 97 个盘放进 97 个位置，
 *        且另外 22 个素数盘全部避开自己的原位（它们的原位都在这 97 个位置里），
 *        用容斥：
 *            A = Σ_{j=0..22} (-1)^j · C(22, j) · (97-j)!
 *     3. 概率 = C(25,3) · A / 100!
 *
 *   分母是 100!（全部盘的全排列）而不是 97!：第 1 步只是「选哪 3 个留在原位」，
 *   这个选择替代了那 3 个盘的 3! 种排列位置——它们已经各自钉在原位。
 *   等价写法：P = [C(25,3)/(100·99·98)] · (A/97!)。若错用 97! 会算出 > 1 的「概率」，
 *   概率越界就是最直接的报警信号（check 里就守着这条）。
 *
 *   大数：阶乘到 100! ≈ 9.3×10^157，必须用 BigInteger；只在最后一步用 BigDecimal
 *   除一次并设 40 位精度，不要用 Double 连乘（中间早就溢出）。
 *
 * 旁证：
 *   1. A/97! = 0.796346…，与粗估 (1 − 1/97)^22 ≈ 0.796 相符；
 *   2. 总概率 1.888×10^-3 与量级自检 [C(25,3)/(100·99·98)]·(1−1/97)^22 同阶；
 *   3. brute-force.kt 枚举 N=8（4 个素数盘）的全部 8! 个排列，得到 3024/40320 = 0.075，
 *      与容斥公式在同规模上逐位吻合。
 *
 * 答案：0.001887854841（按题面要求剥掉前导 0. 后保留 12 位小数）
 * 复杂度：22 次 BigInteger 阶乘/组合数运算，实测约 5 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

private const val N_DISKS = 100
private const val N_DISPLACED = 22          // 恰有 22 个素数盘错位
private const val SCALE = 12                // 答案保留的小数位数

private val FACT = HashMap<Int, BigInteger>()

private fun fact(n: Int): BigInteger = FACT.getOrPut(n) {
    if (n <= 1) BigInteger.ONE else BigInteger.valueOf(n.toLong()).multiply(fact(n - 1))
}

private fun binom(n: Int, k: Int): BigInteger = fact(n).divide(fact(k)).divide(fact(n - k))

private fun primesUpTo(limit: Int): IntArray {
    val isPrime = BooleanArray(limit + 1) { true }
    isPrime[0] = false; isPrime[1] = false
    var i = 2
    while (i.toLong() * i <= limit) {
        if (isPrime[i]) { var j = i * i; while (j <= limit) { isPrime[j] = false; j += i } }
        i++
    }
    return (2..limit).filter { isPrime[it] }.toIntArray()
}

private fun main() {
    val primes = primesUpTo(N_DISKS)
    val nPrimes = primes.size
    val nPinned = nPrimes - N_DISPLACED          // 留在原位的素数盘个数 = 3
    val free = N_DISKS - nPinned                 // 自由排列的盘数 = 97

    println("primes in 1..$N_DISKS = $nPrimes")
    println("pinned primes = $nPinned, freely permuted disks = $free")

    // 容斥：97 元排列中，N_DISPLACED 个指定盘全部不固定
    var good = BigInteger.ZERO
    for (j in 0..N_DISPLACED) {
        val term = binom(N_DISPLACED, j).multiply(fact(free - j))
        good = if (j % 2 == 0) good.add(term) else good.subtract(term)
    }
    println("A = ${good}")
    println("A/97! = " + BigDecimal(good).divide(BigDecimal(fact(free)), MathContext(20)))

    // 概率：分子是「选出哪 3 个素数盘留在原位」×「其余 97 个盘的合法排列数」，
    // 分母是全部 100 个盘的全排列 100!
    val numerator = binom(nPrimes, nPinned).multiply(good)
    val denominator = fact(N_DISKS)
    val prob = BigDecimal(numerator).divide(BigDecimal(denominator), MathContext(SCALE + 8))

    // 取小数点后 SCALE 位（四舍五入）
    val rounded = prob.setScale(SCALE, java.math.RoundingMode.HALF_UP)
    println("probability = $rounded")

    // 剥掉前导 "0." 得到题面要求的 0.abcdefghijkl
    val digits = rounded.toPlainString().removePrefix("0.")
    println("answer = 0.$digits")
    check(digits.length == SCALE) { "答案位数不对：$digits" }

    // 概率必须在 (0,1) 内 —— 用来抓「分母选错」这类 bug
    check(prob > BigDecimal.ZERO && prob < BigDecimal.ONE) { "概率越界：$prob" }
}
