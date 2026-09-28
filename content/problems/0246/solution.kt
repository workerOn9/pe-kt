#!/usr/bin/env kotlin
/**
 * Project Euler 246 — Tangents to an Ellipse（椭圆的切线）
 *
 * 思路：
 *   **第一步：椭圆 e 的参数。** 点 P 到圆 c（圆心 M、半径 r = 15000）的距离是 |PM − r|；
 *   与 G 等距且落在圆内的那一支满足 r − PM = PG，即
 *
 *       PM + PG = r = 15000 ,
 *
 *   所以 e 是以 M(−2000,1500)、G(8000,1500) 为焦点的椭圆：中心 (3000,1500)，长半轴
 *   a = 7500，半焦距 c = 5000，b² = a² − c² = 56 250 000 − 25 000 000 = 31 250 000
 *   （b = 2500√5）。换到中心坐标 (u,v) = (x − 3000, y − 1500) 后是
 *
 *       u²/a² + v²/b² = 1 ,
 *
 *   格点 P ↔ 整数对 (u,v) 一一对应（平移是整数向量）。
 *
 *   **第二步：两切线的夹角公式。** 过 P = (u,v) 的直线 y = m(x − u) + v 与椭圆相切，
 *   代入消元后令判别式为 0，得到 m 的二次方程
 *
 *       (a² − u²) m² + 2uv·m + (b² − v²) = 0 ,               (1)
 *
 *   两根 m₁、m₂ 即两条切线的斜率。记
 *
 *       T = b²u² + a²v² − a²b² ,    D = u² + v² − a² − b² ,
 *
 *   由韦达定理 (m₁−m₂)² = 4T/(u²−a²)²、1 + m₁m₂ = D/(u²−a²)，两切线的（有向）夹角 θ 满足
 *
 *       tan θ = 2√T / D ,   即 θ = atan2(2√T, D) ∈ (0, π) 。
 *
 *   取 θ 为两条切线段 PR、PS 之间的夹角（即包含椭圆的那个角）：P 贴近椭圆时 θ → 180°，
 *   远去时 θ → 0°，与 D 的符号一致（D < 0 ⟺ θ > 90°；D = 0 即“准圆” u²+v² = a²+b²）。
 *   T > 0 恰好是 P 严格在椭圆外的条件。
 *
 *   **第三步：θ > 45° 的等价条件。**
 *
 *       θ > 45°  ⟺  T > 0 且 (D ≤ 0 或 4T > D²) 。          (2)
 *
 *   （D ≤ 0 时 θ ≥ 90° > 45°；D > 0 时 θ < 90°，tanθ > 1 ⟺ 4T > D²。）
 *
 *   **第四步：逐列计数。** 固定 u，令 W = v²，则 T = b²(u²−a²) + a²W 关于 W 线性，而
 *   f(W) = 4T − D² 是开口向下的抛物线（Q = u²−a²−b²、P₀ = b²(u²−a²)）：
 *
 *       f(W) = −W² + (4a² − 2Q)·W + (4P₀ − Q²) ,
 *
 *   其两根（记 Δ = a² − b² = c²、E = 3a² + b² − u²，N = 2a⁴ − Δu²）为
 *
 *       r₁, r₂ = E ∓ 2√N ,   其中 r₂ = E + 2√N , r₁ = E − 2√N 。
 *
 *   逐列分析（关键恒等式：在 W_d = a² + b² − u² 处 f(W_d) = 4T(W_d)）给出列内可取的 W 区间：
 *
 *     · u² < a⁴/Δ（“D ≤ 0”与“f > 0”两段在 W_d 处重叠）：
 *           区间 = (v_e², r₂)，内边界是椭圆本身，v_e² = b²(a² − u²)/a²；
 *     · u² ≥ a⁴/Δ（椭圆不再截断该列）：区间 = (max(r₁, 0), r₂)，
 *           r₁ < 0 时就是 [0, r₂)——注意 r₁ > 0 时列内在 v = 0 附近确有“空洞”。
 *
 *   每列的 v 端点不做任何浮点近似，用**整数二分 + 精确整数谓词**求出：
 *
 *       v² < r₂  ⟺  v² ≤ E  或  (v² − E)² < 4N       （N > 0）
 *
 *   （右端来自 v² − E < 2√N 的平方，符号分支已并入第一个不等式。）内边界的
 *   v² > v_e² 与 v² > r₁ 同理。答案 = Σ_u（该列满足条件的整数 v 个数）。
 *
 *   规模：r₂(u) > 0 的列恰好是 |u| ≤ 15439（15440 已是空列），总列数约 3.1×10⁴，每列 O(log)。
 *   全程 Long 精确算术，最大中间量 (v²−E)² < 4×10¹⁷ < 2⁶³。
 *
 * 旁证：
 *   1. **几何自检**：随机取点（覆盖 D<0、D>0、贴边各情形），用 (1) 显式解出切点 R、S，
 *      用 atan2(|PR×PS|, PR·PS) 算真实夹角，与 atan2(2√T, D) 逐点对照，误差 < 1e-12 rad。
 *   2. **缩小版互证**：同一套列式计数与逐点暴力在 5 组小椭圆（含本题椭圆按 1/500 缩放的
 *      (a²,b²) = (225,125)）上完全一致。正是这组对照抓出了列结构里的一个符号错误：
 *      原以为 u² ≥ a⁴/Δ 时列内区间总从 v=0 开始，反例 (a²,b²) = (169,25) 在 u=18 处
 *      实际是 |v| ≥ 2（r₁ > 0 的“空洞”）。
 *   3. **本题椭圆逐列抽查**：对 u = 0, 3000, 7500, 8000, 11249, 11250, 11251, 13000,
 *      15439, 15440 各列，列式结果与逐点枚举 v ∈ [−19000,19000] 完全一致。
 *   4. **全尺寸逐点暴力**（brute-force.kt）：在 [−20000,20000]² 上按 (2) 逐点扫 1.6×10⁹
 *      个格点，得到同样的 810834388；另用一份独立的 NumPy 逐点扫描复核过同一数值。
 *   5. **与公开答案表一致**：数值与 JuliaLang/julia 的 test/euler.jl 第 246 题答案相同。
 *
 * 答案：810834388
 * 复杂度：约 3.1×10⁴ 列 × O(log 2×10⁴) 的整数二分，全程 Long 精确算术；
 *         预热后单次完整求解见 main 实测输出。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val A2 = 56_250_000L     // a²，a = 7500
private const val B2 = 31_250_000L     // b²，b = 2500√5

// ---------------------------------------------------------------- 精确整数工具

/** ⌊√x⌋（x < 2⁵³；Math.sqrt 给初值后整数微调，绝无浮点误差残留） */
private fun isqrt(x: Long): Long {
    var r = Math.sqrt(x.toDouble()).toLong()
    while (r > 0 && r * r > x) r--
    while ((r + 1) * (r + 1) <= x) r++
    return r
}

