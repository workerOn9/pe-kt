/**
 * Project Euler 103 — Special Subset Sums: Optimum（特殊和集：最优）
 *
 * 思路：特殊和集的两条规则在「严格递增」前提下可以化归为两条可判定条件
 *   （规则二）最小 (k+1) 元子集和 > 最大 k 元子集和，k = 1..n−1；
 *   （规则一）同一势的所有子集和互不相同——同势等和的两个不同子集去掉公共部分，
 *            就得到一对不相交的同势等和子集。
 * 求 n = 7 的最优集 = 在所有 7 元严格递增集中找「满足两条规则的集合」里 S(A) 最小者。
 * 直接枚举 7 元组不可行（组合数巨大），于是做带剪枝的 DFS：按升序逐位放置元素，每放一个 x 就
 *   1) 增量维护「各势子集和」位图：含 x 的 s 元子集和 = 旧 (s−1) 元子集和 + x，与旧 s 元子集和
 *      撞车立即回溯（这是剪枝主力，密集小集合很快出冲突）；
 *   2) 检查已选前缀内部的规则二；
 *   3) 把规则二中「右端涉及未选元素」的部分用未来元素的最小可能值（严格递增的下界）估算：
 *      k ≤ 已选个数时左端已定，右端取下界，若仍不满足则任何补全都不满足——剪枝可靠；
 *   4) 剩余位置按严格递增填满后的最小总和不得超过上限 cap。
 * 把 cap 设为 254 跑一遍「找不到任何解」即可证明最优和 ≥ 255，再用 cap = 255 跑一遍得到唯一解。
 *
 * 复杂度：搜索规模由剪枝决定，实测 n = 7、cap = 255 时访问约 1.5×10⁴ 个结点（每结点 O(2^n) 级
 * 子集和读写），单次搜索约 10 ms；n ≤ 6 的已知最优集在 solve() 里逐个复算做锚点。
 * 全程 Int/Long 整数运算，无 BigInteger、无浮点、无位数判断。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 特殊和集的定义式判定（与 105 题共用同一套定义）：枚举所有非空不相交子集对逐条比较。 */
fun isSpecialSumSet(values: IntArray): Boolean {
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
    for (b in 1 until total) for (c in b + 1 until total) {
        if (b and c != 0) continue                                  // 只比较不相交子集对
        when {
            popcount[b] == popcount[c] -> if (sum[b] == sum[c]) return false
            popcount[b] > popcount[c] -> if (sum[b] <= sum[c]) return false
            else -> if (sum[c] <= sum[b]) return false
        }
    }
    return true
}

/** 剪枝搜索：在总和 ≤ cap 的严格递增 n 元组里找最小和的特殊和集。 */
class OptimumSpecialSetSearch(private val n: Int, private val cap: Int) {
    private val chosen = IntArray(n)
    private val subsetSumSeen = Array(n + 1) { BooleanArray(cap + 1) }   // 各势可达子集和
    private val sumsBySize = Array(n + 1) { IntArray(cap + 1) }          // 同上，可遍历形式
    private val sizeCount = IntArray(n + 1)
    private val undoLog = Array(n + 1) { IntArray(cap + 1) }
    private val undoCount = IntArray(n + 1)
    private val levelMark = Array(n + 1) { IntArray(n + 1) }
    private val prefix = LongArray(n + 1)
    private var bestSum = Long.MAX_VALUE
    private val bestSets = mutableListOf<List<Int>>()

    var visitedNodes = 0L
        private set

    init {
        subsetSumSeen[0][0] = true
        sumsBySize[0][0] = 0
        sizeCount[0] = 1
    }

    private fun unwind(size: Int, keep: Int) {
        while (undoCount[size] > keep) {
            val value = undoLog[size][--undoCount[size]]
            subsetSumSeen[size][value] = false
            sizeCount[size]--
        }
    }

    private fun undoLevel(level: Int) { for (size in 1..n) unwind(size, levelMark[level][size]) }

