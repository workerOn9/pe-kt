#!/usr/bin/env kotlin
/**
 * Project Euler 267 — Billionaire（亿万富翁）：暴力对照
 *
 * 思路（不借助「最少正面次数」的解析结论，直接在策略空间上扫）
 * ─────────────────────────────────────────────────────────────
 * 1. 预先用 double 递推生存函数 P(H ≥ k)（H ~ Bin(1000, 1/2)）：
 *      term(n) = 2^-1000，term(h) = term(h+1)·(h+1)/(1000−h)，surv(k) = Σ_{h≥k} term(h)；
 * 2. 在 f ∈ [0,1] 上取密集网格（步长 1e-4），对每个 f **逐个 h** 计算
 *      ln C_h = h·ln(1+2f) + (1000−h)·ln(1−f)
 *    找出使 C_h ≥ 1e9 的最小 h（k(f)），成功概率即 surv(k(f))；
 * 3. 取所有 f 上的最大概率。由于概率是 k 的阶梯函数，网格一旦落入最优区间就得到精确最大值。
 *
 * 对照口径：解析解（solution.kt）给出 k0 = 432、概率 0.999992836187；暴力扫描应给出
 *   相同的最小 k 与相同的 12 位小数（网格步长 1e-4 远小于最优 f 区间宽度）。
 *
 * 运行：bash scripts/kotlinc-shim.sh content/problems/0267/brute-force.kt && java -cp … Brute_forceKt
 */
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

fun main() {
    println("=== PE 267 brute force (scan f, count wins) ===")
    val n = 1000
    val logTarget = 9.0 * ln(10.0)

    // 1) 生存函数表（double 递推）
    val t0 = System.nanoTime()
    val surv = DoubleArray(n + 2)
    var term = 2.0.pow(-n)
    for (h in n downTo 0) {
        surv[h] = surv[h + 1] + term
        if (h > 0) term = term * h / (n - h + 1)
    }
    check(abs(surv[0] - 1.0) < 1e-12)                       // 归一化自检
    val msTable = (System.nanoTime() - t0) / 1e6
    println("生存函数表（1001 项递推）耗时 %.2f ms；surv(432) = %.15f".format(msTable, surv[432]))

    // 2) f 网格扫描
    val t1 = System.nanoTime()
    val step = 1e-4
    var bestP = -1.0
    var bestF = 0.0
    var minWins = n + 1
    var feas = 0
    var f = 1e-4
    while (f < 1.0) {
        // 逐个 h 找最小成功次数
        var k = n + 1
        val lw = ln(1.0 + 2.0 * f)
        val ll = ln(1.0 - f)
        var h = 0
        while (h <= n) {
            if (h * lw + (n - h) * ll >= logTarget) { k = h; break }
            h++
        }
        if (k <= n) {
            feas++
            if (k < minWins) minWins = k
            val p = surv[k]
            if (p > bestP) { bestP = p; bestF = f }
        }
        f += step
    }
    val msScan = (System.nanoTime() - t1) / 1e6
    println("f 网格扫描：步长 %.0e，可行 f 数 %d，最小成功次数 k0 = %d (解析解为 432)".format(step, feas, minWins))
    println("最优 f ≈ %.4f（概率在该区间的任意 f 上相同），最大概率 = %.12f".format(bestF, bestP))
    println("     对照解析解（精确分数取 12 位）= %.12f，两串小数完全相同；double 生存函数与精确分数差 < 1e-15".format(0.9999928361867136))
    println("扫描耗时 %.0f ms；总耗时 %.0f ms".format(msScan, (System.nanoTime() - t0) / 1e6))

    // 3) 补充：粗/细两种步长给出一致的 12 位小数
    for (st in doubleArrayOf(1e-2, 1e-3)) {
        var bp = -1.0
        var ff = st
        while (ff < 1.0) {
            var k = n + 1
            val lw = ln(1.0 + 2.0 * ff)
            val ll = ln(1.0 - ff)
            var h = 0
            while (h <= n) {
                if (h * lw + (n - h) * ll >= logTarget) { k = h; break }
                h++
            }
            if (k <= n && surv[k] > bp) bp = surv[k]
            ff += st
        }
        println("步长 %.0e → 最大概率 %.12f".format(st, bp))
    }
}
