#!/usr/bin/env kotlin
/**
 * Project Euler 319 — Bounded Sequences（有界数列）
 *
 * 题目：x₁ = 2，x₁ < x₂ < … < xₙ，且对一切 i、j 有 xᵢʲ < (xⱼ+1)ⁱ。记 t(n) 为长度 n 的这类
 *      数列个数。已知 t(10) = 86195、t(20) = 5227991891，求 t(10¹⁰) mod 10⁹。
 *
 * 思路推导
 * --------
 * 条件 xᵢʲ < (xⱼ+1)ⁱ 对所有 i、j 成立，等价于
 *
 *   maxᵢ xᵢ^{1/i}  <  minⱼ (xⱼ+1)^{1/j} =: F 。
 *
 * 固定 F 之后每个 xⱼ 被唯一确定：xⱼ ≤ Fʲ 且 xⱼ + 1 > Fʲ，故
 * xⱼ = ⌊Fʲ⌋（Fʲ 为整数时也取 xⱼ = Fʲ）。而 F = maxⱼ xⱼ^{1/j} 必被某一项取到，
 * 于是 F = m^{1/i}，即 F 形如 c^{1/b}，且 b 是它的「最小分母」。
 *
 * 于是长度 n 的合法数列与「b ≤ n、(b,c) 为最小分母表示、c ∈ [2^b, 3^b) 的整数」
 * 一一对应，故
 *
 *   t(n) = Σ_{b=1}^{n} N(b)，N(b) = #{c ∈ [2^b,3^b) : 对 b 的每个素因子 p，c 不是完全 p 次幂}。
 *
 * 容斥给出 N(b) = Σ_{q | rad(b)} μ(q)·(3^{b/q} − 2^{b/q})。把双重和换序（b = q·m）：
 *
 *   t(n) = Σ_{q 无平方因子} μ(q)·W(⌊n/q⌋)，W(m) = Σ_{x≤m}(3ˣ − 2ˣ) = (3^{m+1}−3)/2 − (2^{m+1}−2)。
 *
 * 快速算法：按 q 的「整除分块」只有 O(√n) 个不同的 ⌊n/q⌋，而系数是 μ 的前缀和
 * M(x)，用杜教筛（M(x) = 1 − Σ_{i=2}^{x} M(⌊x/i⌋)，先线性筛出 x ≤ n^{2/3} 的 M，
 * 其余记忆化递归）在 O(n^{2/3}) 内求出。模 10⁹ 下除 2 必须在模 2·10⁹ 里做。
 *
 * 验证
 * --------
 * 1. 题面自检：t(10) = 86195、t(20) = 5227991891；
 * 2. 双方法互证：n ≤ 20000 上「朴素枚举所有 q」与「杜教筛 + 整除分块」两个实现逐一相等；
 * 3. t(10¹⁰) mod 10⁹ = 268457129。
 *
 * 复杂度：杜教筛 O(n^{2/3}) ≈ 5×10⁶ 次运算，秒级；朴素枚举 O(n log n)，
 * n = 20000 时毫秒级，作为对照。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

private const val MOD = 1_000_000_000L
private const val TWO_MOD = 2_000_000_000L

private fun mobiusSieve(n: Int): IntArray {
    val mu = IntArray(n + 1)
    mu[1] = 1
    val primes = IntArray(0)
    val isComp = BooleanArray(n + 1)
    val list = ArrayList<Int>()
    for (i in 2..n) {
        if (!isComp[i]) { list.add(i); mu[i] = -1 }
        for (p in list) {
            if (i.toLong() * p > n) break
            isComp[i * p] = true
            if (i % p == 0) { mu[i * p] = 0; break }
            mu[i * p] = -mu[i]
        }
    }
    return mu
}

private fun powMod(base: Long, exp: Long, mod: Long): Long {
    var b = base % mod
    var e = exp
    var r = 1L % mod
    while (e > 0) {
        if (e and 1L == 1L) r = r * b % mod
        b = b * b % mod
        e = e shr 1
    }
    return r
}

/** W(m) = (3^{m+1}−3)/2 − (2^{m+1}−2) mod 10⁹（模 2·10⁹ 下先减再整除 2）。 */
private fun wMod(m: Long): Long {
    val a = (powMod(3L, m + 1, TWO_MOD) - 3 + TWO_MOD) % TWO_MOD / 2
    val b = (powMod(2L, m + 1, TWO_MOD) - 2 + TWO_MOD) % TWO_MOD
    return ((a - b) % MOD + MOD) % MOD
}

