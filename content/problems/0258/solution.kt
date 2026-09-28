#!/usr/bin/env kotlin
/**
 * Project Euler 258 — A Lagged Fibonacci Sequence（滞后斐波那契数列）
 *
 * 思路：
 *   g_k = g_{k−2000} + g_{k−1999}（k ≥ 2000），初值 g_0 = … = g_1999 = 1，是 2000 阶常系数
 *   线性递推。把指标平移成 g_{n+2000} = g_n + g_{n+1}，特征多项式为
 *
 *       f(x) = x^2000 − x − 1 ，  即  x^2000 ≡ x + 1 (mod f)。
 *
 *   Kitamasa：若 x^k ≡ Σ_{i=0}^{1999} c_i x^i (mod f)，则 g_k = Σ c_i g_i。本题初值全为 1，
 *   于是 g_k ≡ Σ c_i (mod 20092010)，问题化为求 x^k mod f 的系数向量。
 *
 *   方法 A（主路径：多项式快速幂）：
 *     系数向量长度 2000。乘法 = 长度 2000 的卷积，用惰性取模——系数 < 2^25，2000 项乘积
 *     之和 < 2^61，Long 累加安全；再把 t ≥ 2000 的项按 x^t → x^{t−1999} + x^{t−2000} 一次
 *     线性归约回 2000 维。平方利用交换律只算 i ≤ j 的半三角（2·a_i·a_j 一次算两项），
 *     乘法数减半。k = 10^18 的二进制长度为 60、popcount = 24，共 59 次平方 + 24 次乘法。
 *
 *   方法 B（独立复核：Bostan–Mori 生成函数折半）：
 *     生成函数 G(x) = Σ g_n x^n = P(x)/Q(x)，Q(x) = 1 − x^1999 − x^2000，
 *     P(x) = 1 + x + … + x^1998（由初值全 1 推出：G·Q = (1+…+x^1999) − x^1999）。
 *     Bostan–Mori 每步把指数折半：U = P·Q(−x)，n 偶取 U 的偶部、n 奇取奇部为新 P；
 *     Q ← Q(x)Q(−x) 的偶部。走到 n = 0 时 [x^n]P/Q = P(0)/Q(0) = P(0)（Q(0) 恒为 1）。
 *     实现上按奇偶拆成半长卷积，只算需要的那一半系数；Q ← E(y)² − y·O(y)²（Q = E(x²) + x·O(x²)）。
 *     全程只有加法与乘法，不需要除法，对合数模 20092010 完全合法；步数 = k 的二进制位数。
 *
 *   模数 20092010 = 2 × 5 × 2009201 是合数：两条路径都不做除法/求逆元。
 *
 * 旁证：
 *   · O(k) 直接迭代（长度 2000 的循环缓冲）在 k = 10^4 / 10^6 / 10^8 与两条快速路径逐一相等；
 *   · 两条结构不同的快速路径对 k = 10^18 给出同一答案；
 *   · 手算早期值 g_2000 = 2、g_3999 = 3、g_4000 = 4 与迭代一致；
 *   · 公开答案表（luckytoilet）对照：12747994。
 *
 * 答案：g_{10^18} mod 20092010 = 12747994
 * 复杂度：方法 A O(2000² log k)，约 59 次平方（半三角）+ 24 次乘法 ≈ 2.1×10^8 次乘加；
 *         方法 B 同量级（每步 3 个半长卷积 × 60 步 ≈ 1.8×10^8 次乘加）；直接迭代 O(k)。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val DIM = 2000
private const val MOD = 20092010L
private const val MODI = 20092010
private const val TARGET_K = 1_000_000_000_000_000_000L
private const val EXPECTED = 12_747_994L

// ------------------------------------------------------------ 方法 A：Kitamasa 多项式快速幂

/**
 * 归约：t ≥ 2000 的项用 x^t = x^{t−2000}·x^2000 ≡ x^{t−1999} + x^{t−2000} 折回 2000 维。
 * 即 res[j] += tmp[j + 1999]（j ≥ 1）与 tmp[j + 2000]（j ≤ 1998）。
 */
private fun polyReduce(tmp: LongArray): LongArray {
    val res = LongArray(DIM)
    for (j in 0 until DIM) {
        var v = tmp[j] % MOD
        if (j >= 1) v += tmp[j + DIM - 1] % MOD
        if (j <= DIM - 2) v += tmp[j + DIM] % MOD
        res[j] = v % MOD
    }
    return res
}

