#!/usr/bin/env kotlin
/**
 * Project Euler 243 — brute-force：从 d = 2 起逐个扫，靠欧拉函数筛算 φ(d)，
 * 完全不含「素因子集合 / 素数阶乘」任何结构（solution.kt 的全部推理在这里都不出现）。
 *
 *   判据就是题面的定义式：B·φ(d) < A·(d − 1)，A = 15499、B = 94744。
 *   φ 用**分段筛**算：把 [lo, hi] 整块装进数组，初值 φ[i] = i，再对每个素数 p ≤ √hi
 *   把块内 p 的倍数乘上 (1 − 1/p)（即 phi[x] -= phi[x] / p），最后从 2 开始顺序扫，
 *   第一个满足不等式的 d 就是答案。内存与 d 的大小无关（固定一块），代价 O(N log log N)。
 *
 *   题面样例（R(d) < 4/10 → 12）在这里是「同一段代码的同一次扫描」给出的，
 *   另加 1/2 → 6、1/3 → 30、1/4 → 210 三个交叉点。
 *
 *   本题答案在 8.9×10^8 附近，所以真实阈值下的全范围扫描是秒级的；计时取预热后的最优值。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar
 */

private const val A = 15499L
private const val B = 94744L
private const val BLOCK = 1 shl 22          // 每块 4.2×10^6 个数，phi 数组约 16 MB
private const val SIEVE_LIMIT = 30_000      // √9×10^8 ≈ 29875，筛到 30000 足够

/** 筛出 SIEVE_LIMIT 以内的全部素数 */
private fun primesUpTo(limit: Int): IntArray {
    val composite = BooleanArray(limit + 1)
    val out = ArrayList<Int>()
    for (i in 2..limit) {
        if (!composite[i]) {
            out.add(i)
            var j = i.toLong() * i
            while (j <= limit) { composite[j.toInt()] = true; j += i }
        }
    }
    return out.toIntArray()
}

/**
 * 从 d = 2 起顺序扫描，返回第一个满足 B·φ(d) < A·(d−1) 的 d；
 * 扫到 [stopAt] 仍没有则返回 -1。φ(d) 由分段筛现算，d 的素因子结构一概不用。
 */
private fun firstResilientDenominator(a: Long, b: Long, stopAt: Long): Long {
    val primes = primesUpTo(SIEVE_LIMIT)
    val phi = IntArray(BLOCK)
    var lo = 2L
    var hi: Long
    while (lo <= stopAt) {
        hi = minOf(lo + BLOCK - 1, stopAt)
        val size = (hi - lo + 1).toInt()
        for (i in 0 until size) phi[i] = (lo + i).toInt()
        for (p in primes) {
            if (p.toLong() * p > hi) break
            // 块内第一个 p 的倍数
            var x = ((lo + p - 1) / p) * p
            while (x <= hi) {
                val idx = (x - lo).toInt()
                phi[idx] -= phi[idx] / p
                x += p
            }
        }
        for (i in 0 until size) {
            val d = lo + i
            if (b * phi[i] < a * (d - 1)) return d
        }
        lo = hi + 1
    }
    return -1L
}

private fun main() {
    // ---- 题面样例与交叉点（小范围，秒回） ----
    for ((a, b, expected) in listOf(
        Triple(4L, 10L, 12L),    // 题面：R(d) < 4/10 的最小 d 是 12
        Triple(1L, 2L, 6L),
        Triple(1L, 3L, 30L),
        Triple(1L, 4L, 210L),
        Triple(2L, 5L, 12L),
    )) {
        val got = firstResilientDenominator(a, b, 1_000_000L)
        println("R(d) < $a/$b -> d = $got")
        check(got == expected) { "阈值 $a/$b 不吻合：$got != $expected" }
    }
    println("小阈值全部命中（含题面样例 4/10 -> 12）")

    // ---- 本题：全范围扫描，不做任何剪枝，直到第一个满足不等式的 d ----
    var hit = -1L
    var best = Double.MAX_VALUE
    repeat(3) {                       // 3 轮：第 1 轮含 JIT 预热，取后两轮里的最优值
        val t0 = System.nanoTime()
        val d = firstResilientDenominator(A, B, 1_500_000_000L)
        val ms = (System.nanoTime() - t0) / 1e6
        if (d != hit) check(hit == -1L) { "扫描结果不稳定：$hit vs $d" }
        hit = d
        best = minOf(best, ms)
    }
    println()
    println("R(d) < $A/$B  全范围扫描 2..1.5×10^9 -> 第一个满足的 d = $hit")
    check(hit == 892371480L) { "答案不吻合：$hit" }
    println("brute: $best ms（3 轮取最优，含 JIT 预热）")
    println("check() 全部通过")
}
