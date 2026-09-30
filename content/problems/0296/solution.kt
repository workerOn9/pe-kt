#!/usr/bin/env kotlin
/**
 * Project Euler 296 — Angular Bisector and Tangent（角平分线与切线）
 *
 * 题目
 * ────
 * 整数边三角形 ABC（记 a = BC ≤ b = CA ≤ c = AB）。k 是 ∠ACB 的角平分线，m 是 C 处
 * 外接圆的切线，n 是过 B 且平行于 m 的直线，E = k ∩ n。
 * 求周长不超过 100000 且线段 BE 长为整数的三角形个数。
 *
 * 第 1 步：切线定出 BE = ac/(a+b)
 * ─────────────────────────────
 * 取 C 为原点、角平分线 k 为 x 轴，设 ∠ACB = 2θ，则
 *     A = (b cosθ, b sinθ),   B = (a cosθ, −a sinθ)。
 * 外接圆过 C，方程为 x² + y² + Dx + Ey = 0；代入 A、B 得
 *     D = −(a+b)/(2cosθ),   E = (a−b)/(2sinθ)。
 * C 处切线 m 就是 Dx + Ey = 0（圆心在 (−D/2, −E/2)，半径垂直于切线）；过 B 的平行线 n
 * 是 Dx + Ey = D·x_B + E·y_B = −a²。与 k: y = 0 联立得 x_E = −a²/D，于是
 *     |BE|² = (x_E − a cosθ)² + (a sinθ)² = a²c²/(a+b)²,
 * 其中 c² = (b−a)²cos²θ + (a+b)²sin²θ 用了 A、B 的坐标差。即
 *     BE = ac/(a+b)。
 * 程序里用坐标法直接解交点随机对拍（3000 个三角形，实测最大相对误差约 3×10^-16）。
 * 于是「BE 为整数」⟺ (a+b) | ac。
 *
 * 第 2 步：换成 (S, a, r) 参数
 * ──────────────────────────
 * 记 S = a + b，r = a + b − c（周长盈余）。三边序与三角形不等式
 *     a ≤ b ≤ c < a+b  ⟺  r ≤ a ≤ ⌊S/2⌋；
 * 又 ac = a(S − r) ≡ −ar (mod S)，故 (a+b) | ac ⟺ S | ar，而周长 = 2S − r。
 * 于是
 *     Answer = Σ_S #{ r0(S) ≤ r ≤ a ≤ ⌊S/2⌋ : S | ar },   r0(S) = max(1, 2S−L)。
 *
 * 第 3 步：对每个 S 用「交换 (a,r) 的对称性」把二维计数降为一维
 * ────────────────────────────────────────────────────
 * 在方块 [r0, ⌊S/2⌋]² 上，集合 X_S = {S | ar} 关于 (a,r) ↔ (r,a) 对称，所以
 *     count_S = (|X_S| + diag_S) / 2,
 * 其中 diag_S 是对角线 {a = r, S | a²} 的计数（S | a² ⟺ σ(S) | a，σ(S) = Π p^⌈e/2⌉）。
 * 剩下的 |X_S| = Σ_{r=r0}^{⌊S/2⌋} #{a ∈ [r0, ⌊S/2⌋] : S | ar} 每行只依赖 d = gcd(r,S)：
 *     #{a ∈ [r0, ⌊S/2⌋] : S | ar} = ⌊⌊S/2⌋·d/S⌋ − ⌊(r0−1)·d/S⌋,
 * 把 r 按 d | S 分组，组内是「x ≤ ⌊S/(2d)⌋ 且与 S/d 互素」的计数，用莫比乌斯反演的
 * 方波函数（只数无平方因子除数，O(2^ω)）求。r0 = 1 时退化为经典的
 *     T(S) = Σ_{r ≤ ⌊S/2⌋} ⌊gcd(r,S)/2⌋。
 *
 * 对照路径（与主路径并列的第二条独立实现，见 main 的「3. 两条独立路径」）
 * ──────────────────────────────────────────────────────────────
 * 不做对称化，直接对每个 a 数 r：把 a 按 d = gcd(a,S) 分组，组内 r 的个数是
 * 「Q = S/d 的倍数落在 [r0, a] 里」⇒ 区间求和转成 Σ_{x}⌊dx/(S/d)⌋ 的 floor-sum，
 * 用欧几里得式递归（ACL floor_sum）求。与主路径在「在哪里消除二维」上完全不同。
 *
 * 复杂度
 * ──────
 * 主路径：Σ_{S ≤ 2L/3} Σ_{d|S} 2^ω(S/d) ≈ 3.4×10^6 次整数运算（毫秒级），
 *        外加 Σ_{S ≤ 2L/3} τ(S) ≈ 7.5×10^5 个 (S,d) 组合的簿记；
 * 对照路径：同样的分块 + 约 3.4×10^6 个 (S,d,e) 组合各求一次 floor-sum（b = 0 时
 *        多数递归一两轮即退出）。
 * 两者都只用 O(L) 的筛表。
 *
 * 运行
 * ────
 * cd /Users/samuel/Documents/github/pe-kt
 * OUTDIR=/tmp/kc-0296 bash scripts/kotlinc-shim.sh content/problems/0296/solution.kt
 * java -cp /tmp/kc-0296:<kotlin-stdlib> SolutionKt
 */

