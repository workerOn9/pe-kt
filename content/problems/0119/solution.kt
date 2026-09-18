/**
 * Project Euler 119 — Digit Power Sum（数字幂和）
 *
 * 思路：n 满足「n 等于它各位数字之和的某个幂」⇔ 存在 s ≥ 2、k ≥ 2 使 n = s^k
 * 且 digitsum(n) = s。正向枚举 n 不可行（a₃₀ 已达 2.5×10¹⁴），但可以反过来枚举
 * (s, k)：k ≥ 2 时 s^k ≤ limit 蕴含 s ≤ √limit，再加上「limit 以内任何数的数字和
 * ≤ 9·(limit 的位数)」这条上界，s 的枚举范围就被完全钉死了。对每个 (s, k) 只做一次
 * 数字和判定，收集全部命中值、去重排序，第 30 项即 a₃₀。
 *
 * 边界与范围：先取开区间上界 limit = 10¹⁵，小于它的数至多 15 位，数字和 ≤ 135，
 * 枚举 s 到 200 覆盖全部合法底数。实际枚举找到至少 30 项，才确认此上界足够。
 * 共检查 1421 个幂；连乘中间量 < limit·maxBase = 2×10¹⁷ < Long.MAX_VALUE。
 * main 另用 limit = 4×10¹⁴ 复核前 30 项，并检查末项数字和为 63、等于 63⁸。
 *
 * 复杂度：C 个候选、最多 D 位、H 个命中项时，O(CD + H log H) 时间、O(H) 空间。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 十进制各位数字之和：整数除法与取模，不经过字符串、不涉及位数判断。 */
fun digitSum(value: Long): Int {
    var rest = value
    var sum = 0
    while (rest > 0) {
        sum += (rest % 10L).toInt()
        rest /= 10L
    }
    return sum
}

/**
 * limit 以内、形如 s^k（s ≥ 2、k ≥ 2）且数字和恰为 s 的全部数，升序去重。
 * k ≥ 2 且 s ≥ 2 时 s^k ≥ 4；题面要求「至少两位才有数字和」，而两位以上的数
 * 不可能写成 s¹（那将要求 n = digitsum(n)，只有一位数成立），故这里天然满足约定。
 */
fun digitPowerSums(limit: Long, maxBase: Int): List<Long> {
    val found = sortedSetOf<Long>()
    for (base in 2..maxBase) {
        var power = base.toLong() * base
        while (power < limit) {
            if (digitSum(power) == base) found.add(power)
            power *= base
        }
    }
    return found.toList()
}

fun solve(): Long = digitPowerSums(1_000_000_000_000_000L, 200)[29]

fun main() {
    val terms = digitPowerSums(1_000_000_000_000_000L, 200)
    check(terms[1] == 512L) { "题面给出 a₂ = 512，实际 ${terms[1]}" }
    check(terms[9] == 614656L) { "题面给出 a₁₀ = 614656，实际 ${terms[9]}" }
    val tighter = digitPowerSums(400_000_000_000_000L, 200)
    check(tighter.size >= 30 && tighter.take(30) == terms.take(30)) {
        "前 30 项应当与 limit 的选取无关"
    }
    val thirty = terms[29]
    var powerOfSixtyThree = 1L
    repeat(8) { powerOfSixtyThree *= 63L }
    check(digitSum(thirty) == 63 && powerOfSixtyThree == thirty) {
        "末项旁证失败：a₃₀ = $thirty 应为 63⁸ 且数字和为 63"
    }
    println(solve())
}
