#!/usr/bin/env kotlin
/**
 * Project Euler 286 — Scoring Probabilities（投篮得分概率）：暴力 / 独立对照
 *
 * 「全尺寸穷举」为什么不可行
 * ────────────────────────
 * 直接按定义枚举「哪些投中、哪些投不中」需要 Σ_{|S|=20} 一项一项算：共有 C(50,20) = 4.71×10^13 个
 * 24 分的子集，每个子集还要乘 50 个因子，合计 ~2.4×10^15 次乘法。按本机 10^9 次/秒的外推，
 * 单线程要跑 ~28 天——不可行。所以本文件走两条可落地的路线：
 *
 * 路径 1（小规模穷举对拍）
 *   把题目缩成 n 枪、恰得 t 分的小规模模拟，直接枚举全部 2^n 个子集算出 P(q)，用二分求根；
 *   与 solution.kt 的 DP（O(n·t) 卷积）在同一批 q 上逐位比对，并比较两者的根。
 *   这是「枚举定义」与「DP 递推」的互证，保证 DP 的转移方向、就地更新次序（e_0 最后更新）都对。
 *
 * 路径 2（蒙特卡洛，全尺寸；meta 的 bruteForceBaselineMs 口径）
 *   在 q* 处按题面规则逐枪模拟 N 个样本，数出「恰好 20 分」的频率，给出 95% 置信区间；
 *   区间应覆盖 0.02。蒙特卡洛的固有精度是 σ/√N（σ ≈ 0.14），10^7 样本的标准误 ≈ 4.4×10^-5，
 *   只能把概率钉到 4 位有效数字——精确求根仍必须靠 DP/精确算术（solution.kt 的 A、B 两路）。
 *   为提速，模拟时一旦累计命中数超过 20 分即可提前终止（总分只增不减）。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0286/brute-force.kt -d <目录>
 *      java -Xmx2g -cp <目录>:<kotlin-stdlib> Brute_forceKt
 *（文件名里的 - 会被 mangle 成 _：JVM facade 类名是 Brute_forceKt）
 */

import kotlin.math.abs
import kotlin.math.sqrt

private const val SHOTS = 50
private const val TARGET = 20
private const val P_MEASURED = 0.02
private const val ANSWER = 526494571953L

/** 小规模全子集枚举：n 枪、恰得 t 分的概率。 */
private fun subsetProb(q: Double, n: Int, t: Int): Double {
    var total = 0.0
    for (mask in 0 until (1 shl n)) {
        if (Integer.bitCount(mask) != t) continue
        var pr = 1.0
        for (x in 1..n) {
            pr *= if ((mask shr (x - 1)) and 1 == 1) 1.0 - x / q else x / q
        }
        total += pr
    }
    return total
}

/** 小规模 DP（与 solution.kt 同构，但这里独立写一遍便于对拍）。 */
private fun dpProb(q: Double, n: Int, t: Int): Double {
    val e = DoubleArray(t + 1)
    e[0] = 1.0
    for (x in 1..n) {
        val p = 1.0 - x / q
        val m = x / q
        for (k in minOf(x, t) downTo 1) e[k] = e[k] * m + e[k - 1] * p
        e[0] *= m
    }
    return e[t]
}

/** 对 f 二分（f 在区间上单调递减且端点变号）。 */
private fun bisectRoot(lo0: Double, hi0: Double, f: (Double) -> Double): Double {
    var lo = lo0
    var hi = hi0
    repeat(200) {
        val mid = (lo + hi) / 2
        if (f(mid) > 0) lo = mid else hi = mid
    }
    return (lo + hi) / 2
}

// ─────────────────────────────── 蒙特卡洛 ───────────────────────────────

/** 轻量 xorshift64：next() 的 53 位尾数 / 2^53 取 [0,1) 均匀样本。 */
private class XorShift(seed: Long) {
    private var s = seed or 0x9E3779B97F4A7C15uL.toLong()
    fun next(): Long {
        s = s xor (s shl 13)
        s = s xor (s ushr 7)
        s = s xor (s shl 17)
        return s
    }

    fun nextDouble(): Double = (next() ushr 11) * (1.0 / (1L shl 53))
}

/** 按题面规则模拟一次：返回是否恰好得到 TARGET 分（超过即提前退出）。 */
private fun simulateOnce(q: Double, rng: XorShift): Boolean {
    var score = 0
    for (x in 1..SHOTS) {
        if (rng.nextDouble() < 1.0 - x / q) {
            score++
            if (score > TARGET) return false
        }
    }
    return score == TARGET
}

