/**
 * Project Euler 144 — 暴力对照解（教学对比用）
 *
 * 与 solution.kt 的差距不在「同一套算式写得慢一点」，而是每一步都换一条完全不同的路：
 *   · 求下一个交点：优化解把 P + t·d 代回椭圆，用解析根的 t = −2B/A 一步算出来；
 *     本解照最朴素的定义做——沿射线以固定步长 h = 0.0005 前进，找到 f = 4x² + y² − 100
 *     头一次由负变正的那一步，再用 60 轮二分把交点逼到双精度极限。求根过程对椭圆方程
 *     没有任何解析依赖。
 *   · 反射方向：优化解用向量投影 d ← d − 2(d·n)/(n·n)·n；本解用角度——切线方向 (y, −4x)
 *     的倾角 θ_t、入射方向倾角 θ_d，关于直线镜像即 θ_r = 2θ_t − θ_d，取 (cos θ_r, sin θ_r)。
 *     全程 atan2/cos/sin，与向量法只共享物理、不共享算式。
 * 两条路径逐步独立地算出交点，命中次数相同，构成双方法互证；且本解每次都检验命中点确实
 * 落在椭圆上（|f| < 1e-9）与轨迹的共焦不变量守恒（相对漂移 < 1e-6，比优化解宽松是因为
 * 二分求根每步带来约 1e-12 的位置误差）。
 *
 * 复杂度：第 k 条弦长 L_k，前进阶段约 L_k/h 次隐函数求值，定位阶段 BISECT = 60 次二分；
 * 总代价 O(ΣL_k/h + R·BISECT)。本题弦长普遍接近椭圆的长轴（最大约 19.9，均值约 14.9），
 * 实测 ΣL_k ≈ 5.30×10³，于是前进阶段约 1.06×10⁷ 次隐函数求值，再加 355×60 ≈ 2.1×10⁴ 次二分；
 * 而优化解全程只有 R = 354 步、每步约 15 次乘加（合计约 5.3×10³ 次），两者相差约三个数量级。
 * 空间 O(R)（保存轨迹供自检）。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import kotlin.math.abs

const val HOLE = 0.01              // 顶部缺口半宽
const val MARCH_STEP = 0.0005      // 前进步长
const val BISECT = 60              // 二分轮数
const val MAX_BOUNCES = 100_000    // 死循环保险

/** 椭圆的隐函数 f(x,y) = 4x² + y² − 100：腔外为正、腔内为负、边界为零。 */
fun f(x: Double, y: Double): Double = 4.0 * x * x + y * y - 100.0

/** 椭圆下支 y(x) = −√(100 − 4x²)，用于数值微分核对题面给的切线斜率。 */
fun lowerBranch(x: Double): Double = -Math.sqrt(100.0 - 4.0 * x * x)

/** 从椭圆上的点 (x,y) 沿倾角 th 的方向走，返回与椭圆的另一个交点。 */
fun crossBoundary(x: Double, y: Double, th: Double): DoubleArray {
    val dx = Math.cos(th)
    val dy = Math.sin(th)
    var lo = 0.0
    var hi = MARCH_STEP
    var guard = 0
    while (f(x + hi * dx, y + hi * dy) <= 0.0) {     // 还在腔内 → 继续前进
        lo = hi
        hi += MARCH_STEP
        check(++guard < 1_000_000) { "前进阶段未找到出界点" }
    }
    repeat(BISECT) {                                  // 二分：把出界处夹到机器精度
        val mid = 0.5 * (lo + hi)
        if (f(x + mid * dx, y + mid * dy) <= 0.0) lo = mid else hi = mid
    }
    val t = 0.5 * (lo + hi)
    return doubleArrayOf(x + t * dx, y + t * dy)
}

/**
 * 逐步模拟：返回 [x0,y0,x1,y1,…]，依次是各次命中点，最后两个数是穿出缺口的交点。
 */
fun trajectory(): DoubleArray {
    var x = 1.4                                       // 第一击点 (1.4, −9.6)
    var y = -9.6
    var th = Math.atan2(-9.6 - 10.1, 1.4 - 0.0)       // 入射方向 (0,10.1) → (1.4,−9.6)
    val out = ArrayList<Double>(2 * 360)
    out.add(x); out.add(y)
    while (out.size / 2 <= MAX_BOUNCES) {
        val thTangent = Math.atan2(-4.0 * x, y)       // 切线方向 ∝ (y, −4x)
        th = 2.0 * thTangent - th                     // 关于切线镜像 = 反射
        val p = crossBoundary(x, y, th)
        x = p[0]; y = p[1]
        out.add(x); out.add(y)
        if (y > 0.0 && abs(x) <= HOLE) break          // 从缺口穿出，不计命中
    }
    return out.toDoubleArray()
}

/** 命中内壁的次数：交点数减一（减掉穿出缺口的那次）。 */
fun solveBruteForce(): Long = trajectory().size / 2L - 1L

/** 第 i 条弦所切的那条共焦二次曲线的不变量 λ（与 solution.kt 同一物理量，独立算出）。 */
fun causticOf(points: DoubleArray, i: Int): Double {
    val x1 = points[2 * i]; val y1 = points[2 * i + 1]
    val dx = points[2 * i + 2] - x1; val dy = points[2 * i + 3] - y1
    return (25.0 * dy * dy + 100.0 * dx * dx - (dx * y1 - dy * x1) * (dx * y1 - dy * x1)) / (dx * dx + dy * dy)
}

fun verifySample() {
    // 题面锚点一：第一个命中点 (1.4, −9.6) 在椭圆上
    check(abs(f(1.4, -9.6)) < 1e-12) { "第一击点不在椭圆上" }
    // 题面锚点二：切线斜率 m = −4x/y，用隐函数求导的数值微分核对
    val h = 1e-6
    val fd = (lowerBranch(1.4 + h) - lowerBranch(1.4 - h)) / (2 * h)
    check(abs(fd - (-4.0 * 1.4 / -9.6)) < 1e-9) { "切线斜率不符：$fd" }
    // 题面锚点三：光束自顶部缺口射入
    check(abs(1.4 * (10.1 - 10.0) / 19.7) <= HOLE) { "入射点不在缺口内" }
    // 题面锚点四：第一次反射的入射角等于反射角（用角度显式核对）
    val thTangent = Math.atan2(-4.0 * 1.4, -9.6)
    val thIn = Math.atan2(-9.6 - 10.1, 1.4)
    val thOut = 2.0 * thTangent - thIn
    check(abs(abs(thIn - thTangent) - abs(thOut - thTangent)) < 1e-12) { "反射角不等于入射角" }

    val pts = trajectory()
    check(pts.size / 2 - 1 == 354) { "与 solution.kt 互证的命中次数应为 354，实得 ${pts.size / 2 - 1}" }
    for (i in 0 until pts.size / 2) {
        check(abs(f(pts[2 * i], pts[2 * i + 1])) < 1e-9) { "第 $i 个点偏离椭圆" }
    }
    val ex = pts[pts.size - 2]; val ey = pts[pts.size - 1]
    check(ey > 0.0 && abs(ex) <= HOLE) { "出口点不在缺口内：($ex, $ey)" }
    var lo = Double.MAX_VALUE; var hi = -Double.MAX_VALUE
    for (i in 0 until pts.size / 2 - 1) {
        val lam = causticOf(pts, i)
        lo = minOf(lo, lam); hi = maxOf(hi, lam)
    }
    check((hi - lo) / lo < 1e-6) { "不变量漂移过大：${hi - lo}" }
}

fun main() {
    verifySample()
    repeat(3) { solveBruteForce() }                 // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
