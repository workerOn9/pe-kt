/**
 * Project Euler 136 — 唯一的差 · 暴力对照解（按定义正向枚举，教学对比用）
 *
 * 与 solution.kt 的思路不同：
 *   · solution.kt 走数论刻画——把解数化成「合格因子对数」，推出恰有一个解的 n 必然是
 *     {≡ 3 (mod 4) 的素数} ∪ {4, 16} ∪ {4p, 16p}，于是只筛一遍素数就能直接数出来；
 *   · 本解完全按定义枚举：对每对 (d, a)（公差 d ≥ 1，中间项 a ≥ d + 1，末项 z = a − d ≥ 1）
 *     算 n = (a + d)² − a² − (a − d)² = a(4d − a)，在计数数组里累加，最后数出「计数恰为 1」的 n。
 *     它不做任何数论约化，只依赖那条二次式本身。
 *
 * 枚举的规模控制：n = a(4d − a) 中把 m = 4d − a 当主变量（m ∈ [1, 3d − 1]，a ≥ d + 1 ⟺ m ≤ 3d − 1），
 * 则 n = m(4d − m) 对 d 单调增，故固定 m 时 d 从 ⌈(m + 1)/3⌉ 取到 (limit + m² − 1) / (4m)，
 * 索引按步长 4m 前进——固定 m 时是纯等差步进，避免逐个重算乘法。
 * 全量 limit = 5×10⁷ 时总迭代约 1.1×10⁸ 次；计数数组按字节存，超过 2 就截断（只关心「是否恰为 1」）。
 *
 * 复杂度：时间 O(limit · log limit)（严格地说是 Σ_m limit/(4m) = Θ(limit log limit) 次累加），空间 O(limit) 字节。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 整数平方根（向下取整）。 */
fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/**
 * 正向枚举：把小于 limit 的每个 n 的解数记进字节数组（截断于 2），返回解数恰为 1 的 n 的个数。
 */
fun countUnique(limit: Int = 50_000_000): Long {
    val cnt = ByteArray(limit)
    val mMax = isqrt(3L * limit) + 4          // n 的最小值 ≥ (m − 3)² / 3，超过它就没有合法的 d
    var m = 1
    while (m <= mMax) {
        val dMin = (m + 3) / 3                // ⌈(m + 1)/3⌉：保证 a = 4d − m ≥ d + 1
        val nMin = m.toLong() * (4L * dMin - m)
        if (nMin < limit) {
            val dMax = (limit + m.toLong() * m - 1) / (4L * m)   // 最大的 d 使 m(4d − m) < limit
            var n = nMin
            val step = 4L * m
            var d = dMin
            while (d <= dMax) {
                val i = n.toInt()
                if (cnt[i] < 2) cnt[i]++
                n += step
                d++
            }
        }
        m++
    }
    var count = 0L
    for (i in 3 until limit) if (cnt[i].toInt() == 1) count++
    return count
}

/**
 * 反向求单个 n 的全部解（用于样例与手算锚点的核对）：n = a(4d − a) 视为关于 a 的二次方程，
 * a = 2d ± √(4d² − n)，故只需在 d ∈ [√n/2, (n + 1)/4] 这个窗口里找完全平方的 4d² − n
 * （上界来自 a = 4d − 1 时 n = 4d − 1，下界来自 a = 2d 时 n = 4d²）。
 * 枚举方向与 countUnique 相反，且不复用任何计数数组。
 */
fun solutionsOf(n: Int): List<Triple<Int, Int, Int>> {
    val out = ArrayList<Triple<Int, Int, Int>>()
    var d = ((isqrt(n.toLong()) + 1) / 2).toInt()          // ≈ ⌈√n / 2⌉
    if (d < 1) d = 1
    val dMax = (n + 1) / 4
    while (d <= dMax) {
        val disc = 4L * d * d - n
        if (disc >= 0) {
            val s = isqrt(disc)
            if (s * s == disc) {
                val roots = if (s == 0L) intArrayOf(2 * d)               // 重根只算一个解
                            else intArrayOf(2 * d - s.toInt(), 2 * d + s.toInt())
                for (a in roots) {
                    if (a >= d + 1 && a <= 4 * d - 1 && a * (4 * d - a) == n) {
                        out.add(Triple(a + d, a, a - d))
                    }
                }
            }
        }
        d++
    }
    return out
}

fun verifySample() {
    // 题面样例：n = 20 恰有一个解 13² − 10² − 7²
    check(solutionsOf(20) == listOf(Triple(13, 10, 7))) { "n = 20 的解应为 (13, 10, 7)：${solutionsOf(20)}" }
    // 题面：一百以内恰有二十五个 n 有唯一解
    check(countUnique(100) == 25L) { "一百以内应为 25 个，实得 ${countUnique(100)}" }

    // 手算锚点：直接用定义核验每个 n 的解数（含 0 解与 2 解的边界）
    val expected = mapOf(3 to 1, 4 to 1, 8 to 0, 15 to 3, 16 to 1, 20 to 1, 32 to 2, 48 to 1)
    for ((n, k) in expected) {
        check(solutionsOf(n).size == k) { "n = $n 应为 $k 个解，实得 ${solutionsOf(n)}" }
    }
    // 手算样例：n = 15 的三个解分别对应 (5, 3, 1)、(7, 5, 3)、(19, 15, 11)
    check(solutionsOf(15).toSet() == setOf(Triple(5, 3, 1), Triple(7, 5, 3), Triple(19, 15, 11)))
    // n = 32 的两个解对应 (7, 4, 1)、(11, 8, 5)
    check(solutionsOf(32).toSet() == setOf(Triple(7, 4, 1), Triple(11, 8, 5)))
}

fun main() {
    verifySample()
    repeat(3) { countUnique() }                  // JIT 预热
    val start = System.nanoTime()
    val answer = countUnique()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