private const val LIMIT = 100_000
private const val NMAX = 2 * LIMIT / 3 + 1          // 66667：S 的上界

// ───────────────────────── 筛表 ─────────────────────────

private val spf = IntArray(NMAX + 1) { it }          // 最小质因子
private val radStart = IntArray(NMAX + 2)            // 无平方因子除数表：每 m 的区间
private var radVal = IntArray(0)                     // 无平方因子除数 e | m
private var radSign = IntArray(0)                    // μ(e)

private fun initTables() {
    // 最小质因子（埃氏）
    var i = 2
    while (i * i <= NMAX) {
        if (spf[i] == i) {
            var j = i * i
            while (j <= NMAX) { if (spf[j] == j) spf[j] = i; j += i }
        }
        i++
    }
    // 无平方因子除数表（= 全部素因子子集乘积，符号 = μ(e)）；先算总长度 Σ 2^ω(m)
    val twoOmega = IntArray(NMAX + 1) { 1 }
    for (p in 2..NMAX) if (spf[p] == p) {
        var j = p
        while (j <= NMAX) { twoOmega[j] *= 2; j += p }
    }
    var totalSlots = 0
    for (m in 1..NMAX) totalSlots += twoOmega[m]
    radVal = IntArray(totalSlots)
    radSign = IntArray(totalSlots)
    var idx = 0
    val vals = IntArray(128)
    val signs = IntArray(128)
    for (m in 1..NMAX) {
        radStart[m] = idx
        var cnt = 1
        vals[0] = 1
        signs[0] = 1
        var t = m
        while (t > 1) {
            val p = spf[t]
            while (t % p == 0) t /= p
            for (k in 0 until cnt) {
                vals[cnt + k] = vals[k] * p
                signs[cnt + k] = -signs[k]
            }
            cnt *= 2
        }
        for (k in 0 until cnt) { radVal[idx] = vals[k]; radSign[idx] = signs[k]; idx++ }
    }
    radStart[NMAX + 1] = idx
}

/** #{x ∈ [lo, hi] : gcd(x, m) = 1}（莫比乌斯反演，只枚举无平方因子除数）。 */
private fun coprimeCount(lo: Int, hi: Int, m: Int): Long {
    if (lo > hi) return 0L
    var s = 0L
    for (k in radStart[m] until radStart[m + 1]) {
        val e = radVal[k]
        val c = (hi / e - (lo - 1) / e).toLong()
        s += if (radSign[k] > 0) c else -c
    }
    return s
}

/** 把 x 的全部正因子写进 out，返回个数（out 需 ≥ 512）。 */
private fun divisorsOf(x: Int, out: IntArray): Int {
    var cnt = 1
    out[0] = 1
    var t = x
    while (t > 1) {
        val p = spf[t]
        var e = 0
        while (t % p == 0) { t /= p; e++ }
        var pow = 1
        val base = cnt
        repeat(e) {
            pow *= p
            for (j in 0 until base) { out[cnt] = out[j] * pow; cnt++ }
        }
    }
    return cnt
}

/** Π p^⌈e/2⌉（S | a² ⟺ σ(S) | a）。 */
private fun sigmaKernel(x: Int): Int {
    var r = 1
    var t = x
    while (t > 1) {
        val p = spf[t]
        var e = 0
        while (t % p == 0) { t /= p; e++ }
        repeat((e + 1) / 2) { r *= p }
    }
    return r
}

// ───────────────────── 主路径：对称化计数 ─────────────────────

/**
 * Answer(L) = Σ_S count_S，count_S = #{ r0 ≤ r ≤ a ≤ ⌊S/2⌋ : S | ar },
 * r0 = max(1, 2S − L)。用 (a,r) 交换对称性 + 按 d = gcd(r,S) 分组求。
 * parts 非空时顺便把「S ≤ ⌊(L+1)/2⌋（r0 = 1）」与「S > ⌊(L+1)/2⌋（r0 = 2S−L）」
 * 两段分别累加进去。
 */
