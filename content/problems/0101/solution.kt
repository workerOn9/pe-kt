/**
 * Project Euler 101 — Optimum Polynomial（最优多项式）
 *
 * 思路：前 k 项确定的「最优多项式」OP(k, n) 就是过点 (1,u₁)..(k,u_k) 的唯一天然
 * 次数 ≤ k−1 插值多项式，FIT = OP(k, k+1)。不必解方程组：等距节点下「设最高阶差分
 * 为常数」向前外推一次，得到的正是该插值多项式在下一点的值。于是边读入 u₁,u₂,…
 * 边维护差分三角的右斜边 R_j（以当前点收尾的第 j 阶前差分），读入第 i 项后的
 * 外推值就是 Σ_j R_j。新项 x 的更新自低阶向高阶进行：R′₀ = x，R′ⱼ = R′ⱼ₋₁ − Rⱼ₋₁。
 * 生成函数是十次的，11 个点即可唯一确定它，故 k ≥ 11 时 OP(k, k+1) = u_{k+1} 不再出错，
 * 只累加 k = 1..10。立方数列样例（FIT = 1, 15, 58，和 74）在 main 里做自检。
 *
 * 复杂度：O(K²) 时间、O(K) 空间（K = 10，共 55 次 64 位加减 + 12 次右斜边求和）。
 * 数值上界：u₁₂ = 5.7×10¹⁰、最大单项 OP(10,11) = 23772343751、答案 ≈ 3.7×10¹⁰，
 * 全部远小于 Long 上限 9.2×10¹⁸，因此无需 BigInteger，也不用浮点判等或数字符串长度。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 依次由前 1..n 项外推第 2..n+1 项，返回 n 个外推值（下标 i 对应 k = i+1）。 */
fun finiteDifferencePredictions(terms: LongArray): LongArray {
    val edge = LongArray(terms.size)          // edge[j] = 以当前点收尾的第 j 阶前差分 R_j
    return LongArray(terms.size) { index ->
        var difference = terms[index]
        for (order in 0 until index) {        // 旧值要先取走再覆盖
            val previous = edge[order]
            edge[order] = difference
            difference -= previous
        }
        edge[index] = difference
        edge.sum()                            // 右斜边之和 = 向前外推一项
    }
}

/** u_n = 1 − n + n² − n³ + … + n¹⁰，n = 1..count（对变量 −n 做 10 步 Horner）。 */
fun tenthDegreeTerms(count: Int): LongArray =
    LongArray(count) { index ->
        val n = index + 1L
        var value = 1L
        repeat(10) { value = 1L - n * value }
        value
    }

fun solve(): Long {
    val terms = tenthDegreeTerms(12)
    val predictions = finiteDifferencePredictions(terms)
    check(predictions[10] == terms[11]) { "k = 11 起应恢复真实项：${predictions[10]} != ${terms[11]}" }
    return predictions.take(10).sum()         // 只取 k = 1..10 的 FIT
}

fun main() {
    val cubes = LongArray(4) { val n = (it + 1).toLong(); n * n * n }
    val cubePredictions = finiteDifferencePredictions(cubes)
    check(cubePredictions.take(3).sum() == 74L && cubePredictions[3] == 125L) {
        "立方数列样例检查失败：${cubePredictions.toList()}"
    }
    println(solve())
}
