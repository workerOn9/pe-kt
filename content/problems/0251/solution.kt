#!/usr/bin/env kotlin
/**
 * Project Euler 251 — Cardano Triplets（卡尔达诺三元组）
 *
 * 思路：
 *   设 x = ∛(a+b√c)、y = ∛(a−b√c)（实三次根）。由 x³+y³ = 2a、xy = ∛(a²−b²c)
 *   与恒等式 (x+y)³ = x³+y³+3xy(x+y)，条件 x+y = 1 等价于
 *
 *       1 = 2a + 3·∛(a²−b²c)
 *       ⟺ a² − b²c = ((1−2a)/3)³
 *       ⟺ 27b²c = 27a² + (2a−1)³ = (a+1)²(8a−1) .
 *
 *   右边 ≡ 0 (mod 3)：无论 3 | (a+1) 还是 3 | (8a−1)，都给出 a ≡ 2 (mod 3)。
 *   记 a = 3m−1（m ≥ 1），方程化为纯整数的
 *
 *       b²c = m²(8m−3) .
 *
 *   反向亦成立：若 a = 3m−1 且 b²c = m²(8m−3)，则 a²−b²c = −(2m−1)³，
 *   而 (1−2a)/3 = −(2m−1)，条件自动满足。于是问题等价于计数
 *   (m,b,c)：m,b,c ≥ 1、b²c = m²(8m−3)、a+b+c = 3m−1+b+c ≤ N。
 *
 *   双射参数化：取 p = gcd(b,m)，b = pr、m = pq（gcd(q,r) = 1），代入得 r² | 8m−3；
 *   写 s = (8m−3)/r²（r 必为奇数，因为 8m−3 是奇数），于是
 *
 *       a = 3pq−1，  b = pr，  c = sq²，  8pq = sr²+3，  约束 p(3q+r) + sq² ≤ N+1 = L。
 *
 *   反过来任一组满足 8pq = sr²+3（r 奇、gcd(q,r)=1）的 (p,q,r,s) 给出合法三元组，
 *   且 p = gcd(b,m) 由三元组唯一确定——这是 (p,q,r,s) ↔ (a,b,c) 的一一对应。
 *
 *   方法 A（主路径）：按 (q,r) 计数 p。
 *     · q 的界：a ≥ 3q−1、b ≥ 1、c ≥ q² ⇒ q²+3q ≤ N ⇒ q ≤ 10488；
 *     · r 的界：p = (3+sr²)/(8q) ≥ r²/(8q)，由 s ≥ 1 得 r³+3qr²+8q³ ≤ 8qL
 *       （循环用这个必要界，命中后再按精确条件过滤）；
 *     · r = 1：s = 8pq−3，约束化为 p(8q³+3q+1) ≤ L+3q²，
 *       一次除法给出 ⌊(L+3q²)/(8q³+3q+1)⌋ 个解；
 *     · r ≥ 3：r 奇且 gcd(q,r)=1 ⇒ gcd(8q,r²)=1，p 在模 r² 下唯一：
 *       p ≡ p₀ = 3·(8q)^{-1} (mod r²)，最小解对应 s₀ = (8p₀q−3)/r²；
 *       p 每加 r²，s 加 8q，总和加 step = r²(3q+r)+8q³，
 *       故贡献 = ⌊(L−base)/step⌋+1（base = p₀(3q+r) + s₀q² ≤ L 时）。
 *     每个 (q,r) 要算 s₀ = (−3/r²) mod 8q。逐对扩展欧几里得是 4.9×10⁷ 次，
 *     实现改用同一模数下的「批量求逆」（前缀积 + 一次扩展欧几里得 + 回代分发），
 *     欧几里得调用降到每个 q 一次。
 *
 *   方法 B（结构完全不同的复核）：按 (r,s) 枚举。
 *     · 任何解的 m 满足 b+c ≥ 3·(n/4)^{1/3} ≥ 3.2317m（n = m²(8m−3)，AM-GM），
 *       故 6.2317m − 1 ≤ N，m ≤ (N+1)·5/31；
 *     · r 奇、s ≡ 5 (mod 8)（由 8 | sr²+3 推出）唯一确定 m = (sr²+3)/8；
 *     · 用最小质因子筛分解 m，枚举 m 的全部约数 p，
 *       检查 gcd(m/p, r) = 1 与 3m−1 + pr + s(m/p)² ≤ N。
 *
 * 旁证：
 *   · 高精度（50 位十进制）数值校验：a,b,c ≤ 12 的 1728 组全部对拍，
 *     「27b²c = 27a²+(2a−1)³」与原始三次根式方程判定完全一致；
 *   · 题面样例：O(N²) 暴力枚举（只用整数乘除，不碰参数化）给出 a+b+c ≤ 1000 时 149 个；
 *   · N = 1000/2000/5000 暴力 = 方法 A = 方法 B；N = 10⁴…10⁷ 与 1.1×10⁸ 两法逐一相等；
 *   · 完整规模两条路径均为 18946051（公开答案表同为 18946051，见 analysis.md）。
 *
 * 答案：18946051
 * 复杂度：方法 A ≈ 4.9×10⁷ 个 (q,r) 对（约 8.4×10⁴ 对命中）+ 每 q 一次模逆；
 *         方法 B ≈ 2.2×10⁷ 个 (r,s) 对 + 17.7×10⁶ 的最小质因子筛与约数枚举。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import java.math.BigDecimal
import java.math.MathContext

private const val N_LIMIT = 110_000_000L
private const val EXPECTED = 18_946_051L

// ------------------------------------------------------------------ 基础工具

/** 整数 gcd（欧几里得算法）。 */
private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}

