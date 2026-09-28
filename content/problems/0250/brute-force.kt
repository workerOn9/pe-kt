#!/usr/bin/env kotlin
/**
 * Project Euler 250 — 250250 暴力对照
 *
 * 思路：与 solution.kt 的「余数频次 + 群环 DP」**完全不同**的路线——不分组、不递推，
 *   直接按定义枚举 {1^1, 2^2, …, n^n} 的全部 2^n 个子集（Gray 码序，每次只翻转一个
 *   元素，O(1) 维护当前子集和的余数），数出非空且和 ≡ 0 (mod 250) 的子集个数。
 *   f(k) = k^k mod 250 用最朴素的「循环乘 k 次」计算，连快速幂都不与 solution.kt 共用。
 *
 *   局限：指数级。n = 24 是 1.7×10^7 个子集，n = 28 是 2.7×10^8 个；
 *   而本题 n = 250250 是 2^250250 ≈ 10^75333 个子集——比可观测宇宙的原子数（约 10^80）
 *   还多出约 7.5 万个数量级，暴力法连「只跑一遍」都不可能，只能为 DP 提供小规模对照。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar [n]（默认 26）
 */

/** 朴素定义：k^k mod 250 = 乘 k 次 */
private fun fkNaive(k: Int): Int {
    var r = 1
    val b = k % 250
    repeat(k) { r = r * b % 250 }
    return r
}

/** 2^n 全枚举（Gray 码）：返回非空且和 ≡ 0 (mod 250) 的子集数 */
private fun bruteCount(n: Int): Long {
    val f = IntArray(n + 1)
    for (k in 1..n) f[k] = fkNaive(k)
    var sum = 0
    var prev = 0
    var cnt = 0L
    val total = 1 shl n
    var m = 1
    while (m < total) {
        val g = m xor (m shr 1)
        val d = g xor prev
        val k = Integer.numberOfTrailingZeros(d) + 1
        if (g and d != 0) {
            sum += f[k]
            if (sum >= 250) sum -= 250
        } else {
            sum -= f[k]
            if (sum < 0) sum += 250
        }
        prev = g
        if (sum == 0) cnt++
        m++
    }
    return cnt
}

fun main(args: Array<String>) {
    val nMax = if (args.isNotEmpty()) args[0].toInt() else 26

    // 正确性输出：各小上界下的子集计数，供与 solution.kt 的 DP 逐项比对
    println("brute-force（2^n 全枚举，Gray 码序），上界 n ≤ $nMax")
    for (n in intArrayOf(10, 15, 18, 20, 22, 24)) {
        if (n <= nMax) println("n = $n：非空且和 ≡ 0 (mod 250) 的子集 = ${bruteCount(n)}")
    }
    if (nMax > 24) println("n = $nMax：非空且和 ≡ 0 (mod 250) 的子集 = ${bruteCount(nMax)}")

    // 计时：JIT 预热后 3 次取最优
    bruteCount(nMax)
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        bruteCount(nMax)
        val ms = (System.nanoTime() - t0) / 1e6
        if (ms < best) best = ms
    }
    println("brute-force：${"%.1f".format(best)} ms（n = $nMax，2^$nMax = ${"%,d".format(1L shl nMax)} 个子集，3 次最优）")
}