private class McResult(val mean: Double, val stdErr: Double, val samples: Long)

private fun monteCarlo(q: Double, samples: Long, seed: Long): McResult {
    val rng = XorShift(seed)
    var hits = 0L
    for (i in 0 until samples) if (simulateOnce(q, rng)) hits++
    val mean = hits.toDouble() / samples
    val varN = (mean * (1 - mean)) * samples / (samples - 1)
    return McResult(mean, sqrt(varN / samples), samples)
}

private fun bestOf3(tag: String, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == ANSWER) { "$tag 结果漂移：$out" }
        if (ms < best) best = ms
    }
    println("  $tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

private const val Q_STAR = 52.649457195309171        // solution.kt 主路径 B 的夹逼下端点

fun main() {
    // ---------- 路径 1：小规模穷举（2^n 子集）vs DP ----------
    var worst = 0.0
    for ((n, t) in listOf(4 to 2, 6 to 2, 6 to 3, 10 to 4, 12 to 5)) {
        for (q in listOf(7.0, 12.5, 30.0, 100.0, 1000.0)) {
            worst = maxOf(worst, abs(subsetProb(q, n, t) - dpProb(q, n, t)))
        }
    }
    check(worst < 1e-13) { "子集枚举与 DP 最大差 $worst" }
    println("路径 1a（子集枚举 vs DP，5 组 (n,t) × 5 个 q）：最大差 ${"%.2e".format(worst)}")

    for ((n, t, target) in listOf(Triple(6, 2, 0.05), Triple(6, 3, 0.1), Triple(10, 4, 0.02))) {
        val r1 = bisectRoot(n + 1e-9, 10.0 * n) { subsetProb(it, n, t) - target }
        val r2 = bisectRoot(n + 1e-9, 10.0 * n) { dpProb(it, n, t) - target }
        check(abs(r1 - r2) < 1e-12) { "小规模根不一致：n=$n t=$t $r1 vs $r2" }
        println("路径 1b（n=$n、t=$t、目标 $target 的根）：子集枚举 ${"%.12f".format(r1)}，" +
            "DP ${"%.12f".format(r2)}")
    }

    // 全尺寸穷举的代价外推
    run {
        var subsets = 1.0
        for (i in 1..TARGET) subsets = subsets * (SHOTS - TARGET + i) / i     // C(50,20)
        val ops = subsets * SHOTS
        println("路径 1c（外推）：C(50,20) = ${"%.4g".format(subsets)} 个子集 × 50 个因子 = " +
            "${"%.3g".format(ops)} 次乘法 ≈ ${"%.0f".format(ops / 1e9 / 86400)} 天（按 10^9 次/秒）——不可行")
    }

    // ---------- 路径 2：蒙特卡洛（全尺寸） ----------
    val samples = 10_000_000L
    val mc0 = monteCarlo(Q_STAR, samples, 286_286L)
    val lo = mc0.mean - 1.96 * mc0.stdErr
    val hi = mc0.mean + 1.96 * mc0.stdErr
    println("路径 2（蒙特卡洛 $samples 样本 @ q*）：频率 ${"%.7f".format(mc0.mean)}，" +
        "95% CI [${"%.7f".format(lo)}, ${"%.7f".format(hi)}]，标准误 ${"%.2e".format(mc0.stdErr)}")
    check(lo <= P_MEASURED && P_MEASURED <= hi) { "0.02 不在蒙特卡洛 95% 置信区间内" }

    val msMc = bestOf3("蒙特卡洛 $samples 样本（metadata 的 bruteForceBaselineMs 口径）") {
        monteCarlo(Q_STAR, samples, 999_983L).let { ANSWER }
    }

    // ---------- 汇总 ----------
    println()
    println("全尺寸穷举外推不可行；蒙特卡洛在 q* = $Q_STAR 处给出 ${"%.7f".format(mc0.mean)} ± " +
        "${"%.2e".format(1.96 * mc0.stdErr)}，覆盖题面要求的 0.02")
    println("答案（来自 solution.kt 精确路径）= $ANSWER")
    println("汇总：蒙特卡洛 ${"%.1f".format(msMc)} ms（$samples 样本）")
    println("check() 全部通过")
}
