#!/usr/bin/env kotlin
/**
 * Project Euler 288 — An Enormous Factorial（巨大的阶乘）：暴力 / 定义级对照
 *
 * 独立实现，与 solution.kt 不共享核心代码：这里全程用 BigInteger 精确算术，
 * 既不做流式取模、也没有 128 位乘模与逆元闭式。
 *
 * 路径 1（定义级精确求解，meta 的 bruteForceBaselineMs 口径）
 * ──────────────────────────────────────────────────────────
 * 用同一个随机数发生器生成 T₀…T_q，BigInteger Horner 精确算出
 *     N = Σ_{n=0}^{q} T_n·pⁿ
 * （q ≤ 2×10^4 时 N 只有几万位十进制数字），再**按定义**数 N! 中因子 p 的个数：
 * N! 里被 p 整除的数是 p、2p、3p… 的倍数，把它们一层层除下去即
 *     v = Σ_{k≥1} ⌊N / pᵏ⌋
 * 用 BigInteger 逐次除法求和——这才是「数因子」的字面实现；数位和恒等式只是旁路。
 * 同一个精确 N 上还并行核对三件事：
 *   (a) v 与 (N − s_p(N)) / (p − 1) 相等，且 N − s_p(N) 恰被 p − 1 整除；
 *   (b) N mod pᵏ 与 Σ T_n·(pⁿ mod pᵏ) 的逐项权重和相等（两种取模结构）；
 *   (c) v mod pᵏ 与 (N − s_p(N))·(p − 1)⁻¹ mod pᵏ 相等（逆元用 BigInteger.modInverse）。
 * 覆盖规模：p = 61 的 q = 1000/2000/5000/10000/20000 与 p = 3 的 q = 10000（题面校验）/30000。
 * 这些值与 solution.kt 的中小规模对照表逐行对拍（同一批 (p, q, k) 用两条完全不同的算术）。
 *
 * 路径 2（最朴素的因子计数：把 1…n 每个数的 p 因子逐个除出来）
 * ────────────────────────────────────────────────────────────
 * 对普通整数 n（与本题的 N 无关）直接做
 *     for i in 1..n: j = i; while (j % p == 0) { j /= p; count++ }
 * 数出 n! 的 p 指数，与 Σ⌊n/pᵏ⌋、与 (n − s_p(n))/(p − 1) 三者互证——这是对
 * 「数位和恒等式」这条数学前提本身的锚点，n = 10^6、p = 3 与 61。
 *
 * 外推说明（为什么 q = 10^7 只能交给 solution.kt 的流式路径）
 * ──────────────────────────────────────────────────────────
 * 精确算 N 的代价是 O(q²)：N 的位数 ≈ q·log₁₀p 线性增长，每一步 Horner 都要过一遍
 * 全部字（BigInteger 乘小整数是 O(位数)），逐次除法同样。q = 2×10^4、p = 61 时 N 约 3.6 万位
 * （≈1.2×10^5 比特、15 KB），单轮只要几十毫秒；q = 10^7 时 N 约 1.8×10^7 位十进制
 * （≈5.9×10^7 比特、7.4 MB）——内存放得下，但 Horner 的 Σ O(位数) 已达 ~10^13 字节级操作，
 * 时间上完全不可行。而主路径是 O(q)、每步只有常数个 Long 运算、与 N 的位数无关：
 * 两法在 q = 1000…30000 的七个规模上逐位一致，即为外推的全部依据。
 *
 * 构建：bash scripts/kotlinc-shim.sh content/problems/0288/brute-force.kt -d <目录>
 *      java -Xmx4g -cp <目录>:<kotlin-stdlib> Brute_forceKt
 * （文件名里的 - 会被 mangle 成 _，JVM facade 类名是 Brute_forceKt）
 */

import java.math.BigInteger

private const val SEED = 290_797L
private const val RNG_MOD = 50_515_093L

// ─────────────────────────── 定义级精确算术 ───────────────────────────

/** T₀…T_q（IntArray：暴力路径不在乎空间）。 */
private fun digitsOf(p: Int, q: Int): IntArray {
    val digits = IntArray(q + 1)
    var s = SEED
    for (n in 0..q) {
        digits[n] = (s % p).toInt()
        s = s * s % RNG_MOD
    }
    return digits
}

