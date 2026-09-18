/**
 * Project Euler 125 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的遍历方向正好相反：不做「枚举区间求和再判回文」，而是
 * 先按位数「镜像」生成所有小于 limit 的回文数（取半段，把半段反转拼在后面），
 * 再对每个回文单独回答「它能否写成至少两项连续正整数平方之和」——用滑动窗口
 * 在 1², 2², 3², … 上推进：窗口和不足就右端扩张，超过就左端收缩，命中即检查项数。
 * 回文生成、求和方式与判定方向都换了（前缀和差值 vs 窗口推进），但「至少两项」
 * 这条题意解读是共享的：若理解错了，两条路径会一起错，所以另用 1000 以内的
 * 11 个回文（和 4164）与「单个平方数不计」的断言把语义钉死。
 *
 * 复杂度：设 H 为 limit 以内的回文数个数，时间 O(H·√limit)，空间 O(H)。
 * limit = 10⁸ 时 H = 19998；逐个回文重复扫描平方序列，比只扫描合法区间更费时。
 *
 * 题面样例（1000 以内 11 个回文、和 4164）与「单个平方数不算」的约定都写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 半段 → 回文：even 为真时把半段整体镜像（偶数位），否则去掉末位后镜像（奇数位）。 */
fun makePalindrome(half: Long, even: Boolean): Long {
    var source = if (even) half else half / 10
    var palindrome = half
    while (source > 0) {
        palindrome = palindrome * 10 + source % 10
        source /= 10
    }
    return palindrome
}

/** 所有小于 limit 的回文数（每个恰好生成一次）。 */
fun palindromesBelow(limit: Long): List<Long> {
    val palindromes = ArrayList<Long>()
    var half = 1L
    while (true) {
        val odd = makePalindrome(half, false)
        if (odd >= limit) break
        palindromes.add(odd)
        val even = makePalindrome(half, true)
        if (even < limit) palindromes.add(even)
        half++
    }
    return palindromes
}

/** 滑动窗口判定：target 是否等于至少 minTerms 个连续正整数平方之和。 */
fun isConsecutiveSquareSum(target: Long, minTerms: Int): Boolean {
    var low = 1L
    var high = 0L
    var sum = 0L
    while (true) {
        if (sum < target) {
            high++
            if (high * high > target) return false
            sum += high * high
        } else {
            if (sum == target && high - low + 1 >= minTerms) return true
            sum -= low * low
            low++
        }
    }
}

fun solveBruteForce(limit: Long = 100_000_000L, minTerms: Int = 2): Long =
    palindromesBelow(limit).filter { isConsecutiveSquareSum(it, minTerms) }.sum()

fun main() {
    val sample = palindromesBelow(1000L).filter { isConsecutiveSquareSum(it, 2) }
    println("1000 以内：${sample.size} 个回文 = $sample，和 = ${sample.sum()}")
    check(sample.size == 11 && sample.sum() == 4164L) { "题面样例自检失败：$sample" }

    // 「单个平方数不算连续平方之和」：4 = 2²、9 = 3²、121 = 11² 都不应被计入
    for (single in listOf(4L, 9L, 121L)) {
        check(!isConsecutiveSquareSum(single, 2)) { "$single 不应算作连续平方之和" }
    }
    check(isConsecutiveSquareSum(5L, 2)) { "5 = 1² + 2² 应被计入" }

    println(solveBruteForce())
}
