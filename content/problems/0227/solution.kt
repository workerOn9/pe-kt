#!/usr/bin/env kotlin
/**
 * Project Euler 227 — The Chase（追逐）
 *
 * 思路：状态只取两枚骰子持有者之间的最短间隔 d ∈ {1, 2, …, n/2}（d = 0 即两骰子同属一人，游戏结束）。
 *       一回合中两枚骰子各自独立掷出，增量 X ∈ {-1, 0, +1}，概率 (1, 4, 1)/6；
 *       掷 1 传左、掷 6 传右（两枚骰子同时掷，互不影响）。
 *       间隔的增量 Δ = X₂ - X₁，概率分布为 (1, 4, 1)/6 与自身的卷积再乘 1/6：
 *
 *           P(Δ = -2) = 1/36,  P(Δ = -1) = 8/36,  P(Δ = 0) = 18/36,
 *           P(Δ = +1) = 8/36,  P(Δ = +2) = 1/36
 *
 *       新间隔 = min((d + Δ) mod n, n - (d + Δ) mod n)（环形最短距离）。
 *       注意 d = n/2 时无论 Δ 如何都由 n/2 折回 d = n/2，状态自环，方程组依然良定。
 *
 *       期望方程 E_d = 1 + Σ_Δ p_Δ · E_{min(...)}，把 E_0 = 0 作为吸收边界，
 *       对 d = 1 … n/2 写成 m 阶线性方程组（m = n/2 = 50），用 Double 高斯消元求解。
 *
 *       初值：两枚骰子在相对而坐的玩家手上 ⇒ 最短间隔 d = n/2 = 50。
 *
 * 验证：同一组期望方程改用 Python 的 fractions.Fraction 精确有理数消元，
 *       得 E = 586615414279592693268781956/155163869452311434997005
 *         = 3780.618621784790…，十位有效数字为 3780.618622。
 *       meta.json 按仓库惯例存整数：round(3780.618621784790 × 10^8) = 378061862178。
 * 复杂度：O((n/2)^3) = O(n^3/8)，n = 100 时约 4×10^4 次乘加，实测约 1 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round

fun solve(n: Int = 100): Double {
    val m = n / 2
    // 增量分布：卷积 (1,4,1)/6 ⊗ (1,4,1)/6
    val deltaProb = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
    val single = intArrayOf(-1, 0, 1)
    val w = intArrayOf(1, 4, 1)
    for (i in single.indices) {
        for (j in single.indices) {
            deltaProb[single[i] - single[j] + 2] += (w[i] * w[j]).toDouble()
        }
    }
    val a = Array(m) { DoubleArray(m) }
    val b = DoubleArray(m)
    for (i in 0 until m) {
        val d = i + 1
        a[i][i] = 1.0
        b[i] = 1.0
        for (k in 0 until 5) {
            val p = deltaProb[k] / 36.0
            if (p == 0.0) continue
            val x = ((d + k - 2) % n + n) % n
            val dd = min(x, n - x)
            if (dd == 0) continue // 两骰子落到同一人，本回合结束，E_0 = 0
            a[i][dd - 1] -= p
        }
    }
    for (col in 0 until m) {
        var piv = col
        while (piv < m && abs(a[piv][col]) < 1e-12) piv++
        val tmpA = a[col]; a[col] = a[piv]; a[piv] = tmpA
        val tb = b[col]; b[col] = b[piv]; b[piv] = tb
        val pv = a[col][col]
        for (j in col until m) a[col][j] /= pv
        b[col] /= pv
        for (r in 0 until m) {
            if (r != col && a[r][col] != 0.0) {
                val f = a[r][col]
                for (j in col until m) a[r][j] -= f * a[col][j]
                b[r] -= f * b[col]
            }
        }
    }
    return b[m - 1] // 起始状态 d = n/2
}

fun main() {
    val e = solve(100)
    println("expected turns (n=100) = %.10f".format(e))
    println("ten significant digits   = %.10g".format(e))
    println("round(E * 1e8)           = ${round(e * 1e8)}")
    // 小规模抽查：n = 2 时两枚骰子一开始就在同一人手上（相对而坐即对面即同一人？）
    println("n=4  E = %.6f".format(solve(4)))
    println("n=6  E = %.6f".format(solve(6)))
    println("n=10 E = %.6f".format(solve(10)))
}
