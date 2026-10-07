#!/usr/bin/env kotlin
/**
 * Project Euler 324 — Building a Tower（搭一座塔）
 *
 * 题目：用 2×1×1 方块填满 3×3×n 高塔，方块可任意旋转，塔的旋转/镜像视为不同方案。
 *       记方案数为 f(n)。已知 f(2)=229、f(4)=117805，
 *       f(10) mod q = 96149360，f(10^3) mod q = 24806056，f(10^6) mod q = 30808124，
 *       其中 q = 100000007。求 f(10^10000) mod q。
 *
 * 思路推导
 * --------
 * 逐层 DP：把高塔沿 z 方向切成 n 个 3×3 截面。用 9 位掩码表示「当前层已被上一层
 * 伸下来的方块占住的格子」（in）。处理当前层时，剩下的空格要么用层内（x/y 方向）
 * 的骨牌覆盖，要么放一块朝 z 方向伸出的骨牌——它占据本层该格，并在下一层同位置
 * 留下一个已填格，于是产生出射掩码 out。枚举「层内骨牌 + 伸出骨牌」对 3×3 的精确
 * 覆盖，得到转移矩阵
 *
 *   M[in][out] = 在入射掩码 in 下恰好盖满本层、出射掩码为 out 的方案数，
 *
 * 则 f(n) = (M^n)[0][0]，且 f(0)=1（空塔）。自检：奇数 n 体积 9n 为奇数不可铺 → f=0。
 *
 * n = 10^10000 极大，无法直接矩阵幂。f 模素数 q 满足线性递推：先用 f(0..K) 跑
 * Berlekamp–Massey 求出极小递推（本题阶数 d=38），再用 Kitamasa（多项式快速幂）
 * 算出 f(10^10000) mod q。另用 38×38 伴随矩阵快速幂作独立互证。
 *
 * 验证
 * ----
 * 1. 暴力枚举真实铺法：f(2)=229、f(4)=117805，与题面一致；
 * 2. 掩码 DP：f(10) mod q=96149360、f(10^3) mod q=24806056、f(10^6) mod q=30808124；
 * 3. 双方法互证：Kitamasa 与伴随矩阵快速幂对 N=10^10000 给出同一结果。
 *
 * 复杂度：建 M 为 O(枚举)，BM 为 O(K²)，Kitamasa 为 O(d² log N)≈10^8；
 *         伴随矩阵互证为 O(d³ log N)≈4×10^9（仅作交叉验证）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

import java.math.BigInteger

private const val MOD = 100000007L
private const val SIZE = 1 shl 9

/** 枚举一层 3×3 的所有精确覆盖，构造转移矩阵。 */
private fun buildTransfer(): Array<LongArray> {
    val t = Array(SIZE) { LongArray(SIZE) }
    val out = LongArray(SIZE)
    for (inn in 0 until SIZE) {
        java.util.Arrays.fill(out, 0L)
        fun rec(filled: Int, outMask: Int) {
            var cell = -1
            for (i in 0 until 9) if ((filled shr i) and 1 == 0) { cell = i; break }
            if (cell == -1) { out[outMask]++; return }
            val r = cell / 3
            val c = cell % 3
            if (c < 2) {                       // 层内 +x 骨牌
                val j = cell + 1
                if ((filled shr j) and 1 == 0) rec(filled or (1 shl cell) or (1 shl j), outMask)
            }
            if (r < 2) {                       // 层内 +y 骨牌
                val j = cell + 3
                if ((filled shr j) and 1 == 0) rec(filled or (1 shl cell) or (1 shl j), outMask)
            }
            rec(filled or (1 shl cell), outMask or (1 shl cell))   // 沿 +z 伸出
        }
        rec(inn, 0)
        for (o in 0 until SIZE) t[inn][o] = out[o] % MOD
    }
    return t
}

/** f(0..k) mod q：直接逐层矩阵-向量递推。 */
private fun sequence(t: Array<LongArray>, k: Int): LongArray {
    val f = LongArray(k + 1)
    var v = LongArray(SIZE); v[0] = 1L; f[0] = 1L
    for (n in 1..k) {
        val nv = LongArray(SIZE)
        for (i in 0 until SIZE) {
            val row = t[i]
            var s = 0L
            for (j in 0 until SIZE) s += row[j] * v[j]
            nv[i] = s % MOD
        }
        v = nv
        f[n] = v[0]
    }
    return f
}

