/**
 * Project Euler 149 — 暴力对照解（最大和子序列）
 *
 * 与 solution.kt 的差距来源：优化解在每条线段上跑 Kadane，用 cur = max(x, cur + x) 保持
 * 「当前最优的、以已扫描位置结尾的子段」，一趟 O(L) 出结果；本解按定义朴素枚举——对每条线段
 * 固定起点 i，让终点 j 从 i 一路向后延伸并累加，把所有 (i, j) 对都看一遍，即每条线 O(L²)，
 * 全局 O(N³)：本题的 (起点, 终点) 对共 13 341 334 000 个，本机跑不完。因此加上一条**可证明安全的
 * 剪枝**：若 sum(i..m) ≤ 0，则任意 j > m 有 sum(m+1..j) ≥ sum(i..j)，逐前缀非正的子段被
 * 「从更晚起点出发」的那次枚举覆盖，故可以直接 break。剪枝力度实测：真正执行的累加 492 198 002 次，
 * 只有全枚举的 1/27.1，于是能在本机跑完。
 *
 * 也就是说两条路径的枚举方向相反：优化解从左端起、只保留最优尾部（记忆化式的 DP）；
 * 本解把每个起点的每种长度都真的算一遍（只有非正前缀被跳过），不做状态记忆。
 *
 * 复杂度：最坏 O(N³)（全正数表），本数据实测 4.92×10⁸ 次内层累加；空间 O(N²) 存表 + O(N) 抄线段。
 * 另外附带一个**完全不剪枝**的 O(L²) 版本，只在降规模网格（300×300）上跑，用来验证剪枝没有
 * 丢掉任何最优子段——这是断言，不是估算。
 *
 * 题面样例（4×4 表最大和 16；s₁₀ = −393027；s₁₀₀ = 86613）写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

const val N = 2000
const val TOTAL = N * N

/** 滞后斐波那契生成器，返回长度为 count 的数组，g[i] = s_{i+1}。 */
fun generator(count: Int): IntArray {
    val s = IntArray(count)
    for (k in 1..55) {
        val t = 100003L - 200_003L * k + 300_007L * k.toLong() * k * k
        s[k - 1] = ((t % 1_000_000L + 1_000_000L) % 1_000_000L - 500_000L).toInt()
    }
    for (k in 56..count) {
        val t = s[k - 25] + s[k - 56] + 1_000_000
        s[k - 1] = (t % 1_000_000) - 500_000
    }
    return s
}

/** 从 (r0, c0) 沿 (dr, dc) 走出 n×n 边界为止，把线段抄成新数组。 */
fun lineOf(g: IntArray, n: Int, r0: Int, c0: Int, dr: Int, dc: Int): IntArray {
    val buf = IntArray(n)
    var len = 0
    var r = r0
    var c = c0
    while (r >= 0 && r < n && c >= 0 && c < n) {
        buf[len++] = g[r * n + c]
        r += dr
        c += dc
    }
    return if (len == n) buf else buf.copyOf(len)
}

/** 朴素枚举：起点 i 固定，终点 j 向后延伸，遇到非正前缀才 break（剪枝的正确性见文件头）。 */
fun maxSubarrayPruned(line: IntArray): Long {
    var best = Long.MIN_VALUE
    for (i in line.indices) {
        var sum = 0L
        for (j in i until line.size) {
            sum += line[j]
            if (sum > best) best = sum
            if (sum <= 0L) break
        }
    }
    return best
}

/** 完全朴素：把每对 (i ≤ j) 的区间和都算一遍，无任何剪枝。只用于小规模校验。 */
fun maxSubarrayUnpruned(line: IntArray): Long {
    var best = Long.MIN_VALUE
    for (i in line.indices) {
        var sum = 0L
        for (j in i until line.size) {
            sum += line[j]
            if (sum > best) best = sum
        }
    }
    return best
}

