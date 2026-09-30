#!/usr/bin/env kotlin
/**
 * Project Euler 298 — Selective Amnesia（选择性遗忘）：暴力 / 独立对照
 *
 * 与 solution.kt 不共享任何代码，三条独立手段：
 *   ① 定义级全序列枚举：t ≤ 7 时把 10^t 条序列**全部**枚举（精确期望 = Σ|Δ| / 10^t），
 *      与 solution.kt 路径 B 的精确值逐 t 对拍；t = 50 需要 10^50 条序列，物理不可行
 *      （10^50 ≈ 宇宙年龄的秒数的 10^32 倍），所以只能缩到 t ≤ 7 做精确对照；
 *   ② 蒙特卡洛：xorshift64 固定种子，t = 5,10,20,50 各抽 10^7 条完整序列
 *      （t = 50 时 5×10^8 个「回合」，属于能实际跑完的最大规模）；
 *   ③ 题面示例序列（1,2,4,6,1,8,10,2,4,1）的分数复算。
 *
 * 三条手段的对照目标写在 EXPECTED / 断言里（数值来自 solution.kt 的实跑输出，
 * 其中 t ≤ 10 是精确有理数 Σ|Δ|/10^t）。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0298/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.abs
import kotlin.math.sqrt

private const val CAP = 5
private const val DIGITS = 10

/** solution.kt 路径 B 给出的精确值 Σ|Δ|（t ≤ 10，分母 10^t）；t ≤ 7 恒为 0。 */
private val EXACT_NUMERATOR = longArrayOf(
    0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1209600L, 44755200L, 986428800L,
)

/** solution.kt 给出的 E|L−R|（t = 5,10,20,50），用作蒙特卡洛的中心值。 */
private val DP_EXPECT = mapOf(
    5 to 0.0,
    10 to 0.098642880000,
    20 to 0.798841914400,
    50 to 1.7688229423801100584,
)

// ───────────────────────── ① 定义级全序列枚举（精确） ─────────────────────────

/**
 * 枚举全部 10^turns 条序列，返回 Σ|L − R|（整数）。
 * 迭代式？不——用按深度预分配现场数组的 DFS：每个节点只做栈式写入，不做任何分配。
 * Larry 记忆 lary[0..lk) 按「最近被叫」排序；Robin 队列 robin[0..rk) 按「进入时间」排序。
 */
private fun exactEnumeration(turns: Int): Long {
    val laryStack = Array(turns + 1) { IntArray(CAP) }
    val robinStack = Array(turns + 1) { IntArray(CAP) }
    var sum = 0L

    fun rec(depth: Int, lk: Int, rk: Int, ls: Int, rs: Int) {
        if (depth == turns) {
            sum += abs(ls - rs)
            return
        }
        val cur = laryStack[depth]
        val curR = robinStack[depth]
        val nxt = laryStack[depth + 1]
        val nxtR = robinStack[depth + 1]
        for (d in 1..DIGITS) {
            var nlk = lk
            var nrk = rk
            var nls = ls
            var nrs = rs
            // Larry：命中则提到最近端；未命中则插到最近端、满 5 丢最久未叫（末端）
            var hit = -1
            var i = 0
            while (i < lk) {
                if (cur[i] == d) { hit = i; break }
                i++
            }
            if (hit >= 0) {
                nls++
                nxt[0] = d
                var w = 1
                for (j in 0 until lk) if (j != hit) nxt[w++] = cur[j]
            } else {
                nxt[0] = d
                val keep = minOf(lk, CAP - 1)
                for (j in 0 until keep) nxt[j + 1] = cur[j]
                nlk = keep + 1
            }
            // Robin：命中则队列不变；未命中则压队尾、满 5 丢队首（最早进入）
            hit = -1
            i = 0
            while (i < rk) {
                if (curR[i] == d) { hit = i; break }
                i++
            }
            if (hit >= 0) {
                nrs++
                for (j in 0 until rk) nxtR[j] = curR[j]
            } else if (rk == CAP) {
                for (j in 1 until CAP) nxtR[j - 1] = curR[j]
                nxtR[CAP - 1] = d
            } else {
                for (j in 0 until rk) nxtR[j] = curR[j]
                nxtR[rk] = d
                nrk = rk + 1
            }
            rec(depth + 1, nlk, nrk, nls, nrs)
        }
    }
    rec(0, 0, 0, 0, 0)
    return sum
}

