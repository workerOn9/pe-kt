/**
 * Project Euler 125 — Palindromic Sums（回文连续平方和）
 *
 * 思路：平方前缀和 P(k) = 1²+…+k² 给出区间和 P(b)−P(a−1)。末项 b² < N，
 * 因此前缀必须覆盖 b < √N，而不是只保留 P(b) < N 的前缀。
 * 随 b 递增，维护最小的合法左前缀 firstValid，只枚举和 < N、长度至少 2 的区间。
 * 算术反转判断回文，集合去重后求和。重复表示只计一次，起点必须为正整数。
 *
 * 复杂度：设 W 为合法区间数，时间 O(√N + W log N)，空间 O(√N + H)，
 * H 为命中的不同回文数。W = O(N^(2/3))，推导见 analysis.md。
 * N ≤ 10⁸ 时前缀和 < 3.34×10¹¹、答案 < 2×10¹²，统一用 Long。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 数字反转：12321 → 12321、120 → 21（不经过字符串）。 */
fun reverseDigits(value: Long): Long {
    var rest = value
    var reversed = 0L
    while (rest > 0) {
        reversed = reversed * 10 + rest % 10
        rest /= 10
    }
    return reversed
}

fun isPalindromic(value: Long): Boolean = value > 0 && reverseDigits(value) == value

/**
 * 平方前缀和 P(0..maxBase)，P(k) = 1² + … + k²。上界取「单个平方数仍小于 limit」：
 * 区间和 P(b) − P(a−1) ≥ b²，所以 b² ≥ limit 的区间必然全部超限，可以安全截断。
 */
fun squarePrefixSums(limit: Long): LongArray {
    val sums = ArrayList<Long>(10_000)
    sums.add(0L)
    var base = 1L
    while (base * base < limit) {
        sums.add(sums[sums.size - 1] + base * base)
        base++
    }
    return sums.toLongArray()
}

/** 小于 limit 的「回文 + 至少 minTerms 项连续正整数平方和」之和（同一数只计一次）。 */
fun solve(limit: Long = 100_000_000L, minTerms: Int = 2): Long {
    val prefix = squarePrefixSums(limit)
    val palindromes = HashSet<Long>()
    var firstValid = 0                               // 最小的 start，使 P(end) − P(start) < limit
    for (end in 1 until prefix.size) {
        while (firstValid < end && prefix[end] - prefix[firstValid] >= limit) firstValid++
        for (start in firstValid until end) {        // 区间是 (start+1)² … end²，共 end−start 项
            if (end - start < minTerms) break        // 项数随 start 递增而减少 → 可以 break
            val sum = prefix[end] - prefix[start]    // start ≥ firstValid ⇒ sum 必 < limit
            if (isPalindromic(sum)) palindromes.add(sum)
        }
    }
    return palindromes.sum()
}

fun verifySample() {
    check(solve(1000L) == 4164L)                     // 题面样例：1000 以内 11 个回文，和 4164
    check(solve(5L) == 0L && solve(6L) == 5L)        // 边界：5 = 1² + 2² 只在 limit > 5 时计入
    val prefix = squarePrefixSums(100_000_000L)
    check(prefix[12] - prefix[5] == 595L)            // 题面例子：595 = 6² + 7² + … + 12²
}

fun main() {
    verifySample()
    println(solve())
}
