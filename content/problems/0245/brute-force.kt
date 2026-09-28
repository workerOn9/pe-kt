#!/usr/bin/env kotlin
/**
 * Project Euler 245 — Coresilience（核心韧性）暴力对照
 *
 * 思路：与 solution.kt 的「前缀枚举 + 末位素数由 K 唯一确定」递归**完全不同的路线**——
 * 不假设 n 无平方因子、不枚举素因子链，直接用**线性筛**一次性算出 [0, N] 内每个数的
 * 欧拉函数 φ(n)，再对每个 n 按定义逐条判：
 *
 *   C(n) = (n − φ(n)) / (n − 1) 是单位分数
 *       ⟺ (n − 1) / (n − φ(n)) 是整数
 *       ⟺ (n − φ(n)) | (n − 1)，且 n 为合数（题面只对合数求和）。
 *
 *   线性筛求 φ 的递推（对 i 与素数 p，i·p ≤ N）：
 *     - p | i：φ(i·p) = φ(i)·p
 *     - p ∤ i：φ(i·p) = φ(i)·(p−1)
 *
 *   每个 n 恰好被其最小素因子筛到一次，复杂度 O(N)，与解法搜索无关。
 *   局限：N 只能取到 3×10⁷ 量级（两个 IntArray），但足以覆盖解法递归链上的**前 169 个解**，
 *   做逐个解的比对而不是只比总和。
 *
 * 复杂度：时间 O(N)，空间 O(N)。
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar [N]
 */

private fun linearSievePhi(n: Int): IntArray {
    val phi = IntArray(n + 1)
    val primes = IntArray(n / 10 + 100)
    var pc = 0
    if (n >= 1) phi[1] = 1
    var i = 2
    while (i <= n) {
        if (phi[i] == 0) {            // i 是素数
            primes[pc++] = i
            phi[i] = i - 1
        }
        for (j in 0 until pc) {
            val p = primes[j]
            val v = i.toLong() * p
            if (v > n) break
            if (i % p == 0) {
                phi[v.toInt()] = phi[i] * p
                break
            } else {
                phi[v.toInt()] = phi[i] * (p - 1)
            }
        }
        i++
    }
    return phi
}

private fun run(n: Int): Pair<List<Int>, Long> {
    val phi = linearSievePhi(n)
    val sol = ArrayList<Int>()
    var k = 4L                             // n > 1 且合数：最小候选是 4
    while (k <= n) {
        val gap = k - phi[k.toInt()]
        // gap > 0 且 phi[k] != k-1（非素数），且 gap | (k-1)
        if (gap > 0 && phi[k.toInt()] != k.toInt() - 1 && (k - 1) % gap == 0L) sol += k.toInt()
        k++
    }
    return sol to sol.sumOf { it.toLong() }
}

fun main(args: Array<String>) {
    val n = if (args.isNotEmpty()) args[0].toInt() else 30_000_000

    // 首次运行：正确性输出（全量解集，供与 solution.kt 逐项比对）
    val (sol, sum) = run(n)
    println("brute-force (linear phi sieve), n <= $n")
    println("solutions (${sol.size}): $sol")
    println("sum = $sum")

    // 计时：JIT 预热后 3 次取最优。线性筛每次都要重新分配 IntArray，成本如实计入。
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        val (s2, sum2) = run(n)
        val ms = (System.nanoTime() - t0) / 1e6
        check(s2 == sol && sum2 == sum) { "计时中结果漂移" }
        if (ms < best) best = ms
    }
    println("brute: ${"%.1f".format(best)} ms（3 次取最优）")
}
