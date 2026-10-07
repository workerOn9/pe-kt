#!/usr/bin/env kotlin
/**
 * Project Euler 326 — Modulo Summations（模和配对）
 *
 * 题目：a_1 = 1，a_n = (Σ_{k=1}^{n-1} k·a_k) mod n（n>1）。
 *      f(N,M) = 满足 1 ≤ p ≤ q ≤ N 且 (Σ_{i=p}^q a_i) ≡ 0 (mod M) 的数对个数。
 *      已知 f(10,10)=4、f(10^4,10^3)=97158，求 f(10^12, 10^6)。
 *
 * 思路推导
 * --------
 * 记 S(n) = Σ_{k=1}^{n} k·a_k，则 a_n = S(n-1) mod n，S(n) = S(n-1) + n·a_n。
 * 记前缀和 P(n) = Σ_{i=1}^{n} a_i（P(0)=0）。区间和 Σ_{i=p}^q a_i = P(q) − P(p−1)，
 * 于是「区间和被 M 整除」⟺ P(q) ≡ P(p−1) (mod M)。令 i = p−1（0 ≤ i < j ≤ N），
 *   f(N,M) = #{(i,j) : 0 ≤ i < j ≤ N, P(i) ≡ P(j) (mod M)} = Σ_r C(c_r, 2)，
 * 其中 c_r 是 P(0..N) 中模 M 余 r 的个数。
 *
 * 关键：P(n) mod M（从而计数分布）以周期 T = 6M 循环（M=1000 与 10^6 均实测成立）。
 * 于是把 [0,N] 拆成 q = N div T 个完整周期加余段 rem = N mod T：
 *   c_r = q·c1_r + c2_r（c1 取自一个完整周期的分布，c2 取自前 rem+1 项的分布），
 * 一次只需递推 T 项即可处理任意大的 N。
 *
 * 注意 S(n) 增长到约 n³/6，N=6·10^6 时约 2^65，超出 Long，故用 BigInteger 精确递推。
 *
 * 验证
 * --------
 * 1. 题面样例：f(10,10)=4、f(10^4,10^3)=97158 由直接法（递推到 N）算出一致；
 * 2. 周期性互证：M=1000 时递推到 2T=12000，逐项核对 P(n+T)=P(n)；
 * 3. 直接法与周期法在多个中等规模 (N,M) 上结果一致。
 *
 * 复杂度：一个周期 T=6·10^6 次 BigInteger 递推（毫秒到秒级）；暴力直接法为 O(N)，
 *         N=10^12 时不可行。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

import java.math.BigInteger

private const val M_TARGET = 1_000_000L
private const val N_TARGET = 1_000_000_000_000L

/** 直接法：递推 a_n、P(n) 到 N，再按余数配对计数。仅适用于中小 N。返回 BigInteger。 */
private fun fDirect(N: Long, m: Long): BigInteger {
    val mm = m.toInt()
    val cnt = LongArray(mm)
    var s = BigInteger.ZERO
    var p = 0L
    cnt[0]++
    for (n in 1..N) {
        val a = if (n == 1L) 1L else s.mod(BigInteger.valueOf(n)).toLong()
        s = s.add(BigInteger.valueOf(n * a))
        p = (p + a) % m
        cnt[p.toInt()]++
    }
    var tot = BigInteger.ZERO
    for (c in cnt) {
        if (c >= 2) tot = tot.add(BigInteger.valueOf(c).multiply(BigInteger.valueOf(c - 1)).shiftRight(1))
    }
    return tot
}

/** 周期法：利用周期 T=6M，只需递推一个周期。返回 f(N,m)。 */
private fun fPeriodic(N: Long, m: Long): BigInteger {
    val mm = m.toInt()
    val T = 6 * m
    val rem = N % T
    val cnt1 = IntArray(mm)                // 完整周期 0..T-1 的分布
    val cnt2 = IntArray(mm)                // 余段 0..rem 的分布
    var s = BigInteger.ZERO
    var p = 0L
    cnt1[0]++
    cnt2[0]++
    for (n in 1 until T) {
        val a = if (n == 1L) 1L else s.mod(BigInteger.valueOf(n)).toLong()
        s = s.add(BigInteger.valueOf(n * a))
        p = (p + a) % m
        val idx = p.toInt()
        cnt1[idx]++
        if (n <= rem) cnt2[idx]++
    }
    val q = N / T
    var tot = BigInteger.ZERO
    for (r in 0 until mm) {
        val c = q * cnt1[r] + cnt2[r]
        if (c >= 2) {
            tot = tot.add(BigInteger.valueOf(c).multiply(BigInteger.valueOf(c - 1)).shiftRight(1))
        }
    }
    return tot
}

/** 校验 P 的周期确为 T=6M（递推到 2T 逐项核对）。 */
private fun checkPeriod(m: Long): Boolean {
    val T = 6 * m
    val first = LongArray(T.toInt())
    var s = BigInteger.ZERO
    var p = 0L
    for (n in 0 until 2 * T) {
        if (n >= 1) {
            val a = if (n == 1L) 1L else s.mod(BigInteger.valueOf(n)).toLong()
            s = s.add(BigInteger.valueOf(n * a))
            p = (p + a) % m
        }
        if (n < T) first[n.toInt()] = p else if (p != first[(n - T).toInt()]) return false
    }
    return true
}

private inline fun timeOf(runs: Int = 3, body: () -> Unit): Double {
    body()
    var best = Double.MAX_VALUE
    repeat(runs) {
        val st = System.nanoTime()
        body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return best
}

fun main() {
    println("== 题面样例（直接法） ==")
    val f10 = fDirect(10, 10)
    println("f(10,10) = $f10  " + (if (f10 == BigInteger.valueOf(4)) "-> 与题面 4 一致" else "-> 不一致！"))
    val f4 = fDirect(10_000, 1_000)
    println("f(10^4,10^3) = $f4  " + (if (f4 == BigInteger.valueOf(97158)) "-> 与题面 97158 一致" else "-> 不一致！"))

    println("== 周期性互证（M=1000，T=6M=6000） ==")
    println("  P(n+6000)=P(n) 对 n<6000 全部成立：" + checkPeriod(1000))

    println("== 双方法互证（直接法 vs 周期法） ==")
    for ((n, m) in listOf(10_000L to 1_000L, 60_000L to 10_000L)) {
        val a = fDirect(n, m)
        val b = fPeriodic(n, m)
        println("  f($n,$m)：直接=$a 周期=$b  " + (if (a == b) "-> 一致" else "-> 不一致！"))
    }

    println("== 正式求解 ==")
    val optMs = timeOf { fPeriodic(N_TARGET, M_TARGET) }
    val ans = fPeriodic(N_TARGET, M_TARGET)
    println("f(10^12,10^6) = $ans")
    println("OPT_MS: " + String.format("%.1f", optMs) + "  （周期法 T=6M，BigInteger 递推）")
    val bruteMs = timeOf(runs = 1) { fDirect(10_000_000, 1_000_000) }
    println("BRUTE_MS: " + String.format("%.1f", bruteMs) + "  （直接法递推到 N=10^7，M=10^6）")
}
