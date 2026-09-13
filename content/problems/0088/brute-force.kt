/**
 * Project Euler 088 — Product-sum Numbers（暴力解）
 *
 * 暴力思路：不做「按因子分解滚出 k」的正向搜索，而是逐个数枚举：对每个 N ≤ 2·12000
 * 递归列出 N 的全部乘法分拆（因子 ≥ 2、非降序、至少两个因子），由因子个数 m 与因子和 s
 * 反推它对应的 k = m + (N − s)，再把 N 记入 best[k] 的最小值。等价但比正解慢得多
 * （每个 N 都要做一遍因数试除递归）。
 *
 * 复杂度：O(Σ_{N≤2K} √N · 分拆数)，实测约 0.3 s 量级。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 */

private const val b088KMax = 12000
private const val b088Limit = 2 * b088KMax

/** 枚举 rest 的乘法分拆：已选 m 个因子（和 s，全部 ≥ minF），整数的最终乘积为 n。 */
private fun b088Split(rest: Int, minF: Int, m: Int, s: Int, n: Int, best: IntArray) {
    if (m >= 1) {
        val m2 = m + 1
        val s2 = s + rest
        val k = m2 + (n - s2)
        if (k in 2..b088KMax && n < best[k]) best[k] = n
    }
    var f = minF
    while (f * f <= rest) {
        if (rest % f == 0) b088Split(rest / f, f, m + 1, s + f, n, best)
        f++
    }
}

fun solveBruteForce(): Long {
    val best = IntArray(b088KMax + 1) { Int.MAX_VALUE }
    for (n in 4..b088Limit) b088Split(n, 2, 0, 0, n, best)
    var sum = 0L
    val seen = HashSet<Int>()
    for (k in 2..b088KMax) if (best[k] != Int.MAX_VALUE && seen.add(best[k])) sum += best[k]
    return sum
}

fun main() {
    println(solveBruteForce())
}
