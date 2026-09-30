#!/usr/bin/env kotlin
/**
 * Project Euler 284 — Steady Squares（稳定平方数）：暴力 / 对照
 * 独立实现，与 solution.kt 不共享核心代码。
 *
 * 路径 1（定义级枚举，meta 的 bruteForceBaselineMs 口径）
 * ───────────────────────────────────────────────────
 * 直接照题面定义：对每个 n 枚举区间 [14^{n−1}, 14ⁿ) 内的**全部**整数 x，判定
 * x² − x ≡ 0 (mod 14ⁿ)，对通过者累加 14 进制的数位和。代价 Θ(14ⁿ)：n = 8 共
 * 14⁸ ≈ 1.5×10⁹ 个候选（本机约 1 s，x² 仍在 Long 范围内），n = 9 是 2×10¹⁰ 个、
 * 到 n = 10000 需要 14^10000 次——完全不可行。因此它只能把主路径钉到 n = 8，
 * n ≤ 9 的题面样例（582）由主路径承担。
 *
 * 路径 2（候选提升暴力：不做任何公式推导的 Hensel）
 * ──────────────────────────────────────────────
 * 只用「模 14ⁿ 的幂等元提升到模 14^{n+1} 时必形如 x + d·14ⁿ（d = 0…13）」这一点，
 * 对 14 个候选逐个用 BigInteger 直接算 (x′)² mod 14^{n+1} 是否等于 x′，命中者即新一层解
 * （实测每层恰有一个候选可行）。它不依赖 u、v 的递推公式，但每层要做 14 次 O(n²) 比特的
 * 大整数乘法，n = 300 已明显变慢——这正是主路径（每层一次 O(n) 位运算）的意义所在。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0284/brute-force.kt -d <目录>
 *      java -cp <目录>:<kotlin-stdlib> Brute_forceKt
 */

import java.math.BigInteger

private const val BASE = 14

// ─────────────────────── 主路径（与 solution.kt 同构，供对拍） ───────────────────────

private val INV_2_POW_MOD7 = intArrayOf(1, 4, 2)
private val POW7_BI = BigInteger.valueOf(7)

/** 返回 [0, N) 的全部 14 进制数字：索引 0 为 A 支、1 为 B 支（数字 tbl[支][位]）。 */
private fun mainDigits(N: Int): Array<IntArray> {
    val out = Array(2) { IntArray(N) }
    val u = arrayOf(BigInteger.valueOf(4), BigInteger.valueOf(3))
    val v = arrayOf(BigInteger.ONE, BigInteger.ONE)
    var pow2 = BigInteger.TWO
    var pow7 = BigInteger.valueOf(7)
    out[0][0] = 8
    out[1][0] = 7
    for (n in 1 until N) {
        for (kind in 0..1) {
            val d2 = u[kind].mod(BigInteger.TWO).toInt()
            val inv2 = INV_2_POW_MOD7[n % 3]
            val d7 = ((-v[kind].mod(POW7_BI).toInt() * inv2) % 7 + 7) % 7
            var d = -1
            for (cand in 0 until BASE) if (cand % 2 == d2 && cand % 7 == d7) { d = cand; break }
            check(d >= 0)
            u[kind] = (u[kind] + pow7.multiply(BigInteger.valueOf(d.toLong()))).shiftRight(1)
            v[kind] = (v[kind] + pow2.multiply(BigInteger.valueOf(d.toLong()))).divide(POW7_BI)
            out[kind][n] = d
        }
        pow2 = pow2.shiftLeft(1)
        pow7 = pow7.multiply(POW7_BI)
    }
    return out
}

// ─────────────────────── 路径 1：定义级枚举 ───────────────────────

/** 枚举 [14^{n−1}, 14ⁿ) 全部整数，返回逐层数位和（索引 n）。1 这位数只在 n = 1 计入。 */
private fun bruteEnum(maxN: Int): LongArray {
    val out = LongArray(maxN + 1)
    var lo = 1L
    for (n in 1..maxN) {
        val m = lo * BASE
        var s = 0L
        for (x in lo until m) {
            if ((x * x - x) % m == 0L) {
                var t = x
                while (t > 0L) { s += t % BASE; t /= BASE }
            }
        }
        out[n] = s
        lo = m
    }
    return out
}

// ─────────────────────── 路径 2：候选提升暴力 ───────────────────────

/**
 * 从模 14 的 4 个幂等元 {0,1,7,8} 出发，每层对 14 个候选逐一用大整数验证，
 * 返回非平凡的 2 支（A ≡ 0 mod 2ⁿ、B ≡ 1 mod 2ⁿ）在第 n 层的解（n = 1…maxN）。
 */
