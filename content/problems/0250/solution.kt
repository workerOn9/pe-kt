#!/usr/bin/env kotlin
/**
 * Project Euler 250 — 250250
 *
 * 思路：
 *   记 f(k) = k^k mod 250。一个子集的和模 250 只取决于「每种余数各取了多少个元素」：
 *   设 c_r = #{k ∈ [1, 250250] : f(k) = r}。在循环群 Z/250 的群环 Z[x]/(x^250 − 1) 里
 *
 *       P(x) = ∏_{r=0}^{249} (1 + x^r)^{c_r} ,
 *
 *   每个因子对应一个具体元素（选它乘 x^r、不选乘 1），P 的 x^0 系数就是「元素和 ≡ 0」
 *   的子集个数（含空集），**答案 = 该系数 − 1**（空集之和为 0，题面要求非空）。
 *   各元素 k^k 严格递增、互不相同，子集与选择方案一一对应，没有同值重复计数问题。
 *
 *   第一步：f(k) 的周期（CRT 结构 + 数值验证）。
 *     250 = 2·5³。mod 2：f(k) ≡ k (mod 2)；mod 125：若 5 | k（此时 k ≥ 5），则 5³ | k^k，
 *     故 f(k) ≡ 0 (mod 125)，配合奇偶性得 f(k) = 0（k ≡ 0 mod 10）或 125（k ≡ 5 mod 10）；
 *     若 5 ∤ k，则指数可按 λ(125) = 100 取模、底数按 125 取模，k^k mod 125 只由
 *     (k mod 125, k mod 100) 决定，即只由 k mod 500 决定。两种情形都是 k mod 500 的函数，
 *     所以 **500 是周期**。数值验证（main 打印）：1..499 的每个候选都在 1..250250 内出现
 *     反例——250 不行，反例 k=2：f(2) = 4 而 f(252) = 246（这正是「250250 = 250·1001
 *     所以周期 250」直觉的陷阱）；500 则在全范围（249750 个位置）无一处违例。
 *     而 250250 = 500·500 + 250：整整 500 个周期，再加前半个周期。
 *
 *   第二步：两种做法（结构互相独立，完整规模结果一致）。
 *     方法 A（逐份移位）：对每个余数 r 的 c_r 个元素逐一份做 dp ← dp + shift_r(dp)。
 *       总量 Σ_r c_r·250 = 250250·250 ≈ 6.3×10^7 次「加法 + 条件减」；原地更新沿
 *       i ↦ i + r (mod 250) 的环（gcd(r, 250) 个环）完成，不需要第二份数组。
 *     方法 B（周期分解 + 群环快速幂）：c_r = 500·a_r + b_r（a_r 取 k = 1..500，b_r 取
 *       k = 1..250），于是 P = (∏_r (1+x^r)^{a_r})^{500} · ∏_r (1+x^r)^{b_r}：
 *       先构造一个周期的多项式（500 次移位），用二进制快速幂升到 500 次方（8 次平方 +
 *       6 次乘法，共 14 次长度 250 的循环卷积），再乘前半周期（250 次移位），
 *       总计约 8×10^5 次模乘。
 *
 *   模 10^16：只要末 16 位。10^16 = 2^16·5^16 不是素数，**不能除、不能求逆元**
 *   （例如按 C(c_r, j) 递推的二项式写法在此走不通）。方法 A 只有加法；方法 B 只有乘法，
 *   用 10^8 进制拆位在 Long 内完成 128 位积的取模，main 里与 BigInteger 随机对拍。
 *
 * 旁证：
 *   · 2^n 全枚举（n = 18/20/22/24/26）与 DP 逐一相等；
 *   · 方法 A 与方法 B 在完整规模（250250 个元素）下答案一致；
 *   · 周期 500 在 1..250250 逐点验证，250 及 1..499 的其余候选都有反例；
 *   · mulMod 与 BigInteger 随机对拍 2000 组通过。
 *
 * 答案：1425480602091519
 * 复杂度：方法 A ≈ 6.3×10^7 次加法；方法 B ≈ 8×10^5 次模乘。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val MOD = 10_000_000_000_000_000L          // 10^16
private const val BASE = 100_000_000L                    // 10^8：mulMod 的拆位基
private const val STATES = 250
private const val PERIOD = 500
private const val N_MAX = 250_250
private const val EXPECTED = 1_425_480_602_091_519L      // 实跑得到，见 main 的 check

// ------------------------------------------------------------------ 基本运算

/** f(k) = k^k mod 250（快速幂；指数不超过 250250） */
private fun fk(k: Int): Int {
    var r = 1
    var b = k % 250
    var e = k
    while (e > 0) {
        if (e and 1 == 1) r = r * b % 250
        b = b * b % 250
        e = e shr 1
    }
    return r
}