/** 精确 N = Σ T_n·pⁿ（BigInteger Horner，从最高位起）。 */
private fun exactN(p: Int, q: Int, digits: IntArray): BigInteger {
    val bp = BigInteger.valueOf(p.toLong())
    var n = BigInteger.ZERO
    for (i in q downTo 0) n = n.multiply(bp).add(BigInteger.valueOf(digits[i].toLong()))
    return n
}

/** 定义级因子计数：v = Σ_{k≥1} ⌊N / pᵏ⌋（BigInteger 逐次除法，不经过数位和）。 */
private fun factorCountByDivision(n: BigInteger, p: Int): BigInteger {
    val bp = BigInteger.valueOf(p.toLong())
    var v = BigInteger.ZERO
    var x = n
    while (x.signum() > 0) {
        x = x.divide(bp)
        v = v.add(x)
    }
    return v
}

/** 数位和恒等式旁路：(N − s_p(N)) / (p − 1)。 */
private fun factorCountByDigitSum(n: BigInteger, p: Int, digitSum: Long): BigInteger {
    val pm1 = BigInteger.valueOf((p - 1).toLong())
    val num = n.subtract(BigInteger.valueOf(digitSum))
    check(num.mod(pm1).signum() == 0) { "N − s_p(N) 不被 p − 1 整除" }
    return num.divide(pm1)
}

/** 另一种取模结构：Σ T_n·(pⁿ mod m)（权重每步取模），与 Horner 逐位递推无关。 */
private fun nModByWeightSum(digits: IntArray, p: Int, mod: BigInteger): BigInteger {
    val bp = BigInteger.valueOf(p.toLong())
    var acc = BigInteger.ZERO
    var w = BigInteger.ONE
    for (d in digits) {
        acc = acc.add(BigInteger.valueOf(d.toLong()).multiply(w)).mod(mod)
        w = w.multiply(bp).mod(mod)
    }
    return acc
}

private class ExactResult(val nfMod: BigInteger, val nMod: BigInteger, val digitSum: Long)

/** 一个 (p, q, k)：定义级精确求解 NF(p, q) mod p^k，并对三条旁路做自检。 */
private fun solveExact(p: Int, q: Int, k: Int): ExactResult {
    val digits = digitsOf(p, q)
    val n = exactN(p, q, digits)
    var s = 0L
    for (d in digits) s += d.toInt()
    val v = factorCountByDivision(n, p)
    check(v == factorCountByDigitSum(n, p, s)) { "(p=$p, q=$q)：逐次除法与数位和恒等式不一致" }
    val mod = BigInteger.valueOf(p.toLong()).pow(k)
    val nMod = n.mod(mod)
    check(nMod == nModByWeightSum(digits, p, mod)) { "(p=$p, q=$q)：Horner 与逐项权重取模不一致" }
    val viaInverse = nMod.subtract(BigInteger.valueOf(s)).mod(mod)
        .multiply(BigInteger.valueOf((p - 1).toLong()).modInverse(mod)).mod(mod)
    check(v.mod(mod) == viaInverse) { "(p=$p, q=$q)：定义级因子数与模逆路线不一致" }
    return ExactResult(v.mod(mod), nMod, s)
}

// ─────────────────────────── 最朴素的因子计数 ───────────────────────────

/** 把 1…n 每个数的 p 因子逐个除出来（n = 10^6 量级可行）。 */
private fun literalFactorCount(n: Int, p: Int): Long {
    var count = 0L
    for (i in 1..n) {
        var j = i
        while (j % p == 0) {
            count++
            j /= p
        }
    }
    return count
}

/** 普通整数 n 的 p 进制数位和（重复取模）。 */
private fun digitSumBase(n: Long, p: Int): Long {
    var x = n
    var s = 0L
    while (x > 0) {
        s += x % p
        x /= p
    }
    return s
}

// ─────────────────────────────── 批处理与计时 ───────────────────────────────

