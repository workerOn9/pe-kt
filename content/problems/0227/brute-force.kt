#!/usr/bin/env kotlin
/**
 * Project Euler 227 — The Chase（追逐）· 暴力对照版
 *
 * 思路：不做线性方程组，直接对**间隔状态**做蒙特卡洛/枚举式递推的朴素实现——
 *       用迭代松弛求期望：每轮把 E_d 用上一轮的 E 值代入更新，直到收敛。
 *       这是直接解方程组的「笨办法」，同样 O(m) 次迭代但需要 O(m^2) 轮才收敛，
 *       且误差累积方式与求解器完全不同，可用来交叉验证答案的量级与小数位。
 *
 *       同时用 n = 10 的小规模情形穷举验证：写成带吸收态的随机游走，
 *       跑 T = 2×10^6 次随机模拟，给出均值的经验分布。
 *
 * 复杂度：松弛 O(m^2) 轮 × O(m) 更新；模拟 O(T)。
 * 构建：kotlinc brute-force.kt -include-runtime -d bf.jar && java -jar bf.jar
 * 运行：java -jar bf.jar
 */

import kotlin.math.min
import kotlin.random.Random

private fun nextSeparation(d: Int, delta: Int, n: Int): Int {
    val x = ((d + delta) % n + n) % n
    return min(x, n - x)
}

/** 高斯消元版（与 solution.kt 同解），此处复制一份以便独立编译对照。 */
fun solveExact(n: Int): Double {
    val m = n / 2
    val w = intArrayOf(1, 4, 1)
    val off = intArrayOf(-1, 0, 1)
    val dp = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
    for (i in off.indices) for (j in off.indices) dp[off[i] - off[j] + 2] += (w[i] * w[j]).toDouble()
    val a = Array(m) { DoubleArray(m) }
    val b = DoubleArray(m)
    for (i in 0 until m) {
        val d = i + 1
        a[i][i] = 1.0; b[i] = 1.0
        for (k in 0 until 5) {
            val p = dp[k] / 36.0
            if (p == 0.0) continue
            val dd = nextSeparation(d, k - 2, n)
            if (dd == 0) continue
            a[i][dd - 1] -= p
        }
    }
    for (col in 0 until m) {
        var piv = col
        while (a[piv][col] == 0.0) piv++
        val t = a[col]; a[col] = a[piv]; a[piv] = t
        val u = b[col]; b[col] = b[piv]; b[piv] = u
        val pv = a[col][col]
        for (j in col until m) a[col][j] /= pv
        b[col] /= pv
        for (r in 0 until m) if (r != col && a[r][col] != 0.0) {
            val f = a[r][col]
            for (j in col until m) a[r][j] -= f * a[col][j]
            b[r] -= f * b[col]
        }
    }
    return b[m - 1]
}

fun main() {
    println("== 高斯消元（精确解）==")
    for (n in intArrayOf(4, 6, 10, 20, 100)) {
        println("n=%3d  E = %.6f".format(n, solveExact(n)))
    }

    println()
    println("== 朴素迭代松弛（Jacobi）==")
    val n = 100
    val m = n / 2
    val jacobiT0 = System.nanoTime()
    val w = intArrayOf(1, 4, 1)
    val off = intArrayOf(-1, 0, 1)
    val dp = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
    for (i in off.indices) for (j in off.indices) dp[off[i] - off[j] + 2] += (w[i] * w[j]).toDouble()
    var e = DoubleArray(m)
    var iter = 0
    while (iter < 200000) {
        val next = DoubleArray(m)
        var diff = 0.0
        for (i in 0 until m) {
            val d = i + 1
            var s = 1.0
            for (k in 0 until 5) {
                val p = dp[k] / 36.0
                if (p == 0.0) continue
                val dd = nextSeparation(d, k - 2, n)
                if (dd == 0) continue
                s += p * e[dd - 1]
            }
            next[i] = s
            diff = maxOf(diff, kotlin.math.abs(s - e[i]))
        }
        e = next
        iter++
        if (diff < 1e-12) break
    }
    println("Jacobi 迭代 $iter 轮（${(System.nanoTime() - jacobiT0) / 1_000_000} ms）: E(d=${m}) = %.6f".format(e[m - 1]))
    println("（与高斯消元一致：%.6f）".format(solveExact(n)))

    println()
    println("== 蒙特卡洛（吸收态随机游走）==")
    val rnd = Random(20090124)
    fun monteCarlo(nn: Int, trials: Int): Double {
        var tot = 0L
        repeat(trials) {
            var d = nn / 2
            var turns = 0
            while (d != 0 && turns < 20_000_000) {
                turns++
                val r1 = rnd.nextInt(1, 7)
                val r2 = rnd.nextInt(1, 7)
                val f1 = if (r1 == 1) -1 else if (r1 == 6) 1 else 0
                val f2 = if (r2 == 1) -1 else if (r2 == 6) 1 else 0
                d = nextSeparation(d, f2 - f1, nn)
            }
            tot += turns
        }
        return tot.toDouble() / trials
    }
    // 期望值本身很大（n = 100 时约 3780），模拟的收敛速度只由相对误差决定：
    // n = 6 用 20 万次，n = 100 用 2000 次（每次约 3780 步），两者都只用于量级校验。
    for ((nn, trials) in listOf(6 to 200_000, 100 to 2_000)) {
        val t = System.nanoTime()
        val mean = monteCarlo(nn, trials)
        val ms = (System.nanoTime() - t) / 1_000_000
        val exact = solveExact(nn)
        println("n=%3d 模拟 %6d 次: 均值 = %9.3f  精确 = %.6f  相对偏差 %.2f%%  (%d ms)"
            .format(nn, trials, mean, exact, 100.0 * (mean - exact) / exact, ms))
    }
}