/** 朴素定义版 k^k mod 250（乘 k 次），只用于小 k 复核快速幂 */
private fun fkNaive(k: Int): Int {
    var r = 1
    repeat(k) { r = r * (k % 250) % 250 }
    return r
}

/**
 * (a·b) mod 10^16，a、b ∈ [0, 10^16)。
 * 直接相乘是 128 位会溢出：拆成 10^8 进制 a = ah·B + al、b = bh·B + bl，
 * 则 ab ≡ (ah·bl + al·bh)·B + al·bl (mod 10^16)（B² = 10^16 项消掉），
 * 再把中间项对 B 取模后乘回 B，全部落在 Long 内。
 */
private fun mulMod(a: Long, b: Long): Long {
    val ah = a / BASE; val al = a % BASE
    val bh = b / BASE; val bl = b % BASE
    val mid = ((ah * bl + al * bh) % BASE) * BASE
    return (mid + al * bl) % MOD
}

/** base^exp mod 10^16（本题只用到 base = 2） */
private fun powMod16(base: Long, exp: Long): Long {
    var r = 1L
    var b = base % MOD
    var e = exp
    while (e > 0L) {
        if (e and 1L == 1L) r = mulMod(r, b)
        b = mulMod(b, b)
        e = e shr 1
    }
    return r
}

private fun gcdInt(a: Int, b: Int): Int {
    var x = a; var y = b
    while (y != 0) { val t = x % y; x = y; y = t }
    return x
}

/** 1..n 中每个余数出现的次数 */
private fun countsUpTo(n: Int): LongArray {
    val c = LongArray(STATES)
    for (k in 1..n) c[fk(k)]++
    return c
}

// ------------------------------------------------------------------ 群环上的操作

/**
 * dp ← dp · (1 + x^r)，原地：new[i] = old[i] + old[(i − r) mod 250]。
 * i ↦ (i + r) mod 250 是置换，沿它的每个环推进：s、s+r、s+2r、…（共 len = 250/gcd(r,250) 个），
 * 环上首元素的前驱是环尾，先把它存下来，环内单趟扫描即可原地完成。
 */
private fun shiftOnce(dp: LongArray, r: Int, gcd: Int, len: Int) {
    for (s in 0 until gcd) {
        var prev = dp[(s + (len - 1) * r) % STATES]     // 环尾 = 环首 s 的前驱
        var pos = s
        var j = 0
        while (j < len) {
            val cur = dp[pos]
            var v = cur + prev
            if (v >= MOD) v -= MOD
            dp[pos] = v
            prev = cur
            pos += r
            if (pos >= STATES) pos -= STATES
            j++
        }
    }
}

/** dp ← dp · (1 + x^r)^{times}；r = 0 时 (1 + x^0)^{times} = 2^{times} 是整体数乘 */
private fun applyFactor(dp: LongArray, r: Int, times: Long) {
    if (times <= 0L) return
    if (r == 0) {
        val t = powMod16(2L, times)
        for (i in dp.indices) dp[i] = mulMod(dp[i], t)
        return
    }
    val g = gcdInt(r, STATES)
    val len = STATES / g
    var left = times
    while (left > 0L) {
        shiftOnce(dp, r, g, len)
        left--
    }
}

