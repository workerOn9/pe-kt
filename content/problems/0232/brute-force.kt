#!/usr/bin/env kotlin
/**
 * Project Euler 232 — The Race：暴力对照（不动点迭代）
 *
 * 思路：完全不使用「按 a、b 递增一趟填表」的拓扑序，也不做同格消元。
 *       把 F、G 的方程组当成定义在 [0,1]^(2·101²) 上的单调算子，从全 0 出发反复整体松弛：
 *
 *           F'[a][b] = ½·H[a][b] + ½·G[a][b]
 *           G'[a][b] = max_T [ P·J_T + (1-P)·F[a][b] ]
 *
 *       H、J_T 全部取自上一轮的整张表（同步更新，不就地写）。算子单调（max 与正系数
 *       线性组合），从下方出发必然单调递增收敛到最小不动点，即所求概率。
 *       代价是 O(轮数 × 状态数)，轮数约 10^4——比 solution.kt 的消元填表慢两个数量级，
 *       但完全不依赖「谁依赖谁」的顺序分析，因此是一份真正独立的对照实现。
 *
 * 用途：与 solution.kt 的消元填表对拍，threshold = 100 上两者必须逐位一致。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar
 */

private const val TMAX = 8


fun valueIteration(threshold: Int, tmax: Int, tol: Double = 1e-15): Triple<Double, Int, Double> {
    val n = threshold
    var f = Array(n + 1) { DoubleArray(n + 1) }
    var g = Array(n + 1) { DoubleArray(n + 1) }
    var rounds = 0
    var delta = 1.0
    while (rounds < 200000) {
        rounds++
        val nf = Array(n + 1) { DoubleArray(n + 1) }
        val ng = Array(n + 1) { DoubleArray(n + 1) }
        for (a in 1..n) {
            for (b in 1..n) {
                val h = if (a <= 1) 0.0 else g[a - 1][b]
                var best = -1.0
                for (t in 1..tmax) {
                    val prob = Math.pow(2.0, -t.toDouble())
                    val gain = 1 shl (t - 1)
                    val j = if (gain >= b) 1.0 else f[a][b - gain]
                    val v = prob * j + (1.0 - prob) * f[a][b]
                    if (v > best) best = v
                }
                ng[a][b] = best
                nf[a][b] = 0.5 * (h + best)
            }
        }
        delta = 0.0
        for (a in 0..n) {
            for (b in 0..n) {
                val d1 = Math.abs(nf[a][b] - f[a][b])
                val d2 = Math.abs(ng[a][b] - g[a][b])
                if (d1 > delta) delta = d1
                if (d2 > delta) delta = d2
            }
        }
        f = nf
        g = ng
        if (delta < tol) break
    }
    return Triple(f[n][n], rounds, delta)
}

fun main() {
    val t0 = System.currentTimeMillis()
    println("--- 小阈值结构性检查（收敛轮数随阈值增长）")
    for (th in intArrayOf(1, 2, 5, 10, 25, 50)) {
        val (v, r, d) = valueIteration(th, TMAX)
        println("  threshold=%-3d -> %.12f  (rounds=%d, delta=%.2e)".format(th, v, r, d))
    }
    println("小规模用时 ${System.currentTimeMillis() - t0} ms")

    val t1 = System.currentTimeMillis()
    val (p, rounds, d) = valueIteration(100, TMAX)
    println("--- threshold=100 不动点迭代：%.15f (rounds=%d, delta=%.2e, %d ms)"
        .format(p, rounds, d, System.currentTimeMillis() - t1))
    println("rounded to 8 dp = %.8f".format(p))
    println("answer = " + Math.round(p * 1e8))
    println("总用时 ${System.currentTimeMillis() - t0} ms")
}