private fun countBySymmetry(L: Int, parts: LongArray? = null): Long {
    val s1 = (L + 1) / 2               // S ≤ s1 时 r0 = 1
    val s2 = 2 * L / 3                 // S > s2 时 r0 > ⌊S/2⌋，无解
    val divs = IntArray(512)
    var total = 0L
    for (S in 2..s2) {
        val r0 = if (S <= s1) 1 else 2 * S - L
        val half = S / 2
        if (r0 > half) continue
        val nd = divisorsOf(S, divs)
        val sig = sigmaKernel(S)
        var symmetric = 0L
        for (i in 0 until nd) {
            val d = divs[i]
            val lo = (r0 + d - 1) / d          // ⌈r0/d⌉
            val hi = half / d                  // ⌊⌊S/2⌋/d⌋
            if (lo > hi) continue
            // r ∈ [r0, ⌊S/2⌋] 且 gcd(r,S) = d ⟺ r = d·x, x ∈ [lo,hi], gcd(x, S/d) = 1
            val rows = coprimeCount(lo, hi, S / d)
            // 每行 a 的个数 = ⌊⌊S/2⌋·d/S⌋ − ⌊(r0−1)·d/S⌋
            val perRow = (half.toLong() * d) / S - ((r0 - 1).toLong() * d) / S
            symmetric += perRow * rows
        }
        val diag = (half / sig - (r0 - 1) / sig).toLong()   // a = r 且 S | a²
        val cS = (symmetric + diag) / 2
        total += cS
        parts?.let { if (S <= s1) it[0] += cS else it[1] += cS }
    }
    return total
}

// ────────────── 对照路径：(S, a) 分组 + floor-sum ──────────────

/** Σ_{i=0}^{n−1} ⌊(a·i + b)/m⌋（ACL 的 floor_sum）。 */
private fun floorSum(n0: Long, m0: Long, a0: Long, b0: Long): Long {
    var n = n0; var m = m0; var a = a0; var b = b0
    var ans = 0L
    while (true) {
        if (a >= m) { ans += (n - 1) * n / 2 * (a / m); a %= m }
        if (b >= m) { ans += n * (b / m); b %= m }
        val yMax = a * n + b
        if (yMax < m) break
        n = yMax / m
        b = yMax % m
        val t = m; m = a; a = t
    }
    return ans
}

/**
 * 同一答案的独立算法：对每个 a 数 r 的个数——
 *   #{r ∈ [r0, a] : (S/gcd(a,S)) | r}，按 d = gcd(a,S) 分组后，
 *   x 上和式 Σ⌊d·x/(S/d)⌋ 用 floor_sum 求。
 */
private fun countByFloorSums(L: Int): Long {
    val s1 = (L + 1) / 2
    val s2 = 2 * L / 3
    val divs = IntArray(512)
    var total = 0L
    for (S in 2..s2) {
        val r0 = if (S <= s1) 1 else 2 * S - L
        val half = S / 2
        if (r0 > half) continue
        val nd = divisorsOf(S, divs)
        var acc = 0L
        for (i in 0 until nd) {
            val d = divs[i]
            val m = S / d                     // Q = S/d = m
            val c0 = (r0 - 1) / m             // 每行被扣掉的整倍数个数
            val xhi = half / d
            // ⌊d·x/m⌋ ≥ c0 ⟺ x ≥ c0·m/d
            val xlo = maxOf(1L, (c0.toLong() * m + d - 1) / d).toInt()
            if (xlo > xhi) continue
            for (k in radStart[m] until radStart[m + 1]) {
                val e = radVal[k]
                val lo2 = (xlo + e - 1) / e
                val hi2 = xhi / e
                if (lo2 > hi2) continue
                // Σ_{x=lo2}^{hi2} ⌊d·e·x/m⌋ = F(hi2) − F(lo2−1), F(n)=Σ_{x=1}^{n}
                val sHi = floorSum(hi2 + 1L, m.toLong(), (d * e).toLong(), 0L)
                val sLo = floorSum(lo2.toLong(), m.toLong(), (d * e).toLong(), 0L)
                val contrib = sHi - sLo - c0.toLong() * (hi2 - lo2 + 1)
                acc += if (radSign[k] > 0) contrib else -contrib
            }
        }
        total += acc                 // 这里已经是 (r ≤ a) 的直接计数，不需要再折半
    }
    return total
}

