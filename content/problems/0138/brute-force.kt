import java.math.BigInteger

/**
 * Project Euler 138 — 独立对照解（勾股参数化直接搜索，与 solution.kt 的 Pell 解链互补）
 *
 * 思路：完全不做代数数论变换，回到「整数直角三角形」的定义去搜。
 * 记半底边 x = b/2（等腰三角形的高把底边平分），则 (x, h, L) 是直角三角形三边，且 h = 2x ± 1。
 * 由 ①（模 4 论证：b 奇则 4L² = 4h² + b² 左 ≡ 0、右 ≡ 1）知 b 偶，x 为整数。
 *
 * 所有整数直角三角形都能写成 x = d·2mn、h = d(m² − n²)、L = d(m² + n²)（m > n ≥ 1，d ≥ 1）。
 * 把 h = 2x ± 1 代入并约去公因子：
 *     d(m² − n²) = 4dmn ± 1  ⟹  d(m² − 4mn − n²) = ±1  ⟹  d = 1 且 (m − 2n)² = 5n² ± 1。
 * 注意另一套指派（x = d(m² − n²)、h = 2dmn）会给出「偶数 = 奇数」的矛盾，故不存在。
 * 缩放因子 d 被逼成 1 这一点很关键：它说明**每个解都必来自 n 上的完全平方判别**，
 * 于是可以直接扫描：
 *     n = 1, 2, 3, …，若 5n² + 1 或 5n² − 1 是完全平方数 s²，取 m = 2n + s，
 *     还原 x = 2mn、h = m² − n²、L = m² + n²，校验 h = 2x ± 1 后计入。
 * （另一支 m = 2n − s 永远不合法：5n² + 1 = s² 给 s > 2n；5n² − 1 = s² 在 n = 1 时 s = 2n，
 *   n ≥ 2 时因 5n² − 1 > 4n² 而 s > 2n。两支都会被 m > n 的检查挡下。）
 * 扫到第 12 个为止：n 需到 7465176，m 到 31622993，L 到 1055742538989025。
 *
 * 与优化解的差别：优化解沿 Pell 解链 (2 + √5)^(2k+1) 以 O(1)/步「跳」过去；
 * 本解在 n ∈ [1, 7.47×10^6] 上逐个做判别式开方，是「解不会漏」的独立佐证。
 * 文件里还附了一条最朴素的枚举：直接按定义扫底边 b 并用 4L² = 4h² + b² 判 L 是否为整数，
 * 在小范围（b ≤ 10^6）上与参数化搜索逐项比对。
 *
 * 复杂度：时间 O(n_max) ≈ 7.5×10^6 次整数开方判别，空间 O(1)；
 * 完全不经过 (2 + √5) 的幂递推，也不共享优化解的任何代码路径。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

/** 一个满足 |h − b| = 1 的等腰三角形。 */
data class Sides(val base: Long, val leg: Long, val height: Long)

/** 整数开方（向下取整）；输入不超过 2.8×10^14，Long 全程安全。 */
private fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** 由勾股参数 (m, n) 还原三角形；参数非法或 h ≠ b ± 1 时返回 null。 */
private fun fromParameters(m: Long, n: Long): Sides? {
    if (n < 1 || m <= n) return null
    val x = 2 * m * n                       // 半底边
    val height = m * m - n * n
    val leg = m * m + n * n
    if (height != 2 * x + 1 && height != 2 * x - 1) return null
    // x² ≈ 2.2×10^29 超过 Long，用 BigInteger 校验勾股关系
    val bigX = BigInteger.valueOf(x)
    val bigH = BigInteger.valueOf(height)
    val bigL = BigInteger.valueOf(leg)
    check(bigL * bigL == bigX * bigX + bigH * bigH) { "勾股关系不成立：m=$m n=$n" }
    return Sides(2 * x, leg, height)
}

/** 判别式 v 为完全平方数 s² 时的候选三角形（取 m = 2n + s），否则返回 null。 */
private fun candidateOf(n: Long, v: Long): Sides? {
    val s = isqrt(v)
    return if (s * s == v) fromParameters(2 * n + s, n) else null
}

