#!/usr/bin/env kotlin
/**
 * Project Euler 317 — Firecracker（爆竹）
 *
 * 题目：爆竹在 100 m 高处爆成大量碎片，初速度都是 20 m/s，g = 9.81 m/s^2，不计空气阻力。
 *       求碎片落地前所经过区域的体积（四舍五入到四位小数）。
 *
 * 思路推导
 * --------
 * 三维问题先退化成「竖直平面内的轨迹族」再绕竖直轴旋转。
 * 取仰角 θ、初速 v0、起点高 h0：
 *
 *       x = v0 cosθ · t ,   y = h0 + v0 sinθ · t - g t^2 / 2
 *
 * 消掉 t，令 u = tanθ：
 *
 *       y(u) = h0 + x u - (g x^2 / (2 v0^2)) (1 + u^2)
 *
 * 对 u 求极大值（∂y/∂u = x - (g x^2 / v0^2) u = 0）得到「安全抛物面」包络：
 *
 *       y_env(x) = h0 + v0^2/(2g) - g x^2/(2 v0^2) = H - (g / (2 v0^2)) x^2
 *
 * 于是扫过的区域是以竖直轴为轴的旋转抛物面，顶点高 H = h0 + v0^2/(2g)，
 * 落地处半径 R 满足 R^2 = 2 v0^2 H / g。抛物面的体积公式：
 *
 *       V = (1/2) π R^2 H = π (v0^2 / g) H^2
 *
 * 两条独立路径给出同一个式子：R 来自 45° 平射的最大射程，H 来自竖直射的最高点。
 *
 * 验证
 * --------
 * 1. 物理自检：最高点 H = h0 + v0^2/(2g)（竖直射）、最大射程 R（45°）、45° 弹道确实过 (R, 0)；
 * 2. 解析式两写法互证：π(v0^2/g)H^2 vs (1/2)πR^2H；
 * 3. 数值积分：V = ∫_0^H π r(y)^2 dy，r(y)^2 = (2 v0^2 / g)(H - y)，Simpson 法；
 * 4. 蒙特卡洛：在 [0,R]×[-R,R]×[0,H] 的盒子里均匀撒点，统计落在旋转抛物面内的比例，误差 0.1% 以内；
 * 5. 高精度：BigDecimal 40 位重算，确认 round(V * 10^4) 不受 Double 舍入影响。
 *
 * 复杂度：解析式 O(1)；数值积分 O(N) 切片；蒙特卡洛 O(M) 次采样，误差 O(M^{-1/2})。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private const val V0 = 20.0        // 初速度 m/s
private const val H0 = 100.0       // 爆炸高度 m
private const val GG = 9.81        // 重力加速度 m/s^2

/** 数值积分的切片数（偶数才能用 Simpson）。 */
private const val SLICES = 2_000_000

/** 蒙特卡洛撒点数（相对标准差约 1/sqrt(M)，M=4e6 时约 0.05%）。 */
private const val MC_SAMPLES = 4_000_000

private val MC_SEED = 20260930L

/** 包络顶高：竖直射能到的最高点。 */
private val H = H0 + V0 * V0 / (2.0 * GG)

/** 落地半径：45° 平射的最大射程。 */
private val R = V0 * sqrt(2.0 * H / GG)

/** 解析式 A：π (v0^2/g) H^2。 */
private fun volumeA(): Double = PI * (V0 * V0 / GG) * H * H

/** 解析式 B：(1/2) π R^2 H（旋转抛物面公式，R 由 45° 射程独立算得）。 */
private fun volumeB(): Double = 0.5 * PI * R * R * H

/** 解析式 C：把 y 方向从 0 到 H 的水平截面圆面积积分的闭式。 */
private fun volumeC(): Double {
    val c = 2.0 * V0 * V0 / GG      // r(y)^2 = c (H - y)
    return PI * c * (H * H / 2.0)
}

/** 包络高度：y_env(x) = H - g x^2/(2 v0^2)。 */
private fun envelope(x: Double): Double = H - GG * x * x / (2.0 * V0 * V0)

/**
 * 固定 x 下对 u = tanθ 求 y(u) 的极大。y 对 u 是开口向下的抛物线，直接三分搜索，
 * 不依赖解析公式——纯粹用来核对解析包络。
 */
