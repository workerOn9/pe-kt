/**
 * Project Euler 088 — Product-sum Numbers
 *
 * 思路：若 N 有一组至少两项的因子分解 {f_1..f_m}（f_i ≥ 2，含重数），把它配上
 * N − Σf_i 个 1，就得到长度 k = m + (N − Σf_i) 的乘积和表示，乘积恰为 N；
 * 反之任何乘积和表示去掉全部 1 后正是这样的分解。所以「枚举所有因子分解」覆盖了全部候选。
 * 又因 {2, k} 加 k−2 个 1 给出 N = 2k，最小乘积和数必 ≤ 2k，搜索上限取 2·12000。
 * 实现用非降因子序的 DFS，途中直接把 N 记入 best[k] 取最小。
 *
 * 复杂度：搜索的因子分解个数远小于 O(2k log(2k))，实测 &lt; 5 ms。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private const val p088KMax = 12000
private const val p088Limit = 2 * p088KMax

private fun p088Search(p: Int, s: Int, m: Int, minFactor: Int, best: IntArray) {
    var f = minFactor
    while (f <= p088Limit / p) {
        val p2 = p * f
        val s2 = s + f
        val m2 = m + 1
        val k = m2 + (p2 - s2)
        if (k <= p088KMax && p2 < best[k]) best[k] = p2
        if (p2 * f <= p088Limit) p088Search(p2, s2, m2, f, best)
        f++
    }
}

fun solve(): Long {
    val best = IntArray(p088KMax + 1) { Int.MAX_VALUE }
    p088Search(1, 0, 0, 2, best)
    var sum = 0L
    val seen = HashSet<Int>()
    for (k in 2..p088KMax) if (best[k] != Int.MAX_VALUE && seen.add(best[k])) sum += best[k]
    return sum
}

fun main() {
    println(solve())
}
