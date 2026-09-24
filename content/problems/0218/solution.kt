#!/usr/bin/env kotlin
// PE 218 — Perfect Right-angled Triangles（完美直角三角形）
// 思路：完美三角形 = 本原直角三角形且斜边是完全平方数。
//   本原三元组参数化为 a = m²-n²、b = 2mn、c = m²+n²（m > n、互素、一奇一偶）。
//   要求 c = m²+n² 是完全平方数，即 (m, n, √c) 本身也是本原勾股三元组，于是
//     m = 2uv,  n = u²-v²,  √c = u²+v²        （u > v、互素、一奇一偶）
//   从而 c = (u²+v²)² ≤ 10^16 ⇔ u²+v² ≤ 10^8，枚举 u ≤ 10^4 即可覆盖全部完美三角形。
//   面积 A = ab/2 = mn|m²-n²|（m、n 互换对称，取绝对值），代进 (u,v) 得
//     A = 2uv·(u²-v²)·|4u²v² - (u²-v²)²|.
//   逐对检查 84 | A，统计不满足的个数即为答案。
// 结论：不存在非超完美的完美三角形，答案 0。整除性可严格证明：
//   · 4 | A：u、v 一奇一偶 ⇒ uv 为偶数 ⇒ m = 2uv ≡ 0 (mod 4)，而 m²-n² 是奇数；
//   · 3 | A：m、n 若都不被 3 整除则 m² ≡ n² ≡ 1 (mod 3) ⇒ 3 | m²-n²；
//   · 7 | A：反设 m² ≢ n² (mod 7) 且 m,n ≢ 0 (mod 7)。
//           k² = m²+n² 由两个非零二次剩余 {1,2,4} 中不同的两项相加，
//           结果只能是 {3,5,6}，都不是模 7 的二次剩余，与 k² 是平方数矛盾。
//   84 = lcm(3,4,7) 同时蕴含 6 | A 与 28 | A，故每个完美三角形都超完美。
// 复杂度：约 2×10^7 对 (u,v)，配合 gcd 剪枝，实测数百毫秒。
// 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
// 运行：java -jar solution.jar

import kotlin.math.sqrt

/** 整数平方根（Newton 迭代兜底），stdlib 无 isqrt 时自备。 */
private fun isqrt(v: Long): Long {
    if (v < 2L) return if (v < 0L) -1L else v
    var x = sqrt(v.toDouble()).toLong()
    while (x * x > v) x--
    while ((x + 1) * (x + 1) <= v) x++
    return x
}

private const val LIMIT = 100_000_000      // u² + v² 上限，对应 c ≤ 10^16
private const val UMAX = 10_000

/**
 * 统计 c ≤ 10^16 的完美三角形总数，以及其中「非超完美」（面积不被 84 整除）的个数。
 * 返回 Pair(总数, 非超完美数)。
 */
private fun enumerate(): Pair<Long, Long> {
    var total = 0L
    var notSuper = 0L
    for (u in 2..UMAX) {
        var vMax = isqrt((LIMIT - u * u).toLong()).toInt()
        if (vMax >= u) vMax = u - 1
        var v = if (u % 2 == 0) 1 else 2      // 只取与 u 一奇一偶的 v
        while (v <= vMax) {
            if (gcd(u, v) == 1) {
                total++
                val m = 2L * u * v
                val n = 1L * u * u - 1L * v * v
                // 面积可达 10^32，超出 Long 范围，只按模 84 判定（84 | A ⟺ 84 | |A|）
                val mm = m % 84L
                val nn = n % 84L
                val diff = ((mm * mm - nn * nn) % 84L + 84L) % 84L
                val areaMod = (mm * nn % 84L) * diff % 84L
                if (areaMod != 0L) notSuper++
            }
            v += 2
        }
    }
    return total to notSuper
}

private fun gcd(a: Int, b: Int): Int {
    var x = a
    var y = b
    while (y != 0) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

private fun solve218(): Long = enumerate().second

fun main() {
    repeat(2) { enumerate() }                // JIT 预热
    val t0 = System.nanoTime()
    val (total, notSuper) = enumerate()
    val ms = (System.nanoTime() - t0) / 1_000_000
    println("c ≤ 10^16 的完美三角形：$total 个，其中非超完美：$notSuper 个")
    println(solve218())
    System.err.println("enumerate wall = $ms ms")
}