/** 按 n 递增扫描完全平方判别式，收集前 count 个三角形（顺序即三角形由小到大）。 */
fun scanTriangles(count: Int): List<Sides> {
    val found = ArrayList<Sides>(count)
    var n = 1L
    while (found.size < count) {
        val square = 5 * n * n
        candidateOf(n, square + 1)?.let { found.add(it) }
        candidateOf(n, square - 1)?.let { found.add(it) }
        n++
    }
    return found
}

fun solveBruteForce(count: Int = 12): Long = scanTriangles(count).sumOf { it.leg }

/**
 * 最朴素的按定义枚举：底边 b 从 2 到 baseLimit 逐个试 h = b ± 1，
 * 用 4L² = 4h² + b² 判 L 是否为整数（奇偶底边一并试，用于实证「底边必为偶数」）。
 */
fun naiveTriangles(baseLimit: Long): List<Sides> {
    val found = ArrayList<Sides>()
    var b = 2L
    while (b <= baseLimit) {
        acceptIfIntegerLeg(found, b, b - 1)
        acceptIfIntegerLeg(found, b, b + 1)
        b++
    }
    return found
}

/** 4L² = 4h² + b² 中的 L 若为整数（含底边为奇数的情形），记入 found。 */
private fun acceptIfIntegerLeg(found: MutableList<Sides>, b: Long, h: Long) {
    val fourLs = 4 * h * h + b * b
    val s = isqrt(fourLs)
    if (s * s == fourLs && s % 2 == 0L) found.add(Sides(b, s / 2, h))
}

fun verifySample() {
    // 题面样例：第一个 (b, L, h) = (16, 17, 15)，第二个 (272, 305, 273)
    val naive = naiveTriangles(1_000_000L)
    check(naive.map { it.base } == listOf(16L, 272L, 4896L, 87840L)) { "朴素枚举：$naive" }
    check(naive[0] == Sides(16L, 17L, 15L))
    check(naive[1] == Sides(272L, 305L, 273L))
    check(naive.all { it.base % 2 == 0L }) { "底边必须全为偶数（模 4 论证）" }

    val full = scanTriangles(12)
    check(full.take(naive.size) == naive) { "参数化扫描与朴素枚举在 b ≤ 10^6 内结果不一致" }
    check(full[0] == Sides(16L, 17L, 15L) && full[1] == Sides(272L, 305L, 273L))
    check(full.zipWithNext().all { (a, b) -> b.base > a.base }) { "底边必须严格递增" }
    check(solveBruteForce(1) == 17L && solveBruteForce(2) == 322L)

    // 扫描顺带产出的勾股参数结构：由 x = 2mn、h = m² − n²、L = m² + n² 反解
    // m² = (L + h)/2、n² = (L − h)/2，再检查相邻解之间的递推。
    val params = full.map { t ->
        val mSq = (t.leg + t.height) / 2
        val nSq = (t.leg - t.height) / 2
        val m = isqrt(mSq)
        val n = isqrt(nSq)
        check(m * m == mSq && n * n == nSq) { "无法还原勾股参数：$t" }
        check(2 * m * n == t.base / 2) { "2mn 应等于半底边：m=$m n=$n $t" }
        m to n
    }
    check(params.zipWithNext().all { (a, b) -> b.second == a.first }) { "n_{k+1} 应等于 m_k：$params" }
    check((2 until params.size).all { i -> params[i].first == 4 * params[i - 1].first + params[i - 2].first }) {
        "m 应满足 a_{k+1} = 4a_k + a_{k−1}：$params"
    }
}

fun main() {
    verifySample()
    repeat(3) { solveBruteForce() }                  // JIT 预热
    // 本机同时跑着二十多个 Agent，单次计时会被别的进程挤慢（实测 27–150 ms 波动），
    // 取 9 次独立计时的最小值作为基线
    var best = Double.MAX_VALUE
    repeat(9) {
        val start = System.nanoTime()
        solveBruteForce()
        best = minOf(best, (System.nanoTime() - start) / 1e6)
    }
    System.err.printf("brute: %.4f ms%n", best)
    println(solveBruteForce())
}
