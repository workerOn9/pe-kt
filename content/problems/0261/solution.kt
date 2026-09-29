#!/usr/bin/env kotlin
/**
 * Project Euler 261 — Pivotal Square Sums（关键平方和）
 *
 * 思路
 * ────
 * 记 S₂(x) = x(x+1)(2x+1)/6。条件「(k−m)² + … + k² 与 (n+1)² + … + (n+m)² 相等」即
 *   S₂(k) − S₂(k−m−1) = S₂(n+m) − S₂(n)
 * 用闭式展开并整理可得等价的
 *   (m+1)·k·(k−m) = m·n·(n+m+1).                                … (1)
 * 两边各自配方（左 = (m+1)((2k−m)² − m²)/4，右 = m((2n+m+1)² − (m+1)²)/4）：
 *   m·(2n+m+1)² − (m+1)·(2k−m)² = m(m+1).                        … (2)
 * 记 X = 2k−m、Y = 2n+m+1，由 (2) 推出 m | X²、(m+1) | Y²；写
 *   m = α·a²、m+1 = β·c²（α、β 为 m、m+1 的平方因子自由部分，互素），X = αab、Y = βcd，
 * 代入 (2) 后化为经典 Pell 型方程
 *   β·d² − α·b² = 1.                                             … (3)
 * 反过来，任取 (3) 的一组解 (d,b)，令
 *   k = α·a·(a+b)/2,   n = (β·c·d − m − 1)/2,
 * 都满足 (1)（等价变形可逆）。把 n ≥ k 与 k 的整数性代进去，再令 s = n−k ≥ 0，
 * 又可以把每个 pivot 唯一地写成
 *   k = m·(z+1) + w,   z = s+m ≥ m,   w² = m(m+1)·z(z+1).        … (4)
 * （由 (2) 对 v = Y+X 解二次方程，判别式为完全平方 ⟺ (4) 成立；取负号分支只会给出 k ≤ 0。）
 * (4) 还直接把「同一个 k 可能有多组 (m,n)」暴露成「不同 (m,z) 对给出同一个 k」——题目要求
 * **different/ distinct** 的 k，所以最后必须去重。
 *
 * 若 m(m+1) = α·t²、z(z+1) = α·u²（同一个平方自由部分 α），则 w = αtu。于是枚举方案是：
 *   · 对每个 m，α 与 t 由 m(m+1) 的素因数分解读出；
 *   · 固定 α，满足 z(z+1) = αu² 的全体 z 恰好是 Pell 方程 x² − αy² = 1 的全部解中 x 为奇数
 *     的那些（x = 2z+1、y = 2u），它们构成一条由基本解（连分数算法求得）生成的轨道；
 *   · 从每个 m 出发沿轨道前进（z ≥ m），由 (4) 算出 k，k > 10¹⁰ 即停（k 关于 z 单调增）。
 * 因为 k ≥ 2m(z+1) ≥ 2m(m+1)，所以只需 m ≤ 70710；每个 α 的轨道项数 ≤ 十几项。
 *
 * 方法 A（主路径）= 上述 (m,z)/Pell 轨道枚举 + 排序去重。
 * 方法 B（独立复核）= 完全不同的参数化：直接枚举 (3) 的解 (d,b)（以 m 的种子解 (c,a) 为起点、
 *   乘基本单位前进），用 k = αa(a+b)/2、n = (βcd−m−1)/2 逐个验证「整数性 + n ≥ k」后收集 k。
 *   两条路径的公式、变量与约束条件都不同，只在「同一个 Pell 基本解」处共享数学背景。
 * 直接暴力（brute-force 口径的小规模对照，见 main 内 brutePivots）= 按定义对每个 k 枚举 m，
 *   解关于 n 的二次方程并直接检查两个平方和相等；K=20000 以内与方法 A、B 逐值一致。
 *
 * 复杂度
 * ──────
 * 枚举 O(M) 个 m（M = 70710），每个 α 求一次连分数基本解（轨道项数 O(log k)，实测总步数
 * 约 2×10⁵），总时长毫秒级；内存 O(M)。暴力为对每个 k 枚举 m，O(K²) 次判断。
 *
 * 验证
 * ────
 *   · 题面锚点：4、21、24、110 都是 pivot，且直接按定义验证出题面给出的分解
 *     （3²+4²=5²；20²+21²=29²；21²+22²+23²+24²=25²+26²+27²；108²+109²+110²=133²+134²）；
 *   · K = 20000 的直接暴力与方法 A、方法 B 的集合逐值一致（含 12、40、60 … 等题面未列出的 pivot）；
 *   · 完整规模下方法 A 与方法 B 的 distinct 集合完全相同。
 *
 * 答案（本机实跑）：238890850232021（≤ 10¹⁰ 的 distinct square-pivot 共 72067 个）。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0261/solution.kt && java -cp … SolutionKt
 */

