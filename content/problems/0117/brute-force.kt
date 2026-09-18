/**
 * Project Euler 117 — 暴力解 / 独立复核（教学对比用）
 *
 * solution.kt 用「按最右端砖分类」的四步递推（四那契数）O(n) 求出答案。本文件走两条与之
 * 独立的路径：
 *
 *  1) 字面穷举：从左到右逐格决策——放一块灰砖（长 1）或一块彩色砖（红 2/绿 3/蓝 4），
 *     把能恰好铺满一行的「砖序」一个个数出来。它数的是可见的排列本身，不含任何递推或
 *     组合公式。代价是搜索树的叶子数与铺法数同量级（长度 50 时约 10¹⁴ 种），本机不可能
 *     穷举完，因此穷举只用来在 n ≤ 12 的整个区间上逐点对照递推结果。
 *  2) 多重集排列闭式：设一行里灰/红/绿/蓝砖各有 a、b、c、d 块，则 a + 2b + 3c + 4d = n，
 *     而这一组块数对应的铺法数就是多组组合数 (a+b+c+d)! / (a!·b!·c!·d!)——先把 a+b+c+d 个
 *     位置分给四种砖，砖的排列顺序即铺法。对所有非负解求和。阶乘用 BigInteger 精确算，
 *     与递推路径的数值表示完全独立。
 *
 * 题面长度 5 的样例（15）在 main 里作自检锚点，同时兜住「闭式写错」与「递推写错」两类失误。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

/** 穷举所有恰好铺满的砖序（字面暴力法），参数是当前已铺长度。 */
fun countByEnumeration(rowLength: Int, position: Int): Long {
    if (position == rowLength) return 1L
    var count = countByEnumeration(rowLength, position + 1)              // 放一块灰砖
    for (colourLength in 2..4) {                                          // 放一块彩色砖
        if (position + colourLength <= rowLength) {
            count += countByEnumeration(rowLength, position + colourLength)
        }
    }
    return count
}

/** solution.kt 的递推，仅用于在 n ≤ 12 区间与穷举逐点对照。 */
fun waysByRecurrence(rowLength: Int): Long {
    val ways = LongArray(rowLength + 1)
    ways[0] = 1
    for (i in 1..rowLength) {
        var total = 0L
        for (length in 1..4) if (i >= length) total += ways[i - length]
        ways[i] = total
    }
    return ways[rowLength]
}

/** 多重集排列闭式：Σ_{(a,b,c,d): a+2b+3c+4d=n} (a+b+c+d)! / (a!·b!·c!·d!)。 */
fun countByMultinomials(rowLength: Int): BigInteger {
    var total = BigInteger.ZERO
    for (blue in 0..rowLength / 4) {
        for (green in 0..(rowLength - 4 * blue) / 3) {
            for (red in 0..(rowLength - 4 * blue - 3 * green) / 2) {
                val grey = rowLength - 4 * blue - 3 * green - 2 * red
                val slots = grey + red + green + blue
                total += factorial(slots) /
                    (factorial(grey) * factorial(red) * factorial(green) * factorial(blue))
            }
        }
    }
    return total
}

private val factorialCache = HashMap<Int, BigInteger>()

private fun factorial(value: Int): BigInteger =
    factorialCache.getOrPut(value) {
        var result = BigInteger.ONE
        for (i in 2..value) result *= BigInteger.valueOf(i.toLong())
        result
    }

fun solveBruteForce(): Long = countByMultinomials(50).longValueExact()

fun main() {
    // 自检锚点：题面给出长度 5 时共 15 种铺法
    val sampleEnumerated = countByEnumeration(5, 0)
    val sampleRecurrence = waysByRecurrence(5)
    val sampleClosed = countByMultinomials(5).longValueExact()
    println("长度 5：穷举 $sampleEnumerated、递推 $sampleRecurrence、闭式 $sampleClosed（题面 15）")
    check(sampleEnumerated == 15L && sampleRecurrence == 15L && sampleClosed == 15L) {
        "题面样例自检失败：$sampleEnumerated / $sampleRecurrence / $sampleClosed"
    }

    // 穷举 vs 递推 vs 闭式：n = 1..12 整个区间逐点对照（n = 12 有 2872 条铺法，属全量穷举）
    var checked = 0
    for (n in 1..12) {
        val brute = countByEnumeration(n, 0)
        val recurrence = waysByRecurrence(n)
        val closed = countByMultinomials(n).longValueExact()
        check(brute == recurrence && recurrence == closed) {
            "n = $n：穷举 $brute、递推 $recurrence、闭式 $closed 不一致"
        }
        checked++
    }
    println("三条路径在 n = 1..$checked 上完全一致")

    println("长度 50 的多重集排列闭式 = ${countByMultinomials(50)}")
    println("solveBruteForce() = ${solveBruteForce()}")
}
