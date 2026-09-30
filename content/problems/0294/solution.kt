#!/usr/bin/env kotlin
/**
 * Project Euler 294 — Sum of Digits - Experience #23（数位和与 23）
 *
 * 题目：S(n) = 满足「k < 10^n、23 | k、d(k) = 23」的正整数 k 的个数（d 为十进制数位和）。
 * 已知 S(9) = 263626、S(42) = 6377168878570056。求 S(11^12) mod 10^9。
 *
 * 建模：把 n 位十进制串按位权的模 23 周期分组
 * ────────────────────────────────────────────
 * k < 10^n ⇔ n 位十进制串（允许前导零）d_0…d_{n−1}，每位 d_i ∈ [0,9]，
 *   d(k) = Σ d_i = 23，k ≡ Σ d_i·10^i (mod 23) ≡ 0。
 * ord_23(10) = 22（10^11 ≡ −1，10^i ≠ 1 对 0 < i < 22），故位权 10^i mod 23 以 22 为周期。
 * 把 n 写 n = 22q + r：位权取遍 1..22，其中 r 个权类（i = 0..r−1）各出现 q+1 次，
 * 其余 22−r 个权类各出现 q 次。
 *
 * 同一权类 w 内 c 个位置的热点：设该类数字和为 j，则贡献权值 w·j（模 23），
 * 而把 j 写成 c 个 0..9 数字的方案数是（隔板 + 上界容斥）
 *   a_c(j) = Σ_{t≥0} (−1)^t C(c,t)·C(c+j−10t−1, j−10t)，  t ≤ j/10 ≤ 2。
 * 于是问题化为 22 个权类的卷积：Σ (j_1..j_22) ∏ a_{c_w}(j_w)，约束 Σ j_w = 23、
 * Σ w·j_w ≡ 0 (mod 23)。DP 状态只有 (已用数位和 ≤ 23, 加权和 mod 23) = 24×23 格。
 *
 * 主路径 A：Long 模 1e9 的 DP（系数先对 1e9 取模）。
 * 复核路径 B：BigInteger 精确 DP（同一分组公式，不取模）→ 复现 S(9)、S(42) 的题面值。
 * 复核路径 C：逐位 DP（每位一个转移，O(n·24·23·10)），只适合 n 不大时对拍；
 *             它在 n = 9/42/100/421/1234 上与分组法全等。
 * 校验路径 D：n = 7 直接枚举 k < 10^7 逐值判定。
 *
 * 复杂度：分组法 O(22 × 24 × 23 × 24) ≈ 3×10^5 次乘加，微秒~毫秒级；
 *         逐位 DP O(n·24·23·10)；直接枚举 O(10^n)。
 *
 * 运行
 * ────
 * OUTDIR=/tmp/kc-0294 bash scripts/kotlinc-shim.sh content/problems/0294/solution.kt
 * java -cp /tmp/kc-0294:<kotlin-stdlib> SolutionKt
 */

import java.math.BigInteger

private const val MOD = 1_000_000_000L

/** k 的十进制数位和。 */
private fun digitSum(k: Long): Int {
    var n = k
    var s = 0
    while (n > 0L) {
        s += (n % 10L).toInt()
        n /= 10L
    }
    return s
}

// ─────────────────── 精确组合数（小下标） ───────────────────

private fun binomBig(n: Long, k: Int): BigInteger {
    if (k < 0 || n < k) return BigInteger.ZERO
    var num = BigInteger.ONE
    var den = BigInteger.ONE
    for (i in 0 until k) {
        num = num.multiply(BigInteger.valueOf(n - i))
        den = den.multiply(BigInteger.valueOf((i + 1).toLong()))
    }
    return num.divide(den)
}

private fun binomMod(n: Long, k: Int, mod: Long): Long =
    binomBig(n, k).mod(BigInteger.valueOf(mod)).toLong()

// ─────────────────── 位权分组（两版系数 + 两版 DP） ───────────────────

/** 位权 10^i mod 23 的周期（10^11 ≡ −1）。 */
private const val PERIOD = 22

