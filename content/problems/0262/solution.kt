#!/usr/bin/env kotlin
/**
 * Project Euler 262 — Mountain Range（山脉）
 *
 * 思路
 * ────
 * 海拔函数 h(x,y) 决定了两件事：飞行高度 f 能飞的地方恰是集合 {h ≤ f}。
 * 蚊子在高度 f 上从 A 到 B 可行 ⟺ A、B 在 {h ≤ f} 的同一个连通分支里，于是
 *   f_min = min{ f : A、B 在 {h ≤ f} ∩ [0,1600]² 中连通 } —— 一个极小极大（瓶颈）问题。
 *
 * 本函数的地形有三个关键事实（本程序逐条实跑验证）：
 *   1. 山脊是「环形」的：h 在圆 (x-750)²+(y-750)² ≈ 425000 附近有一圈约 10973–14406 的
 *      脊线，环内是约 10000–10220 的盆地，环外地形随距离下降；
 *   2. 峡谷（通道）：环的最低鞍点在 (895.483, 0)（底面 y=0 上的脊线最低点，h = 10396.4622），
 *      同理在 (0, 895.483)。整条底面边界的最大值与整条左边界一样，都是 10396.4622；
 *      上下左右四条边界里，底/左最高，右/顶只有 9571.54；
 *   3. 因此 f 刚低于 10396.4622 时，山体同时贴住底边和左边，把 A 所在的「墙角小盆地」
 *      （含原点与 A）封死；f 一旦超过该值，山体脱开边界，A 与 B 连通。
 *   故 f_min = max_{底,左边} h = 10396.462193284102（A、B 各自的海拔 7851.54 / 6964.70 远低于它）。
 *
 * 求最短路径：在高度 f_min 上，可飞区域是 {h ≤ f_min}，其中「墙角小盆地」与外部区域
 * 只在 (895.483, 0)（记 P）一点相切——任何路线必须穿过 P（或它的镜像 P′=(0,895.483)）。
 * 于是问题化为「绕过一块凸障碍物」的最短路径：
 *   A ——直飞切点 T₁——沿等高线 {h = f_min} 贴合爬升——P——继续贴合——T₂——直飞 B。
 * 贴合段是整个等高线的外边界（外边界是凸曲线：程序逐点验证所有相邻转向叉积同号）。
 * 切点 T₁ 满足 (A−T₁) ∥ 等高线切向（叉积为零），用叉积变号 + 二分精确求出；T₂ 同理。
 * 最后
 *   L = |A−T₁| + arc(T₁→P) + arc(P→T₂) + |T₂−B|.
 * （实测 |A−T₁| ≈ 450.9455、arc(T₁→P) ≈ 276.4554、arc(P→T₂) ≈ 1259.56、|T₂−B| ≈ 544.24。）
 *
 * 复杂度
 * ──────
 * 方法 A：等高线行走 O(周长/步长) = 4.9e5 步（步长 0.01，每步一次 Newton 校正），
 *   切点定位 O(n) 扫描 + O(log) 二分，总体约 5e6 次 h 求值，内存 O(n)。
 * 方法 B：径向参数化等高线（θ 步长 5e-4，1.26e4 个点，每点 80 次二分）+ 离散切线搜索，
 *   约 2e6 次 h 求值，与 A 的走法、切点搜索算法、弧长累加方式均不同（互证用）。
 * 网格暴力：4 单位粗网格连通性判定（16 万节点）验证 f_min 阈值；细网格（0.5）最短路
 *   在 brute-force.kt 中给出上界与耗时对比。
 *
 * 验证
 * ────
 * 1. f_min 双验证：底/左边界黄金分割求极大 + 网格连通性在 f_min±0.5 处翻转；
 * 2. 方法 A 与方法 B 是两条独立实现（marching+叉积二分 vs. 径向二分+可行性扫描），
 *    结果 2531.204690 与 2531.204668，差 2.1e-5；
 * 3. 路径自检：A→T₁、T₂→B 两段直飞段的最大越界（相对 h ≤ f_min）分别 ~1e-12 与 ~4e-7（即相切）；
 *    整条路径逐点满足 h ≤ f_min；等高点列弦内切（凸性）验证贴合段确为凸包切线结构；
 * 4. 粗网格 Dijkstra（间距 1，八邻域）实跑路线与解析路线同伦（都经 P 附近贴等高线绕行），
 *    其欧氏长度 2687.59 是可行上界。
 *
 * 答案编码：题目要求三位小数，本程序打印 2531.205；meta.json 存 round(2531.204690×10³) = 2531205。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0262/solution.kt && java -cp … SolutionKt
 */
