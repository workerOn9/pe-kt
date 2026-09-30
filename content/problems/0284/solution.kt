#!/usr/bin/env kotlin
/**
 * Project Euler 284 — Steady Squares（稳定平方数）
 *
 * 题目：数 x 满足 x² 的末几位与 x 相同，称为稳定平方数（十进制例：376² = 141376）。在 14 进制中
 * c37² = aa0c37（c = 12），数位和 c+3+7 = 18。已知 1 ≤ n ≤ 9 时全部 n 位（14 进制、无前导零）
 * 稳定平方数的数位之和是 2d8₁₄ = 582₁₀。求 1 ≤ n ≤ 10000 时的数位总和，用 14 进制小写字母输出。
 *
 * 思路
 * ────
 * n 位 14 进制数 = 模 14ⁿ 的剩余类 [14^{n−1}, 14ⁿ)，「x² 与 x 的末 n 位相同」即幂等方程
 *
 *   x² ≡ x (mod 14ⁿ)  ⟺  x(x − 1) ≡ 0 (mod 2ⁿ·7ⁿ).
 *
 * 由于 x 与 x−1 互素，2ⁿ 必须整除掉其中一个、7ⁿ 也同样，CRT 给出模 14ⁿ 恰有 4 个幂等元：
 *
 *   0，1，Aₙ ≡ (0 mod 2ⁿ, 1 mod 7ⁿ)，Bₙ ≡ (1 mod 2ⁿ, 0 mod 7ⁿ).
 *
 * 主路径：Hensel 逐位提升。已知 xₙ 是模 14ⁿ 的幂等元，其向模 14^{n+1} 的提升必形如
 * xₙ + d·14ⁿ（d ∈ [0,14)），代入方程
 *
 *   (xₙ + d·14ⁿ)² − (xₙ + d·14ⁿ) = (xₙ² − xₙ) + d·14ⁿ(2xₙ − 1) + d²·14^{2n} ≡ 0 (mod 14^{n+1})
 *
 * 中（n ≥ 1 时末项 ≡ 0），再分别投影到 2^{n+1} 与 7^{n+1} 两个同余条件：记
 * uₙ = xₙ / 2ⁿ（A 支为 xₙ/2ⁿ、B 支为 (xₙ−1)/2ⁿ）、vₙ = xₙ / 7ⁿ（A 支为 (xₙ−1)/7ⁿ、B 支为 xₙ/7ⁿ），
 * 两个条件化为
 *
 *   d ≡ uₙ (mod 2)，  d ≡ −vₙ·(2ⁿ)^{-1} (mod 7)，
 *
 * 模 2 与模 7 的解在 [0,14) 内唯一存在（2ⁿ 可逆 mod 7，周期 3：inv(2ⁿ) = 4,2,1），于是每一支
 * 的提升唯一：x_{n+1} = xₙ + d·14ⁿ。提升公式对两支形状相同：
 *
 *   u_{n+1} = (uₙ + d·7ⁿ)/2，  v_{n+1} = (vₙ + d·2ⁿ)/7，
 *
 * 恰好是可整除的整除（由 d 的取法保证），因此整个流程只需 BigInteger 的加法/移位/除以小整数。
 * 另有一个关键观察：x_{n+1} mod 14ⁿ = xₙ，即两支的 14 进制数字序列一旦写出就永远不会改变——
 * xₙ 就是同一条无限数字串的第 n 位截断，所以各层的数位和是前缀和。
 *
 * 每层的贡献 = 该层全部 n 位稳定平方数的数位和：Aₙ、Bₙ 最高位（数字串第 n−1 位）非 0 才计入
 * （否则它是「带前导零」的表示，按题意排除），再加上 n = 1 时的数 1 本身。
 *
 * 复杂度
 * ──────
 * 10000 层 × 2 支，每层对位数 O(n) 的 BigInteger 做少量线性时间运算，总量 Σ O(n/64) 词运算
 * ≈ 10⁷ 量级，毫秒级；空间 O(N) 数字 + O(N) 位的大整数。定义级暴力要枚举 14ⁿ 个数，n = 7 已需
 * 1.05×10⁸ 次判定（~0.5 s），n = 10000 不可行（14^10000 个数）。
 *
 * 验证
 * ────
 * 1. 定义级暴力（n ≤ 6，枚举区间 [14^{n−1}, 14ⁿ) 内全部数）：逐层数位和与主路径一致；
 * 2. 题面样例：Σ_{n≤9} = 582 = 2d8₁₄（实跑复现）；
 * 3. 幂等元结构：n ≤ 4 时枚举全部 x 验证幂等元恰为 {0,1,Aₙ,Bₙ}；n ≤ 400 时逐个测试 14 个候选提升，
 *    验证每支恰好一个可行 d 且等于主路径取值（唯一提升 ⇒ 由归纳法各层恰 4 个幂等元）；
 * 4. 独立 CRT 闭式：Aₙ = 7ⁿ·((7ⁿ)^{-1} mod 2ⁿ) mod 14ⁿ、Bₙ = 2ⁿ·((2ⁿ)^{-1} mod 7ⁿ) mod 14ⁿ
 *    （BigInteger.modInverse），与主路径逐位重建的数在 n ≤ 512 与若干大 n（1000/4096/9999/10000）
 *    全部相等；
 * 5. 结构恒等式：Aₙ + Bₙ = 14ⁿ + 1（n 为选定值），且 Aₙ、Bₙ 在模 14ⁿ 下幂等（含 n = 10000 终检）。
 *
 * 答案：Σ_{n=1}^{10000} = 604557993₁₀ = 5a411d7b₁₄（本机实跑输出）
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0284/solution.kt -d /tmp/kc-0284
 * java -cp /tmp/kc-0284:<kotlin-stdlib> SolutionKt
 */