private const val KMAX = 10_000_000_000L

/** 整数平方根（Long，向下取整）。 */
private fun isqrt(n: Long): Long {
    var r = Math.sqrt(n.toDouble()).toLong()
    while (r > 0 && r * r > n) r--
    while ((r + 1) * (r + 1) <= n) r++
    return r
}

/** 最小素因子筛（用于把 m、m+1 快速分解）。 */
private fun spfSieve(limit: Int): IntArray {
    val spf = IntArray(limit + 1) { it }
    var p = 2
    while (p.toLong() * p <= limit) {
        if (spf[p] == p) {
            var q = p * p
            while (q <= limit) {
                if (spf[q] == q) spf[q] = p
                q += p
            }
        }
        p++
    }
    return spf
}

/** 把 n 写成 n = α·t²（α 平方因子自由）。返回 [α, t]。 */
private fun sqfreeWithT(n: Int, spf: IntArray): LongArray {
    var x = n
    var alpha = 1L
    var t = 1L
    while (x > 1) {
        val p = spf[x]
        var e = 0
        while (x % p == 0) { x /= p; e++ }
        if (e % 2 == 1) alpha *= p
        repeat(e / 2) { t *= p }
    }
    return longArrayOf(alpha, t)
}

/** 连分数求 x² − D·y² = 1 的基本解 (x1, y1)（D 非平方数）。返回 [x1, y1]。 */
private fun fundamentalSolution(D: Long): LongArray {
    val a0 = isqrt(D)
    var m = 0L
    var d = 1L
    var a = a0
    var h2 = 0L; var h1 = 1L
    var k2 = 1L; var k1 = 0L
    while (true) {
        val h = a * h1 + h2
        val k = a * k1 + k2
        h2 = h1; h1 = h
        k2 = k1; k1 = k
        if (h * h - D * k * k == 1L) return longArrayOf(h, k)
        m = d * a - m
        d = (D - m * m) / d
        a = (a0 + m) / d
    }
}

/** 排序 + 去重（原地）。 */
private fun sortedDistinct(a: LongArray): LongArray {
    a.sort()
    var w = 0
    for (i in a.indices) {
        if (i == 0 || a[i] != a[i - 1]) a[w++] = a[i]
    }
    return a.copyOf(w)
}

/** 所有 2m(m+1) ≤ kMax 的 m 的最大值。 */
private fun maxM(kMax: Long): Int {
    var m = 1
    while (2L * (m + 1) * (m + 2) <= kMax) m++
    return m
}

/**
 * 方法 A（主路径）：(m, z) 参数化。
 * 对每个 m 取出 α = sqfree(m(m+1))、t = sqrt(m(m+1)/α)；
 * 固定 α 用基本解 (x1,y1)（若 y1 为奇则取其平方，保证轨道元素都有 x 奇、y 偶的可用形式）
 * 生成全部 z(z+1) = αu² 的解，从 z = m（种子对，k = 2m(m+1)）起算到 k > kMax 为止。
 */