/** 暴力枚举：直接数 3×3×n 盒子被 2×1×1 方块铺满的方案数。 */
private fun bruteCount(n: Int): Long {
    val grid = Array(n) { BooleanArray(9) }
    fun firstEmpty(): Int {
        for (z in 0 until n) for (c in 0 until 9) if (!grid[z][c]) return z * 9 + c
        return -1
    }
    fun rec(): Long {
        val p = firstEmpty()
        if (p < 0) return 1L
        val z = p / 9; val c = p % 9; val y = c / 3; val x = c % 3
        grid[z][c] = true
        var total = 0L
        if (x < 2 && !grid[z][c + 1]) { grid[z][c + 1] = true; total += rec(); grid[z][c + 1] = false }
        if (y < 2 && !grid[z][c + 3]) { grid[z][c + 3] = true; total += rec(); grid[z][c + 3] = false }
        if (z + 1 < n && !grid[z + 1][c]) { grid[z + 1][c] = true; total += rec(); grid[z + 1][c] = false }
        grid[z][c] = false
        return total
    }
    return rec()
}

private fun modpow(a: Long, e: Long): Long {
    var b = a % MOD; var x = e; var r = 1L
    while (x > 0) { if (x and 1L == 1L) r = r * b % MOD; b = b * b % MOD; x = x shr 1 }
    return r
}

/** Berlekamp–Massey：返回 C[0..L]，使 s[n] = -Σ_{i=1..L} C[i] s[n-i]（mod q）。 */
private fun berlekampMassey(s: LongArray): LongArray {
    var c = LongArray(1) { 1L }
    var b = LongArray(1) { 1L }
    var l = 0; var m = 1; var bb = 1L
    for (n in s.indices) {
        var d = 0L
        for (i in 0..l) if (i < c.size) d = (d + c[i] * s[n - i]) % MOD
        if (d == 0L) { m++; continue }
        val t = c.copyOf()
        val coef = d * modpow(bb, MOD - 2) % MOD
        if (c.size < b.size + m) c = c.copyOf(b.size + m)
        for (j in b.indices) c[j + m] = (c[j + m] - coef * b[j]) % MOD
        if (2 * l <= n) { l = n + 1 - l; b = t; bb = d; m = 1 } else m++
    }
    return c.copyOf(l + 1)
}

/** Kitamasa：由递推 rec[1..d] 与初值 init[0..d-1]，用多项式快速幂算 s[N]。 */
private fun kitamasa(rec: LongArray, init: LongArray, n: BigInteger): Long {
    val d = rec.size - 1
    fun mul(a: LongArray, b: LongArray): LongArray {
        val tmp = LongArray(2 * d - 1)
        for (i in 0 until d) {
            val ai = a[i]
            if (ai == 0L) continue
            for (j in 0 until d) tmp[i + j] = (tmp[i + j] + ai * b[j]) % MOD
        }
        for (i in 2 * d - 2 downTo d) {
            val ci = tmp[i]
            if (ci == 0L) continue
            for (j in 1..d) tmp[i - j] = (tmp[i - j] + ci * rec[j]) % MOD
        }
        return tmp.copyOf(d)
    }
    var result = LongArray(d); result[0] = 1L
    var base = LongArray(d)
    if (d > 1) base[1] = 1L else base[0] = rec[1]   // 多项式 x（模特征多项式）
    var e = n
    while (e.signum() > 0) {
        if (e.testBit(0)) result = mul(result, base)
        base = mul(base, base)
        e = e.shiftRight(1)
    }
    var s = 0L
    for (i in 0 until d) s = (s + result[i] * init[i]) % MOD
    return s
}

