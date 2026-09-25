#!/usr/bin/env kotlin
/**
 * Project Euler 226 — A Scoop of Blancmange（一勺奶冻）
 *
 * 思路：奶冻曲线 B(x) = Σ_{n>=0} s(2^n x)/2^n（s 为到最近整数的距离），塔卡吉函数。
 *       圆 C 的圆心 (1/4,1/2)、半径 1/4，占据 x ∈ [0,1/2]；在列 x 上圆的竖直截面为
 *       [1/2 - h(x), 1/2 + h(x)]，h(x) = sqrt(1/16 - (x-1/4)^2)。
 *       曲线下方的点又要求 y <= B(x)，而 [0,1/2] 上 B(x) <= 1/2 + h(x) 恒成立
 *       （B 的最大值 2/3，圆顶在 x=1/4 处高达 3/4；数值扫描也确认无交叉），
 *       故所求面积
 *           I = ∫_0^{1/2} [ B(x) + h(x) - 1/2 ]_+ dx
 *
 *       数值难点：B 处处不可微、振荡幅度随尺度衰减，直接做黎曼和误差 ~ m·2^{-m}，
 *       要 1e-9 精度需要 2^30 级采样。本解按二进制自相似结构处理：
 *       把 [0,1/2] 切成 2^{m-1} 个长 2^{-m} 的小区间，在区间内
 *           B(x) = T_m(x) + 2^{-m} · B(frac(2^m x))，
 *       T_m 是前 m 项之和、在区间上是**直线**（斜率是整数 b_k），尾巴项在整区间上的积分恒为
 *           2^{-m} · 2^{-m} · ∫_0^1 B = 2^{-2m}/2。
 *       于是逐区间：
 *         1) T_m + h - 1/2 在区间内恒正（凹函数，最小值在端点）-> 整段可解析积分：
 *            直线部分 + 圆弓形原函数 + 尾巴均值，误差只有浮点级；
 *         2) 最大值加上尾巴上界 2^{-m}·(2/3) 仍为负 -> 整段贡献 0；
 *         3) 只有跨过零线的极少数区间（m = 20 时仅 2 个）用 256 段精细 Simpson
 *            直接对精确 B 求积，其贡献本身是 O(2^{-2m}) 量级，误差可忽略。
 *       取 m = 20（约 52 万个区间）即可稳定到 1e-12：I = 0.113160169518...
 *
 * 结果：0.11316017（八位小数）。meta.json 按仓库惯例存整数：round(I × 10^8) = 11316017。
 * 复杂度：O(2^m)，m = 20，实测约 40 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */
import kotlin.math.*

/** 奶冻曲线 B(x)；x 为二进制有理数（double）时该级数在浮点意义下精确。 */
fun blancmange(x: Double): Double {
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

/** 圆下半弧高度 h(x) = sqrt(1/16 - (x-1/4)^2)（圆外取 0）。 */
fun circleLower(x: Double): Double {
    val t = 0.0625 - (x - 0.25) * (x - 0.25)
    return if (t > 0.0) sqrt(t) else 0.0
}

/** ∫ h dx 的原函数：(t/2)sqrt(r^2-t^2) + (r^2/2)asin(t/r)，t = x-1/4，r = 1/4。 */
fun circleLowerIntegral(x: Double): Double {
    val r = 0.25
    val t = (x - 0.25).coerceIn(-r, r)
    return 0.5 * t * sqrt(max(0.0, r * r - t * t)) + 0.5 * r * r * asin(t / r)
}

/** 被圆围住的曲线下方面积，m 为区间层数（20 已足够 1e-12）。 */
fun scoopArea(m: Int = 20): Double {
    val intervals = 1 shl (m - 1)
    val hh = 2.0.pow(-m)
    val tailBound = (2.0 / 3.0) * hh
    var total = 0.0
    for (k in 0 until intervals) {
        val x0 = k * hh
        val x1 = x0 + hh
        // T_m(x0)（x0 为二进制有理数，尾巴为 0，故等于 B(x0)）与区间斜率 b
        var r = x0
        var v = 0.0
        var w = 1.0
        var plus = 0
        for (i in 0 until m) {
            v += (if (r <= 0.5) r else 1.0 - r) * w
            if (r < 0.5) plus++
            w *= 0.5
            r *= 2.0
            if (r >= 1.0) r -= 1.0
        }
        val b = (2 * plus - m).toDouble()
        val f0 = v + circleLower(x0) - 0.5
        val f1 = v + b * hh + circleLower(x1) - 0.5
        val fmin = min(f0, f1)
        var fmax = max(f0, f1)
        val xc = 0.25 + 0.25 * b / sqrt(1.0 + b * b)   // 凹函数极值点（h' = -b）
        if (xc > x0 && xc < x1) fmax = max(fmax, v + b * (xc - x0) + circleLower(xc) - 0.5)
        if (fmax + tailBound < 0.0) continue
        if (fmin > 0.0) {
            total += v * hh + b * hh * hh / 2.0 + (circleLowerIntegral(x1) - circleLowerIntegral(x0)) -
                0.5 * hh + hh * hh / 2.0
        } else {
            val steps = 256
            val sub = hh / steps
            var acc = 0.0
            for (j in 0..steps) {
                val x = x0 + j * sub
                var g = blancmange(x) + circleLower(x) - 0.5
                if (g < 0.0) g = 0.0
                val wt = if (j == 0 || j == steps) 1 else if (j % 2 == 0) 2 else 4
                acc += wt * g
            }
            total += acc * sub / 3.0
        }
    }
    return total
}

fun main() {
    for (m in intArrayOf(14, 18, 20, 22)) {
        val a = scoopArea(m)
        println("m=$m  area = ${"%.12f".format(a)}  ->  ${Math.round(a * 1e8)}")
    }
    val answer = Math.round(scoopArea(20) * 1e8)
    println("PE 226 answer = $answer  (0.11316017)")
    check(answer == 11_316_017L) { "226 mismatch: $answer" }
}
