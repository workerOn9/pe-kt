#!/usr/bin/env kotlin
/**
 * Project Euler 276 — Primitive Triangles（本原整数边三角形）
 *
 * 思路
 * ────
 * 记
 *   T(p) = 周长恰为 p 的整数边三角形数（a ≤ b ≤ c，a + b > c），
 *   F(n) = Σ_{p≤n} T(p) ——周长不超过 n 的全部整数边三角形数，
 *   c(p) = 周长恰为 p 的本原三角形数（gcd(a,b,c) = 1），P(n) = Σ_{p≤n} c(p)（所求）。
 *
 * 每个三角形唯一地写成 g·(a,b,c)，其中 (a,b,c) 本原、g = gcd(a,b,c)；于是按周长做
 * Dirichlet 卷积：T = 1 * c，故 c = μ * T（μ 为 Möbius 函数），求和形式为
 *
 *   P(n) = Σ_{d≤n} μ(d) · F(n/d)。
 *
 * T(p) 的闭式（二次准多项式，周期 2）。固定 a 后 b 需满足 a ≤ b、2b ≤ p−a、2(a+b) > p，
 * 对 a 求和的格点计数给出
 *   T(p) = ⌊(p² + 24)/48⌋             （p 偶），
 *   T(p) = ⌊((p+3)² + 24)/48⌋         （p 奇），
 * 等价地记 u(j) = ⌊(j²+6)/12⌋，则 p = 2m 时 T = u(m)，p = 2m+1 时 T = u(m+2)。
 * 于是 F 也可闭式求和（E(M) = Σ_{j≤M} u(j)）：
 *   F(2K) = E(K) + E(K+1)，F(2K+1) = E(K) + E(K+2)，
 *   E(M) = ( M(M+1)(2M+1)/6 + 6M − 43·⌊M/6⌋ − R(M mod 6) ) / 12，R = (0,7,17,20,30,37)。
 * （推导见 analysis.md；代码里这条闭式与逐项递推互相验证。）
 *
 * 规模与溢出
 * ──────────
 * F(10^7) ≈ 10^21/144 ≈ 6.94×10^18（Long 范围内但已接近上限），
 * 最终答案 ≈ 5.78×10^18。方法 A 的累加用 Math.addExact 守住溢出；
 * 方法 B 全程 BigInteger，绕开一切中间量溢出。复杂度 O(n)。
 *
 * 验证
 * ────
 * 1. 本题题面没有给任何样例，锚点自行建立：直接枚举求 c(p) 与 P(n)（小周长），
 *    并用「枚举全部整数边三角形」核对 T(p)、F(n)（p ≤ 600）；
 * 2. 方法 A（线性筛 μ + 流式 F + 逐 d 求和）与方法 B（翻倍筛 μ + E 闭式 BigInteger +
 *    按商分块求和）在 n = 10^5、10^6、10^7 上一致；
 * 3. F 的两条独立实现（逐项递推 / 闭式公式）在全部分块商点与 5 万个正整数上一致；
 * 4. 暴力对照：直接三重循环枚举 a ≤ b ≤ c、gcd = 1（brute-force.kt 另做 O(n²) 的
 *    对偶计数），在 n ≤ 500 与主路径一致；
 * 5. 公开答案表旁证：5777137137739632912。
 *
 * 答案：5777137137739632912
 *
 * 运行
 * ────
 * bash scripts/kotlinc-shim.sh content/problems/0276/solution.kt -d /tmp/kc-0276
 * java -cp /tmp/kc-0276:<kotlin-stdlib-2.1.21.jar> SolutionKt
 */

import java.math.BigInteger

private const val LIMIT = 10_000_000

// ───────────────────────── 方法 A（主路径） ─────────────────────────

/** 线性筛 μ：mu[i] = μ(i)，i ≥ 1；O(n)。 */
private fun mobiusLinear(n: Int): ByteArray {
    val mu = ByteArray(n + 1)
    val comp = BooleanArray(n + 1)
    val primes = IntArray(700_000)
    var cnt = 0
    mu[1] = 1
    for (i in 2..n) {
        if (!comp[i]) {
            primes[cnt++] = i
            mu[i] = -1
        }
        var j = 0
        while (j < cnt) {
            val p = primes[j]
            val ip = i.toLong() * p
            if (ip > n) break
            val m = ip.toInt()
            comp[m] = true
            if (i % p == 0) { mu[m] = 0; break } else mu[m] = (-mu[i]).toByte()
            j++
        }
    }
    return mu
}