/** 朴素实现：直接对所有无平方因子的 q 求和。 */
private fun tNaive(n: Long): Long {
    val mu = mobiusSieve(n.toInt())
    var tot = 0L
    for (q in 1..n.toInt()) {
        if (mu[q] == 0) continue
        tot = (tot + mu[q].toLong() * wMod(n / q)) % MOD
    }
    return ((tot % MOD) + MOD) % MOD
}
/** 杜教筛 + 整除分块：t(n) mod 10⁹。 */
private fun tFast(n: Long): Long {
    val n0 = (Math.pow(n.toDouble(), 2.0 / 3.0)).toLong() + 10
    val mu = mobiusSieve(n0.toInt())
    val m0 = IntArray(n0.toInt() + 1)
    var s = 0
    for (i in 1..n0.toInt()) { s += mu[i]; m0[i] = s }
    val cache = HashMap<Long, Int>()
    fun mertens(x: Long): Int {
        if (x <= n0) return m0[x.toInt()]
        cache[x]?.let { return it }
        var res = 1L
        var i = 2L
        while (i <= x) {
            val q = x / i
            val j = x / q
            res -= (j - i + 1) * mertens(q).toLong()
            i = j + 1
        }
        val r = res.toInt()
        cache[x] = r
        return r
    }
    var total = 0L
    var i = 1L
    while (i <= n) {
        val w = n / i
        val hi = n / w
        val d = ((mertens(hi) - mertens(i - 1)).toLong() % MOD + MOD) % MOD
        total = (total + d * wMod(w)) % MOD
        i = hi + 1
    }
    return total
}

private inline fun timeOf(runs: Int = 3, body: () -> Long): Pair<Long, Double> {
    body()
    var best = Double.MAX_VALUE
    var r = 0L
    for (k in 0 until runs) {
        val st = System.nanoTime()
        r = body()
        val ms = (System.nanoTime() - st) / 1e6
        if (ms < best) best = ms
    }
    return r to best
}

fun main() {
    println("== 题面自检 ==")
    val t10 = tFast(10)
    val t20 = tFast(20)
    println("  t(10) = " + t10 + "  " + (if (t10 == 86195L) "-> 与题面一致" else "-> 不一致！"))
    println("  t(20) = " + t20 + "  " + (if (t20 == 5227991891L % MOD) "-> 与题面一致" else "-> 不一致！"))
    println("== 双方法互证（朴素枚举 vs 杜教筛 + 整除分块） ==")
    var ok = true
    for (n in longArrayOf(10, 100, 1000, 5000, 20000)) {
        val a = tNaive(n)
        val b = tFast(n)
        ok = ok && a == b
        println("  n=" + n + " : 朴素=" + a + ", 杜教筛=" + b + "  " + (if (a == b) "-> 一致" else "-> 不一致！"))
    }
    println("全部一致：" + ok)
    val res = timeOf { tFast(10_000_000_000L) }
    println("t(10^10) mod 10^9 = " + res.first)
    println("OPT_MS: " + String.format("%.1f", res.second) + "  （杜教筛 + 整除分块）")
    val bres = timeOf { tNaive(20000) }
    println("BRUTE_MS: " + String.format("%.3f", bres.second) + "  （朴素枚举所有 q，n=20000）")
}