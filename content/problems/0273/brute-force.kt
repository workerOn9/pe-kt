#!/usr/bin/env kotlin
/**
 * Project Euler 273 — Sum of Squares：直接暴力对照（独立实现，与 solution.kt 不共享核心代码）
 *
 * 完全按题面定义：对每个平方自由、素因子全是 4k+1 型的 N，枚举 0 ≤ a ≤ b 满足
 * a² + b² = N，累计 Σa。这里不做任何高斯整数/表示数理论的推理——只用「枚举 a 检查
 * N − a² 是否平方」这一条最朴素的判定。
 *
 * 覆盖范围（每种规模的耗时一览）：
 *   路径 1（锚点）：S(65) = 1 + 4 = 5；
 *   路径 2（规模扫）：前 L 个素数（L = 6…10）的全部 2^L − 1 个非空子集；
 *   路径 3（大素数抽查）：全部 16 个素数的所有 1 元与 2 元子集（含 137、149 等大素数）。
 *
 * 可行性边界：直接枚举单个 N 的代价是 O(√N)，全部 16 个素数的子集之和是
 *   Σ_S √(N_S) ≈ Π(1 + √pᵢ)/√2 ≈ 3.5×10^14 次迭代（本文件会打印这个外推值），
 * 比「前 10 个素数」的 ~1.3×10^8 次大 6 个数量级，完全不可行——这正是需要用
 * 集合递推（solution.kt 方法 A/B）的原因。本暴力只用于把递推的正确性钉到
 * 「前 10 个素数的全部 1023 个子集」这个规模上。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 * 运行：java -jar brute-force.jar
 */

private val PRIMES = intArrayOf(5, 13, 17, 29, 37, 41, 53, 61, 73, 89, 97, 101, 109, 113, 137, 149)

/** 直接按定义求 S(N)：枚举 0 ≤ a ≤ b，a² + b² = N，返回 Σa（N ≤ 2^52 时双精度开方精确）。 */
private fun sumOfSquares(n: Long): Long {
    var total = 0L
    var a = 0L
    while (2 * a * a <= n) {
        val r = n - a * a
        val b = Math.round(Math.sqrt(r.toDouble()))
        var c = b - 1
        while (c <= b + 1) {
            if (c * c == r && c >= a) { total += a; break }
            c++
        }
        a++
    }
    return total
}

/** 子集 [mask] 对应 N 的 S(N) 直接暴力。 */
private fun bruteSubset(primes: IntArray, mask: Int): Long {
    var n = 1L
    for (i in primes.indices) if ((mask shr i) and 1 == 1) n *= primes[i]
    return sumOfSquares(n)
}

/** 前 [l] 个素数的全部非空子集求和，返回 (总和, 毫秒)。 */
private fun bruteFirstL(l: Int): Pair<Long, Double> {
    val sub = PRIMES.copyOfRange(0, l)
    val t0 = System.nanoTime()
    var total = 0L
    for (mask in 1 until (1 shl l)) total += bruteSubset(sub, mask)
    val ms = (System.nanoTime() - t0) / 1e6
    return total to ms
}

private fun bestOf3(tag: String, expected: Long, f: () -> Pair<Long, Double>): Double {
    var best = Double.MAX_VALUE
    repeat(3) { round ->
        val (out, ms) = f()
        check(out == expected) { "$tag 第 ${round + 1} 轮结果漂移：$out ≠ $expected" }
        if (ms < best) best = ms
        println("  $tag 第 ${round + 1} 轮：${"%.1f".format(ms)} ms")
    }
    println("$tag：${"%.1f".format(best)} ms（3 轮最优，JIT 预热后）")
    return best
}

fun main() {
    // ---------- 路径 1：题面锚点 ----------
    val s65 = sumOfSquares(65)
    check(s65 == 5L) { "S(65) = $s65 ≠ 5" }
    println("路径 1（按定义枚举 a）：S(65) = $s65（题面锚点）")

    // ---------- 路径 2：前 L 个素数的全部子集 ----------
    val expected = mapOf(
        6 to bruteFirstL(6).first,
        7 to bruteFirstL(7).first,
        8 to bruteFirstL(8).first,
        9 to bruteFirstL(9).first,
        10 to bruteFirstL(10).first,
    )
    for (l in 6..10) println("路径 2：前 $l 个素数（${(1 shl l) - 1} 个子集）总和 = ${expected[l]}")

    // ---------- 路径 3：16 个素数的全部 ≤2 元子集 ----------
    var total2 = 0L
    var cnt = 0
    for (i in PRIMES.indices) {
        total2 += bruteSubset(PRIMES, 1 shl i)
        cnt++
        for (j in i + 1 until PRIMES.size) {
            total2 += bruteSubset(PRIMES, (1 shl i) or (1 shl j))
            cnt++
        }
    }
    println("路径 3：16 个素数的 $cnt 个 ≤2 元子集总和 = $total2（含 137·149 等大素数组合）")

    // ---------- 计时（meta 的 bruteForceBaselineMs 口径） ----------
    bruteFirstL(10)
    val ms10 = bestOf3(
        "路径 2：前 10 个素数全部 1023 个子集（baseline 口径）",
        expected[10]!!,
    ) { bruteFirstL(10) }

    // ---------- 外推 ----------
    var prod = 1.0
    for (p in PRIMES) prod *= 1.0 + Math.sqrt(p.toDouble())
    val extrapolated = prod / Math.sqrt(2.0)
    println()
    println("外推：全 16 个素数时 Σ_S √(N_S) ≈ ${"%.3e".format(extrapolated)} 次枚举，")
    println("是前 10 个素数（≈1.3×10^8 次）的 ~${"%.0e".format(extrapolated / 1.3e8)} 倍，直接暴力不可行")
    println("汇总：baseline（前 10 个素数全部子集）= ${"%.1f".format(ms10)} ms（3 轮最优，JIT 预热后）")
    println("check() 全部通过")
}
