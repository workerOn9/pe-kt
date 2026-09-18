/**
 * Project Euler 122 — 暴力解 / 独立复核（教学对比用）
 *
 * solution.kt 用「迭代加深 DFS + 翻倍剪枝」自顶向下找最短链。本文件反其道而行：
 * **自底向上做广度优先搜索**，一层一层地枚举「m 次乘法后可能得到哪些指数集合」。
 *
 *   - 状态是一个递增的指数集合（链的元素集合），初始只有 {1}；
 *   - 一层扩展：把集合里任意两个元素的和作为新指数加进来（和必须大于当前最大值、且不超过目标）；
 *   - 第 L 层出现 target 时，L 就是最短链长——BFS 按层推进，天然保证首次出现即最优。
 *   - 剪枝：设二进制法给出的上界为 U，当前层为 L，则这条链再走 U−L 步最多把最大值翻倍
 *     $U-L$ 次，若「最大值 · 2^{U−L} < target」就丢弃该状态（它不可能在上界内到达 target）。
 *     U 本身是合法链长（逐步平方 + 按位补齐），所以只搜到第 U 层就够。
 *   - 同一层的所有集合大小相同，故「集合包含」的支配关系退化为相等，用 BitSet 去重即可。
 *
 * 去重的意义在于：链的后续扩展只取决于集合本身，与元素加入顺序无关。
 * 本题与 solution.kt 的机制完全不同：一个是深度优先 + 上界剪枝，一个是逐层枚举可达集合，
 * 两者对全部 k ≤ 200 给出同一组 m(k)，答案自然一致。
 *
 * 复杂度：单值约为「不超过 U 层的可达集合数」，随目标增大而迅速增长；空间 O(层内状态数)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.util.BitSet

/** 二进制法（逐步平方 + 按位补齐）给出的合法链长上界。 */
fun binaryUpperBound(target: Int): Int =
    if (target == 1) 0 else (31 - Integer.numberOfLeadingZeros(target)) + Integer.bitCount(target) - 1

private fun keyOf(values: IntArray): BitSet {
    val key = BitSet(values[values.size - 1] + 1)
    for (value in values) key.set(value)
    return key
}

/** 逐层 BFS 求最短加法链长度。 */
fun minMultiplicationsByBfs(target: Int): Int {
    if (target == 1) return 0
    val upper = binaryUpperBound(target)
    var current = listOf(intArrayOf(1))
    for (level in 1..upper) {
        val remaining = upper - level
        val seen = HashSet<BitSet>()
        val next = ArrayList<IntArray>()
        for (values in current) {
            val last = values[values.size - 1]
            for (i in values.indices) {
                for (j in i until values.size) {
                    val candidate = values[i] + values[j]
                    if (candidate <= last || candidate > target) continue
                    if (candidate.toLong() shl remaining < target) continue
                    if (candidate == target) return level
                    val extended = values.copyOf(values.size + 1)
                    extended[values.size] = candidate
                    if (seen.add(keyOf(extended))) next.add(extended)
                }
            }
        }
        current = next
    }
    error("目标 $target 在上界 $upper 内未被 BFS 找到")
}

fun solveBruteForce(): Long = (1..200).sumOf { minMultiplicationsByBfs(it).toLong() }

fun main() {
    check(minMultiplicationsByBfs(15) == 5) { "题面锚点：m(15) 应为 5" }
    check(minMultiplicationsByBfs(1) == 0 && minMultiplicationsByBfs(2) == 1) { "小值锚点失败" }
    check(minMultiplicationsByBfs(191) == 11) { "m(191) 应为 11，实际 ${minMultiplicationsByBfs(191)}" }
    println("solveBruteForce() = ${solveBruteForce()}")
}
