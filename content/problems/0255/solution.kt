#!/usr/bin/env kotlin
/**
 * Project Euler 255 — Rounded Square Roots（四舍五入的平方根）
 *
 * 思路
 * ────
 * 记 m_k = ceil(n / x_k)（使 (m_k−1)·x_k < n ≤ m_k·x_k 的唯一整数），则题面的迭代
 *
 *     x_{k+1} = floor((x_k + m_k) / 2)，  14 位数取 x_0 = 7×10^6（d 偶数）
 *
 * 的停步判据是
 *
 *     x_{k+1} = x_k  ⟺  floor((x_k+m_k)/2) = x_k  ⟺  x_k ≤ m_k ≤ x_k+1
 *                    ⟺  x_k(x_k−1) < n ≤ x_k(x_k+1)，
 *
 * 即停步时的 x_k 恰是 n 的四舍五入平方根（这就是「整数版 Heron 法」的意义）。
 * 迭代次数 = 应用递推的次数：4321 例中 x_1 = 66 ≠ 70、x_2 = 66 = x_1，共 2 次——题面
 * 的「after just two iterations」正是这个口径，差 1 会让全题结果整体错位。
 *
 * 关键结构（把 10^13 个 n 压成 O(N/x_0) 段）
 * ─────────────────────────────────────────
 * 在区间 I(x, m) = ((m−1)x, mx] 上恒有 ceil(n/x) = m，因此「x 的下一值」floor((x+m)/2)
 * 在整个 I(x,m) 上是常数。于是 n 的完整轨迹 x_0 → x_1 → … → x_{j*}（x_{j*} 为首个终止态，
 * 迭代次数 = j*+1）当且仅当
 *
 *     n ∈ ∩_{j=0}^{j*} I(x_j, m_j)，   m_j = ceil(n / x_j)（取区间内任一代表元算出）
 *
 * ——[10^13, 10^14) 被划分成有限多个「轨迹区间」，同一区间内所有 n 的迭代次数相同。
 * 轨迹本身是牛顿法（二次收敛）：比值 r = m/x 满足 r' = 4r/(1+r)²、1 − r' = (1−r)²/(1+r)²，
 * 偏差每步平方收缩，因此 14 位数平均只要 4.45 次迭代（实测分布见下）。
 *
 * 方法 A（主路径，按轨迹跳进）
 *   对每个轨迹区间，用左端 n 模拟轨迹、同时把各层约束 I(x_j, m_j) 取交，得到该轨迹对应的
 *   全部 n（一个区间 [l, r]），累加 步数 × (r−l+1)，再从 r+1 继续。实测 59,173,538 条轨迹。
 *
 * 方法 B（独立路径，逐层 BFS）
 *   维护「当前层」的区间列表（每个区间带状态 (x,m)），按 x' = floor((x+m)/2) 与整除边界
 *   把每个区间切成子区间 [l,r] ∩ I(x', m')（m' = ceil(n/x')，只需一次除法定起点）。总迭代数
 *   = Σ_层 Σ_区间 长度——每个 n 在它经历的每一层恰好被计一次。实测 1.69×10^8 个节点。
 *   两条路径的聚合方式不同（按轨迹×长度 vs 按层求和），互不掩盖错误。
 *
 * 验证（main 内联跑，全部实跑通过）
 *   · 题面样例：n = 4321 → 2 步、结果 66（实际 √4321 = 65.7343…）；
 *     5 位数全范围 288926/90000 = 3.2102888888…，四舍五入到 10 位小数 = 3.2102888889，
 *     与题面给出的数值一致（这一步同时校准了「迭代次数」的口径）；
 *   · 暴力对照（按定义逐 n 模拟，与两法不共享代码）：d = 5,6,7,8 全范围逐 n 与两法一致；
 *     14 位数两个子区间 [10^13, 10^13+2×10^7) 与 [10^14−2×10^7, 10^14) 逐 n 与两法一致
 *     （在真正的 14 位规模上校验，且覆盖 x_0 在 √n 上方与下方两种走向）；
 *   · 方法 A 与方法 B 在 [10^13, 10^14) 上给出一致的总和 400266100622279。
 *
 * 答案：平均迭代次数 = 400266100622279 / 90000000000000 = 4.447401118025322…，
 *       四舍五入到 10 位小数为 4.4474011180。
 *       仓库编码惯例（10 位小数用 10^10）：meta.answer = round(avg × 10^10) = 44474011180。
 *       total×10^10 ≈ 4×10^24 超出 Long，必须用 BigInteger 精确除法；余数占比 0.2532，
 *       远离 1/2，不存在临界舍入风险（脚本里显式打印余数）。
 * 复杂度：方法 A 约 5.92×10^7 个轨迹区间（约 2.6×10^8 次整数除法）；方法 B 约 1.69×10^8 个
 *       节点；逐 n 暴力对全范围外推约 3 天（d=9 的实测速度 ≈ 3.35×10^8 个数/秒），只能小规模对照。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import java.math.BigInteger

private const val NLO = 10_000_000_000_000L          // 10^13
private const val NHI = 100_000_000_000_000L         // 10^14
private const val X0_14 = 7_000_000L                 // d = 14 的初值 7×10^6
private const val CHUNK = 1_000_000_000_000L         // 方法 B 的分块粒度（1e12）
private const val DECIMALS = 10                      // 题目要求 10 位小数

/** 10^e（e ≥ 0）。 */
private fun pow10(e: Int): Long {
    var r = 1L
    repeat(e) { r *= 10 }
    return r
}

