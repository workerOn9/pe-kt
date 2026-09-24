#!/usr/bin/env kotlin
// PE 208 暴力参照：真实几何模拟——逐段累加「弦向量」，只对 n ≤ 20 可行。
// 第 i 段弧：朝向类 h_i（单位 72°），转向 eps_i ∈ {+1,−1}，弦方向 = 弧的中点朝向 h_i + eps_i/2。
// 同时统计所有「闭合」路径的弦方向类直方图，用来验证「闭合 ⟺ 五个弦类各 n/5 次」。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

fun main() {
    for (n in intArrayOf(5, 10, 15, 20)) {
        val t0 = System.nanoTime()
        var closed = 0
        var unbalanced = 0
        for (bits in 0 until (1 shl n)) {
            var x = 0.0
            var y = 0.0
            var h = 0                                  // 朝向类，单位 72°
            val hist = IntArray(5)                     // 弦方向类直方图
            for (i in 0 until n) {
                val eps = if ((bits shr i) and 1 == 1) 1 else -1
                val ang = Math.toRadians(90.0 + 72.0 * (h + eps / 2.0))
                x += cos(ang)
                y += sin(ang)
                hist[(h - (1 - eps) / 2 + 5) % 5]++
                h = (h + eps + 5) % 5
            }
            if (abs(x) < 1e-9 && abs(y) < 1e-9) {
                closed++
                if (hist.any { it != n / 5 }) unbalanced++
            }
        }
        val ms = (System.nanoTime() - t0) / 1_000_000
        println("n=$n: 闭合路径 $closed 条，其中弦类非均匀 $unbalanced 条  ($ms ms)")
    }
    println("（题面给出 n=25 的闭合路径数为 70932，本程序 2^25 太大，由 DP 对表）")
}
