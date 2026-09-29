#!/usr/bin/env kotlin
/**
 * Project Euler 264 — Triangle Centres（三角形的中心）
 *
 * 思路
 * ────
 * 设顶点 A,B,C 为整点。外心在原点 O ⇒ |A|=|B|=|C|=R（R² 为整数，记 N=R²）。
 * 外心在原点时垂心满足 H = A+B+C（标准恒等式：(B+C)⊥(B−C)，故 A 到 A+B+C 的连线是高）。
 * 因此题设等价于：
 *   |A|=|B|=|C|，A+B+C=(5,0)，周长 |A−B|+|B−C|+|C−A| ≤ 10⁵。
 *
 * 由 |A+B+C|² = 9N − (a²+b²+c²)（a,b,c 为三边长）得
 *   9N − 25 = a²+b²+c² ≤ (a+b+c)² ≤ 10¹⁰  ⇒  N ≤ 1 111 111 113，R ≤ 33333.33。
 * 于是每个顶点的坐标 |x|,|y| ≤ R，而 s=u+v=(p,q) 满足 |p|,|q| ≤ 2R ≤ 66666。
 *
 * 参数化（对任意一对顶点 u,v）：令 s=u+v=(p,q)，d=u−v，第三点 w=(5,0)−s。
 *   |u|=|v| ⇔ 4(|u|²−|v|²) = (|s+d|²−|s−d|²) = 4 s·d ⇒ s·d = 0；
 *   |w|=|u| ⇔ |s|²−10p+25 = (|s|²+|d|²)/4 ⇒ |d|² = 3|s|²−40p+100。
 * 记 h=gcd(p,q)（s=0 是退化分支，单独处理），(p,q)=h(p',q')，(r,s)=t(q',−p')
 * （垂直方向在格点上唯一），D=p'²+q'²，则两式合并为
 *   (t² − 3h²)·D = 100 − 40hp'。                       (★)
 * u,v 为整点 ⇔ hp'+tq' 与 hq'−tp' 同偶。每个三角形最多由 6 个有序点对表示，按规范三元组去重。
 *
 * 方法 A（主路径）：枚举 (h,p')，|p'| ≤ 66666/h；D 取 |100−40hp'| 的因子（SPF 快速分解），
 *   再要求 D ≥ p'²、D−p'² 为平方数（得 q'）、gcd(p',q')=1、h·q' ≤ 66666，由 (★) 解出 t²。
 * 方法 B（独立复核）：由 D ≥ p'²、D ≤ |100−40hp'| 与 h|p'| ≤ 66666 推出 |p'|,|q'| ≤ 1633；
 *   在矩形内枚举互素的 (p',q')，把 (★) 看成关于 h 的线性同余方程 40p'·h ≡ 100 (mod D)，
 *   用扩展欧几里得解出 h 的剩余类后逐个检验 t² —— 搜索结构与数据结构都与方法 A 不同。
 *
 * 复杂度
 * ──────
 * 方法 A：约 1.5×10⁶ 个 (h,p') 对 × 因子枚举（每对与 |C| 的因子个数成正比），
 *   时间复杂度 ~O(R²log R) 级，实测毫秒级；内存为 SPF 表 O(R) 与结果集合。
 * 方法 B：约 1.1×10⁷ 个 (p',q') 对，绝大部分被互素/同余条件淘汰，同样毫秒级。
 * 暴力对照：按圆分组后逐对枚举，代价 Σ N_R²（N_R 为半径 √N 圆上的整点数），只在小周长下可行。
 * 求和的数值精度：每条周长是三个平方根之和，用 50 位精度的 BigDecimal 求平方根再累加，
 *   最后乘 10⁴ 四舍五入——避免 double 在大数量级下的 1e−5 级误差。
 *
 * 验证
 * ────
 * · 题面锚点：周长 ≤ 50 恰有 9 个三角形，且与题面列出的 9 个完全一致，周长和 = 291.0089473…（四舍五入 291.0089）；
 * · 双方法互证：方法 A 与方法 B 在完整规模（周长 ≤ 10⁵）给出的规范三角形集合逐一相同；
 * · 暴力对照：直接按圆分组逐对枚举在周长 ≤ 200 / ≤ 1000 三组结果与方法 A、B 一致；
 * · 最终数值用 BigDecimal 高精度复核（见下 main）。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0264/solution.kt && java -cp … SolutionKt
 */

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private const val R2MAX = 1_111_111_113L          // R² 上界：(10^10+25)/9 取下整
private const val P_LIMIT = 66_666                 // |p|=|u_x+v_x| ≤ 2R ≤ 66666
private const val PP_LIMIT = 1_640                  // |p'|,|q'| 上界（推导见头部注释）
private const val DERIV_LIMIT = 4L * R2MAX          // |d|²、|s|² ≤ 4R²
private const val FULL_PERIMETER = 100_000L
private const val SQRT_PRECISION = 50
private const val EPS = 1e-3