/** 权类计数 cnt[w] = n 位中位权为 w 的位置个数。 */
private fun weightClassCounts(n: Long): LongArray {
    val cnt = LongArray(23)
    val q = n / PERIOD
    val r = (n % PERIOD).toInt()
    var w = 1L
    for (i in 0 until PERIOD) {
        cnt[w.toInt()] += q
        if (i < r) cnt[w.toInt()] += 1L
        w = w * 10L % 23L
    }
    return cnt
}

/** a_c(j) 的精确值（BigInteger 版，jmax = 23）。 */
private fun coeffsBig(c: Long): Array<BigInteger> {
    val out = Array(24) { BigInteger.ZERO }
    if (c == 0L) {
        out[0] = BigInteger.ONE
        return out
    }
    for (j in 0..23) {
        var tot = BigInteger.ZERO
        var t = 0
        while (10 * t <= j) {
            val term = binomBig(c, t).multiply(binomBig(c + j - 10 * t - 1, j - 10 * t))
            tot = if (t % 2 == 0) tot.add(term) else tot.subtract(term)
            t++
        }
        out[j] = tot
    }
    return out
}

/** a_c(j) mod mod。 */
private fun coeffsMod(c: Long, mod: Long): LongArray {
    val out = LongArray(24)
    if (c == 0L) {
        out[0] = 1L
        return out
    }
    for (j in 0..23) {
        var tot = 0L
        var t = 0
        while (10 * t <= j) {
            val term = binomMod(c, t, mod) * binomMod(c + j - 10 * t - 1, j - 10 * t, mod) % mod
            tot = if (t % 2 == 0) (tot + term) % mod else (tot - term + mod) % mod
            t++
        }
        out[j] = tot
    }
    return out
}

/** 路径 A：分组 + Long 模 DP。 */
private fun countByClassesMod(n: Long, mod: Long): Long {
    val cnt = weightClassCounts(n)
    var dp = Array(24) { LongArray(23) }
    dp[0][0] = 1L
    for (w in 1..22) {
        val a = coeffsMod(cnt[w], mod)
        if (cnt[w] == 0L) continue
        val ndp = Array(24) { LongArray(23) }
        for (s in 0..23) {
            for (m in 0 until 23) {
                val v = dp[s][m]
                if (v == 0L) continue
                var j = 0
                while (s + j <= 23) {
                    if (a[j] != 0L) {
                        val nm = (m + w * j) % 23
                        ndp[s + j][nm] = (ndp[s + j][nm] + v * a[j]) % mod
                    }
                    j++
                }
            }
        }
        dp = ndp
    }
    return dp[23][0]
}

/** 路径 B：分组 + BigInteger 精确 DP。 */
private fun countByClassesExact(n: Long): BigInteger {
    val cnt = weightClassCounts(n)
    var dp = Array(24) { arrayOfNulls<BigInteger>(23) }
    dp[0][0] = BigInteger.ONE
    for (w in 1..22) {
        if (cnt[w] == 0L) continue
        val a = coeffsBig(cnt[w])
        val ndp = Array(24) { arrayOfNulls<BigInteger>(23) }
        for (s in 0..23) {
            for (m in 0 until 23) {
                val v = dp[s][m] ?: continue
                if (v.signum() == 0) continue
                var j = 0
                while (s + j <= 23) {
                    if (a[j].signum() != 0) {
                        val nm = (m + w * j) % 23
                        val cur = ndp[s + j][nm]
                        ndp[s + j][nm] = (cur?.add(v.multiply(a[j]))) ?: v.multiply(a[j])
                    }
                    j++
                }
            }
        }
        dp = ndp
    }
    return dp[23][0] ?: BigInteger.ZERO
}

/** 路径 C：逐位 DP（每位一个转移），可带模数（不带上界时用 Long.MAX_VALUE 表示精确）。 */
private fun countByPositionDp(n: Int, mod: Long): Long {
    var dp = Array(24) { LongArray(23) }
    dp[0][0] = 1L
    var w = 1L
    for (pos in 0 until n) {
        val ndp = Array(24) { LongArray(23) }
        for (s in 0..23) {
            for (m in 0 until 23) {
                val v = dp[s][m]
                if (v == 0L) continue
                for (d in 0..9) {
                    if (s + d > 23) break
                    val nm = ((m + d * w) % 23L).toInt()
                    ndp[s + d][nm] = (ndp[s + d][nm] + v) % mod
                }
            }
        }
        dp = ndp
        w = w * 10L % 23L
    }
    return dp[23][0]
}