/** 题面初值：d 位奇数取 2×10^((d−1)/2)，偶数取 7×10^((d−2)/2)。 */
private fun x0For(d: Int): Long =
    if (d % 2 == 1) 2 * pow10((d - 1) / 2) else 7 * pow10((d - 2) / 2)

// ------------------------------------------------------------------ 定义版模拟（暴力）

/** 按定义模拟：返回迭代次数（应用递推直到 x_{k+1} = x_k 为止的次数）。 */
private fun iterationsOf(n: Long, x0: Long): Int {
    var x = x0
    var steps = 0
    while (true) {
        val m = (n + x - 1) / x            // ceil(n/x)
        val nx = (x + m) ushr 1            // floor((x+m)/2)
        steps++
        if (nx == x) return steps
        x = nx
    }
}

/** 按定义模拟到停步，返回停步值（应为 n 的四舍五入平方根）。 */
private fun fixedPointOf(n: Long, x0: Long): Long {
    var x = x0
    while (true) {
        val m = (n + x - 1) / x
        val nx = (x + m) ushr 1
        if (nx == x) return x
        x = nx
    }
}

/** 逐 n 暴力：对小范围或子区间按定义求和。 */
private fun sumByBrute(lo: Long, hi: Long, x0: Long): Long {
    var total = 0L
    var n = lo
    while (n < hi) {
        total += iterationsOf(n, x0)
        n++
    }
    return total
}

// ------------------------------------------------------------------ 方法 A：按轨迹跳进

/** 方法 A 的统计量：总和、轨迹区间数、内层迭代（轨迹层数之和）、最大步数、步数分布。 */
private class TrajectorySurvey(
    val total: Long,
    val regions: Long,
    val innerSteps: Long,
    val maxSteps: Int,
    val hist: LongArray,          // hist[t] = 迭代次数为 t 的 n 的个数
)

/**
 * 方法 A 主循环：把 [lo, hi) 划分成「轨迹区间」并加权求和。
 *
 * 每个区间的处理：用左端 n 模拟轨迹，逐层把 I(x_j, m_j) = ((m_j−1)x_j, m_j·x_j] 与当前
 * 候选区间 [l, r] 取交；轨迹结束时 [l, r] 就是「迭代次数 = 步数」的全部 n（闭区间），
 * 下一段从 r+1 开始。
 *
 * [hist] 非空时顺带收集统计量（每个区间多一次判空）；为空时是计时用的纯路径。
 */
