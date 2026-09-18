/**
 * Project Euler 122 — Efficient Exponentiation（高效幂运算）
 *
 * 思路：计算 n^k 的最少乘法次数就是指数 k 的「最短加法链」长度。一条长为 m 的加法链是
 *     1 = a_0 < a_1 < … < a_m = k，且每个 a_i = a_j + a_l（j, l < i）——一次乘法把两个
 *     已知的幂相乘，指数相加。求 m(k) 即求最短链。
 * 用迭代加深 DFS：从链长上限 limit = 1, 2, 3, … 逐次尝试，链保持严格递增，找得到就返回，
 * 因此第一次成功时的 limit 就是最优值。两个剪枝：
 *   1) 翻倍上界：链尾为 last、还剩 r 次乘法时，最大只能到 last·2^r，若仍小于 k 就回溯；
 *   2) 候选值按 chain[i] + chain[j] 从大到小枚举，一旦候选值 v 满足 v·2^(r−1) < k，
 *      后续更小的候选（i、j 继续减小）只会更差，直接跳出。
 * 严格递增的假设是安全的：任何加法链都能整理成同长的递增链（每次把新元素插到不大于它的
 * 位置之前，所有元素仍是两个更早元素之和）。
 * 题面锚点：m(15) = 5（1 → 2 → 3 → 6 → 12 → 15 五步），在 main 里自检。
 *
 * 复杂度：单值约 O(C(k))，C(k) 为剪枝后搜索的链数；k ≤ 200 时全部 200 个值合计
 * 在毫秒级完成。空间 O(链长)。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 计算 n^target 所需的最少乘法次数 m(target)。 */
fun minMultiplications(target: Int): Int {
    if (target == 1) return 0
    val chain = IntArray(32)
    chain[0] = 1

    fun search(size: Int, limit: Int): Boolean {
        val last = chain[size - 1]
        if (last == target) return true
        val remaining = limit - (size - 1)                 // 还允许做几次乘法
        if (remaining <= 0) return false
        if (last.toLong() shl remaining < target) return false   // 每步至多翻倍
        for (i in size - 1 downTo 0) {
            // 即使下一步取 chain[i] + chain[i]，其后全翻倍也到不了 target 就可以收手
            if ((chain[i] + chain[i]).toLong() shl (remaining - 1) < target) break
            for (j in i downTo 0) {
                val candidate = chain[i] + chain[j]
                if (candidate <= last || candidate > target) continue
                if (candidate.toLong() shl (remaining - 1) < target) break
                chain[size] = candidate
                if (search(size + 1, limit)) return true
            }
        }
        return false
    }

    var limit = 1
    while (!search(1, limit)) limit++
    return limit
}

fun solve(): Long = (1..200).sumOf { minMultiplications(it).toLong() }

fun main() {
    check(minMultiplications(15) == 5) { "题面锚点：m(15) 应为 5，实际 ${minMultiplications(15)}" }
    check(minMultiplications(1) == 0 && minMultiplications(2) == 1) { "小值锚点失败" }
    check(minMultiplications(191) == 11) { "m(191) 应为 11，实际 ${minMultiplications(191)}" }
    println(solve())
}