// ───────────────────────────── 计时工具 ─────────────────────────────

private fun best(tag: String, rounds: Int = 3, f: () -> Long): Double {
    var bestMs = Double.MAX_VALUE
    var last = 0L
    repeat(rounds) { r ->
        val t0 = System.nanoTime()
        last = f()
        val ms = (System.nanoTime() - t0) / 1e6
        println("  第 ${r + 1} 轮：$last（${"%.3f".format(ms)} ms）")
        if (ms < bestMs) bestMs = ms
    }
    println("$tag：${"%.3f".format(bestMs)} ms（$rounds 轮最优）")
    return bestMs
}

fun main() {
    println("== 1. 题面样例（路径 A 模 1e9 / 路径 B 精确） ==")
    val s9mod = countByClassesMod(9, MOD)
    val s9exact = countByClassesExact(9)
    val s42exact = countByClassesExact(42)
    println("S(9) 精确 = $s9exact（期望 263626）")
    println("S(42) 精确 = $s42exact（期望 6377168878570056）")
    println("S(9) 分组模 = $s9mod")
    check(s9exact == BigInteger.valueOf(263_626L)) { "S(9) 不符" }
    check(s42exact == BigInteger("6377168878570056")) { "S(42) 不符" }
    check(s9mod == 263_626L) { "S(9) 模路径不符" }
    println("两条分组路径与题面样例一致 ✓")

    println()
    println("== 2. 路径 C（逐位 DP）对拍：n = 9/42/100/421/1234 ==")
    for (n in intArrayOf(9, 42, 100, 421, 1234)) {
        val a = countByClassesMod(n.toLong(), MOD)
        val b = countByPositionDp(n, MOD)
        check(a == b) { "n=$n：分组 $a vs 逐位 $b" }
        println("n = $n：分组 = 逐位 = $a")
    }
    val exact42 = countByPositionDp(42, Long.MAX_VALUE / 2)   // 精确（不溢出范围内）
    check(exact42 == 6_377_168_878_570_056L) { "逐位 DP 精确 S(42) 不符：$exact42" }
    println("逐位 DP 精确 S(42) = $exact42 ✓")

    println()
    println("== 3. 直接枚举对拍：n = 7 ==")
    var brute = 0L
    for (k in 23L until 10_000_000L step 23L) if (digitSum(k) == 23) brute++
    val dp7 = countByPositionDp(7, Long.MAX_VALUE / 2)
    check(brute == dp7) { "n=7：枚举 $brute vs DP $dp7" }
    println("k < 10^7 中 23 | k 且 d(k) = 23 的个数 = $brute（逐位 DP 同值）✓")

    println()
    println("== 4. 全量：n = 11^12 = $N_11_12 ==")
    val ansMod = countByClassesMod(N_11_12, MOD)
    val ansExact = countByClassesExact(N_11_12)
    check(ansExact.mod(BigInteger.valueOf(MOD)).toLong() == ansMod) { "两路径模值不一致" }
    println("路径 A（模 1e9）           = $ansMod")
    println("路径 B（精确 % 1e9）       = ${ansExact.mod(BigInteger.valueOf(MOD))}")
    println("（精确值 ${ansExact.toString().length} 位十进制，仅供校验）")

    println()
    println("== 5. 计时 ==")
    best("路径 A：分组 + Long 模 DP（n = 11^12）") { countByClassesMod(N_11_12, MOD) }
    best("路径 C：逐位 DP（n = 1234，对拍口径）") { countByPositionDp(1234, MOD) }

    println()
    println("== 6. 结果 ==")
    println("S(11^12) mod 10^9 = $ansMod")
    println("n = 11^12 = $N_11_12，n mod 22 = ${N_11_12 % 22}")
}

private const val N_11_12 = 3_138_428_376_721L   // 11^12