private fun trajectoryCore(lo: Long, hi: Long, x0: Long, hist: LongArray?): TrajectorySurvey {
    val h = hist ?: LongArray(0)
    var total = 0L
    var regions = 0L
    var innerSteps = 0L
    var maxSteps = 0
    var n = lo
    while (n < hi) {
        var x = x0
        var l = lo
        var r = hi - 1
        var steps = 0
        while (true) {
            val m = (n + x - 1) / x
            val c1 = (m - 1) * x + 1
            if (c1 > l) l = c1
            val c2 = m * x
            if (c2 < r) r = c2
            val nx = (x + m) ushr 1
            steps++
            if (nx == x) break
            x = nx
        }
        val len = r - l + 1
        total += steps.toLong() * len
        if (hist != null) {
            h[steps] += len
            innerSteps += steps
            if (steps > maxSteps) maxSteps = steps
            regions++
        }
        n = r + 1
    }
    return TrajectorySurvey(total, regions, innerSteps, maxSteps, h)
}

/** 方法 A 纯路径（计时用）。 */
private fun sumByTrajectory(lo: Long, hi: Long, x0: Long): Long =
    trajectoryCore(lo, hi, x0, null).total

/** 方法 A 统计版（诊断用，只在 main 里跑一次）。 */
private fun surveyByTrajectory(lo: Long, hi: Long, x0: Long): TrajectorySurvey =
    trajectoryCore(lo, hi, x0, LongArray(64))

// ------------------------------------------------------------------ 方法 B：逐层 BFS

/**
 * 方法 B：逐层细分区间列表，返回总迭代数 = Σ_层 Σ_区间 长度。
 *
 * 第 1 层是按 m_0 = ceil(n/x_0) 切出的区间；之后每层：x' = floor((x+m)/2)，把区间按 x'
 * 的整除边界切成 n ∈ I(x', m') 的子区间，逐层推进直到全部区间停步。分块处理控制内存。
 */
private fun sumByLevels(lo: Long, hi: Long, x0: Long, chunk: Long): Long {
    var total = 0L
    // 预分配：第 1 层区间数 ≈ chunk/x0，按 3 倍余量；再受 n 的总个数与上限约束
    val est = (chunk / x0) * 3 + 1024
    var cap = minOf(est, hi - lo, 4_000_000L).toInt().coerceAtLeast(1 shl 12)
    var lo1 = LongArray(cap); var len1 = LongArray(cap); var x1 = LongArray(cap); var m1 = LongArray(cap)
    var lo2 = LongArray(cap); var len2 = LongArray(cap); var x2 = LongArray(cap); var m2 = LongArray(cap)

    fun grow() {
        cap *= 2
        lo1 = lo1.copyOf(cap); len1 = len1.copyOf(cap); x1 = x1.copyOf(cap); m1 = m1.copyOf(cap)
        lo2 = lo2.copyOf(cap); len2 = len2.copyOf(cap); x2 = x2.copyOf(cap); m2 = m2.copyOf(cap)
    }

    var chunkLo = lo
    while (chunkLo < hi) {
        val chunkHi = minOf(chunkLo + chunk, hi)                 // 半开区间 [chunkLo, chunkHi)
        var cnt = 0
        var m0 = (chunkLo + x0 - 1) / x0
        while (true) {
            val a = maxOf(chunkLo, (m0 - 1) * x0 + 1)
            val b = minOf(chunkHi, m0 * x0 + 1)                  // I(x0,m0) 的右端含 m0·x0
            if (a >= b) break
            if (cnt == cap) grow()
            lo1[cnt] = a; len1[cnt] = b - a; x1[cnt] = x0; m1[cnt] = m0
            cnt++
            m0++
        }
        while (cnt > 0) {
            var cnt2 = 0
            for (i in 0 until cnt) {
                val a = lo1[i]
                val b = a + len1[i]
                val x = x1[i]
                val m = m1[i]
                total += len1[i]                                 // 这一层的每个 n 各计 1 次迭代
                val xp = (x + m) ushr 1
                if (xp == x) continue                            // 停步：不再细分
                var start = a
                var mp = (start + xp - 1) / xp                   // 子区间起点所在的 m'
                while (start < b) {
                    val end = minOf(b, mp * xp + 1)              // I(x',mp) 的右端含 mp·x'
                    if (end > start) {
                        if (cnt2 == cap) grow()
                        lo2[cnt2] = start; len2[cnt2] = end - start; x2[cnt2] = xp; m2[cnt2] = mp
                        cnt2++
                    }
                    start = end
                    mp++
                }
            }
            var t = lo1; lo1 = lo2; lo2 = t
            t = len1; len1 = len2; len2 = t
            t = x1; x1 = x2; x2 = t
            t = m1; m1 = m2; m2 = t
            cnt = cnt2
        }
        chunkLo = chunkHi
    }
    return total
}