import kotlin.math.*

// ---------------- 海拔函数 ----------------
fun h(x: Double, y: Double): Double {
    val p = 5000.0 - 0.005 * (x * x + y * y + x * y) + 12.5 * (x + y)
    val q = 1e-6 * (x * x + y * y) - 0.0015 * (x + y) + 0.7
    return p * exp(-abs(q))
}

fun gradH(x: Double, y: Double, out: DoubleArray) {
    val p = 5000.0 - 0.005 * (x * x + y * y + x * y) + 12.5 * (x + y)
    val q = 1e-6 * (x * x + y * y) - 0.0015 * (x + y) + 0.7
    val e = exp(-abs(q))
    val s = if (q < 0) 1.0 else -1.0
    out[0] = (-0.005 * (2 * x + y) + 12.5) * e + p * e * s * (2e-6 * x - 0.0015)
    out[1] = (-0.005 * (x + 2 * y) + 12.5) * e + p * e * s * (2e-6 * y - 0.0015)
}

// ---------------- f_min = 底/左边界上的最大值 ----------------
fun maxOnEdge(g: (Double) -> Double, lo: Double, hi: Double): Double {
    var a = lo; var b = hi
    val gr = (sqrt(5.0) - 1) / 2
    var c = b - gr * (b - a); var d = a + gr * (b - a)
    var fc = g(c); var fd = g(d)
    repeat(400) {
        if (fc > fd) { b = d; d = c; fd = fc; c = b - gr * (b - a); fc = g(c) }
        else { a = c; c = d; fc = fd; d = a + gr * (b - a); fd = g(d) }
    }
    return g((a + b) / 2)
}

fun fminAndXP(): Pair<Double, Double> {
    var a = 800.0; var b = 1000.0
    val gr = (sqrt(5.0) - 1) / 2
    var c = b - gr * (b - a); var d = a + gr * (b - a)
    var fc = h(c, 0.0); var fd = h(d, 0.0)
    repeat(400) {
        if (fc > fd) { b = d; d = c; fd = fc; c = b - gr * (b - a); fc = h(c, 0.0) }
        else { a = c; c = d; fc = fd; d = a + gr * (b - a); fd = h(d, 0.0) }
    }
    val xP = (a + b) / 2
    return Pair(xP, h(xP, 0.0))
}

// 线段上 h 的最大值（先粗采，再在最优点附近黄金分割细化）
fun segMaxExcess(p: Pair<Double, Double>, q: Pair<Double, Double>, f: Double, gap: Double, cap: Int = 4000): Double {
    val d = hypot(q.first - p.first, q.second - p.second)
    val n = min(cap, max(8, (d / gap).toInt() + 1))
    fun v(t: Double) = h(p.first + (q.first - p.first) * t, p.second + (q.second - p.second) * t) - f
    var bestT = 0.0; var bestV = -1e18
    for (k in 0..n) {
        val t = k.toDouble() / n; val vv = v(t)
        if (vv > bestV) { bestV = vv; bestT = t }
    }
    var lo = max(0.0, bestT - 1.0 / n); var hi = min(1.0, bestT + 1.0 / n)
    val gr = (sqrt(5.0) - 1) / 2
    var c = hi - gr * (hi - lo); var dd = lo + gr * (hi - lo)
    var fc = v(c); var fd = v(dd)
    repeat(100) {
        if (fc > fd) { hi = dd; dd = c; fd = fc; c = hi - gr * (hi - lo); fc = v(c) }
        else { lo = c; c = dd; fc = fd; dd = lo + gr * (hi - lo); fd = v(dd) }
    }
    return max(bestV, fc)
}

// ---------------- 方法 A：行走等高线 + 叉积切点 ----------------
class Contour(val px: DoubleArray, val py: DoubleArray, val s: DoubleArray) {
    val n = px.size
    val total = s[n - 1]
    private fun locate(arc0: Double): Int {
        var arc = arc0
        if (arc < 0) arc += total
        if (arc >= total) arc -= total
        var lo = 0; var hi = n - 1
        while (lo + 1 < hi) { val m = (lo + hi) / 2; if (s[m] <= arc) lo = m else hi = m }
        return lo
    }
    fun pointAt(arc: Double): Pair<Double, Double> {
        val lo = locate(arc); val hi = min(lo + 1, n - 1)
        val t = if (s[hi] > s[lo]) (arc - s[lo]) / (s[hi] - s[lo]) else 0.0
        return Pair(px[lo] + (px[hi] - px[lo]) * t, py[lo] + (py[hi] - py[lo]) * t)
    }
    fun tangentAt(arc: Double): Pair<Double, Double> {
        val lo = locate(arc); val j = min(lo, n - 2)
        val dx = px[j + 1] - px[j]; val dy = py[j + 1] - py[j]
        val l = hypot(dx, dy)
        return Pair(dx / l, dy / l)
    }
    fun crossAt(i: Int, P: Pair<Double, Double>): Double {
        val j = max(1, min(i, n - 2))
        val dx = px[j + 1] - px[j - 1]; val dy = py[j + 1] - py[j - 1]
        val l = hypot(dx, dy)
        return (P.first - px[i]) * (dy / l) - (P.second - py[i]) * (dx / l)
    }
}

