/**
 * Project Euler 142 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的思路完全不同：不碰勾股三元组，直接按题面定义在 x 上递增搜索。
 * 固定 x 后，x + y = a²、x − y = b² 要求 2x = a² + b²，于是枚举 2x 的全部「两平方和」
 * 表示（a 从 √x 之上扫到 √(2x)，用平方表查 b² = 2x − a²），每得到一个表示就给出
 * y = (a² − b²)/2 与 b² = x − y；再任取两个不同表示充当 y 与 z，直接检查 y + z、y − z
 * 是否为平方。x > y 由 b > 0 保证，y > z > 0 显式检查。
 *
 * 提前退出：任一解都满足 x + y + z > x，所以一旦 x ≥ 当前最小和就不可能再改进，循环结束。
 * 也因此本题的暴力解只需扫到 x ≈ 1.0×10⁶（答案的 x = 434657，和 = 1006193）即自动停止。
 *
 * 复杂度：对每个 x 扫描约 (√(2x) − √x) = O(√x) 个 a，总计 O(X^{3/2})，X 为扫描到的最大 x；
 * 空间 O(X)（平方表）。实测算到 X ≈ 1.0×10⁶ 约需数百毫秒，而优化解只枚举腿长 ≤ 2048 的
 * 勾股配对，两者相差约三个数量级。
 *
 * 题面没有给出数值样例，故断言写成：①对求出的 (x, y, z) 按定义逐条验证六个平方条件；
 * ②把上限收到 x ≤ 300000 时搜不到任何解（与 x = 434657 相容，说明提前退出前的空转段确实无解）。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

fun isqrt(n: Long): Long {
    if (n <= 0L) return 0L
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0L && r * r > n) r--
    while ((r + 1L) * (r + 1L) <= n) r++
    return r
}

/**
 * 按定义递增枚举 x，返回第一个最小的 (x, y, z)；cap 是 x 的搜索上限（防止无解时空转）。
 * 返回 null 表示 x ≤ cap 内无解。
 */
fun solveBruteForce(cap: Long): LongArray? {
    // 平方表覆盖所有需要判定的值：b² ≤ 2x − a² < 2x ≤ 2·cap，y + z < 2x ≤ 2·cap
    val limit = 2 * cap
    val isSq = BooleanArray((limit + 1).toInt())
    var s = 1L
    while (s * s <= limit) {
        isSq[(s * s).toInt()] = true
        s++
    }

    var best: LongArray? = null
    var x = 1L
    while (x <= cap && (best == null || x < best[0] + best[1] + best[2])) {
        val twoX = 2 * x
        val repsA = ArrayList<Long>()
        val repsB = ArrayList<Long>()
        var a = isqrt(x) + 1                       // a² > x ⇒ b² = 2x − a² < x ⇒ b < a
        val aEnd = isqrt(twoX)
        while (a <= aEnd) {
            val b2 = twoX - a * a
            if (isSq[b2.toInt()]) {
                val b = isqrt(b2)
                if (b < a) {
                    repsA.add(a)
                    repsB.add(b)
                }
            }
            a++
        }
        for (i in repsA.indices) {
            val yi = (repsA[i] * repsA[i] - repsB[i] * repsB[i]) / 2
            for (j in i + 1 until repsA.size) {
                val yj = (repsA[j] * repsA[j] - repsB[j] * repsB[j]) / 2
                val y = maxOf(yi, yj)
                val z = minOf(yi, yj)
                if (z <= 0L || y <= z) continue
                val sum = x + y + z
                if (sum >= (best?.let { it[0] + it[1] + it[2] } ?: Long.MAX_VALUE)) continue
                if (y + z <= limit && isSq[(y + z).toInt()] && isSq[(y - z).toInt()]) {
                    best = longArrayOf(x, y, z)
                }
            }
        }
        x++
    }
    return best
}

fun main() {
    val sol = solveBruteForce(2_000_000L) ?: error("x ≤ 2×10⁶ 内未找到解")
    val (x, y, z) = sol
    check(x > y && y > z && z > 0L) { "需要 x > y > z > 0" }
    val six = longArrayOf(x + y, x - y, x + z, x - z, y + z, y - z)
    for (v in six) {
        val r = isqrt(v)
        check(r * r == v) { "$v 不是完全平方数" }
    }
    System.err.println("x = $x, y = $y, z = $z，六式 = ${six.toList()} = 其平方根 ${six.map { isqrt(it) }}")
    check(solveBruteForce(300_000L) == null) { "x ≤ 300000 内不应有解" }

    repeat(2) { solveBruteForce(2_000_000L) }       // JIT 预热（单次已近秒级，预热 2 轮足够）
    val start = System.nanoTime()
    val answer = solveBruteForce(2_000_000L)!!
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer[0] + answer[1] + answer[2])
}