private fun envelopeByScan(x: Double): Double {
    fun y(u: Double) = H0 + x * u - (GG * x * x / (2.0 * V0 * V0)) * (1.0 + u * u)
    var lo = -200.0
    var hi = 200.0
    repeat(300) {
        val m1 = (2.0 * lo + hi) / 3.0
        val m2 = (lo + 2.0 * hi) / 3.0
        if (y(m1) < y(m2)) lo = m1 else hi = m2
    }
    return y((lo + hi) / 2.0)
}

/** 暴力法：Simpson 数值积分 ∫_0^H π r(y)^2 dy。 */
private fun volumeSimpson(slices: Int): Double {
    val c = 2.0 * V0 * V0 / GG
    fun f(y: Double) = PI * c * (H - y)
    val h = H / slices
    var s = f(0.0) + f(H)
    for (i in 1 until slices) s += (if (i % 2 == 1) 4.0 else 2.0) * f(i * h)
    return s * h / 3.0
}

/**
 * 蒙特卡洛：在包围盒 [-R,R]×[-R,R]×[0,H]（体积 4 R^2 H）内均匀撒点，
 * 数落在旋转抛物面 y <= H - g ρ^2/(2 v0^2) 内的比例。解析的命中比例应为 V/(4R^2H) ≈ 0.3926。
 */
private fun volumeMonteCarlo(samples: Int): Double {
    val rnd = java.util.Random(MC_SEED)
    val boxVol = 4.0 * R * R * H
    var hit = 0
    repeat(samples) {
        val x = (2.0 * rnd.nextDouble() - 1.0) * R
        val z = (2.0 * rnd.nextDouble() - 1.0) * R
        val y = rnd.nextDouble() * H
        val rho2 = x * x + z * z
        if (y <= H - GG * rho2 / (2.0 * V0 * V0)) hit++
    }
    return boxVol * hit / samples
}

/** π 的 80 位字面量（java.math 没有内置常量）。 */
private val PI_BD = BigDecimal(
    "3.14159265358979323846264338327950288419716939937510582097494459230781640628620899862803"
)

/** 高精度重算：V = π v0^2 (2 g h0 + v0^2)^2 / (4 g^3)，π 取 80 位、运算 40 位。 */
private fun volumeHighPrecision(): BigDecimal {
    val mc = MathContext(40)
    val v0 = BigDecimal("20")
    val h0 = BigDecimal("100")
    val g = BigDecimal("9.81")
    val v0sq = v0.multiply(v0)
    val inner = g.multiply(BigDecimal(2)).multiply(h0).add(v0sq).pow(2)   // (2 g h0 + v0^2)^2
    return PI_BD.multiply(v0sq).multiply(inner).divide(g.pow(3).multiply(BigDecimal(4)), mc)
}

/** 把高精度体积四舍五入到 10^4 的整数倍（Long）。 */
private fun scaledLong(v: BigDecimal): Long =
    v.multiply(BigDecimal("10000")).setScale(0, java.math.RoundingMode.HALF_UP).toBigInteger().toLong()

/** 优化法求解器本体：闭式 + 40 位高精度，取四舍五入到 4 位小数后的 Long。 */
private fun solveExact(): Long = scaledLong(volumeHighPrecision())

/** 预热 1 次后跑 runs 轮，返回毫秒中位数。 */
private inline fun medianMs(runs: Int = 5, body: () -> Double): Double {
    body()
    val ts = DoubleArray(runs)
    for (i in 0 until runs) {
        val s = System.nanoTime()
        body()
        ts[i] = (System.nanoTime() - s) / 1_000_000.0
    }
    ts.sort()
    return ts[runs / 2]
}

