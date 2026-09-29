#!/usr/bin/env kotlin
/**
 * Project Euler 279 — Triangles with Integral Sides and an Integral Angle（整数边整数角三角形）
 *
 * 思路
 * ────
 * 设三角形三边 a, b, c 为整数，某个角为整数度 θ。该角的余弦由余弦定理给出
 * cos θ = (u² + v² − s²) / (2uv)（u, v 为夹边，s 为对边），是有理数；又 θ 为整数度使
 * θ/180 为有理数，Niven 定理（cos 在有理角处的有理值只有 0, ±1/2, ±1）给出
 * cos θ ∈ {0, ±1/2, ±1}。cos θ = ±1 只能出现在退化三角形（θ = 0°, 180°），
 * 于是 θ ∈ {60°, 90°, 120°}，分别对应
 *      s² = u² + v² − uv,    s² = u² + v²,    s² = u² + v² + uv.
 * 三类整数边三角形两两不重合：90° + 60° 的三角形必为 30-60-90，三边比 1 : √3 : 2
 * 无整数实现；120° + 60° = 180° 无第三角；90° + 120° > 180°。等边三角形三个 60°，
 * 但它作为一个三角形只算一次。因此答案 = |90° 类| + |60° 类| + |120° 类|，无需容斥扣除。
 *
 * 每类先给出「本原三角形」的双射参数化，再对缩放 k ≥ 1 计数：
 * 周长为 P 的本原三角形贡献 ⌊N/P⌋ 个（k = 1 … ⌊N/P⌋）。
 *
 *   90°（勾股参数化）：(m, n)，m > n ≥ 1，gcd = 1，m + n 奇；
 *        三边 (m²−n², 2mn, m²+n²)，P = 2m(m+n)。
 *   60°（艾森斯坦型）：(m, n)，m ≥ 2，⌈m/2⌉ ≤ n < m，gcd = 1；记 g = 3（若 3 | m+n）否则 1，
 *        本原三边 = ((m²−n²)/g, (2mn−n²)/g, (m²−mn+n²)/g)，P = (2m²+mn−n²)/g。
 *        条件 m ≤ 2n（即 n ≥ ⌈m/2⌉）挑掉 X = m²−n² 与 Y = 2mn−n² 互换的重复表示
 *        （(m,n) ↦ (m, m−n) 只是交换 X, Y）；3 | m+n 时三边同时被 3 整除，恰好对应
 *        「约掉公因子 3」的一支。两支之并为全部本原 60° 三角形，且互不相交。
 *   120°：本原三边 = 排序后的 (m²−n², 2mn+n²) 与对边 m²+mn+n²；(m, n)，m > n ≥ 1，
 *        gcd = 1，3 ∤ (m−n)（3 | m−n 时三边被 3 整除，只需保留除过的那支），
 *        P = 2m² + 3mn + n²。
 *
 * 答案 = Σ_{90°} ⌊N/P⌋ + Σ_{60°} ⌊N/P⌋ + Σ_{120°} ⌊N/P⌋，N = 10^8。
 *
 * 复杂度
 * ──────
 * 每类的 (m, n) 枚举上界 m ≈ √N（P 是 m 的二次型），枚举对数 ≈ N/2 量级；N = 10^8 时三类
 * 合计约 6×10^7 对（其中约一半通过奇偶/gcd 剪枝），时间 O(N)、额外空间 O(1)。
 * 对照：定义级暴力枚举全部整数边三角形是 Θ(N³)，N = 2000 已需 ~1.7×10^8 次内层判断，
 * N = 10^8 约 10^24 次，完全不可行——暴力只用于把参数化钉到 N ≈ 2000 的规模。
 *
 * 验证
 * ────
 * 1. 题面只有一句提问、没有示例值，锚点用定义级暴力自建：直接枚举 a ≤ b ≤ c 的整数边三角形，
 *    按精确整数等式（s² = u²+v²、u²+v²±uv）判定 60°/90°/120°；对 N = 3…60、100、200、
 *    500、1000、2000 逐值检查「暴力计数 = 方法 A = 方法 B」，并检查三类计数之和等于总数
 *    （即三类的两两不重合在校验规模上成立，无重复计数）；
 * 2. 方法 B（独立域，完全不同的枚举与判定）：90° 换成 (u, v) = (m+n, m−n)（均为奇数、互素），
 *    P = u(u+v)；60°/120° 换成对偶域 n ≤ m/2，且把「三边全能被 3 整除」作为是否除 3 的判据
 *    （与主路径「3 | m+n」的分类实现不同），120° 用 b ≤ c 的镜像剔除。方法 A、B 在
 *    N = 10^3…10^8 的多个规模上一致；
 * 3. 公开答案表（nayuki/luckytoilet 的 projecteuler-solutions、projecteuler.weebly）列出
 *    416577688，与本机实跑一致（仅作旁证；meta.answer 取本机输出）。
 *
 * 答案：416577688（实跑输出：90° 类 113236940 + 60° 类 198759185 + 120° 类 104581563）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0279/solution.kt -d /tmp/kc-0279
 * java -cp /tmp/kc-0279:/Users/samuel/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/2.1.21/97a0975aa19d925e109537af60eb46902920015c/kotlin-stdlib-2.1.21.jar SolutionKt
 */