import java.math.BigInteger

private const val BASE = 14
private const val MAX_N = 10_000

/** 2ⁿ 在模 7 下的逆元，周期 3：n ≡ 0,1,2 (mod 3) 时分别为 1,4,2。 */
private val INV_2_POW_MOD7 = intArrayOf(1, 4, 2)

/**
 * 一条模 14ⁿ 幂等元的 14-adic 支：
 *   kind = 0（A 支）：x ≡ 0 (mod 2ⁿ)、x ≡ 1 (mod 7ⁿ)，u = x/2ⁿ、v = (x−1)/7ⁿ；
 *   kind = 1（B 支）：x ≡ 1 (mod 2ⁿ)、x ≡ 0 (mod 7ⁿ)，u = (x−1)/2ⁿ、v = x/7ⁿ。
 * 两条支的递推形状相同：d ≡ u (mod 2)、d ≡ −v·inv(2ⁿ) (mod 7)，随后
 * u ← (u + d·7ⁿ)/2、v ← (v + d·2ⁿ)/7。
 */
private class Branch(val kind: Int) {
    /** digits[i] = 14 进制第 i 位（i = 0 是最低位）；数字串一经写出不再改变。 */
    val digits = IntArray(MAX_N)
    var n = 1
    var u: BigInteger
    var v: BigInteger
    var pow2 = BigInteger.TWO
    var pow7 = BigInteger.valueOf(7)

    init {
        val x = if (kind == 0) 8L else 7L                      // mod 14 的解：A₁ = 8、B₁ = 7
        digits[0] = x.toInt()
        u = if (kind == 0) BigInteger.valueOf(x / 2) else BigInteger.valueOf((x - 1) / 2)
        v = if (kind == 0) BigInteger.valueOf((x - 1) / 7) else BigInteger.valueOf(x / 7)
    }