/** 长度 2000 的多项式乘法（惰性取模 + 一次归约）。 */
private fun polyMul(a: LongArray, b: LongArray): LongArray {
    val tmp = LongArray(2 * DIM - 1)
    for (i in 0 until DIM) {
        val ai = a[i]
        if (ai == 0L) continue
        var idx = i
        for (j in 0 until DIM) {
            tmp[idx] += ai * b[j]
            idx++
        }
    }
    return polyReduce(tmp)
}

/** 平方：只枚举 i ≤ j，tmp[i+j] += 2·a_i·a_j（i < j）/ a_i²（i = j）。 */
private fun polySquare(a: LongArray): LongArray {
    val tmp = LongArray(2 * DIM - 1)
    for (i in 0 until DIM) {
        val ai = a[i]
        if (ai == 0L) continue
        tmp[2 * i] += ai * ai
        val twice = ai shl 1
        var idx = 2 * i + 1
        for (j in i + 1 until DIM) {
            tmp[idx] += twice * a[j]
            idx++
        }
    }
    return polyReduce(tmp)
}

/** x^k mod f 的系数向量（二进制快速幂）。 */
private fun polyPowX(k: Long): LongArray {
    var result = LongArray(DIM); result[0] = 1L
    var base = LongArray(DIM); base[1] = 1L
    var e = k
    while (e > 0L) {
        if (e and 1L == 1L) result = polyMul(result, base)
        e = e ushr 1
        if (e > 0L) base = polySquare(base)
    }
    return result
}

/** 方法 A 主路径：g_k = Σ c_i（初值全 1）。 */
private fun solveKitamasa(k: Long): Long {
    val c = polyPowX(k)
    var sum = 0L
    for (v in c) sum += v
    return sum % MOD
}

// ------------------------------------------------------------ 方法 B：Bostan–Mori 折半

/** 普通卷积（惰性取模），用于长度约 1000 的半长多项式。 */
private fun convMod(a: LongArray, b: LongArray): LongArray {
    val out = LongArray(a.size + b.size - 1)
    for (i in a.indices) {
        val ai = a[i]
        if (ai == 0L) continue
        var idx = i
        for (j in b.indices) {
            out[idx] += ai * b[j]
            idx++
        }
    }
    for (i in out.indices) out[i] %= MOD
    return out
}

/** 取偶数下标（a_e[k] = a[2k]）与奇数下标（a_o[k] = a[2k+1]）。 */
private fun evenIdx(a: LongArray) = LongArray((a.size + 1) / 2) { a[2 * it] }
private fun oddIdx(a: LongArray) = LongArray(a.size / 2) { a[2 * it + 1] }

/**
 * a·b 中只要偶部（wantOdd = false）或奇部：把 a = ae(x²) + x·ao(x²)、b 同理拆分后，
 * a·b = ae·be(x²) + x·(ae·bo + ao·be)(x²) + x²·ao·bo(x²)，于是
 * 偶部（关于 y = x²）= ae·be + y·ao·bo（要左移一位），奇部 = ae·bo + ao·be。
 */
private fun convParity(a: LongArray, b: LongArray, wantOdd: Boolean): LongArray {
    val ae = evenIdx(a); val ao = oddIdx(a)
    val be = evenIdx(b); val bo = oddIdx(b)
    val c1 = convMod(ae, if (wantOdd) bo else be)
    val c2 = convMod(ao, if (wantOdd) be else bo)
    val shift = if (wantOdd) 0 else 1
    val out = LongArray(maxOf(c1.size, c2.size + shift))
    for (i in c1.indices) out[i] = c1[i]
    for (i in c2.indices) out[i + shift] = (out[i + shift] + c2[i]) % MOD
    return out
}

/** 平方（半三角），长度约 1000。 */
private fun sqMod(a: LongArray): LongArray {
    val out = LongArray(2 * a.size - 1)
    for (i in a.indices) {
        val ai = a[i]
        if (ai == 0L) continue
        out[2 * i] += ai * ai
        val twice = ai shl 1
        var idx = 2 * i + 1
        for (j in i + 1 until a.size) {
            out[idx] += twice * a[j]
            idx++
        }
    }
    for (i in out.indices) out[i] %= MOD
    return out
}