private const val LIMIT = 100_000_000L
private const val ANSWER = 416_577_688L

/** 参数化枚举的 m 上界：2m² ≤ 3·10^8 给出 m ≤ 12247。 */
private const val MAX_M = 12_248

private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** 最小质因子筛（方法 A 用互素标记代替逐对 gcd，先筛出 m 的质因子）。 */
private fun smallestPrimeFactor(limit: Int): IntArray {
    val spf = IntArray(limit + 1)
    for (i in 2..limit) {
        if (spf[i] == 0) {
            var j = i
            while (j <= limit) {
                if (spf[j] == 0) spf[j] = i
                j += i
            }
        }
    }
    return spf
}

private val SPF: IntArray by lazy { smallestPrimeFactor(MAX_M) }

/**
 * 把 [lo, hi] 中与 m 不互素的数在 mark 数组里打上当前 stamp（等价于 gcd(m, n) > 1）：
 * 对 m 的每个不同质因子 p 标出 p 的所有倍数。此后 mark[n] == stamp 即「不互素」。
 */
private fun markNonCoprime(m: Int, lo: Int, hi: Int, mark: IntArray, stamp: Int) {
    var x = m
    while (x > 1) {
        val p = SPF[x]
        while (x % p == 0) x /= p
        var k = ((lo + p - 1) / p) * p
        while (k <= hi) {
            mark[k] = stamp
            k += p
        }
    }
}

// ─────────────────────────── 方法 A：主路径 ───────────────────────────
// 90°：m > n ≥ 1，gcd = 1，m + n 奇（即 m − n 奇），P = 2m(m+n)
private fun count90(N: Long): Long {
    val mark = IntArray(MAX_M + 1)
    var stamp = 0
    var total = 0L
    var m = 2
    while (2L * m * (m + 1) <= N) {
        stamp++
        markNonCoprime(m, 1, m - 1, mark, stamp)
        var n = if (m % 2 == 0) 1 else 2      // m + n 必须为奇数
        while (n < m) {
            val p = 2L * m * (m + n)
            if (p > N) break                 // p 关于 n 递增
            if (mark[n] != stamp) total += N / p
            n += 2
        }
        m++
    }
    return total
}

// 60°：m ≥ 2，⌈m/2⌉ ≤ n < m，gcd = 1；3 | m+n 时约掉公因子 3，P = (2m²+mn−n²)/g
private fun count60(N: Long): Long {
    val mark = IntArray(MAX_M + 1)
    var stamp = 0
    var total = 0L
    var m = 2
    while (2L * m * m <= 3 * N) {
        stamp++
        val lo = (m + 1) / 2
        markNonCoprime(m, lo, m - 1, mark, stamp)
        var n = lo
        while (n < m) {
            if (mark[n] != stamp) {
                var p = 2L * m * m + m.toLong() * n - n.toLong() * n
                if ((m + n) % 3 == 0) p /= 3
                if (p <= N) total += N / p
            }
            n++
        }
        m++
    }
    return total
}