// ───────────────────────── 验证工具 ─────────────────────────

/** 坐标法直接求交点 E，与 ac/(a+b) 比较，返回最大相对误差。 */
private fun lemmaMaxRelativeError(trials: Int): Double {
    val rnd = java.util.Random(296L)
    var worst = 0.0
    var done = 0
    while (done < trials) {
        val a = 1 + rnd.nextInt(400)
        val b = a + rnd.nextInt(600)
        val c = b + rnd.nextInt(a)             // c ∈ [b, a+b−1]
        // c² = (b−a)² + 4ab·sin²θ  ⟹  sin²θ = (c²−(b−a)²)/(4ab)
        val s2 = (c.toDouble() * c - (b - a).toDouble() * (b - a)) / (4.0 * a * b)
        if (s2 < 0.0 || s2 > 1.0) continue
        val st = Math.sqrt(s2)
        val ct = Math.sqrt(1.0 - s2)
        val dCoef = -(a + b) / (2.0 * ct)      // 外接圆方程 x²+y²+Dx+Ey=0 的 D
        val xE = -a.toDouble() * a / dCoef     // E = n ∩ k（k 为 x 轴）
        val xB = a * ct
        val yB = -a * st
        val beCoord = Math.hypot(xE - xB, -yB)
        val beFormula = a.toDouble() * c / (a + b)
        val err = Math.abs(beCoord - beFormula) / beFormula
        if (err > worst) worst = err
        done++
    }
    return worst
}

/** 定义级暴力：a ≤ b ≤ c、c < a+b、周长 ≤ L、(a+b) | ac。 */
private fun naiveCount(L: Int): Long {
    var cnt = 0L
    for (a in 1..L) {
        var b = a
        while (a + 2 * b <= L) {
            val S = a + b
            val cMax = minOf(S - 1, L - S)
            for (c in b..cMax) if ((a * c) % S == 0) cnt++
            b++
        }
    }
    return cnt
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    check(f() == expected) { "$tag 热身轮结果漂移" }     // 先热身一轮（C2 编译），不计时
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（热身 1 轮后取 3 轮最优）")
    return best
}

// ───────────────────────────── main ─────────────────────────────

fun main() {
    initTables()

    println("== 1. 几何引理数值验证：坐标法解交点 vs BE = ac/(a+b) ==")
    val err = lemmaMaxRelativeError(3000)
    check(err < 1e-9) { "引理对拍失败：最大相对误差 $err" }
    println("3000 个随机三角形（a ≤ b ≤ c < a+b）最大相对误差 = ${"%.3e".format(err)} ✓")

    println()
    println("== 2. 小规模定义级对拍（三重循环 (a,b,c) 直接判定 vs 公式法）==")
    for ((L, know) in listOf(100 to 376L, 500 to 13445L, 1000 to 61339L, 2000 to 276390L)) {
        val naive = naiveCount(L)
        val fast = countBySymmetry(L)
        check(naive == fast && fast == know) { "L=$L：暴力 $naive，公式 $fast，预期 $know" }
        println("L = %6d：三重循环 = 公式法 = %d ✓".format(L, naive))
    }

    println()
    println("== 3. 全尺寸：两条独立路径 ==")
    val parts = LongArray(2)
    val ansA = countBySymmetry(LIMIT, parts)
    val ansB = countByFloorSums(LIMIT)
    check(ansA == ansB) { "两条路径不一致：A = $ansA，B = $ansB" }
    println("路径 A（对称化 + 按 gcd(r,S) 分组，闭式行计数）：$ansA")
    println("路径 B（按 gcd(a,S) 分组 + floor-sum 区间求和）：$ansB")
    val s1 = (LIMIT + 1) / 2
    println("分段关键量：S ≤ $s1（r0 = 1）部分 ${parts[0]} ＋ S > $s1（r0 = 2S−100000）部分 ${parts[1]} = $ansA")

    println()
    println("== 4. 计时 ==")
    val msA = bestOf3("主路径 A（对称化）", ansA) { countBySymmetry(LIMIT) }
    val msB = bestOf3("对照路径 B（floor-sum）", ansB) { countByFloorSums(LIMIT) }
    bestOf3("定义级暴力 L = 2000（对拍用）", 276390L) { naiveCount(2000) }

    println()
    println("== 5. 结果 ==")
    println("周长 ≤ 100000 且 BE 为整数的三角形个数 = $ansA")
    println("check() 全部通过；A ${"%.3f".format(msA)} ms，B ${"%.3f".format(msB)} ms")
}
