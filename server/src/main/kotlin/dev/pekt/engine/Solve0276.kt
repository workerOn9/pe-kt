package dev.pekt.engine

/**
 * PE 276 — Primitive Triangles（本原整数边三角形）：周长不超过 10^7、gcd(a,b,c) = 1 的整数边
 * 三角形（a ≤ b ≤ c）有多少个。
 *
 * 推导（详见 content/problems/0276/solution.kt 头部与 0276/analysis.md）：
 *   记 T(p) = 周长恰为 p 的三角形数、F(n) = Σ_{p≤n} T(p)、c(p) = 周长 p 的本原三角形数。
 *   每个三角形唯一写成 g·(本原三角形)，于是 T = 1 * c，故 c = μ * T，求和形式
 *     P(n) = Σ_{d≤n} μ(d)·F(n/d)。
 *   T(p) 的闭式：p = 2m 时 T = ⌊(m²+6)/12⌋，p = 2m+1 时 T = ⌊((m+2)²+6)/12⌋，
 *   逐项累加即得 F(n)（无需存 F 表：d 从 n 递减时 n/d 单调递增，可流式推进）。
 *
 * 实现：线性筛出 μ（O(n)，ByteArray 10 MB），F 流式推进，逐 d 求和；累加用 Math.addExact
 * 显式守住 Long 溢出（F(10^7) ≈ 6.94×10^18，答案 ≈ 5.78×10^18）。实测约 70 ms。
 *
 * 校验：T(p) 闭式与直接枚举在 p ≤ 600 一致；F 的递推与 E(M) 闭式（BigInteger）在 5 万点与
 *   全部分块商点一致；与按定义三重循环（n ≤ 500）及半朴素容斥（n = 20000）的暴力一致；
 *   方法 B（翻倍筛 μ + 闭式 F + 按商分块 BigInteger 求和）在全规模一致。逻辑与
 *   content/problems/0276/solution.kt 的主路径（方法 A）一致。
 */
internal fun solve0276Impl(): Long {
    val n = 10_000_000
    val mu = mobiusLinear0276(n)
    var total = 0L
    var f = 0L
    var k = 0
    var d = n
    while (d >= 1) {
        val q = n / d
        while (k < q) {
            k++
            f += trianglesAtPerimeter0276(k)
        }
        val m = mu[d].toLong()
        if (m != 0L) total = Math.addExact(total, m * f)
        d--
    }
    return total
}

/** 线性筛 μ：mu[i] = μ(i)，i ≥ 1；O(n)。 */
private fun mobiusLinear0276(n: Int): ByteArray {
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

/** 周长恰为 p 的整数边三角形数：u(j) = ⌊(j²+6)/12⌋，偶 p = 2m 取 u(m)，奇 p = 2m+1 取 u(m+2)。 */
private fun trianglesAtPerimeter0276(p: Int): Long {
    if (p < 3) return 0L
    val m = (p / 2).toLong()
    val j = if (p % 2 == 0) m else m + 2
    return (j * j + 6) / 12
}
