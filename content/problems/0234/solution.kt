#!/usr/bin/env kotlin
/**
 * Project Euler 234 — Semidivisible Numbers（半可除数）
 *
 * 思路：
 *   记 lps(n) = 不超过 √n 的最大素数，ups(n) = 不小于 √n 的最小素数。
 *   对相邻素数对 (p, q)（q 为 p 之后的下一个素数），区间 (p², q²) 内的每个 n 都满足
 *   lps(n) = p、ups(n) = q。n 半可除 ⟺ p、q 中恰有一个整除 n。
 *   于是在每个区间（用上界 N 截断）里：
 *       Σ n·[p|n] + Σ n·[q|n] − 2·Σ n·[pq|n]
 *   （同时被 p、q 整除的倍数被两边各算了一次，要从「恰好一个」里彻底剔除，故减两次。）
 *   每个和都是等差数列求和：k ∈ (lo/d, hi/d] 的 dk 之和 = d·(首项+末项)·项数/2。
 *
 *   两个边界细节：n 以开区间取 (p², q²)——n = q² 时 lps(n) = ups(n) = q，永远不是半可除数。
 *   还有一个收尾区间：最后一个 p 的 q² 已超过 N，需按 min(q²−1, N) 截断。
 *
 * 旁证：
 *   1. 题面样例：N ≤ 15 → 30（半可除数 8、10、12）；N ≤ 1000 → 34825（92 个）。
 *   2. brute-force.kt 用「逐个数算 lps/ups 再判定」的朴素做法复算到 10⁶，与分块求和一致。
 *
 * 答案：1259187438574927161
 * 复杂度：筛到 √N + 余量（≈10⁶）为 O(√N log log N)，之后每个素数对 O(1) 求等差数列和，
 *         共约 8×10⁴ 段，实测约 60 ms。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

private const val TARGET = 999_966_663_333L

fun main() {
    val sp = isqrt(TARGET).toInt() + 50 // 需要覆盖 √N 之后的下一个素数（1000003）
    val isPrime = BooleanArray(sp + 1) { true }
    isPrime[0] = false
    isPrime[1] = false
    var i = 2
    while (i.toLong() * i <= sp) {
        if (isPrime[i]) { var j = i * i; while (j <= sp) { isPrime[j] = false; j += i } }
        i++
    }
    val primes = ArrayList<Int>()
    for (x in 2..sp) if (isPrime[x]) primes.add(x)

    println("sample 15   -> " + semiSum(15L, primes))
    println("sample 1000 -> " + semiSum(1000L, primes))
    println("answer = " + semiSum(TARGET, primes))
}

private fun semiSum(n: Long, primes: List<Int>): Long {
    var total = 0L
    var idx = 0
    while (idx + 1 < primes.size) {
        val p = primes[idx].toLong()
        val q = primes[idx + 1].toLong()
        if (p * p > n) break
        val lo = p * p
        val hi = minOf(q * q - 1, n)
        if (hi > lo) {
            total += multSum(p, lo, hi) + multSum(q, lo, hi) - 2 * multSum(p * q, lo, hi)
        }
        idx++
    }
    return total
}

/** (lo, hi] 内所有 d 的倍数之和 */
private fun multSum(d: Long, lo: Long, hi: Long): Long {
    if (hi <= lo) return 0
    val first = lo / d + 1
    val last = hi / d
    if (first > last) return 0
    val cnt = last - first + 1
    return d * (first + last) * cnt / 2
}

private fun isqrt(x: Long): Long {
    var r = Math.sqrt(x.toDouble()).toLong()
    while (r * r > x) r--
    while ((r + 1) * (r + 1) <= x) r++
    return r
}
