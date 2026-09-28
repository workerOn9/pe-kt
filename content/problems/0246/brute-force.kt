#!/usr/bin/env kotlin
/**
 * Project Euler 246 — Tangents to an Ellipse（椭圆的切线）暴力对照
 *
 * 思路：**完全不使用 solution.kt 的列式区间推导**。只保留最原始的一条判据：
 * 对椭圆中心坐标 (u,v) = (x − 3000, y − 1500) 下的每个整数格点，按定义直接判
 *
 *     θ = ∠RPS > 45°  ⟺  T > 0 且 (D ≤ 0 或 4T > D²) ，
 *     T = b²u² + a²v² − a²b² ，  D = u² + v² − a² − b² ，
 *
 * 其中 T > 0 是 P 在椭圆外的充要条件，D ≤ 0 即 θ ≥ 90°（准圆 u²+v² = a²+b² 之内），
 * D > 0 时 θ < 90°、tanθ > 1 ⟺ 4T > D²。判据来自切线斜率方程 (a²−u²)m² + 2uvm + (b²−v²) = 0
 * 的韦达定理；文件末尾另用「显式解出切点 R、S 再算 PR、PS 夹角」对若干代表点做了几何复核，
 * 确保暴力扫的是题目要求的那个角。
 *
 * 扫描范围：区域有界——T ~ r² 而 D² ~ r⁴，远处必有 4T < D²；[−20000,20000]² 覆盖整个
 * 区域并向外留出整圈空白（程序统计最外圈四条边上的命中数，必须为 0）。
 * 整个方格 4·20001² ≈ 1.6×10⁹ 个格点，全程 Long 精确算术（最大中间量 4T ≈ 1.4×10¹⁷ < 2⁶³）。
 *
 * 复杂度：时间 O(UB²) = 1.6×10⁹ 次判据运算。
 * 分工：小规模（5 组小椭圆）的「列式 vs 逐点」互证在 solution.kt 的 main 里；本文件负责
 * 全尺寸复核（同一判据、1.6×10⁹ 格点），并按 θ ≥ 90° / 45° < θ < 90° 分段输出。
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private const val A2 = 56_250_000L
private const val B2 = 31_250_000L
private const val UB = 20_000L

/** 一次完整扫描：总数 + θ ≥ 90°（D ≤ 0）部分 + 45° < θ < 90° 部分。 */
private fun scanFull(): LongArray {
    var total = 0L
    var rightOrMore = 0L
    var acute = 0L
    var u = -UB
    while (u <= UB) {
        val u2 = u * u
        val baseT = B2 * u2 - A2 * B2
        val baseD = u2 - A2 - B2
        var v = -UB
        while (v <= UB) {
            val v2 = v * v
            val t = baseT + A2 * v2
            if (t > 0L) {
                val d = baseD + v2
                if (d <= 0L) {
                    total++
                    rightOrMore++
                } else if (4 * t > d * d) {
                    total++
                    acute++
                }
            }
            v++
        }
        u++
    }
    return longArrayOf(total, rightOrMore, acute)
}

private fun hit(u: Long, v: Long): Boolean {
    val t = B2 * u * u + A2 * v * v - A2 * B2
    if (t <= 0L) return false
    val d = u * u + v * v - A2 - B2
    return d <= 0L || 4 * t > d * d
}

/** 盒子最外圈（|u| = UB 或 |v| = UB）上的命中数，应为 0。 */
private fun ringHits(): Long {
    var c = 0L
    var i = -UB
    while (i <= UB) {
        if (hit(UB, i)) c++
        if (hit(-UB, i)) c++
        if (hit(i, UB)) c++
        if (hit(i, -UB)) c++
        i++
    }
    return c
}

// ---------------------------------------------------------------- 几何复核（解析切点）