/** Q(x)Q(−x) 的偶部：Q = E(x²) + x·O(x²) ⇒ 偶部 = E(y)² − y·O(y)²。 */
private fun bmNextQ(q: LongArray): LongArray {
    val ee = sqMod(evenIdx(q))
    val oo = sqMod(oddIdx(q))
    val out = LongArray(maxOf(ee.size, oo.size + 1))
    for (i in ee.indices) out[i] = ee[i]
    for (i in oo.indices) out[i + 1] = (out[i + 1] - oo[i] + MOD) % MOD
    return out
}

/** 方法 B：Bostan–Mori 折半。 */
private fun solveBostanMori(k: Long): Long {
    var p = LongArray(DIM) { 1L }        // P = 1 + x + … + x^1998
    p[DIM - 1] = 0L
    var q = LongArray(DIM + 1)           // Q = 1 − x^1999 − x^2000
    q[0] = 1L
    q[DIM - 1] = MOD - 1L
    q[DIM] = MOD - 1L
    var n = k
    while (n > 0L) {
        val qm = LongArray(q.size) { i -> if (i and 1 == 0) q[i] else (MOD - q[i]) % MOD }
        p = convParity(p, qm, (n and 1L) == 1L)
        q = bmNextQ(q)
        n = n ushr 1
    }
    check(q[0] == 1L) { "Bostan–Mori 的 Q(0) 应恒为 1，实际 ${q[0]}" }
    return p[0] % MOD
}

// ------------------------------------------------------------ 暴力：O(k) 直接迭代

/**
 * 按定义逐步迭代，长度 2000 的循环缓冲：g_n 写入槽 n mod 2000，写之前该槽正是 g_{n−2000}，
 * 相邻槽 (n+1) mod 2000 正是 g_{n−1999}，读完再覆盖即可原地滚动。
 */
private fun directIterate(k: Long): Long {
    val buf = IntArray(DIM) { 1 }
    var n = DIM
    while (n.toLong() <= k) {
        val i = n % DIM
        val j = if (i + 1 == DIM) 0 else i + 1
        var v = buf[i] + buf[j]
        if (v >= MODI) v -= MODI
        buf[i] = v
        n++
    }
    return buf[(k % DIM).toInt()].toLong()
}

// ------------------------------------------------------------ main

fun main() {
    println("== Project Euler 258 — 滞后斐波那契数列 ==")

    // 0) 定义核验（手算：g_2000 = g_0 + g_1 = 2，g_3999 = g_1999 + g_2000 = 3，g_4000 = 4）
    check(directIterate(0) == 1L && directIterate(1999) == 1L)
    check(directIterate(2000) == 2L && directIterate(3999) == 3L && directIterate(4000) == 4L)
    println("定义核验：g_0 = g_1999 = 1、g_2000 = 2、g_3999 = 3、g_4000 = 4，与手算一致")

    // 1) 小规模三方对照：直接迭代 vs Kitamasa vs Bostan–Mori
    for (k in longArrayOf(10_000L, 1_000_000L, 100_000_000L)) {
        val d = directIterate(k)
        val a = solveKitamasa(k)
        val b = solveBostanMori(k)
        check(d == a) { "k = $k：直接迭代 $d ≠ Kitamasa $a" }
        check(a == b) { "k = $k：Kitamasa $a ≠ Bostan–Mori $b" }
        println("k = $k：直接迭代 = Kitamasa = Bostan–Mori = $d")
    }

    // 2) 完整规模：两条独立路径
    val ansA = solveKitamasa(TARGET_K)
    val ansB = solveBostanMori(TARGET_K)
    println("方法 A（Kitamasa 多项式快速幂）g_{10^18} mod 20092010 = $ansA")
    println("方法 B（Bostan–Mori 生成函数折半）g_{10^18} mod 20092010 = $ansB")
    check(ansA == ansB) { "两种方法不一致：$ansA vs $ansB" }
    check(ansA == EXPECTED) { "与公开答案表不符：$ansA" }
    println("两种方法一致，且与公开答案表 12747994 一致")

    // 3) 计时：JIT 预热后 3 轮取最优
    check(solveKitamasa(TARGET_K) == ansA)
    var bestA = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveKitamasa(TARGET_K) == ansA)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestA) bestA = ms
        println("  方法 A 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 A 完整求解：${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    check(solveBostanMori(TARGET_K) == ansB)
    var bestB = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveBostanMori(TARGET_K) == ansB)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestB) bestB = ms
        println("  方法 B 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 B 完整求解：${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")

    println("check() 全部通过")
}