/** a 模 m 的逆元（要求 gcd(a, m) = 1）。 */
private fun inverseMod(a: Long, m: Long): Long {
    var r0 = a % m
    var r1 = m
    var t0 = 1L
    var t1 = 0L
    while (r1 != 0L) {
        val q = r0 / r1
        val r = r0 - q * r1; r0 = r1; r1 = r
        val t = t0 - q * t1; t0 = t1; t1 = t
    }
    check(r0 == 1L) { "gcd($a, $m) ≠ 1，逆元不存在" }
    val res = t0 % m
    return if (res < 0L) res + m else res
}

// ------------------------------------------------------------------ 判据与高精度数值校验

/** 整数判据：27b²c = 27a²+(2a−1)³（推导见文件头）。 */
private fun criterion(a: Long, b: Long, c: Long): Boolean {
    val m = 2 * a - 1
    return 27 * b * b * c == 27 * a * a + m * m * m
}

private val TWO = BigDecimal(2)
private val THREE = BigDecimal(3)
private val ONE = BigDecimal.ONE

/**
 * BigDecimal 实立方根（牛顿迭代，负号单独处理）。
 * 注意：定点小数下的牛顿迭代可能在末位来回振荡（x 与 nx 永不相等），
 * 因此用「步长小于容差」而不是「完全相等」作为停机条件。
 */
private fun cbrt(v: BigDecimal, mc: MathContext): BigDecimal {
    if (v.signum() == 0) return BigDecimal.ZERO
    val neg = v.signum() < 0
    val a = if (neg) v.negate() else v
    var x = BigDecimal(Math.cbrt(a.toDouble()))
    if (x.signum() <= 0) x = ONE
    val tolerance = BigDecimal("1e-45")
    var iteration = 0
    while (iteration < 100) {
        val nx = x.multiply(TWO).add(a.divide(x.multiply(x, mc), mc)).divide(THREE, mc)
        val step = nx.subtract(x).abs()
        x = nx
        iteration++
        if (step.compareTo(tolerance) < 0) break
    }
    return if (neg) x.negate() else x
}

/** ∛(a+b√c) + ∛(a−b√c) 的高精度数值。 */
private fun cubeRootSum(a: Int, b: Int, c: Int, mc: MathContext): BigDecimal {
    val s = BigDecimal(c).sqrt(mc)
    val t = BigDecimal(b).multiply(s, mc)
    val x = cbrt(BigDecimal(a).add(t, mc), mc)
    val y = cbrt(BigDecimal(a).subtract(t, mc), mc)
    return x.add(y, mc)
}

/**
 * 高精度校验：在 a,b,c ≤ bound 上把所有 (a,b,c) 过一遍，
 * 比较「数值解是否等于 1」与整数判据，两者必须完全一致。
 */
private fun verifyCriterionNumerically(bound: Int) {
    val mc = MathContext(50)
    val eps = BigDecimal("1e-25")
    var triples = 0
    var mismatches = 0
    for (a in 1..bound) for (b in 1..bound) for (c in 1..bound) {
        val sum = cubeRootSum(a, b, c, mc)
        val exactOne = sum.subtract(ONE).abs().compareTo(eps) < 0
        val crit = criterion(a.toLong(), b.toLong(), c.toLong())
        if (exactOne != crit) mismatches++
        if (crit) triples++
    }
    check(mismatches == 0) { "高精度校验出现 $mismatches 处不一致" }
    println(
        "高精度校验（50 位）：a,b,c ≤ $bound 共 ${bound * bound * bound} 组，判据与数值解完全一致" +
            "（其中 $triples 组满足方程）",
    )
}

