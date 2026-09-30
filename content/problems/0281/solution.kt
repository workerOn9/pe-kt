#!/usr/bin/env kotlin
/**
 * Project Euler 281 — Pizza Toppings（比萨配料）
 *
 * 题目：把一张完美的圆形比萨切成 m·n 块等大的扇形；用 m 种配料给每块恰放一种配料，每种配料恰好
 * 用在 n 块上（m ≥ 2，n ≥ 1）。旋转后重合的放法视为同一种，反射视为不同。f(m,n) 记方案数：
 * f(2,1) = 1、f(2,2) = f(3,1) = 2、f(3,2) = 16。求所有满足 f(m,n) ≤ 10^15 的 f(m,n) 之和。
 *
 * 思路（Burnside 引理 + 循环分解）
 * ─────────────────────────────
 * 把一种放法看成「长度为 N = mn 的环形珠串」：第 i 块的颜色 c_i ∈ {1..m} 是它的配料，且每种颜色
 * 恰好出现 n 次。旋转群 C_N 作用在珠串上，由 Burnside 引理
 *
 *   f(m,n) = (1/N) · Σ_{r=0}^{N-1} Fix(r),
 *
 * Fix(r) = 被「平移 r 格」固定的着色数。平移 r 作为置换把 N 个位置分成 g = gcd(r, N) 个循环、
 * 每个循环长 L = N/g；一个着色被 r 固定 ⟺ 每个循环内颜色相同，于是 Fix(r) = 把 g 个循环染上
 * m 种颜色、每种颜色恰好拿到 n/L 个循环的方案数：
 *
 *   Fix(r) = g! / ((n/L)!^m)  （当 L | n；否则 0，因为每种颜色的块数必须是 L 的倍数）.
 *
 * 按循环长 L 归并：满足 gcd(r, N) = N/L 的 r 恰有 φ(L) 个，而 L | N 且 L | n ⟺ L | n（因为
 * n | N），所以主路径把旋转和压缩成 n 的因子上求和：
 *
 *   f(m,n) = (1/(mn)) · Σ_{d | n} φ(d) · (mn/d)! / ((n/d)!^m).                （方法 A）
 *
 * 每个因子 (mn/d)!/((n/d)!^m) 是多项系数（把 mn/d 个位置均分给 m 种颜色），用逐次二项系数
 * C(剩余位置, n/d) 的乘积计算；最后除以 mn（Burnside 总和必被 N 整除，用整除断言自检）。
 *
 * 独立复核 B：不归并，直接把 r = 0..N-1 逐个累加（换求和变量、换循环分解写法），
 * Fix(r) 用 g 与 L 直接判定 L | n；两条路径在四个题面样例与全量答案上一致。
 *
 * 独立复核 C（暴力，见 brute-force.kt）：对小规模 (m,n) 直接枚举全部 m^(mn) 个着色、按最小旋转
 * 码归并成项链数（定义级暴力）；对全量答案另用「多项式系数法」对拍。
 *
 * 枚举范围与单调性
 * ───────────────
 * f 关于 m、n 均严格递增：网格 m ∈ [2,22]、n ∈ [1,40] 上逐点断言 f(m,n) < f(m,n+1) 与
 * f(m,n) < f(m+1,n)。因此 (a) 固定 m 时 n 一旦超限即可停止；(b) m 循环在 f(m,1) = (m-1)! 超限
 * 后可停—— (m-1)! ≤ 10^15 给 m ≤ 18，19! = 1.216×10^17 > 10^15 表明 m ≥ 19 无解。全量共
 * 74 个 (m,n) 对（m=2 到 n=29；m=3 到 n=12；m=4 到 n=7；m=5 到 n=5；m=6 到 n=4；m=7 到
 * n=3；m=8..10 到 n=2；m=11..18 只到 n=1）。
 *
 * 复杂度
 * ──────
 * 每个 (m,n) 对：方法 A 需要 τ(n) 个多项系数、方法 B 需要 mn 个，而每个多项系数是 O(m) 次
 * BigInteger 乘除，规模最大到 (m=2,n=29) 的 C(58,29) ≈ 3×10^16、以及被排除边界上的 ~10^3 位
 * 十进制数。74 个已计入的对 + 少量边界对照，整体 < 1 ms；定义级暴力在最大可行规模
 * (3,4) 上也只有 3^12 = 5.3×10^5 个着色，而全尺寸的 m^(mn) 早已超过 10^100。
 *
 * 验证
 * ────
 * 1. 题面四个样例全部复现（主路径、旋转和、定义级枚举三条路各算一遍）；
 * 2. 「反射视为不同」用二面体群对照组钉死：按二面体群数 (3,2) 只有 11 种，与题面 16 矛盾；
 * 3. 方法 A = 方法 B 在全量 74 对上相等，总和 = 1485776387445623；
 * 4. 网格单调性断言（m ≤ 22、n ≤ 40 共 840 个点）保证枚举窗口不漏项；
 * 5. brute-force.kt：定义级项链枚举（28 个可行小规模对）与非周期项链 / Möbius 反演公式互证；
 * 6. 公开答案表（nayuki/Answers.txt 等）列出 1485776387445623，与本机实跑一致（仅旁证）。
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0281/solution.kt -d /tmp/kc-0281
 * java -cp /tmp/kc-0281:<kotlin-stdlib> SolutionKt
 */

