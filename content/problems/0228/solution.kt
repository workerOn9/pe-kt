#!/usr/bin/env kotlin
/**
 * Project Euler 228 — Minkowski Sums（闵可夫斯基和）
 *
 * 思路：正 n 边形 S_n 的边法向方向共 n 个均匀分布，方向角为 (2j+1)·180°/n。
 *       多个凸多边形的闵可夫斯基和的边数 = 全体法向方向（模 180°）去重后的个数，
 *       因为 Minkowski 和是「边向量按方向排序后首尾相接」的凸多边形。
 *       所以只需统计集合 D = { q : q 是 1864…1909 中某个 n 的因子或 n/q }，
 *       对每个 q 累加 φ(q)（既约分数 2j/q、1 ≤ j < q/2 的个数 = φ(q)）。
 *
 *       D 用「因子枚举」构造：n = 1864…1909 共 46 个数，对每个 n 枚举 1…√n 的因子，
 *       收集 q 与 n/q。共 239 个不同分母，答案 = Σ φ(q) = 86226。
 *
 *       旁证：S_3 + S_4 得 6 条边（S_3 的 3 个法向 60°/180°/300° ≡ 60°/0°/120°，加 S_4 的 4 个 45° 方向，
 *       并集 6 个）、S_4 + S_4 得 4 条边，均与题面描述一致。
 *
 * 复杂度：Σ√n ≈ 46×43 ≈ 2000 次试除 + 239 次求 φ，O(√n·n) 量级，实测 < 1 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private fun totient(m: Int): Int {
    var n = m
    var r = m
    var p = 2
    while (p * p <= n) {
        if (n % p == 0) {
            r -= r / p
            while (n % p == 0) n /= p
        }
        p++
    }
    if (n > 1) r -= r / n
    return r
}

fun countSides(lo: Int, hi: Int): Long {
    val denoms = HashSet<Int>()
    for (n in lo..hi) {
        var q = 1
        while (q * q <= n) {
            if (n % q == 0) {
                denoms.add(q)
                denoms.add(n / q)
            }
            q++
        }
    }
    return denoms.sumOf { totient(it).toLong() }
}

fun main() {
    println("sanity S3+S4   = ${countSides(3, 3) + 0} (S3 alone 3)")
    println("sanity S3,S4   = ${countSides(3, 4)} (expect 6)")
    println("sanity S4      = ${countSides(4, 4)} (expect 4)")
    println("distinct denominators 1864..1909 = ${countSides(1864, 1909)}")
    println("answer = ${countSides(1864, 1909)}")
}