/** 周长恰为 p 的整数边三角形数 T(p)：u(j) = ⌊(j²+6)/12⌋，偶 p = 2m 取 u(m)，奇 p = 2m+1 取 u(m+2)。 */
private fun trianglesAtPerimeter(p: Int): Long {
    if (p < 3) return 0L
    val m = (p / 2).toLong()
    val j = if (p % 2 == 0) m else m + 2
    return (j * j + 6) / 12
}

/**
 * 方法 A：线性筛 μ + 流式推进 F（只有 F(n/d) 单调递增地被用到，无需存 F 表）
 * + 逐 d 求和 P(n) = Σ μ(d)·F(n/d)。累加用 addExact 显式守住 Long 溢出。
 */
private fun solveStream(n: Int): Long {
    val mu = mobiusLinear(n)
    var total = 0L
    var f = 0L
    var k = 0
    var d = n
    while (d >= 1) {
        val q = n / d
        while (k < q) {
            k++
            f += trianglesAtPerimeter(k)
        }
        val m = mu[d].toLong()
        if (m != 0L) total = Math.addExact(total, m * f)
        d--
    }
    return total
}

// ───────────────────────── 方法 B（独立复核） ─────────────────────────

/** μ 的另一种筛法（素数倍数翻转 + 平方数清零），与线性筛实现完全不同。 */
private fun mobiusFlip(n: Int): ByteArray {
    val mu = ByteArray(n + 1) { 1 }
    val comp = BooleanArray(n + 1)
    mu[0] = 0
    for (p in 2..n) {
        if (!comp[p]) {
            var m = p
            while (m <= n) {
                comp[m] = true
                mu[m] = (-mu[m]).toByte()
                m += p
            }
            val p2 = p.toLong() * p
            if (p2 <= n) {
                val pp = p2.toInt()
                var m2 = pp
                while (m2 <= n) {
                    mu[m2] = 0
                    m2 += pp
                }
            }
        }
    }
    return mu
}

/** E(M) = Σ_{j≤M} ⌊(j²+6)/12⌋ 的精确闭式（BigInteger，内部断言可整除 12）。 */
private fun eSum(m: Long): BigInteger {
    if (m <= 0) return BigInteger.ZERO
    val table = longArrayOf(0, 7, 17, 20, 30, 37)
    val big = BigInteger.valueOf(m)
    val s2 = big.multiply(big.add(BigInteger.ONE)).multiply(BigInteger.valueOf(2 * m + 1))
        .divide(BigInteger.valueOf(6))
    val num = s2 + BigInteger.valueOf(6 * m - 43 * (m / 6) - table[(m % 6).toInt()])
    check(num.mod(BigInteger.valueOf(12)) == BigInteger.ZERO) { "E($m) 闭式不整除 12：$num" }
    return num / BigInteger.valueOf(12)
}

/** F(n) = 周长 ≤ n 的整数边三角形数（闭式）。 */
private fun bigF(n: Long): BigInteger {
    if (n < 3) return BigInteger.ZERO
    return if (n % 2 == 0L) {
        val k = n / 2
        eSum(k) + eSum(k + 1)
    } else {
        val k = (n - 1) / 2
        eSum(k) + eSum(k + 2)
    }
}

/** 方法 B：μ 的翻倍筛 + F 闭式 + 按商分块的 BigInteger 精确求和。 */
private fun solveGrouped(n: Int): BigInteger {
    val mu = mobiusFlip(n)
    val pts = sortedSetOf<Int>()
    var d = 1
    while (d <= n) {
        val q = n / d
        val last = n / q
        pts.add(last)
        pts.add(d - 1)
        d = last + 1
    }
    val arr = pts.toIntArray()
    val mertens = HashMap<Int, Long>(arr.size * 2)
    var acc = 0L
    var idx = 0
    for (x in 0..n) {
        acc += mu[x]
        while (idx < arr.size && arr[idx] == x) {
            mertens[x] = acc
            idx++
        }
    }
    check(idx == arr.size) { "Mertens 取样点未全部命中" }
    var total = BigInteger.ZERO
    d = 1
    while (d <= n) {
        val q = n / d
        val last = n / q
        val c = mertens[last]!! - mertens[d - 1]!!
        if (c != 0L) total = total.add(BigInteger.valueOf(c).multiply(bigF(q.toLong())))
        d = last + 1
    }
    return total
}

