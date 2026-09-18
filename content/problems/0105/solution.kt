/**
 * Project Euler 105 — Special Subset Sums: Testing（特殊和集：判定）
 *
 * 思路：把两条规则拆成可线性检验的形式。
 * 规则二（势大者和大）：集合排序后 a₁<…<a_n，「任意 k+1 元子集之和 > 任意 k 元子集之和」
 * 等价于只比两个极端——最小 (k+1) 元子集（前 k+1 小）与最大 k 元子集（后 k 大）：
 *   a₁+…+a_{k+1} > a_{n-k+1}+…+a_n,  k = 1..n−1。
 * 规则一（不相交子集和不等）：只需看等势子集——势不同已被规则二排除。进一步，若同势的两个
 * 不同子集 X,Y 满足 S(X)=S(Y)，去掉公共部分就得到一对不相交的同势等和子集，故规则一等价于
 *   「同一势的所有子集和互不相同」。
 * 于是判定 = 对每个势 s 把子集和塞进位图（重复即失败），再检查 n−1 条形如「前缀和 > 后缀和」的不等式。
 * 子集和用 lowbit 递推 S(mask) = S(mask∖lowbit) + a[lsb]，整套判定是 2ⁿ 次加法 + 位图读写。
 *
 * 复杂度：每个集合 O(2ⁿ) 时间与空间（n ≤ 12 → 4096），100 个集合合计约 1.2×10⁵ 次加法，
 * 微秒到亚毫秒量级；答案（各特殊集之和的总和）约 7×10⁴，Long 足够，无需 BigInteger，
 * 也不做任何位数判断或浮点比较。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import java.io.File

/** 数据文件定位：不同工作目录下依次尝试候选路径。 */
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

/** 规则二：对每个 k，最小 (k+1) 元子集和 > 最大 k 元子集和（升序集合上等价于全部情形）。 */
fun satisfiesSizeRule(sorted: IntArray): Boolean {
    val n = sorted.size
    val prefix = LongArray(n + 1)
    for (i in 1..n) prefix[i] = prefix[i - 1] + sorted[i - 1]
    for (k in 1 until n) {
        val smallestPlusOne = prefix[k + 1]                       // a₁+…+a_{k+1}
        val largestK = prefix[n] - prefix[n - k]                  // a_{n−k+1}+…+a_n
        if (smallestPlusOne <= largestK) return false
    }
    return true
}

/** 规则一：同一势的所有子集和互不相同（位图按势分桶，见即失败）。 */
fun satisfiesDistinctSumRule(sorted: IntArray): Boolean {
    val n = sorted.size
    val total = 1 shl n
    val sum = LongArray(total)
    val popcount = IntArray(total)
    val seen = Array(n + 1) { BooleanArray(sorted.sum() + 1) }
    for (mask in 1 until total) {
        val lowbit = mask and -mask
        val index = lowbit.countTrailingZeroBits()
        val rest = mask xor lowbit
        sum[mask] = sum[rest] + sorted[index]
        popcount[mask] = popcount[rest] + 1
        val size = popcount[mask]
        val value = sum[mask].toInt()
        if (seen[size][value]) return false
        seen[size][value] = true
    }
    return true
}

fun isSpecialSumSet(values: IntArray): Boolean {
    val sorted = values.sortedArray()
    if (sorted.distinct().size != sorted.size) return false       // 集合要求元素互异
    return satisfiesSizeRule(sorted) && satisfiesDistinctSumRule(sorted)
}

fun solve(): Long = findDataFile().readLines()
    .filter { it.isNotBlank() }
    .map { line -> line.split(',').map(String::trim).map(String::toInt).toIntArray() }
    .filter { isSpecialSumSet(it) }
    .sumOf { it.sum().toLong() }

fun main() {
    // 题面给出的两个锚点：第一个不满足规则一，第二个满足两规则且 S(A) = 1286
    val notSpecial = intArrayOf(81, 88, 75, 42, 87, 84, 86, 65)
    val special = intArrayOf(157, 150, 164, 119, 79, 159, 161, 139, 158)
    check(!isSpecialSumSet(notSpecial)) { "反例被判为特殊集" }
    check(isSpecialSumSet(special) && special.sum() == 1286) { "题面正例判定失败" }
    println(solve())
}
