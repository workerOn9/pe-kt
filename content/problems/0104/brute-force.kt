/**
 * Project Euler 104 — 暴力解（完整大整数递推）
 *
 * 思路：逐项保存完整 F_k，以整数余数检查末九位，以十的幂阈值维护位数。
 * 末九位通过时，整除 10^(d-9) 得到精确首九位。判定数字用出现次数，不复用位掩码。
 * 同时检查题面 541、2749 两个锚点，并验证所有末九位候选的精确/对数首部判定一致。
 * 求解结果只取决于整数轨道，对数仅用于断言旁证。
 * 复杂度：O(KD) 时间、O(D) 空间，D 为最大位数；大整数加法、模小整数与乘 10 均线性。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun exactPandigital(value: Long): Boolean {
    if (value !in 100_000_000L..999_999_999L) return false
    val counts = IntArray(10)
    var rest = value
    repeat(9) {
        counts[(rest % 10).toInt()]++
        rest /= 10
    }
    return counts[0] == 0 && (1..9).all { counts[it] == 1 }
}

fun solveBruteForce(): Long {
    val modulus = BigInteger.valueOf(1_000_000_000L)
    var previous = BigInteger.ONE
    var current = BigInteger.ONE
    var threshold = BigInteger.TEN
    var digits = 1
    var firstLast = 0
    var firstLead = 0
    var k = 2
    while (true) {
        val next = previous + current
        previous = current
        current = next
        k++
        if (current >= threshold) {
            threshold *= BigInteger.TEN
            digits++
        }
        val last = current.mod(modulus).toLong()
        val lastMatches = exactPandigital(last)
        if (lastMatches && firstLast == 0) firstLast = k
        if (digits < 9 || (!lastMatches && firstLead != 0)) continue
        val first = current.divide(threshold.divide(modulus)).toLong()
        val firstMatches = exactPandigital(first)
        if (firstMatches && firstLead == 0) firstLead = k
        if (!lastMatches) continue
        val x = k * Math.log10((1 + Math.sqrt(5.0)) / 2) - Math.log10(5.0) / 2
        val approximate = Math.pow(10.0, x - Math.floor(x) + 8).toLong()
        check(firstMatches == exactPandigital(approximate)) { "k=$k 精确首部 $first 与近似 $approximate 的判定不一致" }
        if (firstMatches) {
            check(firstLast == 541 && firstLead == 2749)
            println("锚点核对通过：首个末九位全数字 k = $firstLast，首个首九位全数字 k = $firstLead")
            println("答案 k = $k：F_k 共 $digits 位，首九位 = $first，末九位 = $last")
            return k.toLong()
        }
    }
}

fun main() { println(solveBruteForce()) }
