#!/usr/bin/env kotlin
/**
 * Project Euler 257 — Angular Bisectors（角平分线）
 *
 * 思路：
 *   记 a = BC ≤ b = CA ≤ c = AB（图中 E 在 AB 上、F 在 BC 上、G 在 CA 上）。
 *   角平分线定理：C 的平分线交 AB 于 E，AE/EB = AC/CB = b/a，故 AE = cb/(a+b)；
 *   B 的平分线交 CA 于 G，AG/GC = AB/BC = c/a，故 AG = bc/(a+c)。
 *   角 A 是公共角，所以面积比等于夹边乘积比：
 *
 *       area(AEG)/area(ABC) = (AE/AB)·(AG/AC) = b/(a+b) · c/(a+c)，
 *
 *   所求整比
 *
 *       R = area(ABC)/area(AEG) = (a+b)(a+c)/(bc)。
 *
 *   由 a ≤ b、a ≤ c 得 R = (1+a/b)(1+a/c) ≤ 4，等号只在 a=b=c；又有 R > 1。
 *   所以「R 是整数」⟺ R ∈ {2, 3, 4}：
 *     · R = 4 ⇔ a = b = c，即全部等边三角形（边长 1..⌊N/3⌋，共 ⌊N/3⌋ 个）；
 *     · R = 2 ⇔ (a+b)(a+c) = 2bc；
 *     · R = 3 ⇔ (a+b)(a+c) = 3bc。
 *
 *   令 x = b/a ≥ 1、y = c/a ≥ 1，方程化为 1 + x + y = (R−1)xy，解出
 *
 *       y = (1+x) / ((R−1)x − 1)。
 *
 *   三角形条件：a ≤ b ≤ c ⟺ x ≤ y；a+b > c ⟺ 1+x > y。于是允许的 x 区间为
 *     · R = 2：y = (1+x)/(x−1)，要求 2 < x ≤ 1+√2（下界来自严格三角形不等式，
 *       上界来自 y ≥ x；x = 2 时 a+b = c 退化）；
 *     · R = 3：y = (1+x)/(2x−1)，要求 1 < x ≤ (1+√3)/2。
 *
 * 方法 A（主路径，有理数参数化）：
 *   把 x = p/q 写成最简分数（p > q，gcd(p,q) = 1），则 y = (p+q)/((R−1)p−q)；
 *   约分后 y = P/Q，其中 g = gcd(p+q,(R−1)p−q)、P = (p+q)/g、Q = ((R−1)p−q)/g。
 *   整数三角形 ⟺ b = a·p/q 与 c = a·P/Q 都是整数 ⟺ a 同时被 q 与 Q 整除，
 *   故最小 a₀ = lcm(q, Q)，族内 a = a₀·t（t = 1, 2, …）。周长
 *
 *       L = a₀·(1 + p/q + P/Q) = a₀ + (a₀/q)·p + (a₀/Q)·P
 *
 *   是整数，「周长 ≤ N」即 t ≤ N/L，该 (p,q) 贡献 ⌊N/L⌋ 个三角形。
 *   写 p = (R−1)q + m（R=2 时 p = 2q+m，R=3 时 p = q+m，m ≥ 1，gcd(m,q) = 1），
 *   可以解析地定出 g 与 d = gcd(q,Q)：
 *     · R = 2：g = gcd(3q+m, q+m) = gcd(2q, q+m) ∈ {1,2}，g = 2 ⟺ q+m 偶；
 *       d = 1，于是 a₀ = q(q+m)/g、L = (2q+m)(3q+m)/g；
 *     · R = 3：g = gcd(2q+m, q+2m) ∈ {1,3}，g = 3 ⟺ 3 | 2q+m；d = 2 ⟺ q 偶，故
 *       a₀ = q(q+2m)/(g·d)、L = 2(q+m)(2q+m)/(g·d)。
 *   条件 m²+2qm−q² ≤ 0（R=2）与 2m²+2qm−q² ≤ 0（R=3）都随 m 单调失效，可作循环终止。
 *   枚举上界：L ≥ q²（R=2）、L > 2q²/3（R=3），即 q ≤ √N 与 q ≤ √(3N/2)。
 *
 * 方法 B（独立参数化，另两条代码路径）：
 *   · R = 2 ⟺ uv = 2a²（u = b−a、v = c−a）。由 uv = 2a² 知 u、v 的 2 进赋值一奇一偶，
 *     按「谁含奇数次 2」分两种情形，各自唯一分解为 u = 2dr²、v = ds² 或 u = dr²、v = 2ds²
 *     （gcd(r,s) = 1，且 d 的 2 进赋值必为偶数——这正是唯一性条件）。
 *     约束 √2 r < s < 2r（或 s < r < √2 s），周长 = d(r+s)(s+2r)（或 d(r+s)(r+2s)）。
 *   · R = 3 ⟺ w² = u²+10uv+v²（w = 2a−u−v），且 a = (u+v+w)/2、周长 = (5(u+v)+3w)/2。
 *     这是二次型锥面，用基点 (0,1,1) 的有理直线 W = V + (m/n)U 参数化：对互素 m,n（0 < n < m < 5n）
 *     得 (U,V,W) = (2n(5n−m), m²−n², −m²+10mn−n²)，除以 G = gcd(U,V,W) 得本原解，
 *     再乘缩放 d 得全部三角形，贡献 ⌊2N/(5(u+v)+3w)⌋。G | 24（奇素因子只能是 3 且恰一次，
 *     2 进赋值 ≤ 3），故枚举 n 的界由 ⌊2N/D⌋ ≥ 1 保底：D ≥ 103.4n²/24。
 *
 * 旁证：
 *   · 方法 A 闭式与方法 A 参考实现（lcm + 求和定义式）在 q ≤ 2000 的每一对 (p,q) 上
 *     的 a₀、L 逐项相等；
 *   · 小规模（N = 100、300、1000、3000、10⁴、3×10⁴、10⁵、3×10⁵）与暴力枚举计数一致，
 *     前五档三角形集合逐一相等，且每个三角形的 R 值都回代验证 ∈ {2,3,4}；
 *   · 方法 A 与方法 B 两条结构不同的路径在完整规模上给出同一答案；
 *   · 公开答案表（lucky-bai/projecteuler-solutions）对照一致。
 *
 * 答案：139012411
 * 复杂度：方法 A ≈ 4.8×10⁷ 次 gcd 级运算，实测约 1.5 s；方法 B ≈ 4.4×10⁷ 次，约 4 s。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val FULL_LIMIT = 100_000_000L
private const val EXPECTED = 139_012_411L

// ------------------------------------------------------------------ 基础工具

/** 非负 gcd（欧几里得算法）。 */
private fun gcd(a: Long, b: Long): Long {
    var x = if (a < 0) -a else a
    var y = if (b < 0) -b else b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** ⌊√n⌋（整数精确）。 */
private fun isqrt(n: Long): Long {
    require(n >= 0) { "isqrt 参数非负" }
    var x = Math.sqrt(n.toDouble()).toLong()
    while (x > 0L && x * x > n) x--
    while ((x + 1) * (x + 1) <= n) x++
    return x
}

// ------------------------------------------------------------------ 方法 A：有理数参数化 (p,q)

/** p 的偏移：R=2 时 p = 2q+m，R=3 时 p = q+m。 */
private fun pBase(r: Int, q: Long): Long = if (r == 2) 2 * q else q

/** m 的允许上界判据（随 m 单调）：R=2 用 m²+2qm−q² ≤ 0，R=3 用 2m²+2qm−q² ≤ 0。 */
private fun mOk(r: Int, m: Long, q: Long): Boolean =
    if (r == 2) m * m + 2 * m * q - q * q <= 0L else 2 * m * m + 2 * m * q - q * q <= 0L

/**
 * 方法 A 参考实现：对一般 (p,q) 先用 lcm 求出最小 a₀，再直接求和得到周长 L。
 * 返回 (a₀, L)。用于验证下面的闭式。
 */
private fun pairRef(r: Int, p: Long, q: Long): Pair<Long, Long> {
    val u = (r - 1) * p - q
    val g = gcd(p + q, u)
    val pp = (p + q) / g
    val qq = u / g
    val d = gcd(q, qq)
    val a0 = q / d * qq
    val len = a0 + (a0 / q) * p + (a0 / qq) * pp
    return a0 to len
}

/** 方法 A 闭式（主路径）：由 (r, q, m) 直接给出 (a₀, L)。 */
private fun pairFast(r: Int, q: Long, m: Long): Pair<Long, Long> =
    if (r == 2) {
        val g = if (((q + m) and 1L) == 0L) 2L else 1L          // g = gcd(2q, q+m) ∈ {1,2}
        val a0 = q * (q + m) / g
        val len = (2 * q + m) * (3 * q + m) / g
        a0 to len
    } else {
        val g = if ((2 * q + m) % 3L == 0L) 3L else 1L            // g = gcd(2q+m, q+2m) ∈ {1,3}
        val d = if ((q and 1L) == 0L) 2L else 1L                  // d = gcd(q, (q+2m)/g) ∈ {1,2}
        val a0 = q * (q + 2 * m) / (g * d)
        val len = 2 * (q + m) * (2 * q + m) / (g * d)
        a0 to len
    }

/**
 * 方法 A 主路径：对 R = 2、3 枚举最简分数 x = p/q（p = (R−1)q + m，gcd(m,q) = 1）。
 * 返回计数（不含 R = 4 的等边三角形）。
 */
private fun countRationalR(limit: Long, r: Int): Long {
    var total = 0L
    val qMax = if (r == 2) isqrt(limit) + 1 else isqrt(limit * 3 / 2) + 1
    var q = 1L
    while (q <= qMax) {
        var m = 1L
        while (mOk(r, m, q)) {
            if (gcd(m, q) == 1L) {
                val (_, len) = pairFast(r, q, m)
                if (len <= limit) total += limit / len
            }
            m++
        }
        q++
    }
    return total
}

/** 全计数（方法 A）：R = 4 的等边三角形共有 ⌊N/3⌋ 个（边长 a = 1..⌊N/3⌋）。 */
private fun countRational(limit: Long): Long =
    limit / 3 + countRationalR(limit, 2) + countRationalR(limit, 3)

/** 小规模三角形排序用的比较器。 */
private val triOrder = compareBy<Triple<Long, Long, Long>>({ it.first }, { it.second }, { it.third })

/** 方法 A 在小规模下把三角形逐个列出（用于与暴力法比集合）。 */
private fun collectRational(limit: Long): List<Triple<Long, Long, Long>> {
    val out = ArrayList<Triple<Long, Long, Long>>()
    var e = 1L
    while (3 * e <= limit) {                              // R = 4：全部等边三角形
        out.add(Triple(e, e, e))
        e++
    }
    for (r in intArrayOf(2, 3)) {
        val qMax = if (r == 2) isqrt(limit) + 1 else isqrt(limit * 3 / 2) + 1
        var q = 1L
        while (q <= qMax) {
            var m = 1L
            while (mOk(r, m, q)) {
                if (gcd(m, q) == 1L) {
                    val p = pBase(r, q) + m
                    val (a0, len) = pairFast(r, q, m)
                    if (len <= limit) {
                        var t = 1L
                        while (t * len <= limit) {
                            out.add(Triple(a0 * t, (a0 / q) * p * t, cSide(r, q, m, a0, t)))
                            t++
                        }
                    }
                }
                m++
            }
            q++
        }
    }
    out.sortWith(triOrder)
    return out
}

/** 方法 A 族内第 t 个三角形的第三边 c = a·y = a₀t·(p+q)/((R−1)p−q) 的化简写法。 */
private fun cSide(r: Int, q: Long, m: Long, a0: Long, t: Long): Long =
    if (r == 2) a0 * t * (3 * q + m) / (q + m)
    else a0 * t * (2 * q + m) / (q + 2 * m)

// ------------------------------------------------------------------ 方法 B：uv 分解 + 二次型锥面

/** v₂(d) 为偶数的 d ≤ x 的个数（d 的 2 的幂部分是完全平方）。 */
private fun countEvenV2(x: Long): Long {
    var total = 0L
    var pw = 1L
    while (pw <= x) {
        total += x / pw - x / (2 * pw)
        pw *= 4
    }
    return total
}

/** 方法 B 的 R = 2 部分：uv = 2a² 的两种唯一分解。 */
private fun countDecompR2(limit: Long): Long {
    var total = 0L

    // 情形 A：u = 2dr²、v = ds²（r 侧含奇数次 2），√2 r < s < 2r，周长 = d(r+s)(s+2r)
    var r = 1L
    while (true) {
        val sMin = isqrt(2 * r * r) + 1
        if ((r + sMin) * (sMin + 2 * r) > limit) break
        var s = sMin
        while (s < 2 * r) {
            val per = (r + s) * (s + 2 * r)
            if (per > limit) break
            if (gcd(r, s) == 1L) total += countEvenV2(limit / per)
            s++
        }
        r++
    }

    // 情形 B：u = dr²、v = 2ds²（s 侧含奇数次 2），s < r < √2 s，周长 = d(r+s)(r+2s)
    var s = 1L
    while (true) {
        val rMin = s + 1
        if ((rMin + s) * (rMin + 2 * s) > limit) break
        val rMax = isqrt(2 * s * s)
        var rr = rMin
        while (rr <= rMax) {
            val per = (rr + s) * (rr + 2 * s)
            if (per > limit) break
            if (gcd(rr, s) == 1L) total += countEvenV2(limit / per)
            rr++
        }
        s++
    }

    return total
}

/** 方法 B 的 R = 3 部分：w² = u²+10uv+v² 的锥面参数化，贡献 ⌊2N/(5(u+v)+3w)⌋。 */
private fun countDecompR3(limit: Long): Long {
    var total = 0L
    val twoLimit = 2 * limit
    // D = 5(u+v)+3w = 2(m+3n)(m+7n)/G ≥ 103.4n²/24，D ≤ 2N ⇒ n ≤ 6812，留出余量取 6900。
    val nMax = 6900L
    var n = 1L
    while (n <= nMax) {
        // u ≤ v ⟺ m²+2mn−11n² ≥ 0 ⟺ m ≥ (√12−1)n
        var m = isqrt(12 * n * n) - n + 1
        if (m < n + 1) m = n + 1
        while (m < 5 * n) {
            if (gcd(m, n) == 1L) {
                val u0 = 2 * n * (5 * n - m)
                val v0 = m * m - n * n
                val w0 = -m * m + 10 * m * n - n * n
                val g = gcd(gcd(u0, v0), w0)
                check(g in 1L..24L) { "G = $g 超出 24（m=$m, n=$n）" }
                val u = u0 / g
                val v = v0 / g
                val w = w0 / g
                val den = 5 * (u + v) + 3 * w
                if (den <= twoLimit) total += twoLimit / den
            }
            m++
        }
        n++
    }
    return total
}

/** 全计数（方法 B）：等边三角形 ⌊N/3⌋ 个 + uv 分解（R=2）+ 锥面参数化（R=3）。 */
private fun countDecomp(limit: Long): Long =
    limit / 3 + countDecompR2(limit) + countDecompR3(limit)

// ------------------------------------------------------------------ 暴力对照（独立路径）

/**
 * 暴力法：枚举 a ≤ b ≤ c 中的 (a, b)，由比例条件反解 c：
 *   R = 2：c(b−a) = a(a+b)  → c = a(a+b)/(b−a)，要求 b > 2a（三角形不等式）且 b ≤ a(1+√2)（b ≤ c）；
 *   R = 3：c(2b−a) = a(a+b) → c = a(a+b)/(2b−a)，要求 a < b ≤ a(1+√3)/2。
 * 整除 + 三角形 + 周长三重检验后计数，另加 R = 4 的全部等边三角形。
 */
private fun bruteCount(limit: Long): Long {
    var total = limit / 3                                 // R = 4：全部等边三角形
    // R = 2：周长 = a(1+x+y) ≥ (3+2√2)a > 5a
    var a = 1L
    while (5 * a <= limit) {
        val bMax = a + isqrt(2 * a * a)
        var b = 2 * a + 1
        while (b <= bMax) {
            val num = a * (a + b)
            val den = b - a
            if (num % den == 0L) {
                val c = num / den
                if (c >= b && a + b > c && a + b + c <= limit) total++
            }
            b++
        }
        a++
    }
    // R = 3：周长 ≥ (2+√3)a > 3a
    a = 1L
    while (3 * a <= limit) {
        val bMax = (a + isqrt(3 * a * a)) / 2
        var b = a + 1
        while (b <= bMax) {
            val num = a * (a + b)
            val den = 2 * b - a
            if (num % den == 0L) {
                val c = num / den
                if (c >= b && a + b > c && a + b + c <= limit) total++
            }
            b++
        }
        a++
    }
    return total
}

/** 暴力法逐三角形列出（小规模用）。 */
private fun collectBrute(limit: Long): List<Triple<Long, Long, Long>> {
    val out = ArrayList<Triple<Long, Long, Long>>()
    var e = 1L
    while (3 * e <= limit) {
        out.add(Triple(e, e, e))
        e++
    }
    var a = 1L
    while (5 * a <= limit) {
        val bMax = a + isqrt(2 * a * a)
        var b = 2 * a + 1
        while (b <= bMax) {
            val num = a * (a + b)
            val den = b - a
            if (num % den == 0L) {
                val c = num / den
                if (c >= b && a + b > c && a + b + c <= limit) out.add(Triple(a, b, c))
            }
            b++
        }
        a++
    }
    a = 1L
    while (3 * a <= limit) {
        val bMax = (a + isqrt(3 * a * a)) / 2
        var b = a + 1
        while (b <= bMax) {
            val num = a * (a + b)
            val den = 2 * b - a
            if (num % den == 0L) {
                val c = num / den
                if (c >= b && a + b > c && a + b + c <= limit) out.add(Triple(a, b, c))
            }
            b++
        }
        a++
    }
    out.sortWith(triOrder)
    return out
}

// ------------------------------------------------------------------ main

private fun checkSmallScale(limit: Long, withSet: Boolean) {
    val m1 = countRational(limit)
    val m2 = countDecomp(limit)
    val bf = bruteCount(limit)
    check(m1 == m2 && m2 == bf) { "N=$limit：方法 A=$m1，方法 B=$m2，暴力=$bf" }
    var msg = "小规模对照 N = $limit：方法 A = 方法 B = 暴力 = $m1"
    if (withSet) {
        val s1 = collectRational(limit)
        val s2 = collectBrute(limit)
        check(s1 == s2) { "N=$limit：三角形集合不一致（${s1.size} vs ${s2.size}）" }
        for ((a, b, c) in s1) {                            // 回代验证 R 值
            val num = (a + b) * (a + c)
            val den = b * c
            check(num % den == 0L) { "N=$limit：($a,$b,$c) 的 R 非整数" }
            val rat = num / den
            check(rat == 2L || rat == 3L || rat == 4L) { "N=$limit：($a,$b,$c) 的 R = $rat" }
        }
        msg += "，三角形集合相等（${s1.size} 个，R 值全部回代验证）"
    }
    println(msg)
}

/** 方法 A 闭式与参考实现（lcm 定义式）逐对核对：a₀、L 必须逐项相等。 */
private fun checkPairFormulas(qMaxCheck: Long) {
    var checked = 0
    for (r in intArrayOf(2, 3)) {
        var q = 1L
        while (q <= qMaxCheck) {
            var m = 1L
            while (mOk(r, m, q)) {
                if (gcd(m, q) == 1L) {
                    val p = pBase(r, q) + m
                    val (a0f, lenF) = pairFast(r, q, m)
                    val (a0r, lenR) = pairRef(r, p, q)
                    check(a0f == a0r && lenF == lenR) {
                        "闭式不符：r=$r p=$p q=$q：闭式 ($a0f,$lenF) vs 参考 ($a0r,$lenR)"
                    }
                    checked++
                }
                m++
            }
            q++
        }
    }
    println("方法 A 闭式 vs lcm 参考实现：$checked 对 (p,q) 的 a₀ 与 L 逐项相等")
}

fun main() {
    // 0) 工具函数自检
    for (x in longArrayOf(0, 1, 2, 3, 4, 8, 9, 15, 16, 10_000_000_000L, 99_999_999_998L)) {
        val s = isqrt(x)
        check(s * s <= x && (s + 1) * (s + 1) > x) { "isqrt($x) = $s 错误" }
    }
    println("isqrt / gcd 自检通过")

    // 1) 闭式核对（q ≤ 2000 的全部 (p,q)，两侧方法各自独立计算）
    checkPairFormulas(2000L)

    // 2) 小规模三路对照（含逐三角形集合比较与 R 值回代）
    checkSmallScale(100L, true)
    checkSmallScale(300L, true)
    checkSmallScale(1000L, true)
    checkSmallScale(3000L, true)
    checkSmallScale(10_000L, true)
    checkSmallScale(30_000L, false)
    checkSmallScale(100_000L, false)
    checkSmallScale(300_000L, false)

    // 3) 完整规模：两条路径
    val fullA = countRational(FULL_LIMIT)
    val fullB = countDecomp(FULL_LIMIT)
    println("方法 A（有理参数化 (p,q)，闭式主路径）= $fullA")
    println("方法 B（uv 分解 + 锥面参数化）        = $fullB")
    check(fullA == fullB) { "全规模两方法不一致：$fullA vs $fullB" }
    check(fullA == EXPECTED) { "答案与预期不符：$fullA" }
    println("两方法一致，且与公开答案表一致")
    run {
        val eq = FULL_LIMIT / 3
        val c2 = countRationalR(FULL_LIMIT, 2)
        val c3 = countRationalR(FULL_LIMIT, 3)
        println("分项：R=4（等边）$eq + R=2 $c2 + R=3 $c3 = ${eq + c2 + c3}")
        check(eq + c2 + c3 == fullA)
    }

    // 4) 计时：JIT 预热后 3 轮取最优
    var bestA = Double.MAX_VALUE
    repeat(3) { k ->
        val t0 = System.nanoTime()
        check(countRational(FULL_LIMIT) == fullA)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestA) bestA = ms
        println("  方法 A 第 ${k + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 A：${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    var bestB = Double.MAX_VALUE
    repeat(3) { k ->
        val t0 = System.nanoTime()
        check(countDecomp(FULL_LIMIT) == fullB)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestB) bestB = ms
        println("  方法 B 第 ${k + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 B：${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")

    println("答案 = $fullA")
    println("check() 全部通过")
}