// ───────────────────────── ② 蒙特卡洛（固定种子） ─────────────────────────

private class McResult(val mean: Double, val sd: Double, val trials: Int) {
    val se: Double get() = sd / sqrt(trials.toDouble())
}

/** 一条序列的定义级模拟，返回 |L − R|；全部就地更新，无分配。 */
private fun monteCarlo(turns: Int, trials: Int, seed: Long): McResult {
    val lary = IntArray(CAP)
    val robin = IntArray(CAP)
    var s = seed or 1L
    var sum = 0L
    var sum2 = 0.0
    repeat(trials) {
        var lk = 0
        var rk = 0
        var ls = 0
        var rs = 0
        for (t in 0 until turns) {
            s = s xor (s shl 13)
            s = s xor (s ushr 7)
            s = s xor (s shl 17)
            val d = ((s ushr 33) % DIGITS).toInt() + 1
            var hit = -1
            var i = 0
            while (i < lk) {
                if (lary[i] == d) { hit = i; break }
                i++
            }
            if (hit >= 0) {
                ls++
                var w = hit
                while (w > 0) { lary[w] = lary[w - 1]; w-- }
                lary[0] = d
            } else {
                var w = if (lk == CAP) CAP - 1 else lk
                while (w > 0) { lary[w] = lary[w - 1]; w-- }
                lary[0] = d
                if (lk < CAP) lk++
            }
            hit = -1
            i = 0
            while (i < rk) {
                if (robin[i] == d) { hit = i; break }
                i++
            }
            if (hit >= 0) {
                rs++
            } else if (rk == CAP) {
                var w = 1
                while (w < CAP) { robin[w - 1] = robin[w]; w++ }
                robin[CAP - 1] = d
            } else {
                robin[rk] = d
                rk++
            }
        }
        val v = abs(ls - rs)
        sum += v
        sum2 += v.toDouble() * v
    }
    val mean = sum.toDouble() / trials
    val varSample = (sum2 / trials - mean * mean).coerceAtLeast(0.0)
    return McResult(mean, sqrt(varSample), trials)
}

// ───────────────────────── ③ 题面示例序列复算 ─────────────────────────

private fun exampleScores(): Pair<Int, Int> {
    val seq = intArrayOf(1, 2, 4, 6, 1, 8, 10, 2, 4, 1)
    val lary = IntArray(CAP)
    val robin = IntArray(CAP)
    var lk = 0
    var rk = 0
    var ls = 0
    var rs = 0
    for (d in seq) {
        var hit = -1
        var i = 0
        while (i < lk) {
            if (lary[i] == d) { hit = i; break }
            i++
        }
        if (hit >= 0) {
            ls++
            var w = hit
            while (w > 0) { lary[w] = lary[w - 1]; w-- }
            lary[0] = d
        } else {
            var w = if (lk == CAP) CAP - 1 else lk
            while (w > 0) { lary[w] = lary[w - 1]; w-- }
            lary[0] = d
            if (lk < CAP) lk++
        }
        hit = -1
        i = 0
        while (i < rk) {
            if (robin[i] == d) { hit = i; break }
            i++
        }
        if (hit >= 0) {
            rs++
        } else if (rk == CAP) {
            var w = 1
            while (w < CAP) { robin[w - 1] = robin[w]; w++ }
            robin[CAP - 1] = d
        } else {
            robin[rk] = d
            rk++
        }
    }
    return ls to rs
}

private fun best(tag: String, expected: Long, rounds: Int = 3, f: () -> Long): Double {
    var bestMs = Double.MAX_VALUE
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${r + 1} 轮漂移：$out ≠ $expected" }
        if (ms < bestMs) bestMs = ms
    }
    println("   $tag：%.1f ms（$rounds 轮最优）".format(bestMs))
    return bestMs
}