// 120°：m > n ≥ 1，gcd = 1，3 ∤ (m−n)，P = 2m² + 3mn + n²
private fun count120(N: Long): Long {
    val mark = IntArray(MAX_M + 1)
    var stamp = 0
    var total = 0L
    var m = 2
    while (2L * m * m < N) {
        stamp++
        markNonCoprime(m, 1, m - 1, mark, stamp)
        var n = 1
        while (n < m) {
            val p = 2L * m * m + 3L * m * n + n.toLong() * n
            if (p > N) break                 // p 关于 n 递增
            if ((m - n) % 3 != 0 && mark[n] != stamp) total += N / p
            n++
        }
        m++
    }
    return total
}

private fun methodA(N: Long): Long = count90(N) + count60(N) + count120(N)

// ─────────────────────── 方法 B：独立域（换参数/换判定） ───────────────────────
// 90°：用 u = m+n, v = m−n（u > v ≥ 1，同为奇数，gcd = 1），P = u(u+v)
private fun count90B(N: Long): Long {
    var total = 0L
    var u = 3L
    while (u * (u + 1) <= N) {
        var v = 1L
        while (v < u && u * (u + v) <= N) {
            if (gcd(u, v) == 1L) total += N / (u * (u + v))
            v += 2
        }
        u += 2
    }
    return total
}

// 60°：对偶域 n ≤ m/2（不用 m ≤ 2n），是否除 3 由「三边全被 3 整除」判定
private fun count60B(N: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * m <= 3 * N) {
        var n = 1L
        while (2 * n <= m) {
            if (gcd(m, n) == 1L) {
                val a = m * m - m * n + n * n
                val b = 2 * m * n - n * n
                val c = m * m - n * n
                val p = if (a % 3L == 0L && b % 3L == 0L && c % 3L == 0L) (a + b + c) / 3 else a + b + c
                if (p <= N) total += N / p
            }
            n++
        }
        m++
    }
    return total
}

// 120°：对偶域 n ≤ m/2 + 镜像条件 b ≤ c（与主路径的排序口径不同），除 3 判据同上
private fun count120B(N: Long): Long {
    var total = 0L
    var m = 2L
    while (2 * m * m <= 3 * N) {
        var n = 1L
        while (2 * n <= m) {
            if (gcd(m, n) == 1L) {
                val a = m * m + m * n + n * n
                val b = 2 * m * n + n * n
                val c = m * m - n * n
                if (b > c) break                 // b 关于 n 递增、c 递减，可跳出
                val p = if (a % 3L == 0L && b % 3L == 0L && c % 3L == 0L) (a + b + c) / 3 else a + b + c
                if (p <= N) total += N / p
            }
            n++
        }
        m++
    }
    return total
}

private fun methodB(N: Long): Long = count90B(N) + count60B(N) + count120B(N)

// ─────────────────────────── 定义级暴力（小规模） ───────────────────────────
/** 返回角度类型位掩码：1 = 90°，2 = 60°，4 = 120°，0 = 无（s 为对边）。 */
private fun angleType(s: Long, u: Long, v: Long): Int {
    val s2 = s * s
    val base = u * u + v * v
    val uv = u * v
    return when (s2) {
        base -> 1
        base - uv -> 2
        base + uv -> 4
        else -> 0
    }
}

private fun triangleTypes(a: Long, b: Long, c: Long): Int =
    angleType(c, a, b) or angleType(b, a, c) or angleType(a, b, c)

/** 直接枚举整数边三角形 a ≤ b ≤ c，按精确整数等式判角。返回 [90°, 60°, 120°, 去重总数]。 */
private fun bruteClassCounts(N: Long): LongArray {
    val r = LongArray(4)
    var a = 1L
    while (3 * a <= N) {
        var b = a
        while (a + 2 * b <= N) {
            var c = b
            while (a + b + c <= N) {
                if (a + b > c) {
                    val t = triangleTypes(a, b, c)
                    if (t != 0) {
                        r[3]++
                        if (t and 1 != 0) r[0]++
                        if (t and 2 != 0) r[1]++
                        if (t and 4 != 0) r[2]++
                    }
                }
                c++
            }
            b++
        }
        a++
    }
    return r
}