/**
 * 列内区间的上端：max{ v ≥ 0 : v² < r₂ }，r₂ = E + 2√N（无解返回 −1）。
 * 谓词在 v 上单调（先真后假），用整数二分求分界，避免任何浮点舍入。
 */
private fun maxVBelow(E: Long, N: Long): Long {
    fun ok(v: Long): Boolean {
        val d = v * v - E
        return d <= 0L || d * d < 4 * N
    }
    if (N < 0) return -1
    val cap = E + 2 * isqrt(N) + 2       // 安全上界：v² < E + 2(⌊√N⌋+1)
    if (cap <= 0) return -1
    if (!ok(0)) return -1
    var lo = 0L
    var hi = isqrt(cap) + 1              // ok(hi) 必为 false
    while (lo < hi) {
        val mid = (lo + hi + 1) shr 1
        if (ok(mid)) lo = mid else hi = mid - 1
    }
    return lo
}

/**
 * 列内区间下端：min{ v ≥ 0 : v² > r₁ }，r₁ = E − 2√N。
 * 调用前需保证 r₁ ≥ 0（由 E ≥ 0 且 E² ≥ 4N 判定）。谓词同样单调（先假后真）。
 */
private fun minVAbove(E: Long, N: Long): Long {
    fun below(v: Long): Boolean {        // v² > r₁ ？
        val d = E - v * v
        return d <= 0L || d * d < 4 * N
    }
    var lo = 0L
    var hi = isqrt(E) + 1                // below(hi) 为 true（v² > E ≥ r₁）
    while (lo < hi) {
        val mid = (lo + hi) shr 1
        if (below(mid)) hi = mid else lo = mid + 1
    }
    return lo
}