private data class Pt(val x: Int, val y: Int) : Comparable<Pt> {
    override fun compareTo(other: Pt): Int =
        if (x != other.x) x.compareTo(other.x) else y.compareTo(other.y)
}

private data class Tri(val a: Pt, val b: Pt, val c: Pt)

private fun tri(u: Pt, v: Pt, w: Pt): Tri {
    val l = listOf(u, v, w).sorted()
    return Tri(l[0], l[1], l[2])
}

private fun dist2(u: Pt, v: Pt): Long {
    val dx = (u.x - v.x).toLong()
    val dy = (u.y - v.y).toLong()
    return dx * dx + dy * dy
}

/** 用 double 做带容差的周长筛选（最终答案另用 BigDecimal 精确判定）。 */
private fun perimLE(u: Pt, v: Pt, w: Pt, pMax: Long): Boolean {
    val p = Math.sqrt(dist2(u, v).toDouble()) +
        Math.sqrt(dist2(v, w).toDouble()) +
        Math.sqrt(dist2(w, u).toDouble())
    return p <= pMax + EPS
}

private fun addIfValid(out: MutableSet<Tri>, u: Pt, v: Pt, w: Pt, pMax: Long) {
    if (u == v || v == w || u == w) return
    check(dist2(u, u) == dist2(v, v) && dist2(u, u) == dist2(w, w)) { "构造错误：三点不同圆" }
    check(u.x + v.x + w.x == 5 && u.y + v.y + w.y == 0) { "构造错误：垂心不是 (5,0)" }
    if (!perimLE(u, v, w, pMax)) return
    out.add(tri(u, v, w))
}

private fun isqrt(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}

private fun gcdInt(a: Int, b: Int): Int {
    var x = if (a < 0) -a else a
    var y = if (b < 0) -b else b
    while (y != 0) { val t = x % y; x = y; y = t }
    return x
}

/** 扩展欧几里得：返回 gcd(a,b)，并给出 x,y 使 a·x+b·y=gcd。 */
private fun extGcd(a: Long, b: Long): Triple<Long, Long, Long> {
    if (b == 0L) return Triple(a, 1L, 0L)
    val (g, x1, y1) = extGcd(b, a % b)
    return Triple(g, y1, x1 - (a / b) * y1)
}

private fun spfSieve(n: Int): IntArray {
    val spf = IntArray(n + 1)
    for (i in 2..n) {
        if (spf[i] == 0) {
            spf[i] = i
            if (i.toLong() * i <= n.toLong()) {
                var j = i * i
                while (j <= n) {
                    if (spf[j] == 0) spf[j] = i
                    j += i
                }
            }
        }
    }
    return spf
}

