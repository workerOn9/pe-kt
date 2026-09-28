#!/usr/bin/env kotlin
/**
 * Project Euler 253 — 暴力对照（brute-force.kt）
 *
 * 完全按题面定义实现，不共用 solution.kt 的任何「加权 Motzkin 路径」代码：
 *   · 枚举 n 块拼图的全部 n! 个放下顺序（Heap 算法逐个换位生成）；
 *   · 每个顺序按定义模拟：维护「已放位置」位掩码，逐块看左右邻居是否已放，
 *     邻居数 0 / 1 / 2 ⇒ 段数 +1 / 不变 / −1；记录全过程最大段数 M；
 *   · 统计 M 的分布，并用精确分数算平均值（题面 n=10 的样例逐项对照）；
 *   · 逐条轨迹对拍：n ≤ 10 的全部排列按轨迹分组，与乘积权重
 *     w(生段,h)=h+1、w(接边,h)=2h、w(合段,h)=h−1 逐一比较。
 *
 * 数据来源：meta.json 的 bruteForceBaselineMs 取 n = 11（11! = 39916800 个排列）
 * 的 JIT 预热后 3 轮最优毫秒数；n = 10（3628800 个排列）同时计时并对照题面样例。
 * 对照基准：n = 10 分布应为 512 / 250912 / 1815264 / 1418112 / 144000，
 * 平均 385643/113400 = 3.400732。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

/** n! 个排列的 M 分布（下标即 M）；Heap 迭代法生成全部排列并按定义模拟。 */
private fun bruteDistribution(n: Int): LongArray {
    val dist = LongArray(n + 2)
    val a = IntArray(n) { it + 1 }          // a[t] = 第 t+1 个被放下的位置（1..n）
    val c = IntArray(n)
    while (true) {
        // 按定义模拟当前排列的段数轨迹
        var bits = 0
        var blocks = 0
        var mx = 0
        for (t in 0 until n) {
            val p = a[t] - 1
            val left = if (p > 0) (bits shr (p - 1)) and 1 else 0
            val right = if (p + 1 < n) (bits shr (p + 1)) and 1 else 0
            when (left + right) {
                0 -> blocks += 1
                2 -> blocks -= 1
            }
            bits = bits or (1 shl p)
            if (blocks > mx) mx = blocks
        }
        dist[mx]++
        // Heap 算法：生成下一个排列，全部生成完则退出
        var i = 0
        while (i < n && c[i] >= i) {
            c[i] = 0
            i++
        }
        if (i >= n) break
        if (i % 2 == 0) {
            val t = a[0]; a[0] = a[i]; a[i] = t
        } else {
            val t = a[c[i]]; a[c[i]] = a[i]; a[i] = t
        }
        c[i]++
    }
    return dist
}

private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/**
 * 逐条轨迹对拍：n ≤ 10 的全部排列按「段数轨迹」分组（每步 3 bit 打包），
 * 每组大小与乘积权重 w(生段,h)=h+1、w(接边,h)=2h、w(合段,h)=h−1 逐一比较。
 */
private fun verifyWeightsAgainstBrute(nMax: Int) {
    for (n in 2..nMax) {
        val a = IntArray(n) { it + 1 }
        val c = IntArray(n)
        val groups = HashMap<Long, Long>()
        while (true) {
            var bits = 0
            var blocks = 0
            var key = 0L
            for (t in 0 until n) {
                val p = a[t] - 1
                val left = if (p > 0) (bits shr (p - 1)) and 1 else 0
                val right = if (p + 1 < n) (bits shr (p + 1)) and 1 else 0
                when (left + right) {
                    0 -> blocks += 1
                    2 -> blocks -= 1
                }
                bits = bits or (1 shl p)
                key = key or (blocks.toLong() shl (3 * t))
            }
            groups[key] = (groups[key] ?: 0L) + 1L
            var i = 0
            while (i < n && c[i] >= i) {
                c[i] = 0
                i++
            }
            if (i >= n) break
            if (i % 2 == 0) {
                val t = a[0]; a[0] = a[i]; a[i] = t
            } else {
                val t = a[c[i]]; a[c[i]] = a[i]; a[i] = t
            }
            c[i]++
        }
        var bad = 0
        var total = 0L
        for ((key, cnt) in groups) {
            var w = 1L
            var h = ((key shr 0) and 7L).toInt()
            for (t in 1 until n) {
                val h2 = ((key shr (3 * t)) and 7L).toInt()
                val d = h2 - h
                w *= when (d) {
                    1 -> h + 1
                    0 -> 2 * h
                    -1 -> h - 1
                    else -> 0
                }
                h = h2
            }
            if (w != cnt) bad++
            total += cnt
        }
        check(bad == 0) { "n=$n 有 $bad 条轨迹的乘积权重与真实排列数不符" }
        println("逐轨迹对拍 n=$n：${groups.size} 条轨迹权重与真实排列数逐一相等 ✓（共 $total 个排列）")
    }
}

fun main() {
    println("Project Euler 253 — 暴力对照（按题面定义枚举全排列）")

    // 逐条轨迹对拍：乘积权重 vs 真实排列数（n ≤ 10）
    verifyWeightsAgainstBrute(10)

    val expected10 = longArrayOf(512, 250912, 1815264, 1418112, 144000)
    for (n in 1..10) {
        val dist = bruteDistribution(n)
        if (n <= 5) {
            println("n=$n：排列数 ${dist.sum()}，M 分布 ${(1..n).joinToString(", ") { "${dist[it]}" }}")
        }
        if (n == 10) {
            val got = (1..5).map { dist[it] }.toLongArray()
            check(got.contentEquals(expected10)) { "n=10 分布与题面不符：${got.toList()}" }
            val sum = dist.sum()
            val weighted = dist.indices.sumOf { it.toLong() * dist[it] }
            val g = gcd(weighted, sum)
            println(
                "n=10：分布 ${got.toList()}（题面一致 ✓），平均 = ${weighted / g}/${sum / g} = " +
                    "%.6f".format(weighted.toDouble() / sum),
            )
        }
    }

    // 计时：n = 10 与 n = 11，JIT 预热后 3 轮取最优
    fun bestMs(n: Int, rounds: Int): Double {
        bruteDistribution(n)                          // 预热
        var best = Double.MAX_VALUE
        var expected = 1L
        for (k in 2..n) expected *= k
        repeat(rounds) {
            val t0 = System.nanoTime()
            val d = bruteDistribution(n)
            val ms = (System.nanoTime() - t0) / 1e6
            if (ms < best) best = ms
            check(d.sum() == expected) { "n=$n 排列数不对：${d.sum()}" }
        }
        return best
    }
    val ms10 = bestMs(10, 3)
    println("暴力 n=10（10! = 3628800 个排列）：%.1f ms（JIT 预热后 3 轮最优）".format(ms10))
    val ms11 = bestMs(11, 3)
    val dist11 = bruteDistribution(11)
    val avg11 = dist11.indices.sumOf { it.toDouble() * dist11[it] } / dist11.sum()
    println(
        "暴力 n=11（11! = 39916800 个排列）：%.1f ms；平均 M = %.6f（供 meta.bruteForceBaselineMs）"
            .format(ms11, avg11),
    )
}
