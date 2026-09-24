#!/usr/bin/env kotlin
// PE 218 — Perfect Right-angled Triangles（完美直角三角形）暴力参照
// 思路：不走「斜边平方 ⇒ 参数二次嵌套」的推导，直接在 (m, n) 参数空间枚举本原三元组：
//       对 m > n、gcd(m,n) = 1、m、n 一奇一偶，算 a = m²-n²、b = 2mn、c = m²+n²，
//       再用整数开方判断 c 是否为完全平方数——是则得到一个完美三角形，检查面积
//       A = ab/2 = mn|m²-n²| 是否被 84 整除。
//       受限于 O(C) 的枚举量，只能覆盖 c ≤ 10^8（约 5×10^7 组 (m,n)），
//       用于和 solution.kt 的 (u,v) 枚举在重叠区间上互相印证。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

import kotlin.math.sqrt

/** 整数平方根（Newton 迭代兜底），stdlib 无 isqrt 时自备。 */
private fun isqrt(v: Long): Long {
    if (v < 2L) return if (v < 0L) -1L else v
    var x = sqrt(v.toDouble()).toLong()
    while (x * x > v) x--
    while ((x + 1) * (x + 1) <= v) x++
    return x
}

/** 返回 c ≤ limit 的完美三角形个数与非超完美个数。 */
private fun brutePerfect(limit: Long): Pair<Long, Long> {
    var total = 0L
    var notSuper = 0L
    var m = 2L
    while (m * m + 1 <= limit) {
        var n = 1L
        while (n < m) {
            if ((m - n) % 2 == 1L && gcdL(m, n) == 1L) {
                val c = m * m + n * n
                if (c <= limit) {
                    val r = isqrt(c)
                    if (r * r == c) {                     // 斜边是完全平方数 ⇒ 完美三角形
                        total++
                        val a = m * m - n * n
                        val b = 2 * m * n
                        if ((a * b / 2) % 84L != 0L) notSuper++
                    }
                }
            }
            n++
        }
        m++
    }
    return total to notSuper
}

private fun gcdL(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

fun main() {
    val t0 = System.nanoTime()
    for (limit in longArrayOf(1_000_000L, 100_000_000L)) {
        val (total, notSuper) = brutePerfect(limit)
        println("c ≤ $limit：完美三角形 $total 个，非超完美 $notSuper 个")
    }
    val ms = (System.nanoTime() - t0) / 1_000_000
    System.err.println("brute force 218 wall = $ms ms")
}