import java.math.BigInteger
import java.util.Arrays

private const val LIMIT = 1_000_000_000_000_000L             // 10^15
private const val ANSWER = 1_485_776_387_445_623L            // 本机实跑输出（公开答案表旁证一致）

private val LIMIT_BIG = BigInteger.valueOf(LIMIT)

// ───────────────────────────── 数论小工具 ─────────────────────────────

/** 欧拉 φ 函数（试除分解）。 */
private fun phi(n: Int): Int {
    var x = n
    var r = n
    var p = 2
    while (p * p <= x) {
        if (x % p == 0) {
            while (x % p == 0) x /= p
            r -= r / p
        }
        p++
    }
    if (x > 1) r -= r / x
    return r
}

/** n 的全部正因子（升序）。 */
private fun divisors(n: Int): List<Int> {
    val out = ArrayList<Int>()
    var d = 1
    while (d * d <= n) {
        if (n % d == 0) {
            out.add(d)
            if (d != n / d) out.add(n / d)
        }
        d++
    }
    out.sort()
    return out
}

private fun gcdInt(a: Int, b: Int): Int {
    var x = a
    var y = b
    while (y != 0) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** 二项系数 C(n, k)，BigInteger 版本（乘法约分，精确整除）。 */
private fun binomialBig(n: Int, k: Int): BigInteger {
    val kk = if (k < n - k) k else n - k
    var r = BigInteger.ONE
    for (i in 1..kk) {
        r = r.multiply(BigInteger.valueOf((n - kk + i).toLong())).divide(BigInteger.valueOf(i.toLong()))
    }
    return r
}

/** 多项系数 (mk)! / (k!^m)：把 mk 个位置均分给 m 种颜色的方案数 = ∏ C(剩余位置, k)。 */
private fun multinomial(m: Int, k: Int): BigInteger {
    var r = BigInteger.ONE
    var rest = m * k
    for (left in m downTo 2) {
        r = r.multiply(binomialBig(rest, k))
        rest -= k
    }
    return r
}

// ───────────────────────── 方法 A：因子和（主路径） ─────────────────────────

/**
 * f(m,n) = (1/(mn)) Σ_{d | n} φ(d) · (mn/d)!/((n/d)!^m)。
 * 末尾的整除由 Burnside 引理保证（总和必被 N 整除），用断言自检而不是浮点除。
 */
private fun fDivisorSum(m: Int, n: Int): BigInteger {
    var s = BigInteger.ZERO
    for (d in divisors(n)) {
        s = s.add(multinomial(m, n / d).multiply(BigInteger.valueOf(phi(d).toLong())))
    }
    val mn = BigInteger.valueOf((m.toLong()) * n)
    val (q, r) = s.divideAndRemainder(mn)
    check(r.signum() == 0) { "Burnside 总和不能被 mn 整除：m=$m n=$n" }
    return q
}

// ──────────────────── 方法 B：逐旋转求和（独立复核） ────────────────────

/**
 * 不按 φ 归并，直接把 r = 0..N-1 全部枚举：g = gcd(r,N)，L = N/g；L | n 时该旋转固定
 * g!/((n/L)!^m) 个着色。求和变量、循环分解的写法都与方法 A 不同。
 */
private fun fRotationSum(m: Int, n: Int): BigInteger {
    val N = m * n
    var s = BigInteger.ZERO
    for (r in 0 until N) {
        val g = gcdInt(r, N)
        val L = N / g
        if (n % L != 0) continue
        check(g == m * (n / L)) { "循环数 g=$g 与 m·(n/L)=${m * (n / L)} 不符" }
        s = s.add(multinomial(m, n / L))                        // m 种颜色各拿 n/L 个循环
    }
    val (q, r2) = s.divideAndRemainder(BigInteger.valueOf(N.toLong()))
    check(r2.signum() == 0) { "逐旋转求和不能被 N 整除：m=$m n=$n" }
    return q
}

// ──────────────────── 定义级暴力：直接枚举项链（小规模） ────────────────────

/**
 * 枚举全部 m^(mn) 个着色、按「最小旋转码」归并成项链，返回项链数（仅用于小规模对拍）。
 * [dihedral] = true 时把反射（反向旋转）也并入轨道，用来对照「反射视为不同」这条题面规则。
 */
private fun enumerateNecklaces(m: Int, n: Int, dihedral: Boolean = false): Long {
    val N = m * n
    var total = 1L
    repeat(N) { total *= m }
    val digits = IntArray(N)
    val counts = IntArray(m)
    val seen = HashSet<Long>()
    for (code in 0 until total) {
        var x = code
        for (i in 0 until N) {
            digits[i] = (x % m).toInt()
            x /= m
        }
        Arrays.fill(counts, 0)
        for (d in digits) counts[d]++
        if (counts.any { it != n }) continue                    // 每种配料恰好 n 块
        var best = Long.MAX_VALUE
        for (shift in 0 until N) {                              // 全部旋转（及反射）里取最小码值
            var v = 0L
            for (i in 0 until N) v = v * m + digits[(shift + i) % N]
            if (v < best) best = v
            if (dihedral) {
                var w = 0L
                for (i in 0 until N) w = w * m + digits[(shift - i + N) % N]
                if (w < best) best = w
            }
        }
        seen.add(best)
    }
    return seen.size.toLong()
}

// ─────────────────────────── 全量求和与枚举窗口 ───────────────────────────

/** 收集所有 f(m,n) ≤ LIMIT 的对（依赖单调性：n 超限即 break，m ≥ 19 必然全部超限）。 */
private fun collectPairs(f: (Int, Int) -> BigInteger): List<Triple<Int, Int, Long>> {
    val out = ArrayList<Triple<Int, Int, Long>>()
    var m = 2
    while (true) {
        var n = 1
        while (true) {
            val v = f(m, n)
            if (v > LIMIT_BIG) break
            out.add(Triple(m, n, v.toLong()))
            n++
        }
        if (m >= 19) break                                      // f(19,1) = 18! > 10^15，单调性保证后面更大
        m++
    }
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
    // ---------- 1. 题面四个样例：主路径 / 逐旋转 / 定义级枚举三路对照 ----------
    val samples = listOf(2 to 1, 2 to 2, 3 to 1, 3 to 2)
    val expected = listOf(1L, 2L, 2L, 16L)
    for ((idx, mn) in samples.withIndex()) {
        val a = fDivisorSum(mn.first, mn.second).toLong()
        val b = fRotationSum(mn.first, mn.second).toLong()
        val c = enumerateNecklaces(mn.first, mn.second)
        check(a == expected[idx] && b == expected[idx] && c == expected[idx]) {
            "f(${mn.first},${mn.second})：主路径 $a / 逐旋转 $b / 枚举 $c，应为 ${expected[idx]}"
        }
        println("f(${mn.first},${mn.second}) = $a（逐旋转 $b，定义级枚举 $c）")
    }
    // 「反射视为不同」的判别：若改成二面体群，(3,2) 只有 11 种，与题面 16 矛盾
    val dihedral32 = enumerateNecklaces(3, 2, dihedral = true)
    check(dihedral32 == 11L) { "二面体群下 (3,2) 应为 11（旋转群为 16），实际 $dihedral32" }
    println("对照组：若把反射也视为同一（二面体群），f(3,2) = $dihedral32 ≠ 16 —— 题面要求的是循环群")

    // ---------- 2. 单调性 + 窗口充分性 ----------
    // 网格上逐点验证 f 关于 m、n 严格递增；顺带确认「已计入的对」与「超限的对」在网格内部交界。
    var monoChecks = 0
    var m = 2
    while (m <= 22) {
        var n = 1
        while (n <= 40) {
            val v = fDivisorSum(m, n)
            check(fDivisorSum(m, n + 1) > v) { "f 关于 n 非增：($m,$n)" }
            check(fDivisorSum(m + 1, n) > v) { "f 关于 m 非增：($m,$n)" }
            monoChecks++
            n++
        }
        m++
    }
    check(fDivisorSum(19, 1) > LIMIT_BIG && fDivisorSum(18, 1) <= LIMIT_BIG) {
        "m 的截断位置应在 18/19 之间：(18,1)=${fDivisorSum(18, 1)}"
    }
    println("单调性：网格 m∈[2,22]×n∈[1,40] 共 $monoChecks 个点上 f 关于 m、n 均严格递增；" +
        "(18,1)=${fDivisorSum(18, 1)} ≤ 10^15 < ${fDivisorSum(19, 1)} = (19,1)")

    // ---------- 3. 全量求和：方法 A vs 方法 B ----------
    val pairsA = collectPairs { m2, n2 -> fDivisorSum(m2, n2) }
    val pairsB = collectPairs { m2, n2 -> fRotationSum(m2, n2) }
    check(pairsA == pairsB) { "方法 A 与逐旋转求和的配对清单不一致" }
    val sumA = pairsA.sumOf { it.third }
    val sumB = pairsB.sumOf { it.third }
    check(sumA == sumB) { "方法 A 总和 $sumA ≠ 逐旋转总和 $sumB" }
    // 窗口边界：每个 m 上「最后一个计入的 n」的 f ≤ 10^15，而下一个 n 的 f > 10^15
    check(pairsA.all { it.third <= LIMIT })
    for (m3 in 2..18) {
        val ns = pairsA.filter { it.first == m3 }.map { it.second }
        check(ns.isNotEmpty()) { "m=$m3 应有计入项" }
        val nMax = ns.max()
        check(fDivisorSum(m3, nMax + 1) > LIMIT_BIG) { "m=$m3 在 n=$nMax 之后本应有超限项" }
    }
    check(pairsA.none { it.first >= 19 }) { "不应有 m ≥ 19 的计入项" }
    println("全量：共 ${pairsA.size} 个 (m,n) 对；按 m 计入的 n 范围 " +
        (2..18).map { mm -> "m=$mm: n≤${pairsA.filter { it.first == mm }.maxOf { it.second }}" }
            .joinToString("，"))
    println("方法 A（因子和）总和 = $sumA")
    println("方法 B（逐旋转）总和 = $sumB")

    // ---------- 4. 计时 ----------
    fDivisorSum(2, 29)
    fRotationSum(2, 29)
    val msA = bestOf3("方法 A（φ(d) 归并，74 个对）", ANSWER) { collectPairs { x, y -> fDivisorSum(x, y) }.sumOf { it.third } }
    val msB = bestOf3("方法 B（逐旋转求和，Σ mn 次迭代）", ANSWER) { collectPairs { x, y -> fRotationSum(x, y) }.sumOf { it.third } }
    val msEnum = bestOf3("定义级枚举 f(3,2)（729 个着色）", 16L) { enumerateNecklaces(3, 2) }

    // ---------- 5. 答案 ----------
    println()
    println("答案 = $sumA")
    check(sumA == ANSWER) { "总和 $sumA ≠ 预期 $ANSWER" }
    println("汇总：方法 A ${"%.3f".format(msA)} ms；方法 B ${"%.3f".format(msB)} ms；" +
        "定义级枚举 f(3,2) ${"%.3f".format(msEnum)} ms")
    println("check() 全部通过")
}