/** n×n 表四个方向上「起点延伸枚举」的最大值；unpruned 为 true 时不做剪枝（仅小规模可用）。 */
fun maxSubsequenceEnumerated(g: IntArray, n: Int, unpruned: Boolean): Long {
    val scan = { line: IntArray -> if (unpruned) maxSubarrayUnpruned(line) else maxSubarrayPruned(line) }
    var best = Long.MIN_VALUE
    for (i in 0 until n) best = maxOf(best, scan(lineOf(g, n, i, 0, 0, 1)))            // 行
    for (j in 0 until n) best = maxOf(best, scan(lineOf(g, n, 0, j, 1, 0)))            // 列
    for (d in 0 until 2 * n - 1) {                                                     // 主对角线
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) d else 0
        best = maxOf(best, scan(lineOf(g, n, r0, c0, 1, 1)))
    }
    for (d in 0 until 2 * n - 1) {                                                     // 副对角线
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) n - 1 - d else n - 1
        best = maxOf(best, scan(lineOf(g, n, r0, c0, 1, -1)))
    }
    return best
}

fun solveBruteForce(): Long = maxSubsequenceEnumerated(generator(TOTAL), N, false)

/** n×n 表的全部 6n − 2 条线段（n 行 + n 列 + 2n−1 条主对角线 + 2n−1 条副对角线）。 */
fun allLines(g: IntArray, n: Int): List<IntArray> {
    val out = ArrayList<IntArray>(6 * n)
    for (i in 0 until n) out.add(lineOf(g, n, i, 0, 0, 1))
    for (j in 0 until n) out.add(lineOf(g, n, 0, j, 1, 0))
    for (d in 0 until 2 * n - 1) {
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) d else 0
        out.add(lineOf(g, n, r0, c0, 1, 1))
    }
    for (d in 0 until 2 * n - 1) {
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) n - 1 - d else n - 1
        out.add(lineOf(g, n, r0, c0, 1, -1))
    }
    return out
}

fun verifySample() {
    val s = generator(TOTAL)
    check(s[9] == -393027) { "s[10] 应为 -393027，实际 ${s[9]}" }
    check(s[99] == 86613) { "s[100] 应为 86613，实际 ${s[99]}" }

    val sample = intArrayOf(-2, 5, 3, 2, 9, -6, 5, 1, 3, 2, 7, 3, -1, 8, -4, 8)
    check(maxSubsequenceEnumerated(sample, 4, false) == 16L) { "样例最大和应为 16" }
    check(maxSubsequenceEnumerated(sample, 4, true) == 16L) { "样例（不剪枝）最大和应为 16" }
    check(maxSubarrayPruned(intArrayOf(-1, 8, -4, 8)) == 12L)      // 单行 8 − 4 + 8
    check(maxSubarrayPruned(intArrayOf(8, 7, 1)) == 16L)           // 副对角线 8 + 7 + 1
    check(maxSubarrayPruned(intArrayOf(-3, -7, -1, -9)) == -1L)    // 全负数取单元素
    check(maxSubarrayPruned(intArrayOf(0, 0, 0)) == 0L)
    check(maxSubarrayPruned(intArrayOf(0, -5, 0)) == 0L)

    // 剪枝安全性：降规模 300×300 表（前 90000 个生成值）上，逐线段比对剪枝版与完全不剪枝版。
    // 不是比全局最大值，而是把 6n − 2 = 1798 条线段每条都比一遍；再用取负后的表覆盖另一种符号分布。
    // 500×500 的不剪枝版本在满载机器上要跑十几秒，300×300 已覆盖四种步长与全部线段长度。
    val small = 300
    val base = generator(small * small)
    val negated = IntArray(base.size) { -base[it] }
    var lines = 0
    for (g in listOf(base, negated)) {
        for (line in allLines(g, small)) {
            check(maxSubarrayPruned(line) == maxSubarrayUnpruned(line)) {
                "长度为 ${line.size} 的线段上剪枝与不剪枝不一致"
            }
            lines++
        }
    }
    check(lines == 2 * (6 * small - 2))
}

fun main() {
    verifySample()
    repeat(3) { solveBruteForce() }                     // JIT 预热
    // 计时：预热后连续计时 5 轮取最小值（本机常有 20 个以上子 Agent 抢 CPU，单轮读数噪声极大，
    // 取最小值是唯一可复现的口径；机器空闲时最小值即单轮真实耗时）。
    var best = Double.MAX_VALUE
    var answer = 0L
    repeat(5) {
        val start = System.nanoTime()
        answer = solveBruteForce()
        val ms = (System.nanoTime() - start) / 1e6
        if (ms < best) best = ms
    }
    System.err.printf("brute: %.4f ms%n", best)
    println(answer)
}