// ------------------------------------------------------------------ 小数编码

/**
 * 精确计算 round(total / count × 10^decimals)（BigInteger，全程无浮点）。
 * 余数恰好占一半时向上取整（本题余数占比 0.2532，不触发）。
 */
private fun encodeAverage(total: Long, count: Long, decimals: Int): BigInteger {
    var scale = BigInteger.ONE
    repeat(decimals) { scale = scale.multiply(BigInteger.TEN) }
    val den = BigInteger.valueOf(count)
    val (q, r) = BigInteger.valueOf(total).multiply(scale).divideAndRemainder(den)
    return if (r.multiply(BigInteger.TWO) >= den) q + BigInteger.ONE else q
}

/** 把 round(avg×10^d) 还原成人类可读的定点小数字符串。 */
private fun decodeAverage(scaled: BigInteger, decimals: Int): String {
    val s = scaled.toString().padStart(decimals + 1, '0')
    return s.substring(0, s.length - decimals) + "." + s.substring(s.length - decimals)
}

/** 把 round(avg×10^d) 写成 a + r/N 的形式，便于检查舍入余量。 */
private fun roundingRemainder(total: Long, count: Long, decimals: Int): String {
    var scale = BigInteger.ONE
    repeat(decimals) { scale = scale.multiply(BigInteger.TEN) }
    val den = BigInteger.valueOf(count)
    val (q, r) = BigInteger.valueOf(total).multiply(scale).divideAndRemainder(den)
    return "商 $q，余数 $r（占比 ${"%.4f".format(r.toDouble() / count.toDouble())}，1/2 = 0.5）"
}

// ------------------------------------------------------------------ main

