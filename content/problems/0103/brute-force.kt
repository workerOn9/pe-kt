/**
 * Project Euler 103 — 暴力解（教学对比用）
 *
 * 与 solution.kt 结论相同，但搜索结构完全不同：不做「增量维护各势子集和位图」那套剪枝，
 * 而是把枚举空间压到能接受的范围后，对每个候选集合**按定义**逐对检验。
 *   枚举：按升序填 7 个位置，只用两条「显然必要」的约束剪枝——
 *     (a) 总和 ≤ cap（剩余位置按严格递增填满取下界）；
 *     (b) 规则二的极值形式：最小 (k+1) 元子集和 > 最大 k 元子集和，k 取到已选长度为止
 *         （未选位置用「至少比前一个大 1」的下界代入）。
 *   判定：写满 7 位后，用 3ⁿ 三态掩码枚举所有非空不相交子集对，逐条比较 S(B) 与 S(C)
 *         ——势大者必须和大、势同者必须和不等。这一步没有任何化简、没有位图、没有增量状态。
 * cap = 254 时叶子约 1.2×10⁵、判定约 2.6×10⁸ 次比较；两次穷举（cap = 254 与 255）合计实测约
 * 0.23 s。solution.kt 的同规模搜索只需 1.5×10⁴ 个结点、约 20 ms，差距全部来自剪枝时机
 * （增量冲突检测在放第 3、4 个元素时就砍掉整棵子树，暴力解必须填满 7 位才能开始检验）。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 定义式判定：任意两个非空不相交子集，势大者和必须更大，势相同者和必须不等（与 105 同一套定义）。 */
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
    var assignments = 1
    repeat(n) { assignments *= 3 }
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
        when {
            sizeB == sizeC -> if (sum[b] == sum[c]) return false
            sizeB > sizeC -> if (sum[b] <= sum[c]) return false
            else -> if (sum[c] <= sum[b]) return false
        }
    }
    return true
}

/** 穷举「总和 ≤ cap 的严格递增 n 元组」中所有特殊和集，返回 (最小和, 达到最小和的集合列表, 叶子数)。 */
class ExhaustiveOptimum(private val n: Int, private val cap: Int) {
    private val chosen = IntArray(n)
    private var bestSum = Long.MAX_VALUE
    private val bestSets = mutableListOf<List<Int>>()
    var leaves = 0L
        private set

    /** 已选前缀 + 未选位置的下界，能否满足规则二的极值条件（k ≤ filled 时左端已定）。 */
    private fun sizeRuleStillPossible(filled: Int): Boolean {
        for (k in 1..minOf(filled, n - 1)) {
            var low = 0L
            for (p in 0..k) low += chosen[p]                            // 最小 (k+1) 元子集和
            var high = 0L
            for (p in n - k until n) high += if (p <= filled) chosen[p] else chosen[filled] + (p - filled)
            if (low <= high) return false
        }
        return true
    }

    private fun search(level: Int, sumSoFar: Int) {
        if (level == n) {
            leaves++
            if (!isSpecialByDefinition(chosen)) return
            if (sumSoFar < bestSum) { bestSum = sumSoFar.toLong(); bestSets.clear() }
            if (sumSoFar.toLong() == bestSum) bestSets.add(chosen.toList())
            return
        }
        var x = if (level == 0) 1 else chosen[level - 1] + 1
        while (x <= cap - (n - 1 - level)) {
            chosen[level] = x
            var minTotal = sumSoFar + x
            for (p in level + 1 until n) minTotal += x + (p - level)
            if (minTotal > cap) return                                   // x 再大只会更糟
            if (sizeRuleStillPossible(level)) search(level + 1, sumSoFar + x)
            x++
        }
    }

    fun run(): Triple<Long, List<List<Int>>, Long> { search(0, 0); return Triple(bestSum, bestSets, leaves) }
}

fun solveBruteForce(): Long {
    check(ExhaustiveOptimum(7, 254).run().second.isEmpty()) { "cap = 254 不该有解" }
    val (sum, sets) = ExhaustiveOptimum(7, 255).run()
    check(sum == 255L && sets.size == 1) { "n = 7 最优集异常：$sum $sets" }
    return sets[0].joinToString("").toLong()
}

fun main() {
    val known = linkedMapOf(
        1 to (1 to listOf(1)),
        2 to (3 to listOf(1, 2)),
        3 to (9 to listOf(2, 3, 4)),
        4 to (21 to listOf(3, 5, 6, 7)),
        5 to (51 to listOf(6, 9, 11, 12, 13)),
        6 to (115 to listOf(11, 18, 19, 20, 22, 25)),
    )
    for ((size, want) in known) {
        val (sum, sets, leaves) = ExhaustiveOptimum(size, want.first).run()
        println("n = $size：最小和 = $sum，集合 = $sets，叶子 = $leaves（题面 $want）")
        check(sum == want.first.toLong() && sets == listOf(want.second)) { "n = $size 与题面不符" }
    }
    val (sum254, sets254, leaves254) = ExhaustiveOptimum(7, 254).run()
    println("cap = 254：叶子 = $leaves254，找到解 = ${sets254.size}（应为 0，即最优和 ≥ 255）")
    check(sets254.isEmpty())
    val (sum255, sets255, leaves255) = ExhaustiveOptimum(7, 255).run()
    println("cap = 255：叶子 = $leaves255，最小和 = $sum255，集合 = $sets255")
    println("集合串 = ${sets255[0].joinToString("")}")
    println(solveBruteForce())
}