private fun solveA(kMax: Long): LongArray {
    val mMax = maxM(kMax)
    val spf = spfSieve(mMax + 1)
    val alphas = LongArray(mMax + 1)
    val ts = LongArray(mMax + 1)
    for (m in 1..mMax) {
        val f1 = sqfreeWithT(m, spf)
        val f2 = sqfreeWithT(m + 1, spf)
        alphas[m] = f1[0] * f2[0]
        ts[m] = f1[1] * f2[1]
    }
    // 把 (α, m) 打包成 key = α·2^17 + m 用原生 LongArray 排序，按 α 分组处理
    val shift = 17
    val keys = LongArray(mMax) { alphas[it + 1] * (1L shl shift) + (it + 1) }
    keys.sort()
    var ks = LongArray(1 shl 17)
    var kn = 0
    var i = 0
    while (i < keys.size) {
        val alpha = keys[i] shr shift
        var j = i + 1
        while (j < keys.size && keys[j] shr shift == alpha) j++
        val f = fundamentalSolution(alpha)
        val xh: Long
        val yh: Long
        if (f[1] % 2 == 0L) {
            xh = f[0]; yh = f[1]
        } else {
            xh = f[0] * f[0] + alpha * f[1] * f[1]
            yh = 2 * f[0] * f[1]
        }
        for (idx in i until j) {
            val m = (keys[idx] and ((1L shl shift) - 1)).toInt()
            val t = ts[m]
            if (kn == ks.size) ks = ks.copyOf(kn * 2)
            ks[kn++] = 2L * m * (m + 1)              // z = m：w = αt² = m(m+1)，k = 2m(m+1)
            var x = 2L * m + 1                        // 解 (x, y) = (2z+1, 2u)
            var y = 2L * t
            while (true) {
                val nx = x * xh + alpha * y * yh
                val ny = x * yh + y * xh
                x = nx; y = ny
                val z = (x - 1) / 2
                val u = y / 2
                val w = alpha * t * u
                val k = m * (z + 1) + w
                if (k > kMax) break
                if (kn == ks.size) ks = ks.copyOf(kn * 2)
                ks[kn++] = k
            }
        }
        i = j
    }
    return sortedDistinct(ks.copyOf(kn))
}

/**
 * 方法 B（独立复核）：(seed, generator) 参数化。
 * m = αa²、m+1 = βc²；枚举 Pell 方程 βd² − αb² = 1 的解 (d,b)
 * （起点为种子解 (c,a)，每次乘 x²−αβy²=1 的基本单位前进），
 * 检查 k = αa(a+b)/2 的整数性、n = (βcd−m−1)/2 ≥ k，收集 k。
 */
private fun solveB(kMax: Long): LongArray {
    val mMax = maxM(kMax)
    val spf = spfSieve(mMax + 1)
    val ks = ArrayList<Long>()
    for (m in 1..mMax) {
        val (alpha, a) = sqfreeWithT(m, spf).let { it[0] to it[1] }
        val (beta, c) = sqfreeWithT(m + 1, spf).let { it[0] to it[1] }
        val D = alpha * beta
        val f = fundamentalSolution(D)
        val (U, V) = f[0] to f[1]
        var B = beta * c                                // B + b√D = (βc + a√D)·(U+V√D)^j
        var b = a
        while (true) {
            val nB = B * U + D * b * V
            val nb = B * V + b * U
            B = nB; b = nb
            val num = alpha * a * (a + b)
            if (num / 2 > kMax) break
            if (num % 2 != 0L) continue                 // k 必须为整数
            val k = num / 2
            if (k > kMax) break
            check(B % beta == 0L) { "B 应被 β 整除：m=$m B=$B β=$beta" }
            val d = B / beta
            val yy = beta * c * d - m - 1
            if (yy % 2 != 0L) continue                  // n 必须为整数
            val n = yy / 2
            if (n >= k) ks.add(k)
        }
    }
    return sortedDistinct(ks.toLongArray())
}

/**
 * 直接暴力（定义口径，小规模）：对每个 k ≤ kMax 枚举 m < k，把 (1) 看成关于 n 的二次方程
 * n² + (m+1)n − (m+1)k(k−m)/m = 0，判别式为完全平方时得到整数 n；
 * 若 n ≥ k 再用逐项平方和直接验证等式（不依赖任何推导出的公式）。
 * 复杂度 O(K²)，只在 K ≤ 数万时可行。
 */
private fun brutePivots(kMax: Int): LongArray {
    val out = ArrayList<Long>()
    for (k in 1..kMax) {
        var m = 1
        while (m < k) {
            val v = (m + 1).toLong() * k * (k - m)
            if (v % m == 0L) {
                val q = v / m
                val disc = (m + 1).toLong() * (m + 1) + 4 * q
                val r = isqrt(disc)
                if (r * r == disc && (r - (m + 1)) % 2 == 0L) {
                    val n = (r - (m + 1)) / 2
                    if (n >= k) {
                        var left = 0L
                        for (x in (k - m)..k) left += x.toLong() * x
                        var right = 0L
                        for (x in (n + 1)..(n + m)) right += x * x
                        if (left == right) {
                            out.add(k.toLong())
                            break
                        }
                    }
                }
            }
            m++
        }
    }
    return out.toLongArray()
}