// ───────────────────────── 暴力对照（小规模） ─────────────────────────

/** 直接枚举全部整数边三角形，统计周长为 p 的个数（验证 T(p)）。 */
private fun bruteT(p: Int): Long {
    var count = 0L
    for (b in 1..p) for (a in 1..b) {
        val c = p - a - b
        if (c >= b && a + b > c && c > 0) count++
    }
    return count
}

/** 直接枚举本原三角形（a ≤ b ≤ c、周长 ≤ limit、gcd = 1）。 */
private fun brutePrimitive(limit: Int): Long {
    var count = 0L
    for (c in 1..limit) for (b in 1..c) for (a in 1..b) {
        if (a + b <= c || a + b + c > limit) continue
        var x = a
        var y = b
        while (y != 0) { val t = x % y; x = y; y = t }
        var z = c
        while (z != 0) { val t = x % z; x = z; z = t }
        if (x == 1) count++
    }
    return count
}

private fun bestOf3(tag: String, expected: Long, f: () -> Long): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val t0 = System.nanoTime()
        val out = f()
        val ms = (System.nanoTime() - t0) / 1e6
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 1. 小规模锚点：T(p)/F(n) 的直接枚举 ----------
    var fSum = 0L
    for (p in 1..600) {
        val t = trianglesAtPerimeter(p)
        check(t == bruteT(p)) { "T($p) = $t ≠ 暴力 ${bruteT(p)}" }
        fSum += t
    }
    println("T(p) 闭式 vs 直接枚举：p = 1…600 全部一致；F(600) = $fSum")

    // ---------- 2. F 两条独立实现互证 ----------
    var fStream = 0L
    for (p in 1..50_000) {
        fStream += trianglesAtPerimeter(p)
        check(fStream == bigF(p.toLong()).toLong()) { "F($p)：递推 $fStream ≠ 闭式 ${bigF(p.toLong())}" }
    }
    println("F 互证：n = 1…50000 的逐项递推与 E(M) 闭式完全一致")

    // ---------- 3. 暴力对照 P(n) ----------
    val bruteLimits = listOf(100, 200, 300, 400, 500)
    for (l in bruteLimits) {
        val b = brutePrimitive(l)
        val a = solveStream(l)
        check(a == b) { "P($l)：主路径 $a ≠ 暴力 $b" }
        println("P($l) = $b（直接枚举本原三角形与主路径一致）")
    }
    // 与 brute-force.kt 路径 1（半朴素容斥）对拍的中等规模控制值
    println("P(20000) = ${solveStream(20_000)}（与 brute-force.kt 路径 1 对拍）")

    // ---------- 4. 两个方法在全规模上互证 ----------
    for (n in listOf(100_000, 1_000_000, LIMIT)) {
        val a = solveStream(n)
        val b = solveGrouped(n).toLong()
        check(a == b) { "n=$n：方法 A $a ≠ 方法 B $b" }
        println("n = $n：方法 A = 方法 B = $a")
    }

    // 分块商点上的 F 互证（覆盖 10^7 用到的全部大参数）
    run {
        val n = LIMIT
        var d = 1
        var checked = 0
        while (d <= n) {
            val q = n / d
            val last = n / q
            var f = 0L
            var k = 0
            while (k < q) { k++; f += trianglesAtPerimeter(k) }
            check(f == bigF(q.toLong()).toLong()) { "F($q) 两条实现不一致" }
            checked++
            d = last + 1
        }
        println("分块商点 F 互证：$checked 个不同商 q 上两条实现一致（含 q = $n）")
    }

    // ---------- 5. 计时 ----------
    val answer = solveStream(LIMIT)
    solveGrouped(LIMIT)
    val msA = bestOf3("方法 A（线性筛 μ + 流式 F + 逐 d 求和，n=10^7）", answer) { solveStream(LIMIT) }
    val msB = bestOf3("方法 B（翻倍筛 μ + E 闭式 + 按商分块，n=10^7）", answer) { solveGrouped(LIMIT).toLong() }
    brutePrimitive(400)
    val msBrute = bestOf3("暴力对照（直接枚举本原三角形，n=400）", brutePrimitive(400)) { brutePrimitive(400) }

    println()
    println("答案 = $answer")
    println("汇总：方法 A ${"%.1f".format(msA)} ms；方法 B ${"%.1f".format(msB)} ms；暴力 n=400 ${"%.1f".format(msBrute)} ms")
    println("check() 全部通过")
}
