/**
 * Project Euler 105 — 暴力解（教学对比用）
 *
 * 与 solution.kt 同一个定义，但完全「照本宣科」：不把两条规则化简成前缀/后缀不等式，
 * 也不用「同势子集和互异」的等价形式，而是用 3ⁿ 进制的三态掩码枚举出每一对非空不相交
 * 子集 (B, C)，逐个比较 S(B) 与 S(C)——势大者必须更大，势相同者必须不等。
 * 每对子集的和用 lowbit 递推预先算好，判定部分是 3ⁿ 次枚举，没有哈希、没有位图。
 *
 * 与 solution.kt 的分工：solution.kt 走「化简后的必要条件」（O(2ⁿ) 位图去重 + n−1 条不等式），
 * 本文件走「定义逐条穷举」（O(3ⁿ) 对比较）。两者对同一批数据给出相同的特殊集集合，
 * 才说明化简过程没有丢掉或放宽任何条件。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.io.File

fun findDataFile(): File {
    val candidates = listOf(
        "content/problems/0105/sets.txt",
        "problems/0105/sets.txt",
        "../0105/sets.txt",
        "sets.txt",
        "0105/sets.txt",
    )
    return candidates.map(::File).firstOrNull { it.isFile }
        ?: error("找不到 sets.txt，当前目录：${File(".").absolutePath}")
}

private fun pow3(exponent: Int): Int {
    var result = 1
    repeat(exponent) { result *= 3 }
    return result
}

/** 按定义逐对比较：任意两个非空不相交子集，势大者和必更大，势同者和必不等。 */
fun isSpecialByDefinition(values: IntArray): Boolean {
    val n = values.size
    val total = 1 shl n
    val sum = LongArray(total)
    val popcount = IntArray(total)
    for (mask in 1 until total) {
        val lowbit = mask and -mask
        val rest = mask xor lowbit
        sum[mask] = sum[rest] + values[lowbit.countTrailingZeroBits()]
        popcount[mask] = popcount[rest] + 1
    }
    val assignments = pow3(n)
    for (code in 0 until assignments) {
        var b = 0
        var c = 0
        var rest = code
        for (index in 0 until n) {
            when (rest % 3) {
                1 -> b = b or (1 shl index)
                2 -> c = c or (1 shl index)
            }
            rest /= 3
        }
        if (b == 0 || c == 0) continue
        val sizeB = popcount[b]
        val sizeC = popcount[c]
        if (sizeB == sizeC) {
            if (sum[b] == sum[c]) return false
        } else if (sizeB > sizeC) {
            if (sum[b] <= sum[c]) return false
        } else {
            if (sum[c] <= sum[b]) return false
        }
    }
    return true
}

fun solveBruteForce(): Long = findDataFile().readLines()
    .filter { it.isNotBlank() }
    .map { line -> line.split(',').map(String::trim).map(String::toInt).toIntArray() }
    .filter { isSpecialByDefinition(it) }
    .sumOf { it.sum().toLong() }

fun main() {
    val notSpecial = intArrayOf(81, 88, 75, 42, 87, 84, 86, 65)
    val special = intArrayOf(157, 150, 164, 119, 79, 159, 161, 139, 158)
    check(!isSpecialByDefinition(notSpecial) && isSpecialByDefinition(special)) { "题面锚点判定失败" }

    val sets = findDataFile().readLines().filter { it.isNotBlank() }
        .map { line -> line.split(',').map(String::trim).map(String::toInt).toIntArray() }
    val specials = sets.filter { isSpecialByDefinition(it) }
    println("特殊集数量 = ${specials.size} / ${sets.size}")
    specials.forEachIndexed { i, set -> println("A${i + 1}: ${set.toList()} → S = ${set.sum()}") }
    println(solveBruteForce())
}