/** 对 n ≥ 1 枚举全部正因子（含 1 与 n）。 */
private inline fun forEachDivisor(n: Int, spf: IntArray, action: (Int) -> Unit) {
    var k = 0
    val ps = IntArray(12)
    val es = IntArray(12)
    var m = n
    while (m > 1) {
        val pr = spf[m]
        var e = 0
        while (m % pr == 0) { m /= pr; e++ }
        ps[k] = pr
        es[k] = e
        k++
    }
    var divs = IntArray(1) { 1 }
    for (i in 0 until k) {
        val pr = ps[i]
        val e = es[i]
        val next = IntArray(divs.size * (e + 1))
        var idx = 0
        var pk = 1
        for (ee in 0..e) {
            for (d in divs) next[idx++] = d * pk
            pk *= pr
        }
        divs = next
    }
    for (d in divs) action(d)
}

/** s=0 的退化分支：u=−v ⇒ w=(5,0)、N=25；u 取圆 x²+y²=25 上的整点（去掉 ±(5,0)）。 */
private fun addZeroSumFamily(out: MutableSet<Tri>, pMax: Long) {
    val w = Pt(5, 0)
    for (x in -5..5) for (y in -5..5) {
        if (x * x + y * y != 25) continue
        val u = Pt(x, y)
        val v = Pt(-x, -y)
        if (u == w || v == w) continue
        if (u < v) addIfValid(out, u, v, w, pMax)     // 每对 {u,−u} 只取一次
    }
}

/** 由 (h,p',q',t) 生成两个候选三角形（t 的两个符号）。 */
private fun emit(out: MutableSet<Tri>, h: Long, pp: Int, q: Int, t: Long, pMax: Long) {
    for (sign in intArrayOf(1, -1)) {
        val tt = t * sign
        val ax = h * pp + tt * q
        val ay = h * q - tt * pp
        if ((ax and 1L) != 0L || (ay and 1L) != 0L) continue
        val u = Pt((ax / 2).toInt(), (ay / 2).toInt())
        val v = Pt(((h * pp - tt * q) / 2).toInt(), ((h * q + tt * pp) / 2).toInt())
        val w = Pt((5 - h * pp).toInt(), (-h * q).toInt())
        addIfValid(out, u, v, w, pMax)
        if (t == 0L) break
    }
}

/** 方法 A：枚举 (h,p')，对 C=100−40hp' 枚举因子 D，直接解出 t²。 */
private fun solveByDivisors(pMax: Long): Set<Tri> {
    val out = HashSet<Tri>()
    addZeroSumFamily(out, pMax)
    val spf = spfSieve(100 + 40 * P_LIMIT)
    for (h in 1..P_LIMIT) {
        val hi = P_LIMIT / h
        for (pp in -hi..hi) {
            val c = 100 - 40 * h * pp
            if (c == 0) continue
            val absC = if (c > 0) c else -c
            val pp2 = pp * pp
            forEachDivisor(absC, spf) { dd ->
                if (dd >= pp2) {
                    val qq2 = dd - pp2
                    val q = isqrt(qq2.toLong()).toInt()
                    if (q.toLong() * q == qq2.toLong() &&
                        gcdInt(pp, q) == 1 &&
                        h * q <= P_LIMIT &&
                        h.toLong() * h * dd <= DERIV_LIMIT
                    ) {
                        val m = c / dd
                        val t2 = 3L * h * h + m
                        if (t2 >= 0) {
                            val t = isqrt(t2)
                            if (t * t == t2) emit(out, h.toLong(), pp, q, t, pMax)
                        }
                    }
                }
            }
        }
    }
    return out
}