/** 长度 250 的循环卷积（群环乘法），系数全程 mod 10^16 */
private fun ringMul(a: LongArray, b: LongArray): LongArray {
    val out = LongArray(STATES)
    for (i in 0 until STATES) {
        val ai = a[i]
        if (ai == 0L) continue
        var k = i
        for (j in 0 until STATES) {
            val bj = b[j]
            if (bj != 0L) {
                var v = out[k] + mulMod(ai, bj)
                if (v >= MOD) v -= MOD
                out[k] = v
            }
            k++
            if (k == STATES) k = 0
        }
    }
    return out
}

/** 群环元素的二进制快速幂 */
private fun ringPow(p: LongArray, exp: Int): LongArray {
    var result = LongArray(STATES); result[0] = 1
    var base = p
    var e = exp
    while (e > 0) {
        if (e and 1 == 1) result = ringMul(result, base)
        e = e shr 1
        if (e > 0) base = ringMul(base, base)
    }
    return result
}

// ------------------------------------------------------------------ 两种全规模解法

/** 方法 A：c_r 份逐份移位 */
private fun solveByShifts(c: LongArray): Long {
    val dp = LongArray(STATES)
    dp[0] = 1
    for (r in 0 until STATES) applyFactor(dp, r, c[r])
    return (dp[0] - 1 + MOD) % MOD
}

/** 方法 B：c_r = 500·a_r + b_r，P = (∏(1+x^r)^{a_r})^{500} · ∏(1+x^r)^{b_r} */
private fun solveByPeriod(c: LongArray, period: Int, nMax: Int): Long {
    val fullPeriods = nMax / period          // 500 个整周期
    val tail = nMax % period                 // 余 250 个（等于前半周期）
    val a = LongArray(STATES)
    val b = LongArray(STATES)
    for (k in 1..period) a[fk(k)]++
    for (k in 1..tail) b[fk(k)]++
    for (r in 0 until STATES) {
        check(c[r] == fullPeriods.toLong() * a[r] + b[r]) { "周期分解不成立：r = $r" }
    }
    val base = LongArray(STATES); base[0] = 1
    for (r in 0 until STATES) applyFactor(base, r, a[r])
    val poly = ringPow(base, fullPeriods)
    for (r in 0 until STATES) applyFactor(poly, r, b[r])
    return (poly[0] - 1 + MOD) % MOD
}

/** 完整求解（含频次统计），用于计时 */
private fun solveFullA(): Long = solveByShifts(countsUpTo(N_MAX))

private fun solveFullB(): Long = solveByPeriod(countsUpTo(N_MAX), PERIOD, N_MAX)

// ------------------------------------------------------------------ 暴力对照与自检

/**
 * 2^n 全枚举（Gray 码序，每次翻转一个元素），O(1) 维护和的余数，
 * 数非空且和 ≡ 0 (mod 250) 的子集。本题自检只跑到 n = 26（2^26 ≈ 6.7×10^7 个子集）。
 */
private fun bruteCount(n: Int): Long {
    val f = IntArray(n + 1)
    for (k in 1..n) f[k] = fk(k)
    var sum = 0
    var prev = 0
    var cnt = 0L
    val total = 1 shl n
    var m = 1
    while (m < total) {
        val g = m xor (m shr 1)
        val d = g xor prev
        val k = Integer.numberOfTrailingZeros(d) + 1
        if (g and d != 0) {
            sum += f[k]
            if (sum >= 250) sum -= 250
        } else {
            sum -= f[k]
            if (sum < 0) sum += 250
        }
        prev = g
        if (sum == 0) cnt++
        m++
    }
    return cnt
}

