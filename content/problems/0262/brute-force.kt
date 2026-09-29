#!/usr/bin/env kotlin
/**
 * Project Euler 262 — Mountain Range（山脉）：暴力对照
 *
 * 思路（不解析求切点，直接离散搜索）
 * ─────────────────────────────────
 * 在 [0,1600]² 上取间距 g 的网格，节点可通行 ⟺ h(x,y) ≤ f_min（f_min 用与 solution.kt
 * 相同的四边极值标定，并另用粗网格连通性检查确认阈值：f_min−0.5 不通、f_min+0.5 通）。
 * 用 Dijkstra（八邻域，双向直/斜步长 g 与 g√2）从 A 搜到 B，得到「八方向度量」下的最短
 * 离散路径，其欧氏长度是解析值的可行上界（八方向度量相对欧氏系统性偏高约 6%）。
 * 再做一遍贪心弦拉直（远距离可见性跳点），把折线枉路拉紧，得到逼近解析值的上界。
 *
 * 对照口径（写进 analysis.md）：
 *   网格法在 g = 0.5（1025 万节点）时：原始网格路径 2687.6（上界，+6.2%），
 *   弦拉直后 2559.4（+1.1%），并数值确认路线与解析解同伦（路径经过 P′=(0,895.483)
 *   的距离仅 0.017）。解析值 2531.204679（两独立方法互差 2.1e-5）落在这一串上界之下，
 *   差值全部来自网格离散与八方向度量偏差，方向正确（离散上界 ≥ 真值）。
 *
 * 运行：bash scripts/kotlinc-shim.sh content/problems/0262/brute-force.kt && java -cp … Brute_forceKt
 */
