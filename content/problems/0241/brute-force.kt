#!/usr/bin/env kotlin
/**
 * Project Euler 241 — Perfection Quotients（完美商）暴力对照
 *
 * 思路：与 solution.kt **完全不同的路线**——不做任何数论剪枝、不分解大整数，
 * 直接用**线性筛（Euler 筛）**一次性算出区间内每个 n 的 σ(n)，再逐个判条件。
 *
 *   线性筛求 σ 的递推（对 i 与质数 p，i·p ≤ N）：
 *     - p ∤ i：σ(i·p) = σ(i)·σ(p) = σ(i)·(p+1)
 *     - p | i：设 pp[i] = i 的最小素因子幂，则 σ(i·p) = σ(i) + pp[i]·p·σ(i / pp[i])
 *   判据：2σ(n) 能被 n 整除且商为奇数，即 σ(n)/n = k + 1/2。
 *
 *   每个 n 恰好被其最小素因子筛到一次，复杂度 O(N)，与解法搜索无关。
 *   局限：N 只能取到 10^8 量级（两个 IntArray 各 4N 字节），但足以覆盖
 *   solution.kt 在同一上界下的解集，做**逐个解的比对**而不是只比总和。
 *
 * 复杂度：时间 O(N)，空间 O(N)（2 × IntArray）。
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar [N]
 */

private fun linearSieveSigma(n: Int): IntArray {
    val sigma = IntArray(n + 1)
    val pp = IntArray(n + 1)          // pp[i] = i 的最小素因子幂
    val primes = IntArray(n / 2 + 10)
    var pc = 0
    sigma[1] = 1
    pp[1] = 1
    var i = 2
    while (i <= n) {
        if (pp[i] == 0) {             // i 是质数
            primes[pc++] = i
            pp[i] = i
            sigma[i] = i + 1
        }
        for (j in 0 until pc) {
            val p = primes[j]
            val v = i.toLong() * p
            if (v > n) break
            if (i % p == 0) {
                pp[v.toInt()] = pp[i] * p
                sigma[v.toInt()] = sigma[i] + pp[i] * p * sigma[i / pp[i]]
                break
            } else {
                pp[v.toInt()] = p
                sigma[v.toInt()] = sigma[i] * (p + 1)
            }
        }
        i++
    }
    return sigma
}

fun main(args: Array<String>) {
    val n = if (args.isNotEmpty()) args[0].toInt() else 100_000_000

    // 首次运行：正确性输出
    val (sol, sum) = run(n)
    println("brute-force (linear sieve), n <= $n")
    println("solutions (${sol.size}): $sol")
    println("sum = $sum")

    // 计时：JIT 预热后 3 次取最优。线性筛每次都要重新分配两个 IntArray，成本如实计入。
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

private fun run(n: Int): Pair<List<Int>, Long> {
    val sigma = linearSieveSigma(n)
    val sol = ArrayList<Int>()
    var k = 2L
    while (k <= n) {
        val two = 2L * sigma[k.toInt()]
        if (two % k == 0L && (two / k) % 2L == 1L) sol += k.toInt()
        k++
    }
    return sol to sol.sumOf { it.toLong() }
}