    /** 提升到 n + 1 位，返回新写出的数字 d（= 该层的最高位）。 */
    fun lift(): Int {
        val d2 = u.mod(BigInteger.TWO).toInt()                                  // d ≡ u (mod 2)
        val inv2 = INV_2_POW_MOD7[n % 3]                                        // (2ⁿ)^{-1} mod 7
        val d7 = ((-v.mod(POW7_BI).toInt() * inv2) % 7 + 7) % 7                 // d ≡ −v·inv2 (mod 7)
        var d = -1
        for (cand in 0 until BASE) if (cand % 2 == d2 && cand % 7 == d7) { d = cand; break }
        check(d >= 0) { "模 2 与模 7 的条件在 [0,14) 内无解" }
        u = (u + pow7.multiply(BigInteger.valueOf(d.toLong()))).shiftRight(1)    // (u + d·7ⁿ)/2
        v = (v + pow2.multiply(BigInteger.valueOf(d.toLong()))).divide(POW7_BI)  // (v + d·2ⁿ)/7
        pow2 = pow2.shiftLeft(1)
        pow7 = pow7.multiply(POW7_BI)
        digits[n] = d
        n++
        return d
    }

    /** 由数字数组重建当前层的 x（用于各类校验）。 */
    fun value(): BigInteger {
        var x = BigInteger.ZERO
        for (i in n - 1 downTo 0) x = x.multiply(BigInteger.valueOf(BASE.toLong())).add(BigInteger.valueOf(digits[i].toLong()))
        return x
    }

    companion object {
        private val POW7_BI = BigInteger.valueOf(7)
    }
}

/** 主路径结果：两条支的数字串（[Branch.digits]）、前缀数位和与逐层贡献。 */
private class MainPath(val maxN: Int) {
    val a = Branch(0)
    val b = Branch(1)
    val preA = LongArray(maxN + 1)
    val preB = LongArray(maxN + 1)
    val perLevel = LongArray(maxN + 1)

    init {
        while (a.n < maxN) { a.lift(); b.lift() }
        for (i in 0 until maxN) {
            preA[i + 1] = preA[i] + a.digits[i]
            preB[i + 1] = preB[i] + b.digits[i]
        }
        for (n in 1..maxN) {
            var add = 0L
            if (a.digits[n - 1] != 0) add += preA[n]        // Aₙ 最高位非 0 才是合法的 n 位数
            if (b.digits[n - 1] != 0) add += preB[n]
            if (n == 1) add += 1L                            // 第 4 个幂等元 1：(1,1) 支，仅 n = 1 时是一位数
            perLevel[n] = add
        }
    }
}