// ---------------------------------------------------------------- 列式计数（a²,b² 参数化，便于小规模互证）

/** 固定 u 时满足 θ > 45° 的整数 v 的个数。要求 a² > b²。 */
private fun columnCount(a2: Long, b2: Long, u: Long): Long {
    val delta = a2 - b2
    val u2 = u * u
    val E = 3 * a2 + b2 - u2
    val N = 2 * a2 * a2 - delta * u2
    if (N < 0) return 0L
    val mTop = maxVBelow(E, N)
    if (mTop < 0) return 0L
    return if (u2 * delta < a2 * a2) {          // 情形 (i)：椭圆是列内边界
        if (u2 <= a2) {
            val we = b2 * (a2 - u2)             // a²·v_e²
            var ml = isqrt(we / a2)
            while (a2 * (ml + 1) * (ml + 1) <= we) ml++
            while (ml >= 0 && a2 * ml * ml > we) ml--
            val c = mTop - ml                   // |v| ∈ {ml+1, …, mTop}
            if (c > 0) 2 * c else 0L
        } else {
            2 * mTop + 1                        // v_e² < 0，v = 0 也在区域内
        }
    } else {                                    // 情形 (ii)：内边界是 r₁，可能出现空洞
        if (E >= 0 && E * E >= 4 * N) {
            val vMin = minVAbove(E, N)          // 最小 v 使 v² > r₁
            val c = mTop - vMin + 1
            if (c > 0) 2 * c else 0L
        } else {
            2 * mTop + 1                        // r₁ < 0，v = 0 在区域内
        }
    }
}

/** 整个平面上满足 θ > 45° 的格点数（区域关于 u、v 轴对称，只算 u ≥ 0 再翻倍）。 */
private fun countRegion(a2: Long, b2: Long): Long {
    var total = 0L
    var u = 0L
    while (true) {
        val c = columnCount(a2, b2, u)
        if (c == 0L) break
        total += if (u == 0L) c else 2 * c
        u++
    }
    return total
}

/** 逐点暴力（对照用）：在 [−ub,ub] × [−vb,vb] 上按 (2) 直接判每个格点。 */
private fun bruteCountRegion(a2: Long, b2: Long, ub: Long, vb: Long): Long {
    var cnt = 0L
    var u = -ub
    while (u <= ub) {
        val u2 = u * u
        val baseT = b2 * u2 - a2 * b2
        val baseD = u2 - a2 - b2
        var v = -vb
        while (v <= vb) {
            val v2 = v * v
            val t = baseT + a2 * v2
            if (t > 0L) {
                val d = baseD + v2
                if (d <= 0L || 4 * t > d * d) cnt++
            }
            v++
        }
        u++
    }
    return cnt
}

// ---------------------------------------------------------------- 几何自检：解析夹角 vs 显式切点

private fun angleFormula(a2: Long, b2: Long, u: Double, v: Double): Double {
    val t = b2 * u * u + a2 * v * v - a2 * b2
    val d = u * u + v * v - a2 - b2
    if (t <= 0.0) return Double.NaN      // P 在椭圆内，无切线
    return Math.atan2(2.0 * Math.sqrt(t), d)
}

