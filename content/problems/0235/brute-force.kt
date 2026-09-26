#!/usr/bin/env kotlin
/**
 * Project Euler 235 — 暴力对照：直接定义式求和 + 二分
 *
 * 与 solution.kt 的区别：这里完全按定义逐项算 (900−3k)·r^{k−1}，不用 Horner、不用闭式，
 * 也不做任何稳定性优化。r > 1.002 时 r^{k−1} 到 k = 5000 已是 10⁵ 量级，
 * 而总和只有 6×10¹¹——浮点抵消正是在这里发生的。方法更“诚实”，但精度略差，
 * 用来验证 Horner 版与闭式版给出的 12 位结果没有系统性偏差。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar
 */

private const val N = 5000
private const val TARGET = -600_000_000_000.0

fun direct(r: Double): Double {
    var s = 0.0
    var p = 1.0
    for (k in 1..N) {
        s += (900 - 3 * k) * p
        p *= r
    }
    return s
}

fun main() {
    var lo = 1.0
    var hi = 1.5
    repeat(200) {
        val mid = (lo + hi) / 2
        if (direct(mid) > TARGET) lo = mid else hi = mid
    }
    val r = (lo + hi) / 2
    println("brute r = %.15f".format(r))
    println("brute r (12 dp) = " + "%.12f".format(r))
    println("answer = " + Math.round(r * 1e12))
}