private fun angleByTangents(u: Double, v: Double): Double {
    val am = A2.toDouble()
    val bm = B2.toDouble()
    if (u == 0.0 && v == 0.0) return Double.NaN
    val px: Double
    val py: Double
    if (Math.abs(u) / am >= Math.abs(v) / bm) { px = am / u; py = 0.0 } else { px = 0.0; py = bm / v }
    var dx = -v / bm
    var dy = u / am
    val nrm = Math.hypot(dx, dy)
    dx /= nrm
    dy /= nrm
    val qa = dx * dx / am + dy * dy / bm
    val qb = 2 * (px * dx / am + py * dy / bm)
    val qc = px * px / am + py * py / bm - 1
    val disc = qb * qb - 4 * qa * qc
    if (disc < 0) return Double.NaN
    val s = Math.sqrt(disc)
    val c1x = px + ((-qb + s) / (2 * qa)) * dx - u
    val c1y = py + ((-qb + s) / (2 * qa)) * dy - v
    val c2x = px + ((-qb - s) / (2 * qa)) * dx - u
    val c2y = py + ((-qb - s) / (2 * qa)) * dy - v
    return Math.atan2(
        Math.abs(c1x * c2y - c1y * c2x),
        c1x * c2x + c1y * c2y
    )
}

/** 用显式切点构造核对判据：角度 > 45° 与谓词命中必须逐一对应。 */
private fun geometryCheck() {
    val pts = longArrayOf(
        0L, 0L,      // 椭圆内，无切线
        7500L, 0L,   // 长轴顶点（在椭圆上）
        0L, 5600L,   // 刚出短轴顶点，θ ≈ 180°
        7849L, 0L,   // θ > 90° 与 θ < 90° 的分界附近
        9354L, 0L,   // 准圆附近（θ 略大于 90°）
        12000L, 0L,
        0L, 18949L,  // 短轴方向刚好在 45° 边界内
        0L, 18950L,  // 短轴方向刚好在 45° 边界外
        15440L, 0L,  // 长轴方向 45° 边界外侧（恰为空）
        15441L, 0L,
        8000L, 12000L, 3000L, 17500L, 15000L, 6000L
    )
    var i = 0
    while (i < pts.size) {
        val u = pts[i]
        val v = pts[i + 1]
        val ang = angleByTangents(u.toDouble(), v.toDouble())
        val h = hit(u, v)
        val gt = !ang.isNaN() && ang > Math.PI / 4
        check(gt == h) { "($u,$v) 谓词与显式夹角不一致：hit=$h，angle=$ang" }
        val label = if (ang.isNaN()) "无切线" else "%.3f°".format(Math.toDegrees(ang))
        println("  复核 ($u, $v)：∠RPS = $label，判据命中 = $h")
        i += 2
    }
}

fun main() {
    println("brute-force：在 [−$UB,$UB]² 上逐点扫描（${4.0 * (UB + 1) * (UB + 1) / 1e9} ×10⁹ 格点）")

    geometryCheck()

    val ring = ringHits()
    println("盒子最外圈命中数 = $ring（应为 0，说明区域完整落在盒内）")
    check(ring == 0L) { "最外圈有命中，需要扩大盒子" }

    // 第一次完整扫描：正确性输出
    val r = scanFull()
    println("总命中 = ${r[0]}")
    println("  其中 θ ≥ 90°（D ≤ 0）：${r[1]}")
    println("  其中 45° < θ < 90°（D > 0 且 4T > D²）：${r[2]}")

    // 计时：JIT 预热后 2 次取最优（每次都是完整 1.6×10⁹ 格点扫描）
    var best = Double.MAX_VALUE
    repeat(2) {
        val t0 = System.nanoTime()
        val s = scanFull()
        val ms = (System.nanoTime() - t0) / 1e6
        check(s[0] == r[0] && s[1] == r[1] && s[2] == r[2]) { "计时中结果漂移" }
        if (ms < best) best = ms
    }
    println("brute: ${"%.0f".format(best)} ms（JIT 预热后 2 次取最优，单次完整扫描）")
}
