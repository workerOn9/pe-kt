package dev.pekt.engine

/**
 * PE 296 — Angular Bisector and Tangent（角平分线与切线）：周长 ≤ 100000 且线段 BE 长为
 * 整数的整数边三角形个数 = 1137208419。
 *
 * 推导（详见 content/problems/0296/solution.kt 头部与 0296/analysis.md）：
 *   1. 切线引理：k 为 C 处角平分线、m 为 C 处外接圆切线、n 过 B 平行于 m，设 a = BC ≤ b = CA，
 *      则 |BE| = ac/(a+b)——取 C 为原点、k 为 x 轴，用外接圆方程求出 n ∩ k 即可。于是
 *      「BE 为整数」⟺ (a+b) | ac。
 *   2. 换元 S = a+b、r = a+b−c（周长盈余）：a ≤ b ≤ c < a+b ⟺ r ≤ a ≤ ⌊S/2⌋，
 *      且 ac = a(S−r) ≡ −ar (mod S)，故条件 ⟺ S | ar；周长 = 2S − r，于是
 *        Answer = Σ_S #{ r0(S) ≤ r ≤ a ≤ ⌊S/2⌋ : S | ar },  r0(S) = max(1, 2S−L)。
 *   3. 对称化降维：集合 X_S = {S | ar} 在方块 [r0, ⌊S/2⌋]² 上关于 (a,r) ↔ (r,a) 对称，故
 *      count_S = (|X_S| + diag_S)/2。|X_S| 按 d = gcd(r,S) 分组，组内 r = d·x 要求
 *      gcd(x, S/d) = 1，用只枚举无平方因子除数的莫比乌斯反演方波函数求；每行 a 的个数有闭式
 *      ⌊⌊S/2⌋·d/S⌋ − ⌊(r0−1)·d/S⌋。对角线 a = r 的条件 S | a² ⟺ σ(S) | a
 *      （σ(S) = Π p^⌈e/2⌉）给出 diag_S。
 *
 * 复杂度：Σ_{S ≤ 2L/3} Σ_{d|S} 2^ω(S/d) ≈ 3.4×10^6 次整数运算，外加 O(L) 筛表
 *   （NMAX = 2·10^5/3 + 1 = 66667，Σ 2^ω ≈ 5×10^5 个槽位）。JIT 预热后本机约 11 ms，
 *   远低于 10 s 熔断线；见 0296/meta.json（optimizedBaselineMs = 11.164）。
 *
 * 校验：content 的 floor-sum 对照路径在 L = 100000 上与本路径一致，定义级三重循环暴力在
 *   L ≤ 2000 上对拍一致（100→376、500→13445、1000→61339、2000→276390）；引擎只保留主路径。
 */
internal fun solve0296Impl(): Long {
    initTables296()
    return countBySymmetry296(LIMIT296)
}

private const val LIMIT296 = 100_000
private const val NMAX296 = 2 * LIMIT296 / 3 + 1          // 66667：S 的上界

// ───────────────────────── 筛表（首次调用时构建一次） ─────────────────────────

private var spf296 = IntArray(0)            // 最小质因子
private var radStart296 = IntArray(0)       // 无平方因子除数表：每个 m 的区间起点
private var radVal296 = IntArray(0)         // 无平方因子除数 e | m
private var radSign296 = IntArray(0)        // μ(e)

@Volatile private var tablesReady296 = false
private val tablesLock296 = Any()

/**
 * 构建最小质因子筛与无平方因子除数表（= 全部素因子子集的乘积，符号 = μ(e)）。
 * 幂等：双检锁保证并发首次调用也只构建一次（RunEngine 在 Dispatchers.Default 上执行）。
 */