private val CASES = listOf(
    intArrayOf(61, 1000, 10), intArrayOf(61, 2000, 10), intArrayOf(61, 5000, 10),
    intArrayOf(61, 10000, 10), intArrayOf(61, 20000, 10),
    intArrayOf(3, 10000, 20), intArrayOf(3, 30000, 20),
)

/** meta 的 bruteForceBaselineMs 口径：整批定义级精确求解；返回结果的滚动校验和。 */
private fun runExactBatch(verbose: Boolean): Long {
    var checksum = 0L
    for (c in CASES) {
        val r = solveExact(c[0], c[1], c[2])
        checksum = checksum * 31 + r.nfMod.toLong()
        if (verbose) {
            println("  (p=${c[0]}, q=${c[1]}): NF mod ${c[0]}^${c[2]} = ${r.nfMod}")
        }
    }
    return checksum
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮校验和漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.3f".format(ms)} ms")
    }
    println("$tag：${"%.3f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

// ─────────────────────────────────── main ───────────────────────────────────

fun main() {
    println("=== 路径 1：定义级精确求解（BigInteger 全量 N + Σ⌊N/pᵏ⌋ 逐次除法）===")
    val given = 624_955_285L
    val r3 = solveExact(3, 10_000, 20)
    check(r3.nfMod.toLong() == given) { "NF(3,10000) mod 3^20 = ${r3.nfMod} ≠ 题面给定 $given" }
    println("  题面给定校验：(3, 10000) → ${r3.nfMod}（题面给定 $given），" +
        "N mod 3^20 = ${r3.nMod}，s_3(N) = ${r3.digitSum}")
    println("  中小规模对照表（与 solution.kt 的路径 A/B 逐行对拍）：")
    val checksum = runExactBatch(verbose = true)

    println()
    println("=== 路径 2：最朴素的因子计数（普通整数 n = 10^6，与 RNG 无关）===")
    var literalMs = 0.0
    for (p in intArrayOf(3, 61)) {
        val n = 1_000_000
        val lt0 = System.nanoTime()
        val literal = literalFactorCount(n, p)
        literalMs += (System.nanoTime() - lt0) / 1e6
        var floorSum = 0L
        var pk = p.toLong()
        while (pk <= n) {
            floorSum += n / pk
            pk *= p
        }
        val viaDigitSum = (n - digitSumBase(n.toLong(), p)) / (p - 1)
        check(literal == floorSum && floorSum == viaDigitSum) {
            "p = $p：逐个数因子 $literal ≠ Σ⌊n/pᵏ⌋ $floorSum ≠ 数位和恒等式 $viaDigitSum"
        }
        println("  p = $p：逐个除出来的因子数 = Σ⌊n/pᵏ⌋ = (n − s_p(n))/(p − 1) = $literal")
    }
    println("  （路径 2 逐个数因子本身：p = 3 与 61 合计 ${"%.1f".format(literalMs)} ms，n = 10^6）")

    println()
    println("=== 计时（meta 的 bruteForceBaselineMs 口径）===")
    val ms = bestOf3("定义级精确批（${CASES.size} 个规模，q ≤ 3×10^4）", checksum) { runExactBatch(verbose = false) }

    println()
    println("=== 外推说明 ===")
    println("  精确 N 的位数 ≈ q·log₁₀p：q = 2×10^4、p = 61 时约 3.6 万位（15 KB）；q = 10^7、p = 61")
    println("  时约 1.8×10^7 位十进制（5.9×10^7 比特、7.4 MB）——内存放得下，但 Horner 的每一步都要")
    println("  过一遍全部字，合计 Σ O(位数) ≈ 10^13 字节级操作，时间上不可行。主路径每步只有常数个")
    println("  Long 运算、与 N 的位数无关（O(q)），且上面七个规模上两法逐位一致，q = 10^7 只是把同一")
    println("  循环多跑三个数量级。")
    println("  因此题目答案以 solution.kt 的流式路径输出为准（本文件负责把这些值钉在定义上）。")

    println()
    println("汇总：定义级精确批 ${"%.3f".format(ms)} ms；校验和 $checksum")
    println("check() 全部通过（含 N−s 整除性、Horner vs 权重取模、逐次除法 vs 数位和恒等式）")
}