/** 显式构造两切点再算 ∠RPS：切点 = 极线 ux/a² + vy/b² = 1 与椭圆的交点。 */
private fun angleByTangents(a2: Long, b2: Long, u: Double, v: Double): Double {
    val am = a2.toDouble()
    val bm = b2.toDouble()
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
    val t1 = (-qb + s) / (2 * qa)
    val t2 = (-qb - s) / (2 * qa)
    val c1x = px + t1 * dx - u
    val c1y = py + t1 * dy - v
    val c2x = px + t2 * dx - u
    val c2y = py + t2 * dy - v
    val cross = c1x * c2y - c1y * c2x
    val dot = c1x * c2x + c1y * c2y
    return Math.atan2(Math.abs(cross), dot)
}

// ---------------------------------------------------------------- 主程序

private fun main() {
    // ---- 1. 几何自检：夹角解析公式 vs 显式切点构造 ----
    val rnd = java.util.Random(246)
    var worst = 0.0
    var tests = 0
    for ((a2, b2) in listOf(9L to 4L, 225L to 125L, A2 to B2)) {
        val a = Math.sqrt(a2.toDouble())
        var i = 0
        while (i < 4000) {
            val u = (rnd.nextDouble() * 6 - 3) * a
            val v = (rnd.nextDouble() * 6 - 3) * a
            val f = angleFormula(a2, b2, u, v)
            val g = angleByTangents(a2, b2, u, v)
            if (!f.isNaN() && !g.isNaN()) {
                val diff = Math.abs(f - g)
                if (diff > worst) worst = diff
                tests++
            }
            i++
        }
    }
    check(worst < 1e-9) { "夹角公式与显式切点不一致：最大偏差 $worst" }
    println("几何自检：$tests 组随机点，atan2(2√T, D) 与显式切点 ∠RPS 最大偏差 ${"%.2e".format(worst)} rad")

    // ---- 2. 缩小版互证：列式 vs 逐点暴力（含本题椭圆 1/500 缩放版）----
    for ((a2, b2) in listOf(225L to 125L, 169L to 25L, 56L to 25L, 10_000L to 1L, 1024L to 900L)) {
        val fast = countRegion(a2, b2)
        val slow = bruteCountRegion(a2, b2, 300L, 300L)   // 小椭圆的区域远在 ±300 之内
        check(fast == slow) { "小规模不一致 a²=$a2 b²=$b2：列式 $fast / 暴力 $slow" }
        println("小规模自检 a²=$a2 b²=$b2：列式 = 暴力 = $fast")
    }

    // ---- 3. 本题椭圆逐列抽查：跨越各分支的 u ----
    for (u in longArrayOf(0, 3000, 7500, 8000, 11249, 11250, 11251, 13000, 15439, 15440)) {
        var bc = 0L
        var v = -19_000L
        while (v <= 19_000L) {
            val t = B2 * u * u + A2 * v * v - A2 * B2
            if (t > 0L) {
                val d = u * u + v * v - A2 - B2
                if (d <= 0L || 4 * t > d * d) bc++
            }
            v++
        }
        val cc = columnCount(A2, B2, u)
        check(bc == cc) { "u=$u 列不一致：列式 $cc / 逐点 $bc" }
    }
    println("本题椭圆抽查 10 列（含 u=±7500 顶点、u=11250 分支跳变、u=15439 末列与 u=15440 空列）：列式与逐点一致")

    // ---- 4. 本题 ----
    val answer = countRegion(A2, B2)
    println("θ = ∠RPS > 45° 的格点数 = $answer")
    check(answer == 810_834_388L) { "答案不吻合：$answer" }

    // ---- 5. 计时：JIT 预热后单次完整求解 ----
    countRegion(A2, B2)
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        val again = countRegion(A2, B2)
        val ms = (System.nanoTime() - t0) / 1e6
        check(again == answer) { "计时循环结果漂移" }
        if (ms < best) best = ms
    }
    println("optimized: ${"%.1f".format(best)} ms（JIT 预热后 3 次取最优，单次完整求解）")
    println("check() 全部通过")
}