/** 周长 ≤ N 的合法三角形清单（题面无样例，用于展示前几个锚点）。 */
private fun smallTriangleList(N: Long): List<String> {
    val out = ArrayList<String>()
    var a = 1L
    while (3 * a <= N) {
        var b = a
        while (a + 2 * b <= N) {
            var c = b
            while (a + b + c <= N) {
                if (a + b > c) {
                    val t = triangleTypes(a, b, c)
                    if (t != 0) {
                        val tag = when (t) {
                            1 -> "90°"
                            2 -> "60°"
                            4 -> "120°"
                            else -> "多重角"
                        }
                        out.add("($a,$b,$c) $tag")
                    }
                }
                c++
            }
            b++
        }
        a++
    }
    return out
}

// ─────────────────────────── 计时 ───────────────────────────
private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 0. 题面无样例：打印小规模锚点清单 ----------
    println("周长 ≤ 24 的整数边三角形中至少有一个整数角的：")
    println("  " + smallTriangleList(24L).joinToString(" "))
    check(smallTriangleList(24L).size.toLong() == bruteClassCounts(24L)[3])

    // ---------- 1. 定义级暴力 vs 方法 A / 方法 B：小规模逐值 ----------
    val ns = (3L..60L) + listOf(100L, 200L, 500L, 1000L, 2000L)
    for (n in ns) {
        val r = bruteClassCounts(n)
        check(r[0] + r[1] + r[2] == r[3]) { "N=$n：三类计数之和 ≠ 总数，区间重叠（重复计数）" }
        val a = methodA(n)
        val b = methodB(n)
        check(r[3] == a && a == b) { "N=$n：暴力 ${r[3]} / 方法 A $a / 方法 B $b 不一致" }
        check(r[0] == count90(n) && r[1] == count60(n) && r[2] == count120(n)) {
            "N=$n：按类暴力 ${r[0]}/${r[1]}/${r[2]} 与主路径三项不一致"
        }
    }
    val r2000 = bruteClassCounts(2000L)
    println("定义级暴力 = 方法 A = 方法 B，在 N = 3…60、100、200、500、1000、2000 全部一致；")
    println("  N=2000：90° 类 ${r2000[0]}，60° 类 ${r2000[1]}，120° 类 ${r2000[2]}；" +
        "三类之和 ${r2000[0] + r2000[1] + r2000[2]} = 总数 ${r2000[3]}（三类互不重叠）")

    // ---------- 2. 方法 A / B 在中等规模互证 ----------
    for (n in longArrayOf(10_000L, 1_000_000L, 10_000_000L, 100_000_000L)) {
        val a = methodA(n)
        val b = methodB(n)
        check(a == b) { "N=$n：方法 A $a ≠ 方法 B $b" }
        println("N=$n：方法 A = 方法 B = $a")
    }

    // ---------- 3. 完整规模 ----------
    val n = LIMIT
    val c90 = count90(n)
    val c60 = count60(n)
    val c120 = count120(n)
    val total = c90 + c60 + c120
    check(total == ANSWER) { "完整规模结果 $total ≠ 预期 $ANSWER" }
    println("N=10^8：90° 类 $c90 + 60° 类 $c60 + 120° 类 $c120 = $total")

    // ---------- 4. 计时 ----------
    methodA(10_000_000L); methodB(10_000_000L); bruteClassCounts(1000L)
    val msA = bestOf3("方法 A（三段参数化和，N=10^8）", ANSWER) { methodA(n) }
    val msB = bestOf3("方法 B（对偶域，N=10^8）", ANSWER) { methodB(n) }
    val msBrute = bestOf3("定义级暴力（N=2000）", r2000[3]) { bruteClassCounts(2000L)[3] }

    println()
    println("答案 = $total")
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms；" +
        "定义级暴力 N=2000 ${"%.1f".format(msBrute)} ms")
    println("check() 全部通过")
}
