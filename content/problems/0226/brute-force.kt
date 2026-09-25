#!/usr/bin/env kotlin
/**
 * PE 226 暴力对照 —— 不做自相似区间解析，直接把 [0,1/2] 均匀切成 2^m 段做复合 Simpson：
 *   被积函数 g(x) = [B(x) + h(x) - 1/2]_+，其中 B 用精确定义逐项求和。
 * 这样能得到正确答案的「前几位」，但 B 是处处不可微的分形函数：误差随 m 只按
 * ~m·2^{-m} 缓慢下降，2^24 段也只能到 1e-7 量级，无法确定八位小数——
 * 这正是 solution.kt 必须走「区间解析 + 尾巴均值」的原因。
 * 运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */
import kotlin.math.*

private fun blancmange(x: Double): Double {
    var r = x - floor(x)
    var s = 0.0
    var w = 1.0
    for (i in 0 until 120) {
        s += (if (r <= 0.5) r else 1.0 - r) * w
        w *= 0.5
        r *= 2.0
        if (r >= 1.0) r -= 1.0
        if (r == 0.0) break
    }
    return s
}

private fun g(x: Double): Double {
    val t = 0.0625 - (x - 0.25) * (x - 0.25)
    val h = if (t > 0.0) sqrt(t) else 0.0
    return max(0.0, blancmange(x) + h - 0.5)
}

private fun simpson(m: Int): Double {
    val steps = 1 shl m
    val sub = 0.5 / steps
    var acc = 0.0
    for (j in 0..steps) {
        val wt = if (j == 0 || j == steps) 1 else if (j % 2 == 0) 2 else 4
        acc += wt * g(j * sub)
    }
    return acc * sub / 3.0
}

fun main() {
    for (m in intArrayOf(10, 14, 18, 22, 24)) {
        val t0 = System.nanoTime()
        val a = simpson(m)
        println("m=%2d  Simpson = %.10f   (${(System.nanoTime() - t0) / 1_000_000} ms)".format(m, a))
    }
    println("目标：0.11316017（八位小数）")
}