/** 方法 B：矩形内枚举互素 (p',q')，用同余方程 40p'·h ≡ 100 (mod D) 解 h。 */
private fun solveByCongruence(pMax: Long): Set<Tri> {
    val out = HashSet<Tri>()
    addZeroSumFamily(out, pMax)
    for (pp in -PP_LIMIT..PP_LIMIT) {
        for (qq in -PP_LIMIT..PP_LIMIT) {
            if (gcdInt(pp, qq) != 1) continue
            val dd = pp * pp + qq * qq
            if (dd == 0) continue
            // 40·pp·h ≡ 100 (mod dd)
            var a = (40L * pp) % dd
            if (a < 0) a += dd
            val (g, x0, _) = extGcd(a, dd.toLong())
            if (100L % g != 0L) continue
            val md = dd / g
            var hMax = Long.MAX_VALUE
            if (pp != 0) hMax = minOf(hMax, (P_LIMIT / kotlin.math.abs(pp)).toLong())
            if (qq != 0) hMax = minOf(hMax, (P_LIMIT / kotlin.math.abs(qq)).toLong())
            hMax = minOf(hMax, isqrt(DERIV_LIMIT / dd))
            if (hMax < 1L) continue
            var h = if (md == 1L) 1L else {
                val inv = ((x0 % md) + md) % md
                val r = ((100L / g) % md * inv) % md
                if (r == 0L) md else r
            }
            while (h <= hMax) {
                val c = 100L - 40L * h * pp
                // 同余条件保证 dd | c
                check(c % dd == 0L) { "同余解错误：$c 不被 $dd 整除" }
                val m = c / dd
                val t2 = 3L * h * h + m
                if (t2 >= 0) {
                    val t = isqrt(t2)
                    if (t * t == t2) emit(out, h, pp, qq, t, pMax)
                }
                h += md
            }
        }
    }
    return out
}

/** 直接暴力：按 N=x²+y² 给全部整点分组，逐有序点对枚举第三点。 */
private fun solveByBrute(pMax: Long): Set<Tri> {
    val out = HashSet<Tri>()
    val r2 = (pMax * pMax + 25) / 9
    val rlim = isqrt(r2).toInt() + 2
    val off = 40_000L
    var cnt = 0
    for (x in -rlim..rlim) for (y in -rlim..rlim) if ((x * x + y * y).toLong() <= r2) cnt++
    val keys = LongArray(cnt)
    var n = 0
    for (x in -rlim..rlim) {
        val x2 = x * x
        for (y in -rlim..rlim) {
            val nn = x2 + y * y
            if (nn.toLong() > r2) continue
            keys[n++] = (nn.toLong() shl 34) or ((x + off) shl 17) or (y + off)
        }
    }
    java.util.Arrays.sort(keys)
    var i = 0
    while (i < n) {
        var j = i
        val norm = keys[i] ushr 34
        while (j < n && (keys[j] ushr 34) == norm) j++
        for (ia in i until j) {
            val ax = ((keys[ia] shr 17) and 0x1FFFF) - off
            val ay = (keys[ia] and 0x1FFFF) - off
            for (ib in i until j) {
                if (ia == ib) continue
                val bx = ((keys[ib] shr 17) and 0x1FFFF) - off
                val by = (keys[ib] and 0x1FFFF) - off
                val cx = 5 - ax - bx
                val cy = -ay - by
                if (cx * cx + cy * cy != norm) continue
                val u = Pt(ax.toInt(), ay.toInt())
                val v = Pt(bx.toInt(), by.toInt())
                val w = Pt(cx.toInt(), cy.toInt())
                addIfValid(out, u, v, w, pMax)
            }
        }
        i = j
    }
    return out
}

private fun filterByPerimeter(src: Set<Tri>, pMax: Long): Set<Tri> =
    src.filterTo(HashSet()) { perimLE(it.a, it.b, it.c, pMax) }

/** 用 50 位精度的 BigDecimal 计算 [set] 中周长精确 ≤ pMax 的三角形周长之和。 */
private fun exactSum(src: Set<Tri>, pMax: Long): Pair<BigDecimal, Int> {
    val mc = MathContext(SQRT_PRECISION)
    val limit = BigDecimal(pMax)
    var total = BigDecimal.ZERO
    var count = 0
    for (t in src) {
        val p = BigDecimal(dist2(t.a, t.b)).sqrt(mc)
            .add(BigDecimal(dist2(t.b, t.c)).sqrt(mc))
            .add(BigDecimal(dist2(t.c, t.a)).sqrt(mc))
        if (p.compareTo(limit) <= 0) {
            total = total.add(p, mc)
            count++
        }
    }
    return total to count
}