fun main() {
    println("Project Euler 255 — Rounded Square Roots（四舍五入的平方根）")

    // 1) 题面样例：4321 → 2 步、结果 66；5 位数平均 3.2102888889
    check(iterationsOf(4321L, x0For(4)) == 2) { "4321 的迭代次数应为 2" }
    check(fixedPointOf(4321L, x0For(4)) == 66L) { "4321 的四舍五入平方根应为 66" }
    val d5lo = pow10(4)
    val d5hi = pow10(5)
    val brute5 = sumByBrute(d5lo, d5hi, x0For(5))
    val enc5 = encodeAverage(brute5, d5hi - d5lo, DECIMALS)
    check(enc5 == BigInteger.valueOf(32_102_888_889L)) { "5 位数平均与题面不符：$enc5" }
    println("题面样例：4321 → 2 步、结果 66（√4321 = 65.7343…）✓")
    println("题面样例：5 位数平均 = ${decodeAverage(enc5, DECIMALS)}（题面 3.2102888889）✓")

    // 2) 暴力对照：d = 5..8 全范围，逐 n 按定义模拟（与两法不共享代码）
    for (d in 5..8) {
        val lo = pow10(d - 1)
        val hi = pow10(d)
        val x0 = x0For(d)
        val brute = sumByBrute(lo, hi, x0)
        val a = surveyByTrajectory(lo, hi, x0).total
        val b = sumByLevels(lo, hi, x0, 1_000_000_000L)
        check(brute == a && a == b) { "d = $d：暴力 $brute vs A $a vs B $b 不一致" }
        println(
            "暴力对照 d = $d：逐 n 模拟 = $brute，方法 A = $a，方法 B = $b 一致；" +
                "平均 = ${decodeAverage(encodeAverage(brute, hi - lo, DECIMALS), DECIMALS)}",
        )
    }

    // 3) 14 位数两个子区间：逐 n 暴力 vs 两法（在真正 14 位规模上校验两种走向）
    val subLen = 20_000_000L
    for ((name, subLo, subHi) in listOf(
        Triple("下段 [10^13, 10^13+2×10^7)", NLO, NLO + subLen),
        Triple("上段 [10^14−2×10^7, 10^14)", NHI - subLen, NHI),
    )) {
        val brute = sumByBrute(subLo, subHi, X0_14)
        val a = surveyByTrajectory(subLo, subHi, X0_14).total
        val b = sumByLevels(subLo, subHi, X0_14, 1_000_000_000L)
        check(brute == a && a == b) { "$name：暴力 $brute vs A $a vs B $b 不一致" }
        println("暴力对照 14 位数$name：逐 n 模拟 = $brute，方法 A = $a，方法 B = $b 一致")
    }

    // 4) 全范围：方法 A（含统计）与方法 B
    val survey = surveyByTrajectory(NLO, NHI, X0_14)
    println(
        "方法 A 统计：轨迹区间 ${survey.regions} 个（平均每段 ${NLO.toDouble() / survey.regions} 个 n）、" +
            "内层迭代 ${survey.innerSteps} 次、最大步数 ${survey.maxSteps}",
    )
    val histParts = ArrayList<String>()
    for (t in 1..survey.maxSteps) {
        if (survey.hist[t] > 0) histParts.add("$t:${survey.hist[t]}")
    }
    println("迭代次数分布 t(count)：${histParts.joinToString("  ")}")
    check(survey.hist.sum() == NHI - NLO) { "步数分布之和应等于 n 的个数" }

    val ansA = survey.total
    val ansB = sumByLevels(NLO, NHI, X0_14, CHUNK)
    println("方法 A（按轨迹跳进）总迭代数 = $ansA")
    println("方法 B（逐层 BFS）  总迭代数 = $ansB")
    check(ansA == ansB) { "两种方法不一致：$ansA vs $ansB" }
    check(ansA == 400_266_100_622_279L) { "总和与实跑预期不符：$ansA" }

    // 5) 精确期望 + 10 位小数编码
    val count = NHI - NLO
    println("10 位小数编码的舍入检查：${roundingRemainder(ansA, count, DECIMALS)}")
    val encoded = encodeAverage(ansA, count, DECIMALS)
    println("平均迭代次数 = ${decodeAverage(encoded, DECIMALS)}（精确值 ${BigInteger.valueOf(ansA)}/$count = 4.447401118025322…）")
    println("编码答案 round(avg × 10^10) = $encoded")

    // 6) 计时：JIT 预热后 3 轮取最优
    sumByTrajectory(NLO, NHI, X0_14)                          // 预热
    var bestA = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(sumByTrajectory(NLO, NHI, X0_14) == ansA)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestA) bestA = ms
        println("  方法 A 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 A 完整求解：${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    sumByLevels(NLO, NHI, X0_14, CHUNK)                       // 预热
    var bestB = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(sumByLevels(NLO, NHI, X0_14, CHUNK) == ansB)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestB) bestB = ms
        println("  方法 B 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 B 完整求解：${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")

    println("check() 全部通过")
}