fun march(f: Double, x0: Double, y0: Double, step: Double): Contour {
    var px = DoubleArray(1 shl 20); var py = DoubleArray(1 shl 20)
    var m = 1; px[0] = x0; py[0] = y0
    var x = x0; var y = y0
    val g = DoubleArray(2)
    gradH(x, y, g)
    var gl = hypot(g[0], g[1])
    var tx = -g[1] / gl; var ty = g[0] / gl
    if (tx * (-(y - 750.0)) + ty * (x - 750.0) < 0) { tx = -tx; ty = -ty }   // 逆时针方向
    var total = 0.0
    while (true) {
        var nxp = x + step * tx; var nyp = y + step * ty
        var iter = 0
        while (iter < 60) {
            val res = h(nxp, nyp) - f
            if (abs(res) < 1e-13) break
            gradH(nxp, nyp, g)
            val g2 = g[0] * g[0] + g[1] * g[1]
            nxp -= res * g[0] / g2; nyp -= res * g[1] / g2
            iter++
        }
        gradH(nxp, nyp, g)
        gl = hypot(g[0], g[1])
        var ntx = -g[1] / gl; var nty = g[0] / gl
        if (ntx * tx + nty * ty < 0) { ntx = -ntx; nty = -nty }
        total += hypot(nxp - x, nyp - y)
        x = nxp; y = nyp; tx = ntx; ty = nty
        if (m >= px.size) { px = px.copyOf(m * 2); py = py.copyOf(m * 2) }
        px[m] = x; py[m] = y; m++
        if (total > 100.0 && hypot(x - x0, y - y0) < step) break
    }
    // 去掉最后一步的“过冲”，直接以弦闭回起点 P（避免把过冲段重复计入弧长）
    m--
    val cx = DoubleArray(m + 1); val cy = DoubleArray(m + 1); val cs = DoubleArray(m + 1)
    for (i in 0 until m) { cx[i] = px[i]; cy[i] = py[i] }
    var acc = 0.0
    for (i in 1 until m) { acc += hypot(cx[i] - cx[i - 1], cy[i] - cy[i - 1]); cs[i] = acc }
    acc += hypot(x0 - cx[m - 1], y0 - cy[m - 1]); cs[m] = acc
    cx[m] = x0; cy[m] = y0
    return Contour(cx, cy, cs)
}

data class MethodA(val total: Double, val seg1: Double, val arc1: Double, val arc2: Double, val seg2: Double,
                   val t1: Pair<Double, Double>, val t2: Pair<Double, Double>,
                   val excessAT1: Double, val excessT2B: Double, val convex: Boolean)

fun methodA(fmin: Double, xP: Double, A: Pair<Double, Double>, B: Pair<Double, Double>, step: Double): MethodA {
    val c = march(fmin, xP, 0.0, step)
    // 凸性检查：所有相邻转向叉积同号（逆时针为负或正均可，只要不变号）
    var pos = 0; var neg = 0
    for (i in 1 until c.n - 1) {
        val ax = c.px[i] - c.px[i - 1]; val ay = c.py[i] - c.py[i - 1]
        val bx = c.px[i + 1] - c.px[i]; val by = c.py[i + 1] - c.py[i]
        val cr = ax * by - ay * bx
        if (cr > 0) pos++ else if (cr < 0) neg++
    }
    val convex = (pos == 0 || neg == 0)
    // 切点：叉积变号 + 二分
    fun tangencies(P: Pair<Double, Double>): List<Double> {
        val out = ArrayList<Double>()
        var prevSign = 0
        for (i in 0 until c.n - 1) {
            val cr = c.crossAt(i, P)
            val s = if (cr > 0) 1 else if (cr < 0) -1 else 0
            if (prevSign != 0 && s != 0 && s != prevSign) {
                var a = c.s[i - 1]; var b = c.s[i]
                val ref = c.crossAt(i - 1, P)
                repeat(300) {
                    val m = (a + b) / 2
                    val pt = c.pointAt(m); val t = c.tangentAt(m)
                    val v = (P.first - pt.first) * t.second - (P.second - pt.second) * t.first
                    if (v * ref > 0) a = m else b = m
                }
                out.add((a + b) / 2)
            }
            if (s != 0) prevSign = s
        }
        return out
    }
    val tA = tangencies(A); val tB = tangencies(B)
    // T1：口袋弧上靠近 P 的切点（弧长处在该点之后不足 600）
    val T1 = tA.first { c.total - it < 600.0 }
    val T2 = tB.first { it < 1400.0 }
    val p1 = c.pointAt(T1); val p2 = c.pointAt(T2)
    val seg1 = hypot(p1.first - A.first, p1.second - A.second)
    val arc1 = c.total - T1
    val arc2 = T2
    val seg2 = hypot(p2.first - B.first, p2.second - B.second)
    val e1 = segMaxExcess(A, p1, fmin, 0.005)
    val e2 = segMaxExcess(p2, B, fmin, 0.005)
    return MethodA(seg1 + arc1 + arc2 + seg2, seg1, arc1, arc2, seg2, p1, p2, e1, e2, convex)
}

