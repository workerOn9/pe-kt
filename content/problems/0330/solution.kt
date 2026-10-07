#!/usr/bin/env kotlin
/**
 * Project Euler 330 — Euler's Number（欧拉常数递推）
 *
 * 题目：a(n)=1 (n<0)，a(n)=Σ_{i≥1} a(n−i)/i! (n≥0)。已知 a(n)=(A(n)e+B(n))/n!，
 *      且 a(10)=(328161643e−652694486)/10!。求 A(10⁹)+B(10⁹) mod 77777777。
 *
 * 思路推导
 * --------
 * 记 c(n) = n!·a(n) = A(n)e + B(n)。把 a 的递推两边乘 n!：
 *   c(n) = Σ_{i=1}^{n} C(n,i) c(n−i) + R(n),   R(n) = Σ_{i>n} n!/i! = n!·e − Σ_{i=0}^{n} n!/i!。
 * 分离 e 系数与常数项：
 *   A(n) = Σ_{i<n} C(n,i) A(i) + n!,      B(n) = Σ_{i<n} C(n,i) B(i) − Σ_{i=0}^{n} n!/i!。
 * 对 A 取二项变换 T_A(n)=Σ_{i≤n}C(n,i)A(i)：EGF 上 T̂_A = eˣ·Â，而 2A(n)=T_A(n)+n!，得
 *   2Â − eˣÂ = 1/(1−x)  ⟹  Â(x) = 1/((1−x)(2−eˣ))。
 * 同法得 B̂ = −eˣÂ，即 B(n) = −T_A(n) = n! − 2A(n)。于是关键恒等式
 *   A(n) + B(n) = n! − A(n)。                          （直接由 a(10) 样例佐证）
 *
 * 计算 A(n)：把 Â 写成 (1/(1−x))·(1/(2−eˣ))。1/(2−eˣ)=Σ_m (eˣ−1)^m 的 EGF 系数是 F(m)——
 * 有序贝尔数（Fubini 数），满足 (eˣ−1)^m = m!Σ_n S2(n,m)xⁿ/n!。又 1/(1−x)=Σ_r x^r，展开得
 *   A(n) = Σ_{d=0}^{n} (n)_d · F(n−d),     (n)_d = n(n−1)…(n−d+1)。
 * 模 M = 77777777 = 7·11·73·101·137（无平方因子）时，M | 137!，而 (n)_d = d!·C(n,d)，
 * 故 d ≥ 137 的项全部 ≡ 0 (mod M)；n = 10⁹ 又使 n! ≡ 0。于是
 *   A(10⁹) ≡ Σ_{d=0}^{136} (10⁹)_d F(10⁹−d)  (mod M),  答案 ≡ −A(10⁹)  (mod M)。
 *
 * 大下标 Fubini 数 mod p：F(m)=Σ_{k≥0} k!·S2(m,k)，而 p | k! (k ≥ p)，所以
 *   F(m) ≡ Σ_{k=0}^{p-1} k! S2(m,k) = Σ_{j=0}^{p-1} j^m · c_j,  c_j = Σ_{k=j}^{p-1}(−1)^{k−j}C(k,j)。
 * 对每个素因子 p 算出 F(10⁹−d) mod p，再用 CRT 合回 mod M。
 *
 * 验证
 * --------
 * 1. 题面样例：A(10)=328161643、B(10)=−652694486（由本方法直接算出）；
 * 2. 双方法互证：N ∈ {500, 1000, 2000, 4000} 上「(n)_d·F(n−d) 截断公式」与
 *    「O(n²) 直接按 A(n)=Σ_{i<n}C(n,i)A(i)+n! 递推」模 M 逐一相等；
 * 3. Fubini 递推 F(n)=Σ_{i<n}C(n,i)F(i) 与 Stirling 公式在小 n 上一致（隐含在 2 中）。
 *
 * 复杂度：快速法 = 137 项 × 5 个素数 × O(p·log n) 次幂运算 ≈ 10⁵ 次，微秒级；
 *         暴力对照为 O(N²) 二项递推（N=4000 时约 1.6·10⁷ 次）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *      （本机无 kotlinc，用 scripts/kotlinc-shim.sh 编译运行）
 */

private const val MOD = 77_777_777L
private val PRIMES = intArrayOf(7, 11, 73, 101, 137)

/** 题面规模。 */
private const val N_TARGET = 1_000_000_000L

/** 截断项数：d = 0..136（137 项）。 */
private const val D_MAX = 136

private fun powMod(base0: Long, exp0: Long, mod: Long): Long {
    var base = base0 % mod
    var exp = exp0
    var r = 1L
    while (exp > 0) {
        if (exp and 1L == 1L) r = r * base % mod
        base = base * base % mod
        exp = exp shr 1
    }
    return r
}

/** 每个素数 p 的系数 c_j，使 F(m) ≡ Σ_j c_j·j^m (mod p)。 */
private fun coeffs(p: Int): LongArray {
    val binom = Array(p) { LongArray(p) }
    for (k in 0 until p) {
        binom[k][0] = 1L
        for (j in 1..k) binom[k][j] = (binom[k - 1][j - 1] + binom[k - 1][j]) % p
    }
    val c = LongArray(p)
    for (j in 0 until p) {
        var s = 0L
        for (k in j until p) {
            val t = binom[k][j]
            s = if (((k - j) and 1) == 0) (s + t) % p else (s - t + p) % p
        }
        c[j] = s
    }
    return c
}