// ------------------------------------------------------------------ 暴力对照

/**
 * 暴力枚举：直接扫 (a, b)，由判据解出 c = (27a²+(2a−1)³)/(27b²)，
 * 检查整除性与 a+b+c ≤ n。只用整数乘除，不用任何参数化。
 * 复杂度 O(n²)；限制 n ≤ 2×10⁵ 保证中间量不越过 Long。
 */
private fun bruteForceCount(n: Long): Long {
    require(n <= 200_000L) { "暴力法只在小规模使用（n ≤ 2×10⁵）" }
    var count = 0L
    var a = 1L
    while (a + 2 <= n) {
        val m = 2 * a - 1
        val num = 27 * a * a + m * m * m
        var b = 1L
        while (a + b + 1 <= n) {
            val den = 27 * b * b
            if (num % den == 0L) {
                val c = num / den
                if (a + b + c <= n) count++
            }
            b++
        }
        a++
    }
    return count
}

// ------------------------------------------------------------------ 方法 A：(q,r) 枚举 + 等差数列计数

/**
 * 方法 A：对每个满足 gcd(q,r)=1 的奇 r 统计 p ≡ p₀ (mod r²) 的解数，
 * 同一模数 8q 下用批量求逆一次拿到所有 s₀ = (−3/r²) mod 8q。
 */
private fun countByProgression(n: Long): Long {
    val limit = n + 1
    var total = 0L
    val maxR = 40_000
    val rVals = IntArray(maxR)
    val squareMods = IntArray(maxR)
    val prefix = LongArray(maxR + 1)
    var q = 1L
    while (q * q + 3 * q <= n) {
        val q2 = q * q
        val q3 = q2 * q
        val mod = 8 * q
        // r = 1：s = 8pq−3，p(8q³+3q+1) ≤ L+3q²
        total += (limit + 3 * q2) / (8 * q3 + 3 * q + 1)
        // 收集候选 r：奇、gcd(q,r)=1、r³+3qr²+8q³ ≤ 8qL
        var count = 0
        var r = 3L
        while (r * r * r + 3 * q * r * r + 8 * q3 <= 8 * q * limit) {
            if (gcd(r, q) == 1L) {
                rVals[count] = r.toInt()
                squareMods[count] = ((r * r) % mod).toInt()
                count++
            }
            r += 2
        }
        if (count > 0) {
            // 批量求逆：prefix[i] = x₁·x₂·…·x_i (mod 8q)
            prefix[0] = 1L
            for (i in 0 until count) prefix[i + 1] = prefix[i] * squareMods[i] % mod
            var invAll = inverseMod(prefix[count], mod)
            for (i in count - 1 downTo 0) {
                val invX = invAll * prefix[i] % mod
                invAll = invAll * squareMods[i] % mod
                val s0 = mod - 3 * invX % mod            // (−3·x⁻¹) mod 8q ∈ [1, 8q)
                val rv = rVals[i].toLong()
                val r2 = rv * rv
                val step = r2 * (3 * q + rv) + 8 * q3
                if (s0 * step <= 8 * q * limit - 9 * q - 3 * rv) {
                    val p0 = (3 + s0 * r2) / mod
                    val base = p0 * (3 * q + rv) + s0 * q2
                    total += (limit - base) / step + 1
                }
            }
        }
        q++
    }
    return total
}

// ------------------------------------------------------------------ 方法 B：(r,s) 枚举 + 约数分解

/** 1..limit 的最小质因子表（spf[1] = 0）。 */
private fun smallestPrimeFactors(limit: Int): IntArray {
    val spf = IntArray(limit + 1)
    var i = 2
    while (i <= limit) {
        if (spf[i] == 0) {
            spf[i] = i
            if (i <= limit / i) {
                var j = i * i
                while (j <= limit) {
                    if (spf[j] == 0) spf[j] = i
                    j += i
                }
            }
        }
        i++
    }
    return spf
}

/**
 * 方法 B：r 奇、s ≡ 5 (mod 8) 给出 m = (sr²+3)/8；枚举 m 的全部约数 p = gcd(b,m)，
 * 过滤 gcd(m/p, r) = 1 并检查 a+b+c ≤ N。
 */
