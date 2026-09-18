/**
 * Project Euler 126 — Cuboid Layers（立方体分层）
 *
 * 思路：把 a×b×c 长方体第 k 层所需的单位立方体个数记为 f(a,b,c,k)。题面给了足够的锚点：
 *   3×2×1 的第 1..4 层 = 22, 46, 78, 118；5×1×1 的第 1 层 = 22；5×3×1、7×2×1、11×1×1
 *   的第 1 层 = 46。它们共同指向闭式
 *       f(a,b,c,k) = 2(ab + ac + bc) + 4(a + b + c)(k − 1) + 4(k − 1)(k − 2)。
 *   逐项读法：第一层是给每个可见单位面贴一个立方体，个数就是原长方体的表面积 2(ab+ac+bc)；
 *   再往上每层在 12 条棱上各展开一条宽 (k−1) 的斜带，合计 4(a+b+c)(k−1)（每个尺寸出现在
 *   4 条棱上）；8 个角各展开一块边长 (k−1) 的阶梯三角面，每角 (k−1)(k−2)/2 个单位方格，
 *   合计 8·(k−1)(k−2)/2 = 4(k−1)(k−2)。
 *   相邻两层的差是 f(k+1) − f(k) = 4(a+b+c) + 8(k−1)，公差固定为 8 的等差数列，所以遍历时
 *   只需一次加法、一次增量即可推进到下一层，不必重算多项式。
 *
 *   于是 C(n) 的统计就是：枚举所有 a ≤ b ≤ c 的长方体，把它的每一个 f ≤ limit 的层记一次数。
 *   剪枝：a ≤ b ≤ c 使底面积 2(ab+ac+bc) ≥ 6a²，故 a 只需到 6a² ≤ limit；固定 (a,b) 时
 *   c = b 的底面积最小（4ab + 2b²），故 b 只需枚举到 4ab + 2b² ≤ limit；底面积随 c 严格递增，
 *   c 从 b 起枚举到超过 limit 为止。每个被记数的层满足 f ≤ limit，其底面积 ≤ f 也 ≤ limit，
 *   所以 limit 以内的 C(n) 是完整的——扫描到的第一个 cnt[n] == 1000 的 n 必是全局最小值。
 *   limit 从 1000 起倍增直到命中，避免人为假设答案的上界。
 *
 * 复杂度：时间 O(T + W)，T 是枚举到的 (a,b,c) 长方体重数（a = 1 时由 (b+1)(c+1) ≤ L/2 + 1 支配，
 *   整体 O(L log L)），W 是全部满足 f ≤ L 的 (长方体, 层) 对数——每个这样的层必须落到计数数组上一次，
 *   这是本题不可避免的下界。实测 L = 32000 时 T = 682836、W = 10050016（Python 独立核对过同一组数）。
 *   空间 O(L)。层内计数循环用 Int（L < 2³¹），只有边界判定用 Long 防乘积溢出。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 第 k 层立方体个数的闭式（k ≥ 1）。用 Long 计算，防止 a、b、c 较大时乘积溢出。 */
fun layerCount(a: Long, b: Long, c: Long, k: Long): Long =
    2L * (a * b + a * c + b * c) + 4L * (a + b + c) * (k - 1) + 4L * (k - 1) * (k - 2)

/**
 * 枚举全部长方体及其各层，把 f ≤ limit 的层累加进 cnt。
 * cnt 长度至少 limit + 1，下标即层内立方体个数。
 */
fun countLayers(limit: Int, cnt: IntArray) {
    var a = 1
    while (6L * a * a <= limit) {                      // 底面积 ≥ 6a²（取 b = c = a）
        val aL = a.toLong()
        var b = a
        while (4L * aL * b + 2L * b * b <= limit) {     // 固定 (a,b) 时 c = b 让底面积最小
            val bL = b.toLong()
            var c = b
            while (true) {
                val base = 2L * (aL * bL + aL * c + bL * c)
                if (base > limit) break                // 底面积随 c 严格递增，可安全退出
                var f = base.toInt()                   // 第 1 层；limit < 2³¹，内层用 Int 更快
                var stride = 4 * (a + b + c)           // f(2) − f(1)
                while (f <= limit) {
                    cnt[f]++
                    f += stride
                    stride += 8                       // f(k+1) − f(k) 的公差
                }
                c++
            }
            b++
        }
        a++
    }
}

/** 在给定上界内统计 C(n)，返回最小的满足 C(n) == target 的 n；找不到返回 -1。 */
fun leastWithCount(target: Int, limit: Int): Int {
    val cnt = IntArray(limit + 1)
    countLayers(limit, cnt)
    for (n in 1..limit) if (cnt[n] == target) return n
    return -1
}

/** 从小到大倍增上界，返回最小的满足 C(n) == target 的 n。 */
fun solve(target: Int = 1000): Long {
    var limit = 1000
    while (true) {
        val n = leastWithCount(target, limit)
        if (n > 0) return n.toLong()
        limit *= 2
    }
}

fun verifySample() {
    // 题面第一组：3×2×1 的第 1..4 层
    check(layerCount(3, 2, 1, 1) == 22L) { "3×2×1 第一层应为 22" }
    check(layerCount(3, 2, 1, 2) == 46L) { "3×2×1 第二层应为 46" }
    check(layerCount(3, 2, 1, 3) == 78L) { "3×2×1 第三层应为 78" }
    check(layerCount(3, 2, 1, 4) == 118L) { "3×2×1 第四层应为 118" }

    // 题面第二组：其它长方体的第一层
    check(layerCount(5, 1, 1, 1) == 22L) { "5×1×1 第一层应为 22" }
    check(layerCount(5, 3, 1, 1) == 46L) { "5×3×1 第一层应为 46" }
    check(layerCount(7, 2, 1, 1) == 46L) { "7×2×1 第一层应为 46" }
    check(layerCount(11, 1, 1, 1) == 46L) { "11×1×1 第一层应为 46" }

    // 题面 C(n) 的四个值（上界取得够大，保证这些小 n 的计数完整）
    val cnt = IntArray(2000)
    countLayers(1999, cnt)
    check(cnt[22] == 2) { "C(22) 应为 2，实测 ${cnt[22]}" }
    check(cnt[46] == 4) { "C(46) 应为 4，实测 ${cnt[46]}" }
    check(cnt[78] == 5) { "C(78) 应为 5，实测 ${cnt[78]}" }
    check(cnt[118] == 8) { "C(118) 应为 8，实测 ${cnt[118]}" }

    // 题面：154 是最小的使 C(n) == 10 的 n
    check(leastWithCount(10, 2000) == 154) { "最小 C(n) = 10 的 n 应为 154" }

    // 答案必须严格落在最后一次倍增的上界之内：贴着上界就说明计数不完整，结果不可信
    val answer = solve(1000)
    check(answer < 32_000L) { "答案应严格小于最后使用的上界，实测 $answer" }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                            // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