/** 伴随矩阵快速幂（独立互证）：v_N = C^N v_0，f(N) = (v_N)[0]。 */
private fun companionPow(rec: LongArray, init: LongArray, n: BigInteger): Long {
    val d = rec.size - 1
    fun matMul(a: Array<LongArray>, b: Array<LongArray>): Array<LongArray> {
        val r = Array(d) { LongArray(d) }
        for (i in 0 until d) {
            val ai = a[i]
            for (k in 0 until d) {
                val aik = ai[k]
                if (aik == 0L) continue
                val bk = b[k]
                val ri = r[i]
                for (j in 0 until d) ri[j] = (ri[j] + aik * bk[j]) % MOD
            }
        }
        return r
    }
    // 前向伴随矩阵 C：v_{n+1} = C v_n，v_n = [s[n], ..., s[n+d-1]]
    // 次对角线（超对角线）=1 平移，末行给出 s[n+d] = Σ rec[i] s[n+d-i]
    var base = Array(d) { LongArray(d) }
    for (i in 0 until d - 1) base[i][i + 1] = 1L
    for (j in 0 until d) base[d - 1][j] = rec[d - j]
    var result = Array(d) { LongArray(d) }
    for (i in 0 until d) result[i][i] = 1L
    var e = n
    while (e.signum() > 0) {
        if (e.testBit(0)) result = matMul(result, base)
        base = matMul(base, base)
        e = e.shiftRight(1)
    }
    var s = 0L
    for (j in 0 until d) s = (s + result[0][j] * init[j]) % MOD
    return s
}

private inline fun <T> timed(runs: Int = 3, body: () -> T): Pair<T, Double> {
    body()
    var best = Double.MAX_VALUE
    var r = body()
    for (k in 0 until runs) {
        val st = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return r to best
}

private const val K = 3000

/** 完整求解路径：建转移矩阵 → 生成序列 → BM → Kitamasa。 */
private fun solveFull(): Pair<Long, Int> {
    val t = buildTransfer()
    val f = sequence(t, K)
    val c = berlekampMassey(f)
    val d = c.size - 1
    val rec = LongArray(d + 1)
    for (i in 1..d) rec[i] = (MOD - c[i]) % MOD
    val init = f.copyOf(d)
    return kitamasa(rec, init, BigInteger.TEN.pow(10000)) to d
}

fun main() {
    println("== 1. 暴力枚举真实铺法（对照题面） ==")
    val (b2, _) = timed { bruteCount(2) }
    val (b4, b4ms) = timed(runs = 5) { bruteCount(4) }
    println("  f(2) = $b2 " + if (b2 == 229L) "-> 与题面一致" else "-> 不一致！")
    println("  f(4) = $b4 " + if (b4 == 117805L) "-> 与题面一致" else "-> 不一致！")

    println("== 2. 掩码 DP 序列（对照题面样例） ==")
    val t = buildTransfer()
    val f = sequence(t, K)
    println("  f(10) mod q      = " + f[10] + (if (f[10] == 96149360L) "  一致" else "  不一致！"))
    println("  f(10^3) mod q    = " + f[1000] + (if (f[1000] == 24806056L) "  一致" else "  不一致！"))

    println("== 3. Berlekamp–Massey 求极小递推 ==")
    val c = berlekampMassey(f)
    val d = c.size - 1
    val rec = LongArray(d + 1)
    for (i in 1..d) rec[i] = (MOD - c[i]) % MOD
    println("  递推阶数 d = $d")
    var ok = true
    for (n in d..K) {
        var s = 0L
        for (i in 1..d) s = (s + rec[i] * f[n - i]) % MOD
        if (s != f[n]) { ok = false; break }
    }
    println("  递推在 n=0..$K 上验证：" + (if (ok) "通过" else "失败！"))
    val init = f.copyOf(d)

    val nBig = BigInteger.TEN.pow(10000)
    println("== 4. 求解 f(10^10000) mod q ==")
    val (ansK, kitMs) = timed(runs = 5) { kitamasa(rec, init, nBig) }
    val (ansC, compMs) = timed(runs = 2) { companionPow(rec, init, nBig) }
    println("  Kitamasa          = $ansK")
    println("  伴随矩阵互证      = $ansC  " + (if (ansK == ansC) "-> 一致" else "-> 不一致！"))
    val f1e6 = kitamasa(rec, init, BigInteger.valueOf(1_000_000L))
    println("  f(10^6) mod q     = $f1e6 " + (if (f1e6 == 30808124L) "-> 与题面一致" else "-> 不一致！"))
    // 整条流水线（建矩阵→BM→Kitamasa，不含互证）的端到端耗时
    val (_, fullMs) = timed(runs = 3) { solveFull() }

    println("BRUTE_MS: " + String.format("%.2f", b4ms) + "  （暴力枚举真实铺法 n=4）")
    println("OPT_MS: " + String.format("%.2f", fullMs) + "  （端到端；单步 Kitamasa " +
        String.format("%.2f", kitMs) + " ms，互证 " + String.format("%.2f", compMs) + " ms）")
}