private fun initTables296() {
    if (tablesReady296) return
    synchronized(tablesLock296) {
        if (tablesReady296) return

        // 最小质因子（埃氏）
        val spf = IntArray(NMAX296 + 1) { it }
        var i = 2
        while (i * i <= NMAX296) {
            if (spf[i] == i) {
                var j = i * i
                while (j <= NMAX296) { if (spf[j] == j) spf[j] = i; j += i }
            }
            i++
        }

        // 无平方因子除数表；先算总长度 Σ 2^ω(m)
        val twoOmega = IntArray(NMAX296 + 1) { 1 }
        for (p in 2..NMAX296) if (spf[p] == p) {
            var j = p
            while (j <= NMAX296) { twoOmega[j] *= 2; j += p }
        }
        var totalSlots = 0
        for (m in 1..NMAX296) totalSlots += twoOmega[m]
        val radStart = IntArray(NMAX296 + 2)
        val radVal = IntArray(totalSlots)
        val radSign = IntArray(totalSlots)
        var idx = 0
        val vals = IntArray(128)
        val signs = IntArray(128)
        for (m in 1..NMAX296) {
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
        radStart[NMAX296 + 1] = idx

        spf296 = spf
        radStart296 = radStart
        radVal296 = radVal
        radSign296 = radSign
        tablesReady296 = true
    }
}

/** #{x ∈ [lo, hi] : gcd(x, m) = 1}（莫比乌斯反演，只枚举无平方因子除数）。 */
private fun coprimeCount296(lo: Int, hi: Int, m: Int): Long {
    if (lo > hi) return 0L
    val start = radStart296
    val values = radVal296
    val signs = radSign296
    var s = 0L
    for (k in start[m] until start[m + 1]) {
        val e = values[k]
        val c = (hi / e - (lo - 1) / e).toLong()
        s += if (signs[k] > 0) c else -c
    }
    return s
}

/** 把 x 的全部正因子写进 out，返回个数（out 需 ≥ 512）。 */
private fun divisorsOf296(x: Int, out: IntArray): Int {
    var cnt = 1
    out[0] = 1
    var t = x
    while (t > 1) {
        val p = spf296[t]
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
private fun sigmaKernel296(x: Int): Int {
    var r = 1
    var t = x
    while (t > 1) {
        val p = spf296[t]
        var e = 0
        while (t % p == 0) { t /= p; e++ }
        repeat((e + 1) / 2) { r *= p }
    }
    return r
}

// ───────────────────── 主路径：对称化计数 ─────────────────────

/**
 * Answer(L) = Σ_S count_S，count_S = #{ r0 ≤ r ≤ a ≤ ⌊S/2⌋ : S | ar }，
 * r0 = max(1, 2S − L)。用 (a,r) 交换对称性 + 按 d = gcd(r,S) 分组求：
 * count_S = (|X_S| + diag_S) / 2。
 */
private fun countBySymmetry296(L: Int): Long {
    val s1 = (L + 1) / 2               // S ≤ s1 时 r0 = 1
    val s2 = 2 * L / 3                 // S > s2 时 r0 > ⌊S/2⌋，无解
    val divs = IntArray(512)
    var total = 0L
    for (S in 2..s2) {
        val r0 = if (S <= s1) 1 else 2 * S - L
        val half = S / 2
        if (r0 > half) continue
        val nd = divisorsOf296(S, divs)
        val sig = sigmaKernel296(S)
        var symmetric = 0L
        for (i in 0 until nd) {
            val d = divs[i]
            val lo = (r0 + d - 1) / d          // ⌈r0/d⌉
            val hi = half / d                  // ⌊⌊S/2⌋/d⌋
            if (lo > hi) continue
            // r ∈ [r0, ⌊S/2⌋] 且 gcd(r,S) = d ⟺ r = d·x, x ∈ [lo,hi], gcd(x, S/d) = 1
            val rows = coprimeCount296(lo, hi, S / d)
            // 每行 a 的个数 = ⌊⌊S/2⌋·d/S⌋ − ⌊(r0−1)·d/S⌋
            val perRow = (half.toLong() * d) / S - ((r0 - 1).toLong() * d) / S
            symmetric += perRow * rows
        }
        val diag = (half / sig - (r0 - 1) / sig).toLong()   // a = r 且 S | a²
        total += (symmetric + diag) / 2
    }
    return total
}