private fun bruteLift(maxN: Int): Pair<List<BigInteger>, List<BigInteger>> {
    val b14 = BigInteger.valueOf(BASE.toLong())
    var pool = listOf(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(7), BigInteger.valueOf(8))
    val keepA = ArrayList<BigInteger>()
    val keepB = ArrayList<BigInteger>()
    for (n in 1..maxN) {
        if (n > 1) {
            val modN = b14.pow(n)
            val step = b14.pow(n - 1)
            val next = ArrayList<BigInteger>(4)
            for (x in pool) {
                for (d in 0 until BASE) {
                    val y = x.add(step.multiply(BigInteger.valueOf(d.toLong())))
                    if (y.multiply(y).subtract(y).mod(modN) == BigInteger.ZERO) next.add(y)
                }
            }
            check(next.size == 4) { "n=$n：提升候选数应为 4，实得 ${next.size}" }
            pool = next
        }
        // 按 14 进制末位判别支：末位 8 ⇒ A（x ≡ 0 mod 2 且 ≡ 1 mod 7）；末位 7 ⇒ B
        for (y in pool) {
            when (y.mod(b14).toInt()) {
                8 -> keepA.add(y)
                7 -> keepB.add(y)
            }
        }
    }
    return keepA to keepB
}

private fun digitsOf(x: BigInteger, n: Int): IntArray {
    var t = x
    val out = IntArray(n)
    for (i in 0 until n) { out[i] = t.mod(BigInteger.valueOf(BASE.toLong())).toInt(); t = t.divide(BigInteger.valueOf(BASE.toLong())) }
    return out
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    val N = 10_000
    val md = mainDigits(N)

    // 主路径逐层贡献
    val mainPerLevel = LongArray(N + 1)
    var preA = 0L
    var preB = 0L
    for (n in 1..N) {
        preA += md[0][n - 1]
        preB += md[1][n - 1]
        var add = 0L
        if (md[0][n - 1] != 0) add += preA
        if (md[1][n - 1] != 0) add += preB
        if (n == 1) add += 1L
        mainPerLevel[n] = add
    }

    // ---- 路径 1：定义级枚举，与主路径逐层对拍 ----
    val maxEnum = 8
    val be = bruteEnum(maxEnum)
    for (n in 1..maxEnum) check(be[n] == mainPerLevel[n]) { "n=$n：枚举 ${be[n]} ≠ 主路径 ${mainPerLevel[n]}" }
    println("路径 1（定义级枚举 n ≤ $maxEnum）：逐层数位和 " + (1..maxEnum).joinToString(",") { be[it].toString() } +
        "；n ≤ $maxEnum 合计 ${be.sum()}（主路径同段合计 ${(1..maxEnum).sumOf { mainPerLevel[it] }}）")
    val sum9 = (1..9).sumOf { mainPerLevel[it] }
    check(sum9 == 582L)
    println("题面样例：主路径 Σ_{n≤9} = $sum9 = 2d8₁₄（枚举只能到 n = $maxEnum，其余层由主路径承担）")

    // ---- 路径 2：候选提升暴力，与主路径的数字串逐位对拍 ----
    val maxLift = 300
    val tLift = System.nanoTime()
    val (la, lb) = bruteLift(maxLift)
    val liftMs = (System.nanoTime() - tLift) / 1e6
    for (n in 1..maxLift) {
        val da = digitsOf(la[n - 1], n)
        val db = digitsOf(lb[n - 1], n)
        for (i in 0 until n) {
            check(da[i] == md[0][i]) { "路径 2 A 支第 $i 位（n=$n）：${da[i]} ≠ ${md[0][i]}" }
            check(db[i] == md[1][i]) { "路径 2 B 支第 $i 位（n=$n）：${db[i]} ≠ ${md[1][i]}" }
        }
    }
    println("路径 2（候选提升暴力 n ≤ $maxLift）：每层 14 个候选逐一验证，数字串与主路径逐位一致" +
        "（本次 ${"%.0f".format(liftMs)} ms；代价随 n 平方增长，到 n = 10000 不可行）")

    // ---- 路径 1 的外推 ----
    val b14 = BigInteger.valueOf(BASE.toLong())
    val enumTo24 = b14.pow(24)
    val enumMax = b14.pow(maxEnum)
    println("外推：路径 1 到 n = 24 需枚举 14²⁴ = $enumTo24 个数，是本次可行上限（14^$maxEnum = $enumMax）的约 " +
        "${enumTo24.divide(enumMax)} 倍；到 n = 10000 则是 14^10000 量级，不可行")

    // ---- 汇总与计时 ----
    val totalMain = (1..N).sumOf { mainPerLevel[it] }
    val msEnum = bestOf3("路径 1（定义级枚举，n ≤ $maxEnum，1.6×10⁹ 个候选）", (1..maxEnum).sumOf { be[it] }) {
        bruteEnum(maxEnum).sum()
    }
    println()
    println("主路径数位总和 = $totalMain₁₀；路径 1 可及段（n ≤ $maxEnum）与主路径一致")
    println("汇总：定义级枚举 ${"%.3f".format(msEnum)} ms（n ≤ $maxEnum）；候选提升暴力到 n = $maxLift 约 ${"%.0f".format(liftMs)} ms（一次性，未计预热）")
    println("check() 全部通过")
}