/** F(m) mod p。约定 F(0)=1（0⁰ 项）。 */
private fun fubiniModP(m: Long, p: Int, c: LongArray): Long {
    if (m == 0L) return 1L % p
    var s = 0L
    for (j in 1 until p) {
        s = (s + c[j] * powMod(j.toLong(), m, p.toLong())) % p
    }
    return s
}

/** CRT：把对每个素数的余数合回 mod MOD。 */
private fun crt(residues: LongArray): Long {
    var x = 0L
    for (i in PRIMES.indices) {
        val p = PRIMES[i].toLong()
        val mi = MOD / p
        val inv = powMod(mi % p, p - 2, p)
        x = (x + residues[i] * mi % MOD * inv) % MOD
    }
    return x
}

private fun fubiniModM(m: Long, cs: Array<LongArray>): Long {
    val residues = LongArray(PRIMES.size)
    for (i in PRIMES.indices) residues[i] = fubiniModP(m, PRIMES[i], cs[i])
    return crt(residues)
}

/** 快速法：A(n) mod MOD。 */
private fun fastA(n: Long, cs: Array<LongArray>): Long {
    val ff = LongArray(D_MAX + 1)
    ff[0] = 1L
    for (d in 1..D_MAX) ff[d] = ff[d - 1] * (((n - (d - 1)) % MOD + MOD) % MOD) % MOD
    var a = 0L
    for (d in 0..D_MAX) a = (a + ff[d] * fubiniModM(n - d, cs)) % MOD
    return a
}

/** 暴力法：按 A(n)=Σ_{i<n}C(n,i)A(i)+n! 递推到 N，返回 A(0..N) mod MOD。 */
private fun bruteA(N: Int): LongArray {
    val a = LongArray(N + 1)
    var cur = LongArray(N + 1) // C(k,·)
    cur[0] = 1L
    var fact = 1L // k!
    for (k in 0..N) {
        var s = 0L
        for (i in 0 until k) s = (s + cur[i] * a[i]) % MOD
        a[k] = (s + fact) % MOD
        // 更新 Pascal 行到 k+1
        if (k < N) {
            val next = LongArray(N + 1)
            for (i in 0..k + 1) {
                next[i] = ((if (i <= k) cur[i] else 0L) + (if (i >= 1) cur[i - 1] else 0L)) % MOD
            }
            cur = next
            fact = fact * (k + 1) % MOD
        }
    }
    return a
}

/** 精确（无模）小规模递推，用于题面样例自检。数值小时不会溢出。 */
private fun exactA(N: Int): LongArray {
    val a = LongArray(N + 1)
    var cur = LongArray(N + 1)
    cur[0] = 1L
    var fact = 1L
    for (k in 0..N) {
        var s = 0L
        for (i in 0 until k) s += cur[i] * a[i]
        a[k] = s + fact
        if (k < N) {
            val next = LongArray(N + 1)
            for (i in 0..k + 1) {
                next[i] = (if (i <= k) cur[i] else 0L) + (if (i >= 1) cur[i - 1] else 0L)
            }
            cur = next
            fact *= (k + 1)
        }
    }
    return a
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
    val cs = Array(PRIMES.size) { coeffs(PRIMES[it]) }

    println("== 题面自检 ==")
    val exact = exactA(10)
    val a10Exact = exact[10]
    val b10Exact = 3628800L - 2L * a10Exact
    val a10Fast = fastA(10, cs)
    println("  A(10) = " + a10Exact + "  " + (if (a10Exact == 328161643L) "-> 与题面一致" else "-> 不一致！"))
    println("  B(10) = " + b10Exact + "  " + (if (b10Exact == -652694486L) "-> 与题面一致" else "-> 不一致！"))
    println("  快速公式 A(10) mod M = " + a10Fast + "  " + (if (a10Fast == 328161643L % MOD) "-> 与精确一致" else "-> 不一致！"))

    println("== 双方法互证（截断公式 vs O(n²) 递推） ==")
    val brute = bruteA(4000)
    var ok = true
    for (n in intArrayOf(500, 1000, 2000, 4000)) {
        val f = fastA(n.toLong(), cs)
        val eq = f == brute[n]
        if (!eq) ok = false
        println("  A(" + n + ") 快速 = " + f + " / 暴力 = " + brute[n] + "  " + (if (eq) "-> 一致" else "-> 不一致！"))
    }
    println("  全部一致: " + ok)

    println("== 正式求解 ==")
    val opt = timeOf { (MOD - fastA(N_TARGET, cs)) % MOD }
    println("A(10^9) mod M = " + fastA(N_TARGET, cs))
    println("答案 A(10^9)+B(10^9) mod M = " + opt.first)
    println("OPT_MS: " + String.format("%.3f", opt.second) + "  （137 项截断 + Stirling/CRT）")

    val bres = timeOf(runs = 1) { bruteA(4000)[4000] }
    println("BRUTE_MS: " + String.format("%.1f", bres.second) + "  （O(n²) 二项递推，N=4000）")
}