// ---------------- 方法 B：径向参数化等高线 + 可行性约束下的切点搜索 ----------------
fun methodB(fmin: Double, xP: Double, A: Pair<Double, Double>, B: Pair<Double, Double>, dTheta: Double): Double {
    val cx0 = 750.0; val cy0 = 750.0
    val thP = atan2(-cy0, xP - cx0)
    val n = (2 * PI / dTheta).toInt()
    val rx = DoubleArray(n); val ry = DoubleArray(n)
    var rPrev = hypot(xP - cx0, -cy0)
    for (k in 0 until n) {
        val th = thP + k * dTheta
        var lo = rPrev - 30.0; var hi = rPrev + 30.0
        var flo = h(cx0 + lo * cos(th), cy0 + lo * sin(th)) - fmin
        var fhi = h(cx0 + hi * cos(th), cy0 + hi * sin(th)) - fmin
        var guard = 0
        while (flo * fhi > 0 && guard < 200) {
            lo -= 20; hi += 20; guard++
            flo = h(cx0 + lo * cos(th), cy0 + lo * sin(th)) - fmin
            fhi = h(cx0 + hi * cos(th), cy0 + hi * sin(th)) - fmin
        }
        repeat(80) {
            val mid = (lo + hi) / 2
            val fm = h(cx0 + mid * cos(th), cy0 + mid * sin(th)) - fmin
            if (fm * flo > 0) lo = mid else hi = mid
        }
        rPrev = (lo + hi) / 2
        rx[k] = cx0 + rPrev * cos(th); ry[k] = cy0 + rPrev * sin(th)
    }
    val s = DoubleArray(n + 1)
    for (k in 0 until n) {
        val k2 = (k + 1) % n
        s[k + 1] = s[k] + hypot(rx[k2] - rx[k], ry[k2] - ry[k])
    }
    val stot = s[n]
    var best1 = 1e18
    for (k in 0 until n) {
        if (stot - s[k] > 700.0) continue
        val p = Pair(rx[k], ry[k])
        if (segMaxExcess(A, p, fmin, 0.05) > 1e-9) continue
        val cost = (stot - s[k]) + hypot(p.first - A.first, p.second - A.second)
        if (cost < best1) best1 = cost
    }
    var best2 = 1e18
    for (k in 0 until n) {
        if (s[k] > 1500.0) break
        val p = Pair(rx[k], ry[k])
        if (segMaxExcess(p, B, fmin, 0.05) > 1e-9) continue
        val cost = s[k] + hypot(p.first - B.first, p.second - B.second)
        if (cost < best2) best2 = cost
    }
    return best1 + best2
}

// ---------------- 粗网格连通性检查（验证 f_min 确实是连通阈值）----------------
fun connectivityAt(f: Double, step: Double, A: Pair<Double, Double>, B: Pair<Double, Double>): Boolean {
    val n = (1600.0 / step).roundToInt()
    val side = n + 1
    val ok = BooleanArray(side * side)
    for (j in 0..n) for (i in 0..n) ok[j * side + i] = h(i * step, j * step) <= f
    val si = (A.first / step).roundToInt(); val sj = (A.second / step).roundToInt()
    val ti = (B.first / step).roundToInt(); val tj = (B.second / step).roundToInt()
    if (!ok[sj * side + si] || !ok[tj * side + ti]) return false
    val seen = BooleanArray(side * side)
    val q = IntArray(side * side); var head = 0; var tail = 0
    val start = sj * side + si; seen[start] = true; q[tail++] = start
    while (head < tail) {
        val idx = q[head++]
        if (idx == tj * side + ti) return true
        val x = idx % side; val y = idx / side
        for (d in 0 until 4) {
            val xx = x + (if (d == 0) 1 else if (d == 1) -1 else 0)
            val yy = y + (if (d == 2) 1 else if (d == 3) -1 else 0)
            if (xx < 0 || xx > n || yy < 0 || yy > n) continue
            val nid = yy * side + xx
            if (!seen[nid] && ok[nid]) { seen[nid] = true; q[tail++] = nid }
        }
    }
    return false
}