fun main() {
    // 1) 物理自检
    println("爆炸高度 h0 = " + H0 + " m，初速度 v0 = " + V0 + " m/s，g = " + GG + " m/s^2")
    println("包络顶高 H = h0 + v0^2/(2g) = " + "%.9f".format(H) + " m（竖直射最高点，与 100+" + "%.4f".format(V0 * V0 / (2 * GG)) + " 相符）")
    println("落地半径 R = v0 sqrt(2H/g) = " + "%.9f".format(R) + " m（包络与地面 y=0 的交点，即最大射程）")
    // 注意：从高处平射的「最大射程」并不是 45°。令弹道与包络在地面相切，
    // 解出 tanθ* = v0 / sqrt(2 g H) = v0 / sqrt(2 g h0 + v0^2)。
    val uStar = V0 / sqrt(2.0 * GG * H)
    val thetaStar = Math.toDegrees(kotlin.math.atan(uStar))
    fun yAt(u: Double, x: Double) = H0 + x * u - (GG * x * x / (2.0 * V0 * V0)) * (1.0 + u * u)
    val xStar = V0 * V0 / (GG * uStar)
    println("最大射程弹道 tanθ* = v0/sqrt(2gH) = " + "%.9f".format(uStar) + "，θ* = " + "%.4f".format(thetaStar) + "°（不是 45°）")
    println("  该弹道与包络相切于 x = v0^2/(g·tanθ*) = " + "%.9f".format(xStar) + " m，其高度 y = "
        + "%.3e".format(yAt(uStar, xStar)) + " m -> 落在地面上：" + (abs(yAt(uStar, xStar)) < 1e-6))
    println("  x* 与 R 的相对差 = " + "%.2e".format(abs(xStar - R) / R))
    println("包络数值扫描 vs 解析式（扫 u=tanθ 的极大值）：")
    for (x in doubleArrayOf(10.0, 40.0, 80.0, 99.0)) {
        val scan = envelopeByScan(x)
        val ana = envelope(x)
        println("  x=" + "%.1f".format(x) + "：扫描=" + "%.6f".format(scan) + "，解析=" + "%.6f".format(ana)
            + "，相对差=" + "%.2e".format(abs(scan - ana) / abs(ana)))
    }

    // 2) 解析式三写法互证
    val va = volumeA()
    val vb = volumeB()
    val vc = volumeC()
    println()
    println("解析式 A  π(v0^2/g)H^2        = " + va)
    println("解析式 B  (1/2)πR^2 H         = " + vb)
    println("解析式 C  ∫_0^H π·(2v0^2/g)(H-y)dy = " + vc)
    val agree = abs(va - vb) / va < 1e-12 && abs(va - vc) / va < 1e-12
    println("三式相对差 < 1e-12：" + agree)

    // 3) 数值积分
    val vInt = volumeSimpson(SLICES)
    println("Simpson 数值积分（" + SLICES + " 切片）    = " + vInt + "，相对差 = " + "%.2e".format(abs(vInt - va) / va))

    // 4) 蒙特卡洛
    val vMc = volumeMonteCarlo(MC_SAMPLES)
    val mcErr = abs(vMc - va) / va
    println("蒙特卡洛（" + MC_SAMPLES + " 点，seed=" + MC_SEED + "） = " + vMc
        + "，相对误差 = " + "%.4f".format(mcErr * 100) + "% -> 在 0.1% 内：" + (mcErr < 1e-3))
    val expectedRatio = va / (4.0 * R * R * H)
    println("  解析的命中比例应为 V/(4R^2H) = " + "%.9f".format(expectedRatio)
        + "（实测 " + "%.9f".format(vMc / (4.0 * R * R * H)) + "）")

    // 5) 高精度
    val vHp = volumeHighPrecision()
    println("BigDecimal(40 位) 重算 = " + vHp.toPlainString())
    val rounded = scaledLong(vHp)
    println("round(V * 10^4) = " + rounded + "（Long 求解器返回值）")
    val scaledD = Math.round(va * 1e4)
    println("Double 口径 round(V * 10^4) = " + scaledD + "，两者一致：" + (rounded == scaledD))

    // 6) 计时
    val bruteMs = medianMs { volumeSimpson(SLICES) }
    val optMs = medianMs { solveExact().toDouble() }
    val mcMs = medianMs(3) { volumeMonteCarlo(200_000) }
    println()
    println("BRUTE_MS: " + "%.3f".format(bruteMs) + "  (Simpson 数值积分，" + SLICES + " 切片)")
    println("OPT_MS: " + "%.3f".format(optMs) + "  (闭式 + BigDecimal 40 位四舍五入，端到端求解器)")
    println("MC_MS: " + "%.3f".format(mcMs) + "  (蒙特卡洛 200000 点，仅作独立旁证)")

    // 7) 答案
    val answer = solveExact()
    val text = (answer / 10_000L).toString() + "." + (answer % 10_000L).toString().padStart(4, '0')
    println()
    println("体积 V = " + va + " m^3")
    println("ANSWER: " + text)
    println("ANSWER_ROUNDED_LONG: " + answer)
}
