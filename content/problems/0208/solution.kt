#!/usr/bin/env kotlin
// PE 208 — Robot Walks（机器人行走）
// 思路（完整推导见 analysis.md）：以 72° 为单位的朝向类 h_i（h_0 = 0 正北），转向 ε_i = ±1，
//       h_{i+1} = h_i + ε_i。第 i 段弧的净位移是它的「弦」，弦方向 = 弧的中点朝向 h_i + ε_i/2，
//       故闭合 ⟺ Σ_i ω^{k_i} = 0（ω = e^{2πi/5}，k_i = h_i − (1−ε_i)/2 为弦方向类）。
//       5 次单位根的极小多项式是 4 次，于是这等价于「五个弦方向类各出现 n/5 = 14 次」。
//       本题实现改用与之等价的部分和写法（两套 DP 在 n = 5..30、70 上计数完全相同，
//       并经真实几何暴力核对）：S_j = 前 j 段中某方向的步数 (mod 5)，δ ∈ {0,1}，
//       S_{j+1} = (S_j + δ) mod 5，闭合同样 ⟺ S_1..S_n 各类计数均匀。
//       DP 状态 = (当前 S mod 5, 各类已用次数 c1..c4)（c0 = j − Σci 隐式），超限分支剪掉。
// 复杂度：O(n · 5 · C(n/5+3,4)) 时间与空间 —— n=70 时 ≈ 2×10^6 状态步，<1 s。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

import kotlin.math.abs

fun main() {
    val n = 70
    val half = n / 5                 // 每个剩余类目标次数 14
    // dp key: r*P1 + 编码 c1,c2,c3,c4（0..14，5 位足够）→ 用 Long 编码
    // c0 = j - (c1+c2+c3+c4)
    var dp = HashMap<Long, Long>()
    dp[encode(0, 0, 0, 0, 0)] = 1L
    for (j in 1..n) {
        val nd = HashMap<Long, Long>()
        for ((key, v) in dp) {
            val r = ((key shr 20) and 0xF).toInt()
            val c1 = ((key shr 15) and 0xF).toInt()
            val c2 = ((key shr 10) and 0xF).toInt()
            val c3 = ((key shr 5) and 0xF).toInt()
            val c4 = (key and 0xF).toInt()
            for (t in 0..1) {                      // 部分和模型：δ∈{0,1}，S_{j+1} = (S_j + t) mod 5
                val r2 = (r + t) % 5
                var a1 = c1; var a2 = c2; var a3 = c3; var a4 = c4
                when (r2) {
                    1 -> a1++; 2 -> a2++; 3 -> a3++; 4 -> a4++
                }
                if (a1 > half || a2 > half || a3 > half || a4 > half) continue
                val s = a1 + a2 + a3 + a4
                val c0 = j - s
                if (c0 > half || c0 < 0) continue
                nd.merge(encode(r2, a1, a2, a3, a4), v, Long::plus)
            }
        }
        dp = nd
    }
    // 闭合条件：每个剩余类恰 half 次（含 c0 —— 上面已保证 ≤ half，此时 j = n = 5·half，
    //  c0 = n - Σci，要求 = half ⟺ Σci = 4·half ⟺ 各 ci = half）
    var total = 0L
    for ((key, v) in dp) {
        val c1 = ((key shr 15) and 0xF).toInt()
        val c2 = ((key shr 10) and 0xF).toInt()
        val c3 = ((key shr 5) and 0xF).toInt()
        val c4 = (key and 0xF).toInt()
        if (c1 == half && c2 == half && c3 == half && c4 == half) total += v
    }
    println(total)
}

private fun encode(r: Int, c1: Int, c2: Int, c3: Int, c4: Int): Long =
    (r.toLong() shl 20) or (c1.toLong() shl 15) or (c2.toLong() shl 10) or
        (c3.toLong() shl 5) or c4.toLong()

// 抑制未使用告警的占位（保持文件自洽可独立编译）
private val unused = abs(0)