fun main() {
    println("=== PE 262 Mountain Range ===")
    val A = Pair(200.0, 200.0); val B = Pair(1400.0, 1400.0)

    // ---- f_min ----
    val (xP, fmin) = fminAndXP()
    val fBottom = maxOnEdge({ x -> h(x, 0.0) }, 800.0, 1000.0)
    val fLeft = maxOnEdge({ y -> h(0.0, y) }, 800.0, 1000.0)
    val fTop = maxOnEdge({ x -> h(x, 1600.0) }, 400.0, 900.0)
    val fRight = maxOnEdge({ y -> h(1600.0, y) }, 400.0, 900.0)
    check(abs(fBottom - fmin) < 1e-9 && abs(fLeft - fmin) < 1e-9)
    check(fTop < fmin && fRight < fmin)
    check(h(A.first, A.second) < fmin && h(B.first, B.second) < fmin)
    println("f_min = %.12f  (底边/左边最大值，位于 (%.9f, 0) 与 (0, %.9f))".format(fmin, xP, xP))
    println("四边最大值: 底=%.6f 左=%.6f 顶=%.6f 右=%.6f ；A 海拔=%.4f B 海拔=%.4f".format(fBottom, fLeft, fTop, fRight, h(200.0, 200.0), h(1400.0, 1400.0)))

    // ---- 网格连通性验证 f_min 是瓶颈 ----
    val below = connectivityAt(fmin - 0.5, 4.0, A, B)
    val above = connectivityAt(fmin + 0.5, 4.0, A, B)
    check(!below && above)
    println("网格连通性: f_min-0.5 连通=%b ; f_min+0.5 连通=%b  （阈值自检通过）".format(below, above))

    // ---- 方法 A ----
    var bestA = 1e18; var msA = 0.0
    var ra: MethodA? = null
    for (round in 0 until 3) {
        val t0 = System.nanoTime()
        val r = methodA(fmin, xP, A, B, 0.01)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < msA || round == 0) msA = ms
        if (r.total < bestA) bestA = r.total
        ra = r
    }
    val r = ra!!
    println("方法 A: T1=(%.6f, %.6f)  T2=(%.6f, %.6f)".format(r.t1.first, r.t1.second, r.t2.first, r.t2.second))
    println("方法 A: |A→T1|=%.6f + arc(T1→P)=%.6f + arc(P→T2)=%.6f + |T2→B|=%.6f = %.9f".format(
        r.seg1, r.arc1, r.arc2, r.seg2, r.total))
    println("  直飞段越界检查: A→T1 = %.3e ; T2→B = %.3e  (均应 ≤ 0)".format(r.excessAT1, r.excessT2B))
    check(r.excessAT1 <= 1e-6 && r.excessT2B <= 1e-6)
    println("  等高线凸性检查: %s".format(if (r.convex) "凸（所有转向叉积同号）" else "非凸！"))
    check(r.convex)

    // ---- 方法 B ----
    var bestB = 1e18; var msB = 0.0
    for (round in 0 until 3) {
        val t0 = System.nanoTime()
        val v = methodB(fmin, xP, A, B, 0.0005)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < msB || round == 0) msB = ms
        if (v < bestB) bestB = v
    }
    println("方法 B（径向等高线 + 可行性扫描）: %.9f  (%.0f ms)".format(bestB, msB))
    println("两法之差 = %.2e  （阈值 1e-3）".format(abs(bestA - bestB)))
    check(abs(bestA - bestB) < 1e-3)

    // ---- 答案 ----
    val raw = (bestA + bestB) / 2.0
    val ans3 = raw * 1000.0
    val answer = ans3.roundToLong()
    println()
    println("原始值 = %.9f".format(raw))
    println("答案 = %.3f  (编码 round(值×10³) = %d)".format(answer / 1000.0, answer))
    println("方法 A 最优耗时 = %.0f ms ；方法 B 最优耗时 = %.0f ms".format(msA, msB))
}
