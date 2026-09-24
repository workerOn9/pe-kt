#!/usr/bin/env kotlin
// PE 219 — Skew-cost Coding（偏斜代价编码）
// 思路：n 个码字的最小总代价由「反复劈开当前最便宜的叶子」贪心给出。
//   初始只有根结点（代价 0）算 1 个码字；把代价 c 的叶子劈成两个孩子，得到代价
//   c+1 的左孩子（0 比特，1 便士）与代价 c+4 的右孩子（1 比特，4 便士），
//   码字总数 +1、总代价增加 (c+1)+(c+4)-c = c+5。每次取最小代价叶子即为最优解，
//   题面给的 Cost(6) = 35 正是这条贪心的输出（见 brute-force.kt 的 DP 对照）。
//   直接模拟 n-1 次劈开对 n = 10^9 太慢，但「总是劈开最小者」意味着代价按层递增处理：
//   设 a[c] 为当前代价 c 的叶子数，则 a[c] 只会被第 c-1 层与第 c-4 层的劈开加数，
//   这两层都在处理第 c 层之前完成，因此处理到第 c 层时 a[c] 已定，可整层一次性劈开：
//     a[c+1] += a[c]，a[c+4] += a[c]，总代价 += a[c]·(c+5)。
//   最后一层按剩余次数部分劈开。
// 复杂度：平均码长 ~log n，层数 O(log n)（本题约 90 层），时间与空间均为常数级。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

/** Cost(n)：n 个码字的最小总代价（按层批量模拟贪心劈开）。 */
private fun cost(n: Long): Long {
    if (n <= 1L) return 0L
    val leaves = HashMap<Int, Long>()
    leaves[0] = 1L
    val splits = n - 1
    var done = 0L
    var c = 0
    var total = 0L
    while (done < splits) {
        val k = leaves[c] ?: 0L
        if (k == 0L) {
            c++
            continue
        }
        val take = minOf(k, splits - done)
        total += take * (c + 5)
        done += take
        if (take == k) {                    // 整层劈完，才把两个孩子的数量记到后续层
            leaves[c + 1] = (leaves[c + 1] ?: 0L) + k
            leaves[c + 4] = (leaves[c + 4] ?: 0L) + k
        }
        c++
    }
    return total
}

fun main() {
    println("Cost(6) = ${cost(6)}")         // 题面锚点：35
    repeat(3) { cost(1_000_000_000L) }      // JIT 预热
    val t0 = System.nanoTime()
    val ans = cost(1_000_000_000L)
    val ms = (System.nanoTime() - t0) / 1_000_000
    println(ans)
    System.err.println("cost(1e9) wall = $ms ms")
}
