#!/usr/bin/env kotlin
/**
 * Project Euler 257 — Angular Bisectors 暴力对照
 *
 * 思路：与 solution.kt 的参数化路线**完全不同**——不引入 x = b/a、不反解 y，
 *   直接对 a ≤ b 双重循环，把比例条件当作关于 c 的一次方程解出来：
 *     · R = 2 时 (a+b)(a+c) = 2bc  → c(b−a) = a(a+b)   → c = a(a+b)/(b−a)；
 *     · R = 3 时 (a+b)(a+c) = 3bc  → c(2b−a) = a(a+b)  → c = a(a+b)/(2b−a)。
 *   对每个 (a,b) 检验整除、c ≥ b（保证 b 是三边中间值）、a+b > c（三角形不等式）
 *   与 a+b+c ≤ limit，计数；R = 4 的解只有等边三角形，单独加 ⌊limit/3⌋ 个。
 *   为避免退化 (a+b = c) 被算进去，三角形不等式用严格大于。
 *
 *   内层循环范围（由比例条件推出，纯几何）：
 *     · R = 2 要求 2a < b ≤ a(1+√2)（下界是三角形不等式、上界是 c ≥ b）；
 *     · R = 3 要求 a < b ≤ a(1+√3)/2。
 *   a 的上界取周长下界：R = 2 时周长 ≥ (3+2√2)a > 5a，R = 3 时周长 ≥ (2+√3)a > 3a。
 *
 * 局限：O(limit²) 量级（约 0.017·limit² 次循环），limit = 3×10⁵ 约 1.5×10⁹ 次、
 *   limit = 10⁸ 需约 1.7×10¹⁴ 次——暴力法无法触及完整规模，只作小规模对照。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar [limit]（默认 300000）
 */

/** ⌊√n⌋（整数精确）。 */
private fun isqrt(n: Long): Long {
    var x = Math.sqrt(n.toDouble()).toLong()
    while (x > 0L && x * x > n) x--
    while ((x + 1) * (x + 1) <= n) x++
    return x
}

/** 暴力计数：枚举 (a, b)，解出 c，三重检验。 */
private fun bruteCount(limit: Long): Long {
    var total = limit / 3                                 // R = 4：全部等边三角形
    // R = 2：c = a(a+b)/(b−a)，2a < b ≤ a(1+√2)
    var a = 1L
    while (5 * a <= limit) {
        val bMax = a + isqrt(2 * a * a)
        var b = 2 * a + 1
        while (b <= bMax) {
            val num = a * (a + b)
            val den = b - a
            if (num % den == 0L) {
                val c = num / den
                if (c >= b && a + b > c && a + b + c <= limit) total++
            }
            b++
        }
        a++
    }
    // R = 3：c = a(a+b)/(2b−a)，a < b ≤ a(1+√3)/2
    a = 1L
    while (3 * a <= limit) {
        val bMax = (a + isqrt(3 * a * a)) / 2
        var b = a + 1
        while (b <= bMax) {
            val num = a * (a + b)
            val den = 2 * b - a
            if (num % den == 0L) {
                val c = num / den
                if (c >= b && a + b > c && a + b + c <= limit) total++
            }
            b++
        }
        a++
    }
    return total
}

fun main(args: Array<String>) {
    val limit = if (args.isNotEmpty()) args[0].toLong() else 300_000L

    // 正确性输出：各小上界下的三角形个数，供与 solution.kt 的三路对照逐项比对
    println("brute-force（(a,b) 双重循环 + 反解 c），上界 limit ≤ $limit")
    for (n in longArrayOf(100, 300, 1000, 3000, 10_000, 30_000, 100_000)) {
        if (n <= limit) println("limit = $n：$n 以内有效三角形 = ${bruteCount(n)}")
    }
    if (limit > 100_000L) println("limit = $limit：$limit 以内有效三角形 = ${bruteCount(limit)}")

    // 计时：JIT 预热后 3 次取最优
    bruteCount(limit)
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        bruteCount(limit)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
    }
    println("brute-force：${"%.1f".format(best)} ms（limit = $limit，3 次最优，JIT 预热后）")
}