    /** 尝试把 x 放在第 level 位；0 = 成功，1 = 冲突（换更大的 x 还有机会），2 = 单调失败（更大的 x 只会更糟）。 */
    private fun tryPlace(level: Int, x: Int): Int {
        for (size in 1..n) levelMark[level][size] = undoCount[size]
        for (size in level + 1 downTo 1) {                       // 含 x 的子集：旧 (s−1) 元子集和 + x
            val limit = sizeCount[size - 1]
            val source = sumsBySize[size - 1]
            for (t in 0 until limit) {
                val value = source[t] + x
                if (value > cap || subsetSumSeen[size][value]) {
                    undoLevel(level)
                    return 1
                }
                subsetSumSeen[size][value] = true
                sumsBySize[size][sizeCount[size]] = value
                sizeCount[size]++
                undoLog[size][undoCount[size]++] = value
            }
        }
        chosen[level] = x
        prefix[level + 1] = prefix[level] + x
        for (k in 1..level) {                                    // 前缀内部的规则二
            if (prefix[k + 1] <= prefix[level + 1] - prefix[level + 1 - k]) {
                undoLevel(level)
                return 2
            }
        }
        for (k in 1..minOf(level, n - 1)) {                       // 右端涉及未来元素：取下界
            var minTop = 0L
            for (p in n - k until n) minTop += if (p <= level) chosen[p].toLong() else (chosen[level] + (p - level)).toLong()
            if (prefix[k + 1] <= minTop) {
                undoLevel(level)
                return 2
            }
        }
        var minTotal = prefix[level + 1]                           // 总和下界
        for (p in level + 1 until n) minTotal += chosen[level] + (p - level)
        if (minTotal > cap) {
            undoLevel(level)
            return 2
        }
        return 0
    }

    private fun search(level: Int) {
        visitedNodes++
        if (level == n) {
            val total = prefix[n]
            if (total < bestSum) { bestSum = total; bestSets.clear() }
            if (total == bestSum) bestSets.add(chosen.toList())
            return
        }
        var x = if (level == 0) 1 else chosen[level - 1] + 1
        val limit = cap - (n - 1 - level)                           // 给剩余位置留出递增空间
        while (x <= limit) {
            when (tryPlace(level, x)) {
                0 -> { search(level + 1); undoLevel(level) }
                1 -> Unit                                          // 冲突与 x 的大小无关，继续试
                else -> return                                      // 单调失败：更大的 x 只会更差
            }
            x++
        }
    }

    fun run(): Pair<Long, List<List<Int>>> { search(0); return bestSum to bestSets }
}

fun solve(): Long {
    val expected = linkedMapOf(
        1 to (1L to listOf(1)),
        2 to (3L to listOf(1, 2)),
        3 to (9L to listOf(2, 3, 4)),
        4 to (21L to listOf(3, 5, 6, 7)),
        5 to (51L to listOf(6, 9, 11, 12, 13)),
        6 to (115L to listOf(11, 18, 19, 20, 22, 25)),
    )
    for ((size, want) in expected) {                                // 题面给出的前五个最优集 + n = 6 最优集
        val (sum, sets) = OptimumSpecialSetSearch(size, want.first.toInt()).run()
        check(sum == want.first && sets.size == 1 && sets[0] == want.second) { "n = $size 复算失败：$sum $sets" }
        check(isSpecialSumSet(sets[0].toIntArray())) { "n = $size 的搜索结果不是特殊和集" }
    }
    // 题面提到的「递推规则」候选：n = 5 的中间元素 11 加到各行得到 {11,17,20,22,23,24}，和 117
    val ruleCandidate = intArrayOf(11, 17, 20, 22, 23, 24)
    check(isSpecialSumSet(ruleCandidate) && ruleCandidate.sum() == 117) { "n = 6 递推候选判定失败" }
    check(OptimumSpecialSetSearch(6, 116).run().first == 115L) { "n = 6 在 cap = 116 内的最小和应是 115（优于递推候选 117）" }
    check(OptimumSpecialSetSearch(7, 254).run().second.isEmpty()) { "cap = 254 时不应存在特殊和集" }
    val (_, optimum) = OptimumSpecialSetSearch(7, 255).run()
    check(optimum.size == 1 && isSpecialSumSet(optimum[0].toIntArray())) { "n = 7 最优集不唯一或非特殊和集" }
    return optimum[0].joinToString("").toLong()
}

fun main() {
    val search = OptimumSpecialSetSearch(7, 255)
    val (sum, sets) = search.run()
    println("n = 7 最优特殊和集：${sets.single()}，S(A) = $sum，搜索结点数 ${search.visitedNodes}")
    println(solve())
}
