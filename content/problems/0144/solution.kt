/**
 * Project Euler 144 — Laser Beam Reflections（激光束反射）
 *
 * 思路：椭圆 4x² + y² = 100 可看作隐函数 f(x,y) = 4x² + y² − 100 的零集，梯度
 * ∇f = (8x, 2y) ∝ (4x, y) 就是入射点处的法向。把法向取成未归一化的 n = (4x, y)，
 * 反射公式 d' = d − 2(d·n)/(n·n)·n 对 n 的伸缩不变，于是整条轨迹只需要加减乘除，
 * 既没有开方也没有三角函数：
 *   · 反射：算出 k = 2(d·n)/(n·n)，令 d ← d − k·n；
 *   · 下一交点：把 P + t·d 代入椭圆得 A t² + 2B t = 0（P 已在椭圆上，常数项为零），
 *     A = 4dx² + dy²，B = 4x·dx + y·dy，非零根 t = −2B/A 即下一条弦长；
 *   · 出口判定：交点满足 y > 0 且 |x| ≤ 0.01 时，光束正是从顶部的缺口穿出，
 *     这个交点不算「命中内壁」。
 * 每条弦都切于同一条与椭圆共焦的二次曲线（椭圆台球是可积系统），
 * 解析式里出现的不变量 λ = (25dy² + 100dx² − (dx·y1 − dy·x1)²)/L² 在整条轨迹上守恒，
 * 既可作为反射是否算对的自检，也说明双精度误差不会随反射次数放大（实测相对漂移 ~1e-14）。
 *
 * 复杂度：每次反射 O(1) 次浮点运算（约 15 次乘加），共 R 次反射，
 * 时间 O(R)、空间 O(R)（保存轨迹供自检用；只要计数可以压到 O(1)）。R = 354。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

import kotlin.math.abs

/** 小孔半宽：椭圆顶部 |x| ≤ 0.01 的那一段缺失。 */
const val HOLE = 0.01

/** 椭圆的隐函数 f(x,y) = 4x² + y² − 100：腔外为正、腔内为负、边界为零。 */
fun ellipse(x: Double, y: Double): Double = 4.0 * x * x + y * y - 100.0

/**
 * 逐次反射模拟。返回 [x0,y0,x1,y1,…]：依次是每次命中内壁的位置，
 * 最后两个数是光束穿出小孔的那个边界交点（它不算命中）。
 */
fun simulate(maxPoints: Int = 100_000): DoubleArray {
    var x = 1.4                                  // 第一击点 (1.4, −9.6)
    var y = -9.6
    var dx = 1.4 - 0.0                           // 入射方向：起点 (0, 10.1) → (1.4, −9.6)
    var dy = -9.6 - 10.1
    val out = ArrayList<Double>(2 * 360)
    out.add(x); out.add(y)
    while (out.size / 2 <= maxPoints) {
        val nx = 4.0 * x                         // 法向 ∝ (4x, y)，无需归一化
        val ny = y
        val k = 2.0 * (dx * nx + dy * ny) / (nx * nx + ny * ny)
        dx -= k * nx                             // d' = d − 2(d·n)/(n·n)·n
        dy -= k * ny
        val t = -2.0 * (4.0 * x * dx + y * dy) / (4.0 * dx * dx + dy * dy)
        x += t * dx                              // 下一交点：t = 0 是当前点，另一个根才是下一个
        y += t * dy
        out.add(x); out.add(y)
        if (y > 0.0 && abs(x) <= HOLE) break     // 从缺口穿出，不再计入命中
    }
    return out.toDoubleArray()
}

/** 命中内壁的次数：交点数减一（减掉穿出小孔的那次）。 */
fun solve(): Long = simulate().size / 2L - 1L

/** 椭圆下支 y(x) = −√(100 − 4x²)，用于数值微分核对题面给的切线斜率。 */
fun lowerBranch(x: Double): Double = -Math.sqrt(100.0 - 4.0 * x * x)

