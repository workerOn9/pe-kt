/**
 * Project Euler 111 — 暴力解（教学对比用）
 *
 * 思路：先分配数字出现次数，再枚举多重集的所有不同排列。不剪除末位和数位和，
 * 素性用逐奇数试除到平方根，与优化解的逐位生成及 Miller–Rabin 完全分离。
 * 另穷举全部四位整数，按定义统计 M/N/S，对照题面完整表格。
 * 复杂度：设 C 为候选数、V 为数值上界，时间 O(C·(n + √V))，空间 O(n)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private fun isPrimeBruteForce(candidate: Long): Boolean {
    if (candidate < 2) return false
    if (candidate % 2 == 0L) return candidate == 2L
    var divisor = 3L
    while (divisor * divisor <= candidate) {
        if (candidate % divisor == 0L) return false
        divisor += 2
    }
    return true
}

/** 数字 value 中数字 digit 出现的次数（算术拆位，不经过字符串）。 */
private fun countDigit(value: Int, digit: Int): Int {
    var rest = value
    var count = 0
    while (rest > 0) {
        if (rest % 10 == digit) count++
        rest /= 10
    }
    return count
}

/** 按定义穷举四位素数：返回每个 d 的 [M, N, S]（长度 10 的数组）。 */
fun fourDigitTable(): Array<LongArray> {
    val maxCount = IntArray(10)
    val stats = Array(10) { LongArray(2) }           // [个数, 和]
    for (value in 1000..9999) {
        if (!isPrimeBruteForce(value.toLong())) continue
        for (d in 0..9) {
            val count = countDigit(value, d)
            when {
                count > maxCount[d] -> {             // 新的最大值：计数与和从头来
                    maxCount[d] = count
                    stats[d][0] = 1L
                    stats[d][1] = value.toLong()
                }
                count == maxCount[d] -> {
                    stats[d][0]++
                    stats[d][1] += value.toLong()
                }
            }
        }
    }
    return Array(10) { d -> longArrayOf(maxCount[d].toLong(), stats[d][0], stats[d][1]) }
}

/** 枚举 counts 指定的数字多重集的所有不同排列（首位非零），逐个交给 visitor。 */
private fun permutationsOfCounts(counts: IntArray, digitsLeft: Int, value: Long, visitor: (Long) -> Unit) {
    if (digitsLeft == 0) {
        visitor(value)
        return
    }
    for (digit in 0..9) {
        if (counts[digit] == 0) continue
        if (value == 0L && digit == 0) continue       // 首位不能是 0
        counts[digit]--
        permutationsOfCounts(counts, digitsLeft - 1, value * 10 + digit, visitor)
        counts[digit]++
    }
}

/** 把剩下的 fillers 个位置分配给除 d 外的 9 个数字，对每个次数向量枚举排列并判定素性。 */
private fun distributeFillers(
    n: Int,
    d: Int,
    digit: Int,
    fillers: Int,
    counts: IntArray,
    tally: LongArray,
) {
    if (digit == 10) {
        if (fillers == 0) {
            permutationsOfCounts(counts, n, 0L) { candidate ->
                if (isPrimeBruteForce(candidate)) {
                    tally[0]++
                    tally[1] += candidate
                }
            }
        }
        return
    }
    if (digit == d) {
        distributeFillers(n, d, digit + 1, fillers, counts, tally)
        return
    }
    for (used in 0..fillers) {
        counts[digit] = used
        distributeFillers(n, d, digit + 1, fillers - used, counts, tally)
    }
    counts[digit] = 0
}

/** 返回 [M(n, d), N(n, d), S(n, d)]：自 m = n 下降，取第一个有素数的层。 */
fun statsByDigitCounts(n: Int, d: Int): LongArray {
    for (m in n downTo 1) {
        val counts = IntArray(10)
        counts[d] = m
        val tally = LongArray(2)
        distributeFillers(n, d, 0, n - m, counts, tally)
        if (tally[0] > 0L) return longArrayOf(m.toLong(), tally[0], tally[1])
    }
    return longArrayOf(0L, 0L, 0L)
}

fun solveBruteForce(): Long = (0..9).sumOf { statsByDigitCounts(10, it)[2] }

fun main() {
    // 路径 1：按定义穷举四位素数，复现题面表格
    val expectedM = longArrayOf(2, 3, 3, 3, 3, 3, 3, 3, 3, 3)
    val expectedN = longArrayOf(13, 9, 1, 12, 2, 1, 1, 9, 1, 7)
    val expectedS = longArrayOf(67061, 22275, 2221, 46214, 8888, 5557, 6661, 57863, 8887, 48073)
    val table = fourDigitTable()
    var fourDigitTotal = 0L
    for (d in 0..9) {
        println("四位 d = $d: M = ${table[d][0]}, N = ${table[d][1]}, S = ${table[d][2]}")
        check(table[d][0] == expectedM[d] && table[d][1] == expectedN[d] && table[d][2] == expectedS[d]) {
            "d = $d 与题面表格不符"
        }
        fourDigitTotal += table[d][2]
    }
    check(fourDigitTotal == 273700L) { "四位总和应为 273700，实际 $fourDigitTotal" }
    println("四位素数表与题面完全一致，总和 = $fourDigitTotal")

    // 路径 2：换一种枚举结构算 n = 10
    for (d in 0..9) {
        val stats = statsByDigitCounts(10, d)
        println("d = $d: M = ${stats[0]}, N = ${stats[1]}, S = ${stats[2]}")
    }
    println(solveBruteForce())
}