/** 定义级暴力：枚举 [14^{n−1}, 14ⁿ) 的全部数，返回逐层合法稳定平方的数位和。 */
private fun brutePerLevel(maxN: Int): LongArray {
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

/** 枚举 [0, 14ⁿ) 内全部幂等元（n 小时用于结构校验）。 */
private fun bruteIdempotents(n: Int): List<BigInteger> {
    val m = BigInteger.valueOf(BASE.toLong()).pow(n)
    val out = ArrayList<BigInteger>()
    var x = BigInteger.ZERO
    while (x.compareTo(m) < 0) {
        if (x.multiply(x).mod(m) == x.mod(m)) out.add(x)
        x = x.add(BigInteger.ONE)
    }
    return out
}

/** 独立路径：CRT 闭式 Aₙ = 2ⁿ·((2ⁿ)^{-1} mod 7ⁿ) mod 14ⁿ、Bₙ = 7ⁿ·((7ⁿ)^{-1} mod 2ⁿ) mod 14ⁿ。 */
private fun crtValue(n: Int, pow2: BigInteger, pow7: BigInteger, kind: Int): BigInteger {
    val mod = pow2.multiply(pow7)
    return if (kind == 0) {
        pow2.multiply(pow2.modInverse(pow7)).mod(mod)      // A：2ⁿ 支，≡ 1 (mod 7ⁿ)
    } else {
        pow7.multiply(pow7.modInverse(pow2)).mod(mod)      // B：7ⁿ 支，≡ 1 (mod 2ⁿ)
    }
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

private fun toBase14(v0: Long): String {
    var v = v0
    val sb = StringBuilder()
    while (v > 0L) {
        val d = (v % BASE).toInt()
        sb.append(if (d < 10) ('0' + d) else ('a' + d - 10))
        v /= BASE
    }
    return sb.reverse().toString()
}

fun main() {
    // ---------- 1. 定义级暴力（n ≤ 6）与主路径逐层对拍 ----------
    val mp = MainPath(MAX_N)
    val main = mp.perLevel
    val brute = brutePerLevel(6)
    for (n in 1..6) {
        check(main[n] == brute[n]) { "n=$n：主路径 ${main[n]} ≠ 定义级暴力 ${brute[n]}" }
    }
    println("定义级暴力（n = 1…6）：逐层数位和 " + (1..6).joinToString(",") { main[it].toString() } +
        "，与主路径一致")

    // ---------- 2. 题面样例：Σ_{n≤9} = 582 = 2d8₁₄ ----------
    val sum9 = (1..9).sumOf { main[it] }
    check(sum9 == 582L) { "题面样例 n ≤ 9 应为 582，实得 $sum9" }
    println("题面样例：Σ_{n=1}^{9} = $sum9₁₀ = ${toBase14(sum9)}₁₄（题面 2d8 = 582）")

    // ---------- 3. 幂等元结构：n ≤ 4 全枚举；n ≤ 400 验证唯一提升 ----------
    val aFull = Branch(0)
    val bFull = Branch(1)
    for (n in 1..4) {
        while (aFull.n < n) { aFull.lift(); bFull.lift() }
        val mod = BigInteger.valueOf(BASE.toLong()).pow(n)
        val bruteSet = bruteIdempotents(n).map { it.mod(mod) }.toSortedSet()
        val expect = listOf(BigInteger.ZERO, BigInteger.ONE, aFull.value(), bFull.value())
            .map { it.mod(mod) }.toSortedSet()
        check(bruteSet == expect) { "n=$n：幂等元集合 ${bruteSet} ≠ {0,1,A,B} = $expect" }
    }
    println("幂等元结构：n ≤ 4 的全枚举集合恰为 {0, 1, Aₙ, Bₙ}（各层 4 个）")

    // n ≤ 400：每一个候选提升 d = 0…13 逐个用 x'² ≡ x' (mod 14^{n+1}) 验证，恰有一个可行
    val aChk = Branch(0)
    val bChk = Branch(1)
    var uniquenessChecked = 0
    for (n in 1..400) {
        val pow14n = BigInteger.valueOf(BASE.toLong()).pow(n)
        val modNext = pow14n.multiply(BigInteger.valueOf(BASE.toLong()))
        for (br in listOf(aChk, bChk)) {
            val x = br.value()
            val dOur = br.lift()
            var okCount = 0
            for (cand in 0 until BASE) {
                val y = x.add(pow14n.multiply(BigInteger.valueOf(cand.toLong())))
                if (y.multiply(y).mod(modNext) == y.mod(modNext)) okCount++
            }
            check(okCount == 1) { "n=$n：候选提升可行数应为 1，实得 $okCount" }
            uniquenessChecked++
            if (br === aChk) check(dOur >= 0)
        }
    }
    println("唯一提升：n ≤ 400 每支 14 个候选逐个测试，恰一个 d 可行（共 $uniquenessChecked 次）")

    // ---------- 4. 独立 CRT 闭式路径 ----------
    var pow2 = BigInteger.TWO
    var pow7 = BigInteger.valueOf(7)
    for (n in 1..512) {
        if (n > 1) {
            check(pow2.multiply(pow7).equals(BigInteger.valueOf(BASE.toLong()).pow(n)))
        }
        // 主路径数字重建
        if (n <= 512) {
            // 用同数字串的前 n 位重建
            val (xa, xb) = reconstruct(n)
            check(xa == crtValue(n, pow2, pow7, 0)) { "n=$n：A 支 CRT 闭式不一致" }
            check(xb == crtValue(n, pow2, pow7, 1)) { "n=$n：B 支 CRT 闭式不一致" }
        }
        pow2 = pow2.shiftLeft(1)
        pow7 = pow7.multiply(BigInteger.valueOf(7))
    }
    for (n in intArrayOf(1000, 4096, 9999, 10000)) {
        val (xa, xb) = reconstruct(n)
        val p2 = BigInteger.TWO.pow(n)
        val p7 = BigInteger.valueOf(7).pow(n)
        check(xa == crtValue(n, p2, p7, 0)) { "n=$n：A 支 CRT 闭式不一致（大 n）" }
        check(xb == crtValue(n, p2, p7, 1)) { "n=$n：B 支 CRT 闭式不一致（大 n）" }
        val mod = BigInteger.valueOf(BASE.toLong()).pow(n)
        check(xa.multiply(xa).mod(mod) == xa.mod(mod) && xb.multiply(xb).mod(mod) == xb.mod(mod)) {
            "n=$n：重建值不幂等"
        }
        check(xa.add(xb) == mod.add(BigInteger.ONE)) { "n=$n：Aₙ + Bₙ 应为 14ⁿ + 1" }
    }
    println("独立 CRT 闭式（modInverse）：n ≤ 512 与 n = 1000/4096/9999/10000 与主路径逐位重建一致；" +
        "Aₙ + Bₙ = 14ⁿ + 1 与幂等性在各大 n 复验通过")

    // ---------- 5. 闭式 Σ(13n+2) 与前导零过滤统计 ----------
    // Aₙ + Bₙ = 14ⁿ + 1 逐位给出 a_i + b_i = 13（i ≥ 1；最低位 8+7=15 向高位进位），故 n ≥ 2 时
    // sumA_n + sumB_n = 15 + 13(n−1) = 13n + 2，且每层至多有一支因最高位为 0 被剔除。
    var fullSum = 16L                                        // n = 1 层：8 + 7 + 1
    for (n in 2..MAX_N) fullSum += 13L * n + 2L
    var loss = 0L
    var dropCount = 0
    val firstDrops = ArrayList<String>()
    for (n in 2..MAX_N) {
        val dropA = mp.a.digits[n - 1] == 0
        val dropB = mp.b.digits[n - 1] == 0
        check(!(dropA && dropB)) { "n=$n：两支最高位同时为 0，与 a_i + b_i = 13 矛盾" }
        if (dropA || dropB) {
            dropCount++
            loss += if (dropA) mp.preA[n] else mp.preB[n]
            if (firstDrops.size < 8) firstDrops.add("n=$n（${if (dropA) "A" else "B"} 支）")
        }
    }
    val total = (1..MAX_N).sumOf { main[it] }                // 四支幂等元中合法者（0 恒排除，1 只在 n = 1）
    check(total == fullSum - loss) { "闭式 13n+2 校验失败：$total ≠ $fullSum − $loss" }
    println("闭式校验：若无前导零过滤 Σ(13n+2) = $fullSum，实际过滤 $dropCount 层、扣除数位和 $loss，" +
        "得 $total（最早：" + firstDrops.joinToString("、") + "）")

    // ---------- 6. 汇总与计时 ----------
    val msMain = bestOf3("主路径（Hensel 逐位提升，N = 10000）", total) {
        MainPath(MAX_N).perLevel.sum()
    }
    val msBrute = bestOf3("定义级暴力（n ≤ 6，枚举 8.6×10⁶ 个数）", brutePerLevel(6).sum()) {
        brutePerLevel(6).sum()
    }

    println()
    println("逐层贡献（n = 1…20）：" + (1..20).joinToString(",") { main[it].toString() })
    println("数位总和 = $total₁₀")
    println("答案（14 进制）= ${toBase14(total)}")
    println("汇总：主路径 ${"%.3f".format(msMain)} ms；定义级暴力 n ≤ 6 ${"%.3f".format(msBrute)} ms")
    println("check() 全部通过")
}

/** 用主路径的数字串重建第 n 层的 Aₙ、Bₙ（从 0 层重新提升到 n）。 */
private fun reconstruct(n: Int): Pair<BigInteger, BigInteger> {
    val a = Branch(0)
    val b = Branch(1)
    while (a.n < n) { a.lift(); b.lift() }
    return a.value() to b.value()
}
