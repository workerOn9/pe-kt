/**
 * Project Euler 149 — Maximum-sum Subsequence（最大和子序列）
 *
 * 思路：把 2000×2000 表格的四个方向各自看成若干条独立的「一维序列」——行 2000 条、
 * 列 2000 条、主对角线 3999 条、副对角线 3999 条——问题就化为在每条序列上求最大连续子段和，
 * 用 Kadane 在线性时间内解决：
 *     cur  = max(x, cur + x)      —— 要么从 x 重新开始，要么接在前一段后面
 *     best = max(best, cur)
 * 线段不单独抄成数组，而是用起点 (r0, c0) 与步长 (dr, dc) 直接在表上走，避免 4000 次要扫描的内存分配。
 * 四条线段长度之和恰为 4 × 2000² = 1.6×10⁷，故整个扫描在数十毫秒内完成。
 *
 * 关键点：cur 的转移写成 max(x, cur + x) 而不是 max(0, cur + x)，这样全负数线段也能正确取到
 * 单个最大值；若写成 max(0, ...)，必须另外处理「答案可能是单个负数」的边界。
 *
 * 复杂度：生成 s 为 O(4×10⁶)，扫描为 4N² = O(N²) 时间、O(N²) 空间（存整张表）。
 * 实测最大和约 5×10⁷，前缀量级远小于 Long 上限，但一律用 Long 以避免疏漏。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

const val SIZE = 2000                  // 表格边长
const val COUNT = SIZE * SIZE          // 4 000 000

/** 滞后斐波那契生成器：返回 s[1..count]（下标从 1 起）。 */
fun laggedFibonacci(count: Int): IntArray {
    val g = IntArray(count)                       // g[i] 即 s[i+1]，直接按行优先落地成表格
    for (k in 1..55) {
        val v = 100003L - 200_003L * k + 300_007L * k * k * k
        g[k - 1] = (Math.floorMod(v, 1_000_000L) - 500_000L).toInt()
    }
    for (k in 56..count) {
        g[k - 1] = ((g[k - 25].toLong() + g[k - 56] + 1_000_000L) % 1_000_000L - 500_000L).toInt()
    }
    return g
}

/** s 的前 n² 项按行填成 n×n 表格（行优先）——与生成器落地顺序天然一致。 */
fun buildGrid(n: Int): IntArray = laggedFibonacci(n * n)

/**
 * 从 (r0, c0) 出发、沿 (dr, dc) 前进的整条线段上的最大连续子段和（Kadane）。
 * 越出 n×n 边界即停；线段至少含一个元素。
 */
fun kadaneLine(g: IntArray, n: Int, r0: Int, c0: Int, dr: Int, dc: Int): Long {
    var best = Long.MIN_VALUE
    var cur = 0L
    var r = r0
    var c = c0
    while (r in 0 until n && c in 0 until n) {
        val x = g[r * n + c]
        cur = if (cur > 0L) cur + x else x.toLong()   // 与前段相连 vs 从 x 重新开始
        if (cur > best) best = cur
        r += dr
        c += dc
    }
    return best
}

/** 表格 g（边长 n）在四个方向上的最大连续和。 */
fun maxSubsequence(g: IntArray, n: Int): Long {
    var best = Long.MIN_VALUE

    for (i in 0 until n)                                    // 行：从左到右
        best = maxOf(best, kadaneLine(g, n, i, 0, 0, 1))
    for (j in 0 until n)                                    // 列：从上到下
        best = maxOf(best, kadaneLine(g, n, 0, j, 1, 0))
    for (d in 0 until 2 * n - 1) {                          // 主对角线：左上 → 右下
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) d else 0
        best = maxOf(best, kadaneLine(g, n, r0, c0, 1, 1))
    }
    for (d in 0 until 2 * n - 1) {                          // 副对角线：右上 → 左下
        val r0 = if (d < n) 0 else d - n + 1
        val c0 = if (d < n) n - 1 - d else n - 1
        best = maxOf(best, kadaneLine(g, n, r0, c0, 1, -1))
    }
    return best
}

fun solve(): Long = maxSubsequence(buildGrid(SIZE), SIZE)

fun verifySample() {
    val s = laggedFibonacci(COUNT)
    check(s[9] == -393027) { "s[10] 应为 -393027，实际 ${s[9]}" }
    check(s[99] == 86613) { "s[100] 应为 86613，实际 ${s[99]}" }
    var min = Int.MAX_VALUE
    var max = Int.MIN_VALUE
    for (v in s) {
        if (v < min) min = v
        if (v > max) max = v
    }
    check(min >= -500_000 && max <= 499_999) { "生成值越界：[$min, $max]" }

    // 题面 4×4 样例表，四个方向上的最大和为 16 = 8 + 7 + 1（副对角线 (3,1)→(2,2)→(1,3)）
    val sample = intArrayOf(-2, 5, 3, 2, 9, -6, 5, 1, 3, 2, 7, 3, -1, 8, -4, 8)
    check(maxSubsequence(sample, 4) == 16L) { "样例最大和应为 16" }
    check(kadaneLine(sample, 4, 0, 3, 1, -1) == 9L)     // 副对角线 2,5,2,−1 的最优子段 2+5+2
    check(kadaneLine(sample, 4, 3, 1, -1, 1) == 16L)    // 反方向走同一条副对角线：8,7,1
    check(kadaneLine(sample, 4, 1, 3, 1, -1) == 16L)    // 该子段所在的整条副对角线 1,7,8
    check(kadaneLine(sample, 4, 3, 0, 0, 1) == 12L)     // 第 4 行：8 − 4 + 8
    check(kadaneLine(sample, 4, 0, 0, 1, 1) == 15L)     // 主对角线 −2,−6,7,8 → 7 + 8
    check(kadaneLine(sample, 4, 2, 0, 0, 1) == 15L)     // 第 3 行：3 + 2 + 7 + 3
    check(kadaneLine(intArrayOf(-3, -7, -1, -9), 4, 0, 0, 0, 1) == -1L)  // 全负数取单个最大值
    check(kadaneLine(intArrayOf(0, 0), 2, 0, 0, 0, 1) == 0L)
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    // 计时：预热后连续计时 9 轮取最小值（本机常有 20 个以上子 Agent 抢 CPU，单轮读数噪声极大，
    // 取最小值是唯一可复现的口径；机器空闲时最小值即单轮真实耗时）。
    var best = Double.MAX_VALUE
    var answer = 0L
    repeat(9) {
        val start = System.nanoTime()
        answer = solve()
        val ms = (System.nanoTime() - start) / 1e6
        if (ms < best) best = ms
    }
    System.err.printf("optimized: %.4f ms%n", best)
    println(answer)
}