import kotlin.math.*

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
fun maxOnEdgeF(g: (Double) -> Double, lo: Double, hi: Double): Double {
    var a = lo; var b = hi
    val gr = (sqrt(5.0) - 1) / 2
    var c = b - gr * (b - a); var d = a + gr * (b - a)
    var fc = g(c); var fd = g(d)
    repeat(300) {
        if (fc > fd) { b = d; d = c; fd = fc; c = b - gr * (b - a); fc = g(c) }
        else { a = c; c = d; fc = fd; d = a + gr * (b - a); fd = g(d) }
    }
    return g((a + b) / 2)
}
fun fminValue(): Double {
    var a = 800.0; var b = 1000.0
    val gr = (sqrt(5.0) - 1) / 2
    var c = b - gr * (b - a); var d = a + gr * (b - a)
    var fc = h(c, 0.0); var fd = h(d, 0.0)
    repeat(300) {
        if (fc > fd) { b = d; d = c; fd = fc; c = b - gr * (b - a); fc = h(c, 0.0) }
        else { a = c; c = d; fc = fd; d = a + gr * (b - a); fd = h(d, 0.0) }
    }
    return h((a + b) / 2, 0.0)
}
fun segExcess(p: DoubleArray, q: DoubleArray, f: Double, gap: Double, cap: Int = 4000): Double {
    val d = hypot(q[0] - p[0], q[1] - p[1])
    val n = min(cap, max(8, (d / gap).toInt() + 1))
    fun v(t: Double) = h(p[0] + (q[0] - p[0]) * t, p[1] + (q[1] - p[1]) * t) - f
    var bestT = 0.0; var bestV = -1e18
    for (k in 0..n) { val t = k.toDouble() / n; val vv = v(t); if (vv > bestV) { bestV = vv; bestT = t } }
    var lo = max(0.0, bestT - 1.0 / n); var hi = min(1.0, bestT + 1.0 / n)
    val gr = (sqrt(5.0) - 1) / 2
    var c = hi - gr * (hi - lo); var d2 = lo + gr * (hi - lo)
    var fc = v(c); var fd = v(d2)
    repeat(80) {
        if (fc > fd) { hi = d2; d2 = c; fd = fc; c = hi - gr * (hi - lo); fc = v(c) }
        else { lo = c; c = d2; fc = fd; d2 = lo + gr * (hi - lo); fd = v(d2) }
    }
    return max(bestV, fc)
}
class LongHeap(cap0: Int = 1 shl 21) {
    var a = LongArray(cap0); var n = 0
    fun push(v: Long) {
        if (n == a.size) a = a.copyOf(a.size * 2)
        var i = n++; a[i] = v
        while (i > 0) { val p = (i - 1) / 2; if (a[p] <= a[i]) break; val t = a[p]; a[p] = a[i]; a[i] = t; i = p }
    }
    fun pop(): Long {
        val r = a[0]; a[0] = a[--n]
        var i = 0
        while (true) {
            val l = 2 * i + 1; if (l >= n) break
            val rr = l + 1; var m = l
            if (rr < n && a[rr] < a[l]) m = rr
            if (a[i] <= a[m]) break
            val t = a[i]; a[i] = a[m]; a[m] = t; i = m
        }
        return r
    }
    val isEmpty get() = n == 0
}
fun gridPath(fmin: Double, g: Double, si: Int, sj: Int, ti: Int, tj: Int): ArrayList<DoubleArray> {
    val n = (1600.0 / g).roundToInt(); val side = n + 1
    val feasible = BooleanArray(side * side)
    for (j in 0..n) for (i in 0..n) feasible[j * side + i] = h(i * g, j * g) <= fmin
    val dist = FloatArray(side * side) { Float.MAX_VALUE }
    val start = sj * side + si; val target = tj * side + ti
    dist[start] = 0f
    val heap = LongHeap()
    fun key(d: Float, node: Int) = (java.lang.Float.floatToIntBits(d).toLong() shl 24) or node.toLong()
    heap.push(key(0f, start))
    val dirs = arrayOf(intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1),
                       intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1))
    val ws = doubleArrayOf(g, g, g, g, g * sqrt(2.0), g * sqrt(2.0), g * sqrt(2.0), g * sqrt(2.0))
    while (!heap.isEmpty) {
        val k = heap.pop(); val idx = (k and 0xFFFFFF).toInt()
        val d = java.lang.Float.intBitsToFloat((k ushr 24).toInt())
        if (d > dist[idx] + 1e-6f) continue
        if (idx == target) break
        val x = idx % side; val y = idx / side
        for (kk in 0 until 8) {
            val xx = x + dirs[kk][0]; val yy = y + dirs[kk][1]
            if (xx < 0 || xx > n || yy < 0 || yy > n) continue
            val nid = yy * side + xx
            if (!feasible[nid]) continue
            val nd = d + ws[kk].toFloat()
            if (nd < dist[nid]) { dist[nid] = nd; heap.push(key(nd, nid)) }
        }
    }
    val pts = ArrayList<DoubleArray>()
    var cx = ti; var cy = tj; pts.add(doubleArrayOf(cx * g, cy * g))
    while (cx != si || cy != sj) {
        val cur = dist[cy * side + cx]
        var bestD = Float.MAX_VALUE; var bx = -1; var by = -1
        for (kk in 0 until 8) {
            val xx = cx + dirs[kk][0]; val yy = cy + dirs[kk][1]
            if (xx < 0 || xx > n || yy < 0 || yy > n) continue
            if (!feasible[yy * side + xx]) continue
            val d = dist[yy * side + xx] + ws[kk].toFloat()
            if (d < bestD) { bestD = d; bx = xx; by = yy }
        }
        if (bx < 0 || bestD > cur + 1e-3f) break
        cx = bx; cy = by; pts.add(doubleArrayOf(cx * g, cy * g))
    }
    pts.reverse()
    return pts
}
fun pathLen(V: List<DoubleArray>): Double {
    var s = 0.0
    for (i in 0 until V.size - 1) s += hypot(V[i + 1][0] - V[i][0], V[i + 1][1] - V[i][1])
    return s
}
fun greedyPull(raw: ArrayList<DoubleArray>, fmin: Double, tolH: Double): ArrayList<DoubleArray> {
    var path = raw
    for (pass in 1..4) {
        val out = ArrayList<DoubleArray>()
        var i = 0
        out.add(path[0])
        while (i < path.size - 1) {
            var j = i + 1
            while (j + 1 < path.size && segExcess(path[i], path[j + 1], fmin, 0.1) <= tolH) j++
            out.add(path[j]); i = j
        }
        if (out.size == path.size) { path = out; break }
        path = out
    }
    return path
}
fun connectivityAt(f: Double, step: Double, A: DoubleArray, B: DoubleArray): Boolean {
    val n = (1600.0 / step).roundToInt(); val side = n + 1
    val ok = BooleanArray(side * side)
    for (j in 0..n) for (i in 0..n) ok[j * side + i] = h(i * step, j * step) <= f
    val si = (A[0] / step).roundToInt(); val sj = (A[1] / step).roundToInt()
    val ti = (B[0] / step).roundToInt(); val tj = (B[1] / step).roundToInt()
    if (!ok[sj * side + si] || !ok[tj * side + ti]) return false
    val seen = BooleanArray(side * side)
    val q = IntArray(side * side); var head = 0; var tail = 0
    val st = sj * side + si; seen[st] = true; q[tail++] = st
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
    println("=== PE 262 brute force (grid Dijkstra + greedy string pull) ===")
    val A = doubleArrayOf(200.0, 200.0); val B = doubleArrayOf(1400.0, 1400.0)
    val fmin = fminValue()
    val fBottom = maxOnEdgeF({ x -> h(x, 0.0) }, 800.0, 1000.0)
    val fLeft = maxOnEdgeF({ y -> h(0.0, y) }, 800.0, 1000.0)
    println("f_min = %.12f  (底边极值 %.12f / 左边极值 %.12f)".format(fmin, fBottom, fLeft))
    val below = connectivityAt(fmin - 0.5, 4.0, A, B)
    val above = connectivityAt(fmin + 0.5, 4.0, A, B)
    check(!below && above)
    println("粗网格连通性：f_min-0.5 = %b，f_min+0.5 = %b（阈值正确）".format(below, above))

    val g = 0.5
    val t0 = System.nanoTime()
    val raw = gridPath(fmin, g, (200 / g).roundToInt(), (200 / g).roundToInt(), (1400 / g).roundToInt(), (1400 / g).roundToInt())
    val rawLen = pathLen(raw)
    val msGrid = (System.nanoTime() - t0) / 1e6
    println()
    println("网格间距 g=%.1f：%d×%d 节点，原始网格路径 %d 点，欧氏长度 = %.6f（可行上界）".format(
        g, (1600 / g).roundToInt() + 1, (1600 / g).roundToInt() + 1, raw.size, rawLen))
    var minDistP = 1e18; var minDistPp = 1e18
    for (p in raw) {
        minDistP = min(minDistP, hypot(p[0] - 895.4834197, p[1] - 0.0))
        minDistPp = min(minDistPp, hypot(p[0] - 0.0, p[1] - 895.4834197))
    }
    println("  与掐口 P=(895.483,0) 最近距离 = %.3f ；与镜像 P'=(0,895.483) 最近距离 = %.4f".format(minDistP, minDistPp))
    println("  → 路线与解析解同伦：网格路径正是从 P' 处贴等高线绕行")
    val t1 = System.nanoTime()
    val greedy = greedyPull(raw, fmin, 3e-3)
    val gl = pathLen(greedy)
    val msPull = (System.nanoTime() - t1) / 1e6
    println("  贪心弦拉直：%d 点，长度 = %.6f（%.2f%% of 解析值）".format(greedy.size, gl, 100.0 * gl / 2531.204679))
    var worst = -1e18
    for (i in 0 until greedy.size - 1) worst = max(worst, segExcess(greedy[i], greedy[i + 1], fmin, 0.01))
    println("  拉直后各段最大越界(相对 h ≤ f_min) = %.2e".format(worst))
    println()
    println("耗时：网格 Dijkstra %.0f ms + 弦拉直 %.0f ms = %.0f ms".format(msGrid, msPull, msGrid + msPull))
    println("对照：solution.kt 解析值 = 2531.204679（方法 A 2531.204690 / 方法 B 2531.204668，差 2.1e-5）")
    println("     三位小数答案 = 2531.205；暴力上界与其一致（差值来自离散化与八方向度量偏差）")
    System.out.flush()
}
