/**
 * Project Euler 139 — 暴力解（教学对比用）
 *
 * 与 solution.kt 的思路完全不同：这里不做任何数论化简，直接把周长小于 limit 的勾股三角形
 * 逐个枚举出来，对**每个三角形自身**实算它的铺砖条件 c mod |b − a| == 0。
 *   · 三角形用标准参数化生成：a = k(m² − n²), b = 2kmn, c = k(m² + n²)，
 *     其中 m > n ≥ 1、gcd(m, n) = 1、m − n 为奇数给出全部本原组，k = 1, 2, 3, … 给出其全部倍数；
 *     外层 (m, n) 按定义枚举（只用 gcd 与奇偶判本原性），内层把 k 显式逐个走一遍，
 *     不使用「整除条件在缩放下不变」这条结论，也不用 floor 除法一次算出倍数个数。
 *   · 另设 countNaive()，在小范围抛开参数化、直接扫 (a, b) 并用整数开方判 c，作为语义锚点，
 *     保证「铺砖 ⟺ d | c」这条题意解读不是只在一条路径上自洽。
 *
 * 两解的差距来源：solution.kt 先把条件化简成「两腿相差 1」的本原组，只需要 10 次 Pell 递推；
 * 本解要遍历 17322676 个 (m, n) 对（每个一次 gcd），并对周长小于 limit 的每一个勾股三角形
 * （共 113236937 个）各做一次取模，其中 99.99% 的三角形条件不成立，是纯浪费。
 *
 * 复杂度：外层 O(√(limit/2) · log limit)，内层总迭代数 T = 周长小于 limit 的勾股三角形总数，
 * 时间 O(√limit · log limit + T)，空间 O(1)。中间量 k·(m² + n²) < limit，Long 足够。
 *
 * 题面样例写成运行时断言：(3,4,5) 可铺砖、(5,12,13) 不可铺砖。
 * stdout 只输出最终答案，小范围对齐的诊断信息走 stderr。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 欧几里得算法求最大公约数。 */
fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

/** 铺砖条件：洞边长 d = |b − a| 必须整除外正方形边长 c。 */
fun tiles(a: Long, b: Long, c: Long): Boolean {
    val d = if (a > b) a - b else b - a
    return c % d == 0L
}

/**
 * 周长严格小于 limit 的可铺砖三角形个数：
 * 枚举全部 (m, n) 本原对，再对每个本原组逐个枚举倍数 k，对每个三角形单独检验条件。
 */
fun solveBruteForce(limit: Long = 100_000_000L): Long {
    var count = 0L
    var m = 2L
    while (2 * m * (m + 1) < limit) {            // n ≥ 1 ⇒ 本原周长 ≥ 2m(m + 1)
        var n = 1L
        while (n < m) {
            val primPerimeter = 2 * m * (m + n)
            if (primPerimeter >= limit) break    // 周长随 n 递增，越界即跳出
            if (gcd(m, n) == 1L && (m - n) % 2 == 1L) {
                var k = 1L
                while (k * primPerimeter < limit) {
                    val a = k * (m * m - n * n)
                    val b = k * (2 * m * n)
                    val c = k * (m * m + n * n)
                    if (tiles(a, b, c)) count++
                    k++
                }
            }
            n++
        }
        m++
    }
    return count
}

/** 最朴素的参照：直接扫 a < b，用整数开方判定 c 是否为整数，再检查铺砖条件（小范围用）。 */
fun countNaive(limit: Long): Long {
    var count = 0L
    var a = 1L
    while (3 * a + 2 < limit) {
        var b = a + 1
        while (a + 2 * b < limit) {
            val s = a * a + b * b
            var c = Math.sqrt(s.toDouble()).toLong()
            while (c * c > s) c--
            while ((c + 1) * (c + 1) <= s) c++
            if (c * c == s && a + b + c < limit && tiles(a, b, c)) count++
            b++
        }
        a++
    }
    return count
}

fun main() {
    check(tiles(3, 4, 5)) { "(3,4,5) 应可铺砖" }
    check(!tiles(5, 12, 13)) { "(5,12,13) 不应可铺砖" }

    // 小范围三路对齐：参数化枚举 = (a, b) 直接穷举 = 已知锚点
    for (lim in longArrayOf(13L, 100L, 1000L, 3000L, 10_000L)) {
        val param = solveBruteForce(lim)
        val naive = countNaive(lim)
        check(param == naive) { "limit=$lim：参数化枚举 $param 与直接穷举 $naive 不一致" }
        System.err.println("limit=$lim → $param")
    }
    check(solveBruteForce(13L) == 1L)
    check(solveBruteForce(100L) == 9L)
    check(solveBruteForce(1000L) == 99L)
    check(solveBruteForce(10_000L) == 1003L)

    repeat(3) { solveBruteForce() }              // JIT 预热
    val start = System.nanoTime()
    val answer = solveBruteForce()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)                              // stdout 只有这一行：诊断信息一律走 stderr
}