/** 第 i 条弦（points[2i] → points[2i+2]）所切的那条共焦二次曲线的不变量 λ。 */
fun causticOf(points: DoubleArray, i: Int): Double {
    val x1 = points[2 * i]; val y1 = points[2 * i + 1]
    val dx = points[2 * i + 2] - x1; val dy = points[2 * i + 3] - y1
    val l2 = dx * dx + dy * dy
    return (25.0 * dy * dy + 100.0 * dx * dx - (dx * y1 - dy * x1) * (dx * y1 - dy * x1)) / l2
}

fun verifySample() {
    // 锚点一：题面给的第一个命中点确实落在椭圆上
    check(abs(ellipse(1.4, -9.6)) < 1e-12) { "第一击点不在椭圆上：${ellipse(1.4, -9.6)}" }

    // 锚点二：题面的切线斜率 m = −4x/y，用隐函数求导的数值微分核对
    val h = 1e-6
    val fd = (lowerBranch(1.4 + h) - lowerBranch(1.4 - h)) / (2 * h)
    check(abs(fd - (-4.0 * 1.4 / -9.6)) < 1e-9) { "切线斜率不符：$fd" }

    // 锚点三：起点在腔外，光束是从顶部缺口射进去的
    val sTop = (10.1 - 10.0) / 19.7                     // 直线 (0,10.1)→(1.4,−9.6) 上 y = 10 的参数
    check(abs(1.4 * sTop) <= HOLE) { "入射点不在缺口内：${1.4 * sTop}" }
    check(ellipse(0.0, 10.1) > 0.0) { "起点应在腔外" }

    // 锚点四：第一次反射满足反射定律——入射、反射方向与法向夹角相等
    val nx = 4.0 * 1.4; val ny = -9.6
    val dx = 1.4 - 0.0; val dy = -9.6 - 10.1
    val rdx = dx - 2.0 * (dx * nx + dy * ny) / (nx * nx + ny * ny) * nx
    val rdy = dy - 2.0 * (dx * nx + dy * ny) / (nx * nx + ny * ny) * ny
    val cin = (dx * nx + dy * ny) / (Math.hypot(dx, dy) * Math.hypot(nx, ny))
    val cout = (rdx * nx + rdy * ny) / (Math.hypot(rdx, rdy) * Math.hypot(nx, ny))
    check(cin > 0.0) { "入射方向应由腔内指向壁面（沿外法向）：$cin" }
    check(abs(cin + cout) < 1e-12) { "入射角 ≠ 反射角：$cin vs $cout" }

    // 锚点五：整条轨迹的物理自检
    val pts = simulate()
    check(pts.size / 2 - 1 == 354) { "命中次数应为 354（与独立对照解 brute-force.kt 互证），实得 ${pts.size / 2 - 1}" }
    for (i in 0 until pts.size / 2) {
        check(abs(ellipse(pts[2 * i], pts[2 * i + 1])) < 1e-9) { "第 $i 个点偏离椭圆" }
    }
    val ex = pts[pts.size - 2]; val ey = pts[pts.size - 1]
    check(ey > 0.0 && abs(ex) <= HOLE) { "出口点不在缺口内：($ex, $ey)" }
    var lo = Double.MAX_VALUE; var hi = -Double.MAX_VALUE
    for (i in 0 until pts.size / 2 - 1) {
        val lam = causticOf(pts, i)
        lo = minOf(lo, lam); hi = maxOf(hi, lam)
    }
    check(lo > 0.0 && hi < 25.0) { "焦散线应是共焦椭圆（0 < λ < 25）：[$lo, $hi]" }
    check((hi - lo) / lo < 1e-11) { "不变量漂移过大：[${hi - lo}]" }
}

fun main() {
    verifySample()
    repeat(5) { solve() }                        // JIT 预热
    val start = System.nanoTime()
    val answer = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