private fun countByDivisorEnumeration(n: Long): Long {
    // m 的上界：b+c ≥ 3·(m²(8m−3)/4)^{1/3} ≥ 3·(5/4)^{1/3}·m = 3.2317m
    // ⇒ 6.2317m − 1 ≤ N ⇒ m < (N+1)·5/31
    val mMax = (n + 1) * 5 / 31 + 1
    val spf = smallestPrimeFactors(mMax.toInt())
    val primes = IntArray(16)
    val exponents = IntArray(16)
    val divisors = LongArray(4096)
    var total = 0L
    var r = 1L
    while (5 * r * r + 3 <= 8 * mMax) {
        val r2 = r * r
        var s = 5L
        while (s * r2 + 3 <= 8 * mMax) {
            val m = (s * r2 + 3) / 8
            // 用 spf 分解 m
            var count = 0
            var t = m
            while (t > 1L) {
                val p = spf[t.toInt()].toLong()
                var e = 0
                while (t % p == 0L) { t /= p; e++ }
                primes[count] = p.toInt()
                exponents[count] = e
                count++
            }
            // 枚举全部约数
            var size = 1
            divisors[0] = 1L
            for (k in 0 until count) {
                val base = primes[k].toLong()
                val e = exponents[k]
                val cur = size
                var pe = 1L
                repeat(e) {
                    pe *= base
                    for (i in 0 until cur) divisors[size++] = divisors[i] * pe
                }
            }
            for (i in 0 until size) {
                val p = divisors[i]
                val q = m / p
                if (r != 1L && gcd(q % r, r) != 1L) continue
                if (s > n / (q * q)) continue                 // 先防溢出再算 c = sq²
                if (3 * m - 1 + p * r + s * q * q <= n) total++
            }
            s += 8
        }
        r += 2
    }
    return total
}

// ------------------------------------------------------------------ main

fun main() {
    // 1) 判据的高精度数值校验
    verifyCriterionNumerically(12)

    // 2) 小规模：暴力枚举 vs 两条路径
    println("小规模对照（暴力枚举 = O(N²) 整数判据）：")
    for (nn in longArrayOf(1000, 2000, 5000)) {
        val brute = bruteForceCount(nn)
        val a = countByProgression(nn)
        val b = countByDivisorEnumeration(nn)
        check(brute == a && a == b) { "N=$nn：暴力 $brute，方法 A $a，方法 B $b" }
        println("  N = $nn：暴力 = 方法 A = 方法 B = $brute")
    }
    check(bruteForceCount(1000) == 149L) { "题面样例（N=1000 → 149）不符" }
    println("题面样例 a+b+c ≤ 1000 得 149 个，与暴力枚举一致")

    // 3) 中等规模：两条路径逐点对照（暴力在这里已不可行）
    println("中等规模（方法 A vs 方法 B）：")
    for (nn in longArrayOf(10_000, 100_000, 1_000_000, 10_000_000)) {
        val a = countByProgression(nn)
        val b = countByDivisorEnumeration(nn)
        check(a == b) { "N=$nn：方法 A $a ≠ 方法 B $b" }
        println("  N = $nn：方法 A = 方法 B = $a")
    }

    // 4) 完整规模
    val ansA = countByProgression(N_LIMIT)
    val ansB = countByDivisorEnumeration(N_LIMIT)
    println("方法 A（(q,r) 枚举 + 等差数列计数 + 批量求逆）答案 = $ansA")
    println("方法 B（(r,s) 枚举 + 最小质因子筛 + 约数分解）答案 = $ansB")
    check(ansA == ansB) { "完整规模两条路径不一致：$ansA vs $ansB" }
    check(ansA == EXPECTED) { "答案与预期不符：$ansA" }
    println("两条路径一致；答案 = $ansA（a+b+c ≤ $N_LIMIT）")

    // 5) 计时：JIT 预热后 3 轮取最优
    check(countByProgression(N_LIMIT) == ansA)
    var bestA = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(countByProgression(N_LIMIT) == ansA)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestA) bestA = ms
        println("  方法 A 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 A 完整求解：${"%.1f".format(bestA)} ms（3 轮最优，JIT 预热后）")

    check(countByDivisorEnumeration(N_LIMIT) == ansB)
    var bestB = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        check(countByDivisorEnumeration(N_LIMIT) == ansB)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < bestB) bestB = ms
        println("  方法 B 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("方法 B 完整求解：${"%.1f".format(bestB)} ms（3 轮最优，JIT 预热后）")

    println("check() 全部通过")
}