/** 周期验证：p = 1..499 全部有反例，p = 500 全范围无违例 */
private fun verifyPeriod() {
    val f = IntArray(N_MAX + 1)
    for (k in 1..N_MAX) f[k] = fk(k)

    var bad250 = -1
    for (k in 1..N_MAX - 250) if (f[k] != f[k + 250]) { bad250 = k; break }
    check(bad250 > 0) { "250 竟是周期？" }

    var bad500 = -1
    for (k in 1..N_MAX - PERIOD) if (f[k] != f[k + PERIOD]) { bad500 = k; break }
    check(bad500 < 0) { "周期 500 在 k = $bad500 失败" }

    var survivors = 0
    for (p in 1 until PERIOD) {
        var ok = true
        var k = 1
        while (k <= N_MAX - p) {
            if (f[k] != f[k + p]) { ok = false; break }
            k++
        }
        if (ok) survivors++
    }
    check(survivors == 0) { "有 $survivors 个 p < 500 在本题范围内没有反例" }
    println(
        "周期验证：250 的首个反例 k = $bad250（f($bad250) = ${f[bad250]}，" +
            "f(${bad250 + 250}) = ${f[bad250 + 250]}）；p = 1..499 全部有反例；" +
            "p = 500 在 1..$N_MAX 无违例 → 最小正周期 500",
    )
}

private fun checkMulMod() {
    val biMod = java.math.BigInteger.valueOf(MOD)
    val rnd = java.util.concurrent.ThreadLocalRandom.current()
    fun ref(a: Long, b: Long): Long =
        java.math.BigInteger.valueOf(a).multiply(java.math.BigInteger.valueOf(b)).mod(biMod).toLong()
    repeat(2000) {
        val a = Math.floorMod(rnd.nextLong(), MOD)
        val b = Math.floorMod(rnd.nextLong(), MOD)
        check(mulMod(a, b) == ref(a, b)) { "mulMod($a, $b) 与 BigInteger 不一致" }
    }
    val edge = longArrayOf(0, 1, 2, BASE - 1, BASE, BASE + 1, MOD / 2, MOD - 1, MOD - BASE)
    for (a in edge) for (b in edge) check(mulMod(a, b) == ref(a, b)) { "mulMod 边界 $a·$b" }
    println("mulMod 与 BigInteger 随机对拍 2000 组 + 边界 ${edge.size}² 组通过")
}

// ------------------------------------------------------------------ main

fun main() {
    // 快速幂与朴素定义的对照
    for (k in 1..PERIOD) check(fk(k) == fkNaive(k)) { "k = $k 快速幂与朴素定义不一致" }
    println("快速幂 f(k) = k^k mod 250 与朴素定义在 k = 1..$PERIOD 上一致")

    checkMulMod()
    verifyPeriod()

    // 小规模：2^n 全枚举 vs DP
    for (n in intArrayOf(18, 20, 22, 24, 26)) {
        val brute = bruteCount(n)
        val dp = solveByShifts(countsUpTo(n))
        check(brute == dp) { "n = $n：暴力 $brute ≠ DP $dp" }
        println("小规模对照 n = $n：2^$n 全枚举 = $brute，DP = $dp，一致")
    }

    // 完整规模：频次统计
    val c = countsUpTo(N_MAX)
    println(
        "频次统计：非零余数 ${c.count { it > 0 }} 个，Σc_r = ${c.sum()}，" +
            "c_0 = ${c[0]}（10 的倍数：${N_MAX / 10} 个），c_125 = ${c[125]}（5 的奇数倍）",
    )

    // 方法 A（主路径）
    val ansA = solveByShifts(c)
    // 方法 B（独立结构复核）
    val ansB = solveByPeriod(c, PERIOD, N_MAX)
    println("方法 A（逐份移位 DP）答案 = $ansA")
    println("方法 B（周期分解 + 群环快速幂）答案 = $ansB")
    check(ansA == ansB) { "两种方法不一致：$ansA vs $ansB" }
    check(ansA == EXPECTED) { "答案与预期不符：$ansA" }
    println("两种方法一致；答案（后 16 位）= ${"%016d".format(ansA)}")

    // 计时：完整求解（频次统计 + DP），JIT 预热后 3 轮取最优
    check(solveFullA() == ansA)
    var bestA = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveFullA() == ansA)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestA) bestA = ms
        println("  方法 A 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 A 完整求解（频次统计 + 逐份移位 DP）：${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    check(solveFullB() == ansB)
    var bestB = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(solveFullB() == ansB)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestB) bestB = ms
        println("  方法 B 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 B 完整求解（频次统计 + 周期分解快速幂）：${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")

    println("check() 全部通过")
}