fun main() {
    println("== ① 题面示例序列复算 ==")
    val (ls, rs) = exampleScores()
    check(ls == 2 && rs == 3) { "示例序列应为 L = 2、R = 3，实得 $ls、$rs" }
    println("   L = $ls，R = $rs（题面表格：2、3）✓")

    println()
    println("== ② 定义级全序列枚举（精确）==")
    val mc = MathContext(30)
    for (t in 1..8) {
        val sum = exactEnumeration(t)
        check(sum == EXACT_NUMERATOR[t]) { "t = $t：枚举 $sum ≠ 精确 ${EXACT_NUMERATOR[t]}" }
        val e = BigDecimal(sum).divide(BigDecimal(10).pow(t), mc)
        println("   t = $t：Σ|Δ| = $sum ｜ E|L−R| = ${e.toPlainString()}（10^$t 条全枚举，与 DP 精确值一致）")
    }
    println("   t ≤ 7 时两种策略的分数差恒为 0（要等第 8 轮才可能出现分歧），t = 8 首次非零。")
    println("   t = 9（10^9 条）起枚举进入十秒/分钟级；t = 50 需 10^50 条序列，物理不可行，")
    println("   下面用蒙特卡洛覆盖大 t。")

    println()
    println("== ③ 蒙特卡洛（xorshift64 固定种子，每档 10^7 条完整序列）==")
    for (t in intArrayOf(5, 10, 20, 50)) {
        val trials = 10_000_000
        val res = monteCarlo(t, trials, 0x5EED0298L + t)
        val target = DP_EXPECT.getValue(t)
        val dev = abs(res.mean - target)
        val sigma = if (res.se == 0.0) "样本零方差" else "%.1fσ".format(dev / res.se)
        println(
            "   t = %2d：蒙特卡洛 %.6f ± %.6f（5σ）；DP 精确 %.12f；偏差 %.6f（%s）"
                .format(t, res.mean, 5 * res.se, target, dev, sigma),
        )
        check(if (res.se == 0.0) dev == 0.0 else dev < 6 * res.se) { "t = $t 的蒙特卡洛偏离 DP 超过 6σ" }
    }
    // t ≤ 7 时 |Δ| 恒为 0（分数差要等到第 8 轮才可能出现），蒙特卡洛应当一个非零样本都没有
    check(monteCarlo(7, 2_000_000, 0x298298L).mean == 0.0) { "t = 7 竟然出现了非零 |Δ|" }
    println("   t = 7：200 万条模拟的 |Δ| 全为 0（与 DP 的 E|Δ| = 0 一致）✓")

    println()
    println("== ④ 计时 ==")
    best("全序列枚举 t = 6（10^6 条，对拍档）", EXACT_NUMERATOR[6]) { exactEnumeration(6) }
    best("全序列枚举 t = 7（10^7 条）", EXACT_NUMERATOR[7]) { exactEnumeration(7) }
    best(
        "全序列枚举 t = 8（10^8 条，首次非零；**meta 的 bruteForceBaselineMs 口径**）",
        EXACT_NUMERATOR[8],
        rounds = 1,
    ) { exactEnumeration(8) }
    var bestMc = Double.MAX_VALUE
    var mc50 = 0.0
    repeat(1) {
        val t0 = System.nanoTime()
        val r = monteCarlo(50, 10_000_000, 0x5EED0298L + 50)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestMc) bestMc = ms
        mc50 = r.mean
        println("   蒙特卡洛 t = 50、10^7 条（单轮；只有约 ±0.002 精度）：%.1f ms ｜ 估计 %.6f".format(ms, r.mean))
    }

    println()
    println("== ⑤ 汇总 ==")
    println("   定义级枚举能给出的最强精确对照：t = 7，Σ|Δ| = 0 ⇒ E|L−R| = 0（与 DP 一致）")
    println(
        "   50 轮的暴力模拟（10^7 条）估计 %.6f，与 DP 精确值 1.76882294238 相差 %.6f——模拟到不了 8 位小数。"
            .format(mc50, abs(mc50 - 1.76882294238)),
    )
}