private fun bestOf3(tag: String, f: () -> Set<Tri>): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms（${out.size} 个三角形）")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

/** 题面列出的 9 个周长 ≤ 50 的三角形。 */
private val ANCHOR_50: Set<Tri> = setOf(
    tri(Pt(-4, 3), Pt(5, 0), Pt(4, -3)),
    tri(Pt(4, 3), Pt(5, 0), Pt(-4, -3)),
    tri(Pt(-3, 4), Pt(5, 0), Pt(3, -4)),
    tri(Pt(3, 4), Pt(5, 0), Pt(-3, -4)),
    tri(Pt(0, 5), Pt(5, 0), Pt(0, -5)),
    tri(Pt(1, 8), Pt(8, -1), Pt(-4, -7)),
    tri(Pt(8, 1), Pt(1, -8), Pt(-4, 7)),
    tri(Pt(2, 9), Pt(9, -2), Pt(-6, -7)),
    tri(Pt(9, 2), Pt(2, -9), Pt(-6, 7)),
)

fun main() {
    // ---------- 完整规模：两种独立方法 ----------
    val aFull = solveByDivisors(FULL_PERIMETER)
    val bFull = solveByCongruence(FULL_PERIMETER)
    check(aFull == bFull) { "方法 A（${aFull.size} 个）与方法 B（${bFull.size} 个）不一致" }
    println("方法 A 与方法 B 在周长 ≤ 10⁵ 的完整规模下三角形集合逐一相同：${aFull.size} 个")

    // ---------- 题面锚点：周长 ≤ 50 ----------
    val a50 = filterByPerimeter(aFull, 50)
    check(a50 == ANCHOR_50) { "周长 ≤ 50 的集合与题面不符：只有 ${a50.size} 个" }
    val (sum50, cnt50) = exactSum(a50, 50)
    val scaled50 = sum50.scaleByPowerOfTen(4).setScale(0, RoundingMode.HALF_UP).toLong()
    check(cnt50 == 9 && scaled50 == 2_910_089L) { "锚点不符：cnt=$cnt50 sum=$sum50" }
    println("题面锚点：周长 ≤ 50 有 $cnt50 个三角形（与题面列出的 9 个逐一相同），周长和 = $sum50 → 291.0089")

    // ---------- 暴力对照（小规模） ----------
    for (pm in longArrayOf(50L, 200L, 1000L)) {
        val brute = solveByBrute(pm)
        val flt = filterByPerimeter(aFull, pm)
        check(brute == flt) { "暴力与主方法在周长 ≤ $pm 不一致：brute=${brute.size} main=${flt.size}" }
        println("暴力对照：周长 ≤ $pm 时 ${brute.size} 个三角形，与方法 A/B 逐一相同")
    }

    // ---------- 最终答案（高精度求和 + 定点编码） ----------
    val (total, count) = exactSum(aFull, FULL_PERIMETER)
    val scaled = total.scaleByPowerOfTen(4).setScale(0, RoundingMode.HALF_UP).toLong()
    println()
    println("周长 ≤ 10⁵ 的三角形共 $count 个")
    println("周长总和（50 位精度）= $total")
    println("答案 = round(总和 × 10⁴) = $scaled")

    // ---------- 计时（JIT 预热后 3 轮最优） ----------
    solveByDivisors(50)
    solveByCongruence(50)
    solveByBrute(200)
    val msBrute = bestOf3("直接暴力 周长 ≤ 2000") { solveByBrute(2000) }
    val msA = bestOf3("方法 A 完整规模（周长 ≤ 10⁵）") { solveByDivisors(FULL_PERIMETER) }
    val msB = bestOf3("方法 B 完整规模（周长 ≤ 10⁵）") { solveByCongruence(FULL_PERIMETER) }
    println()
    println("汇总：暴力 周长 ≤ 2000 ${"%.1f".format(msBrute)} ms；方法 A ${"%.1f".format(msA)} ms；方法 B ${"%.1f".format(msB)} ms")
    println("check() 全部通过")
}
