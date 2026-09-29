package dev.pekt.engine

import dev.pekt.math.gcd
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * PE 264 — Triangle Centres（三角形的中心）：求所有「顶点为格点、外心在原点、垂心在 (5,0)、
 * 周长 ≤ 10^5」的三角形周长之和（四舍五入到 4 位小数；返回值按定点编码 = round(和 × 10⁴)）。
 *
 * 推导（详见 content/problems/0264/solution.kt 头部与 0264/analysis.md）：
 *   外心在原点 ⇒ |A|=|B|=|C|=R；此时垂心 = A+B+C，故 A+B+C=(5,0)。
 *   由 |A+B+C|² = 9R² − (a²+b²+c²) 与 a+b+c ≤ 10⁵ 得 R² ≤ 1 111 111 113（R ≤ 33333.33）。
 *   取一对顶点 u,v：令 s=u+v=(p,q)、d=u−v，则 |u|=|v| ⇔ s·d=0，且第三点 w=(5,0)−s 同范数
 *   ⇔ |d|² = 3|s|²−40p+100。写 h=gcd(p,q)、(p,q)=h(p',q')、d=t(q',−p')、D=p'²+q'²，得
 *   (t²−3h²)·D = 100−40hp'。u,v 为整点 ⇔ hp'+tq'、hq'−tp' 同偶。s=0 时 u=−v、w=(5,0)、R²=25，
 *   单独枚举（共 5 个三角形）。每个三角形最多由 6 个有序点对给出，用规范三元组去重。
 *
 * 方法（与 solution.kt 的方法 A 完全一致）：枚举 (h,p')（|p'| ≤ 66666/h），对 C=100−40hp'
 *   用 SPF 枚举因子 D，要求 D ≥ p'²、D−p'² 为平方数（得 q'）、gcd(p',q')=1、h·q' ≤ 66666，
 *   再要求 t² = 3h² + C/D 为平方数；由 h²D、t²D ≤ 4R² 剪枝。命中的三角形用 50 位精度的
 *   BigDecimal 求三条边长之和，精确过滤 ≤ 10⁵ 后累加，最后 round(和 × 10⁴)。
 *
 * 复杂度：约 1.5×10⁶ 个 (h,p') 对，每对枚举 |C| 的因子（SPF 分解），实测约 0.51 s；
 *   完整规模只筛出 155 个三角形。
 * 实测与校验：本机 JIT 预热后约 0.51 s（远低于 10 s 熔断线）；题面锚点「周长 ≤ 50 恰 9 个、
 *   与题面所列逐一相同、和 → 291.0089」通过；独立路径（(p',q') 矩形 + 线性同余解 h）与
 *   暴力（Pmax = 200/1000）都给出同一集合；完整答案 = round(2816417.10552269… × 10⁴) = 28164171055。
 */
internal fun solve0264Impl(): Long {
    val r2Max = 1_111_111_113L                 // R² 上界
    val pLimit = 66_666                        // |p|=|u_x+v_x| ≤ 2R
    val derivLimit = 4L * r2Max                // |s|²、|d|² ≤ 4R²
    val fullPerimeter = 100_000L
    val triangles = HashSet<Tri3>()

    fun put(u: Long, v: Long, w: Long) {
        val l = longArrayOf(u, v, w)
        l.sort()
        triangles.add(Tri3(l[0], l[1], l[2]))
    }

    // s=0 分支：w=(5,0)、R²=25、u=−v 取圆上整点（去掉 ±(5,0)）
    val w0 = packPt(5, 0)
    for (x in -5..5) for (y in -5..5) {
        if (x * x + y * y != 25) continue
        val u = packPt(x, y)
        val v = packPt(-x, -y)
        if (u == w0 || v == w0 || u > v) continue
        put(u, v, w0)
    }

    // 主枚举：(h,p') → |C| 的因子 D → q'、t
    val maxC = 100 + 40 * pLimit
    val spf = IntArray(maxC + 1)
    for (i in 2..maxC) {
        if (spf[i] == 0) {
            spf[i] = i
            if (i.toLong() * i <= maxC) {
                var j = i * i
                while (j <= maxC) {
                    if (spf[j] == 0) spf[j] = i
                    j += i
                }
            }
        }
    }
    for (h in 1..pLimit) {
        val hi = pLimit / h
        for (pp in -hi..hi) {
            val c = 100 - 40 * h * pp
            if (c == 0) continue
            val absC = if (c > 0) c else -c
            val pp2 = pp * pp
            // 枚举 |C| 的全部因子
            var k = 0
            val ps = IntArray(12)
            val es = IntArray(12)
            var m = absC
            while (m > 1) {
                val pr = spf[m]
                var e = 0
                while (m % pr == 0) { m /= pr; e++ }
                ps[k] = pr
                es[k] = e
                k++
            }
            var divs = intArrayOf(1)
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
            for (dd in divs) {
                if (dd < pp2) continue
                val qq2 = dd - pp2
                val q = isqrt264(qq2.toLong()).toInt()
                if (q.toLong() * q != qq2.toLong()) continue
                if (gcd(pp.toLong(), q.toLong()) != 1L) continue
                if (h * q > pLimit) continue
                if (h.toLong() * h * dd > derivLimit) continue
                val mm = c / dd
                val t2 = 3L * h * h + mm
                if (t2 < 0) continue
                val t = isqrt264(t2)
                if (t * t != t2) continue
                for (sign in intArrayOf(1, -1)) {
                    val tt = t * sign
                    val ax = h.toLong() * pp + tt * q
                    val ay = h.toLong() * q - tt * pp
                    if ((ax and 1L) != 0L || (ay and 1L) != 0L) continue
                    val u = packPt((ax / 2).toInt(), (ay / 2).toInt())
                    val v = packPt(
                        ((h.toLong() * pp - tt * q) / 2).toInt(),
                        ((h.toLong() * q + tt * pp) / 2).toInt(),
                    )
                    val w = packPt(5 - h * pp, -h * q)
                    if (u == v || v == w || u == w) continue
                    put(u, v, w)
                    if (t == 0L) break
                }
            }
        }
    }

    // 精确周长与求和
    val mc = MathContext(50)
    val limit = BigDecimal(fullPerimeter)
    var total = BigDecimal.ZERO
    for (t in triangles) {
        val ax = pointX(t.a); val ay = pointY(t.a)
        val bx = pointX(t.b); val by = pointY(t.b)
        val cx = pointX(t.c); val cy = pointY(t.c)
        fun d2v(x1: Int, y1: Int, x2: Int, y2: Int): Long {
            val dx = (x1 - x2).toLong()
            val dy = (y1 - y2).toLong()
            return dx * dx + dy * dy
        }
        val p = BigDecimal(d2v(ax, ay, bx, by)).sqrt(mc)
            .add(BigDecimal(d2v(bx, by, cx, cy)).sqrt(mc))
            .add(BigDecimal(d2v(cx, cy, ax, ay)).sqrt(mc))
        if (p.compareTo(limit) <= 0) total = total.add(p, mc)
    }
    return total.scaleByPowerOfTen(4).setScale(0, RoundingMode.HALF_UP).toLong()
}

private data class Tri3(val a: Long, val b: Long, val c: Long)

private fun packPt(x: Int, y: Int): Long = (x + 40_000).toLong() * 100_000 + (y + 40_000)

private fun pointX(key: Long): Int = (key / 100_000 - 40_000).toInt()

private fun pointY(key: Long): Int = (key % 100_000 - 40_000).toInt()

private fun isqrt264(v: Long): Long {
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return r
}
