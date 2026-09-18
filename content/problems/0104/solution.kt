/**
 * Project Euler 104 — Pandigital Fibonacci Ends（首尾全数字的斐波那契数）
 *
 * 思路：末九位用模 10^9 的斐波那契递推精确维护，只对末九位全数字的候选计算首九位。
 * 设 x=k log10 φ-log10 √5，首九位近似为 floor(10^(x-floor(x)+8))。
 * Binet 公式中的修正项按 φ^(-2k) 衰减，但 Double 舍入仍可能影响取整；
 * brute-force.kt 用完整 BigInteger 递推独立搜索，并逐候选检查两种首九位判定一致。
 * 数字用除法拆位和位掩码判定，不用字符串计数。
 * 复杂度：O(K) 时间、O(1) 空间，九位数检查的长度固定。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun pandigital(value: Long): Boolean {
    if (value !in 100_000_000L..999_999_999L) return false
    var rest = value
    var mask = 0
    repeat(9) {
        val digit = (rest % 10).toInt()
        val bit = 1 shl digit
        if (digit == 0 || mask and bit != 0) return false
        mask = mask or bit
        rest /= 10
    }
    return mask == 1022
}

fun leadingNine(k: Int): Long {
    val x = k * Math.log10((1 + Math.sqrt(5.0)) / 2) - Math.log10(5.0) / 2
    return Math.pow(10.0, x - Math.floor(x) + 8).toLong()
}

fun solve(): Long {
    check(!pandigital(12345678L) && pandigital(123456789L) && !pandigital(123456788L))
    check(pandigital(leadingNine(2749)))
    var previous = 1L
    var current = 1L
    var k = 2
    while (true) {
        val next = (previous + current) % 1_000_000_000L
        previous = current
        current = next
        k++
        if (pandigital(current) && pandigital(leadingNine(k))) return k.toLong()
    }
}

fun main() { println(solve()) }