/** 找 k 的全部 (m, n) 分解（k 小的时候用），并按定义逐项验证平方和相等。 */
private fun decompositions(k: Long): List<Pair<Long, Long>> {
    val res = ArrayList<Pair<Long, Long>>()
    var m = 1L
    while (m < k) {
        val v = (m + 1) * k * (k - m)
        if (v % m == 0L) {
            val q = v / m
            val disc = (m + 1) * (m + 1) + 4 * q
            val r = isqrt(disc)
            if (r * r == disc && (r - (m + 1)) % 2 == 0L) {
                val n = (r - (m + 1)) / 2
                if (n >= k) {
                    var left = 0L
                    for (x in (k - m)..k) left += x * x
                    var right = 0L
                    for (x in (n + 1)..(n + m)) right += x * x
                    check(left == right) { "k=$k m=$m n=$n 平方和不相等" }
                    res.add(m to n)
                }
            }
        }
        m++
    }
    return res
}

/** JIT 预热后 3 轮取最优。 */
private fun bestOf3(tag: String, expected: LongArray, f: () -> LongArray): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out.contentEquals(expected)) { "$tag 第 ${round + 1} 轮结果漂移" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 题面锚点：4、21、24、110 是 pivot，且与题面给出的分解一致 ----------
    val anchors = listOf(4L to (1L to 4L), 21L to (1L to 28L), 24L to (3L to 24L), 110L to (2L to 132L))
    for ((k, mn) in anchors) {
        val ds = decompositions(k)
        check(mn in ds) { "k=$k 应有分解 (m,n)=$mn，实得 $ds" }
        println("k=$k：分解 $ds（直接按定义验证平方和相等）")
    }

    // ---------- 完整规模：方法 A 与方法 B ----------
    val fullA = solveA(KMAX)
    val fullB = solveB(KMAX)
    check(fullA.contentEquals(fullB)) {
        "方法 A 与方法 B 不一致：A=${fullA.size} 个，B=${fullB.size} 个"
    }
    var sum = 0L
    for (k in fullA) sum += k
    println("distinct square-pivots ≤ 10^10：${fullA.size} 个")
    println("答案 = $sum")

    // ---------- 小规模直接暴力对照（K = 20000） ----------
    val bruteK = 20_000
    val brute = brutePivots(bruteK)
    val subA = fullA.filter { it <= bruteK }.toLongArray()
    check(brute.contentEquals(subA)) {
        "暴力与方法 A 不一致：暴力 ${brute.size} 个，A ${subA.size} 个"
    }
    var bruteSum = 0L
    for (k in brute) bruteSum += k
    println("K=$bruteK：直接暴力 ${brute.size} 个 pivot，和方法 A 逐值一致（Σ = $bruteSum）")
    println("前 12 个 pivot：${fullA.take(12)}")

    // 更大规模的一次性暴力对照（K = 100000，O(K²) 已接近实用上限）
    val bruteK2 = 100_000
    val t2 = System.nanoTime()
    val brute2 = brutePivots(bruteK2)
    val ms2 = (System.nanoTime() - t2) / 1e6
    val subA2 = fullA.filter { it <= bruteK2 }.toLongArray()
    check(brute2.contentEquals(subA2)) {
        "K=$bruteK2 暴力与方法 A 不一致：暴力 ${brute2.size} 个，A ${subA2.size} 个"
    }
    println("K=$bruteK2：直接暴力 ${brute2.size} 个 pivot，和方法 A 逐值一致（${"%.1f".format(ms2)} ms）")

    // ---------- 计时 ----------
    repeat(2) { solveA(KMAX); solveB(KMAX) }
    val msA = bestOf3("方法 A 完整规模", fullA) { solveA(KMAX) }
    val msB = bestOf3("方法 B 完整规模", fullB) { solveB(KMAX) }
    brutePivots(2000)
    val msBrute = bestOf3("直接暴力 K=$bruteK", brute) { brutePivots(bruteK) }

    println()
    println("汇总：方法 A ${"%.1f".format(msA)} ms；方法 B ${"%.1f".format(msB)} ms；" +
        "直接暴力 K=$bruteK ${"%.1f".format(msBrute)} ms")
    println("check() 全部通过")
}
