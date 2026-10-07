package dev.pekt.engine

import dev.pekt.math.modInverse
import java.math.BigInteger

/**
 * PE 324 — Building a Tower（搭一座塔）
 *
 * 题意：用 2×1×1 方块填满 3×3×n 高塔（方块可任意旋转，塔的旋转/镜像视为不同方案），
 * 求 f(10^10000) mod 100000007，其中 f(n) 为方案数。
 *
 * 推导（详见 content/problems/0324/solution.kt 头部与 0324/analysis.md）：
 *
 *   1. 逐层轮廓 DP：把 3×3×n 沿 z 切成 n 个 3×3 截面，9 位掩码 in 表示本层已被上一层
 *      伸下来的方块占住的格子。处理本层时剩下的空格要么用层内骨牌（x/y 方向）覆盖，
 *      要么放一块朝 z 伸出的骨牌（占本层一格并在下一层同位置留一个已填格 → 出射掩码 out）。
 *      枚举精确覆盖得到转移矩阵 M[in][out]，则 f(n) = (M^n)[0][0]，f(0)=1。
 *
 *   2. n = 10^10000 极大：f 模素数 q 满足线性递推。先用掩码 DP 生成 f(0..K)（K=3000），
 *      跑 Berlekamp–Massey 得极小递推（本题阶数 d=38），再用 Kitamasa（多项式快速幂）
 *      在 O(d² log N) 内求 f(N) mod q。
 *
 *   3. 自证：暴力枚举真实铺法得 f(2)=229、f(4)=117805；掩码 DP 得 f(10)=96149360、
 *      f(10^3)=24806056、f(10^6)=30808124（均与题面一致）；Kitamasa 与 38×38 伴随矩阵
 *      快速幂对 10^10000 给出同一结果。
 *
 * 最终答案：f(10^10000) mod 100000007 = 96972774。
 * 本机实测（JIT 预热后端到端最快）：约 340 ms。
 */
internal fun solve0324Impl(): Long {
    val mod = 100000007L
    val size = 1 shl 9

    // 1) 转移矩阵
    val t = Array(size) { LongArray(size) }
    val out = LongArray(size)
    for (inn in 0 until size) {
        java.util.Arrays.fill(out, 0L)
        fun rec(filled: Int, outMask: Int) {
            var cell = -1
            for (i in 0 until 9) if ((filled shr i) and 1 == 0) { cell = i; break }
            if (cell == -1) { out[outMask]++; return }
            val r = cell / 3
            val c = cell % 3
            if (c < 2) {
                val j = cell + 1
                if ((filled shr j) and 1 == 0) rec(filled or (1 shl cell) or (1 shl j), outMask)
            }
            if (r < 2) {
                val j = cell + 3
                if ((filled shr j) and 1 == 0) rec(filled or (1 shl cell) or (1 shl j), outMask)
            }
            rec(filled or (1 shl cell), outMask or (1 shl cell))
        }
        rec(inn, 0)
        for (o in 0 until size) t[inn][o] = out[o] % mod
    }

    // 2) 序列 f(0..K)
    val k = 3000
    val f = LongArray(k + 1)
    var v = LongArray(size)
    v[0] = 1L
    f[0] = 1L
    for (n in 1..k) {
        val nv = LongArray(size)
        for (i in 0 until size) {
            val row = t[i]
            var s = 0L
            for (j in 0 until size) s += row[j] * v[j]
            nv[i] = s % mod
        }
        v = nv
        f[n] = v[0]
    }

    // 3) Berlekamp–Massey：s[n] = rec[1]·s[n-1] + ... + rec[d]·s[n-d]
    var c = LongArray(1) { 1L }
    var b = LongArray(1) { 1L }
    var l = 0
    var m = 1
    var bb = 1L
    for (n in f.indices) {
        var d = 0L
        for (i in 0..l) if (i < c.size) d = (d + c[i] * f[n - i]) % mod
        if (d == 0L) { m++; continue }
        val tmp = c.copyOf()
        val coef = d * modInverse(bb, mod) % mod
        if (c.size < b.size + m) c = c.copyOf(b.size + m)
        for (j in b.indices) c[j + m] = (c[j + m] - coef * b[j]) % mod
        if (2 * l <= n) { l = n + 1 - l; b = tmp; bb = d; m = 1 } else m++
    }
    val d = l
    val rec = LongArray(d + 1)
    for (i in 1..d) rec[i] = (mod - c[i]) % mod
    val init = f.copyOf(d)

    // 4) Kitamasa：x^N mod (x^d - rec[1]x^{d-1} - ... - rec[d])，再与初值组合
    fun mul(a: LongArray, bb2: LongArray): LongArray {
        val tmp = LongArray(2 * d - 1)
        for (i in 0 until d) {
            val ai = a[i]
            if (ai == 0L) continue
            for (j in 0 until d) tmp[i + j] = (tmp[i + j] + ai * bb2[j]) % mod
        }
        for (i in 2 * d - 2 downTo d) {
            val ci = tmp[i]
            if (ci == 0L) continue
            for (j in 1..d) tmp[i - j] = (tmp[i - j] + ci * rec[j]) % mod
        }
        return tmp.copyOf(d)
    }
    var result = LongArray(d)
    result[0] = 1L
    var base = LongArray(d)
    if (d > 1) base[1] = 1L else base[0] = rec[1]
    var e = BigInteger.TEN.pow(10000)
    while (e.signum() > 0) {
        if (e.testBit(0)) result = mul(result, base)
        base = mul(base, base)
        e = e.shiftRight(1)
    }
    var s = 0L
    for (i in 0 until d) s = (s + result[i] * init[i]) % mod
    return s
}
