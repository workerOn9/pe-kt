#!/usr/bin/env kotlin
/**
 * Project Euler 249 — Prime Subset Sums（素子集和）暴力对照
 *
 * 思路：与 solution.kt 的「0/1 背包计数」递推走**完全不同的路线**——直接按定义穷举：
 * 用位掩码枚举前 k 个素数的全部 2^k 个子集，逐个求和、逐个查筛表判定素性，
 * 得到完整的「子集和 → 子集个数」直方图；再在同一前缀上跑一遍 DP 递推，两者逐项比对。
 *
 * 对 k = 1..N 的**每个**前缀都独立做一遍（N 默认 20，总枚举量 Σ2^k ≈ 2^21 ≈ 2×10^6 个
 * 子集），并逐项核对直方图与「和为素数」的汇总数，等价于逐个子集地交叉验证 DP。
 *
 * **局限（必须诚实说明）**：全量 S 有 669 个素数，子集总数 2^669 ≈ 10^201——比可观测宇宙
 * 的原子数（约 10^80）还多约 120 个数量级，枚举在物理上不可能完成。所以暴力法在这里只能
 * 充当「小规模正确性证据」：它钉死递推方向（倒序 0/1 背包）、素性筛表与计数定义都对；
 * 全量正确性由 solution.kt 的取模值域分析、BigInteger 精确对照与 2^669/2^668 质量守恒
 * 恒等式接力。
 *
 * 复杂度：时间 O(Σ_{k≤N} k·2^k)，空间 O(前 N 个素数之和)。
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar [N]   （N 默认 20，取到 24 左右仍可秒级完成）
 */

/** 埃氏筛：isPrime[i] = i 是否为素数 */
private fun sieve(limit: Int): BooleanArray {
    val isPrime = BooleanArray(limit + 1) { it >= 2 }
    var i = 2
    while (i.toLong() * i <= limit) {
        if (isPrime[i]) {
            var j = i * i
            while (j <= limit) {
                isPrime[j] = false
                j += i
            }
        }
        i++
    }
    return isPrime
}

/** 前 count 个素数（升序，count ≤ 24 时最大不过 89） */
private fun firstPrimes(count: Int): IntArray {
    val isPrime = sieve(1000)
    val out = IntArray(count)
    var n = 0
    for (i in 2..1000) {
        if (isPrime[i]) {
            out[n++] = i
            if (n == count) break
        }
    }
    return out
}

/** 暴力：枚举前 k 个素数的全部 2^k 个子集，返回直方图 hist[s] = 和为 s 的子集个数 */
private fun bruteHistogram(primes: IntArray, k: Int): LongArray {
    var maxSum = 0
    for (i in 0 until k) maxSum += primes[i]
    val hist = LongArray(maxSum + 1)
    val masks = 1 shl k
    for (mask in 0 until masks) {
        var sum = 0
        for (i in 0 until k) if (mask shr i and 1 == 1) sum += primes[i]
        hist[sum]++
    }
    return hist
}

/** DP：与 solution.kt 同一递推（此处小规模，不需要取模） */
private fun dpHistogram(primes: IntArray, k: Int): LongArray {
    var maxSum = 0
    for (i in 0 until k) maxSum += primes[i]
    val dp = LongArray(maxSum + 1)
    dp[0] = 1L
    var reach = 0
    for (i in 0 until k) {
        val p = primes[i]
        for (s in reach + p downTo p) dp[s] += dp[s - p]
        reach += p
    }
    return dp
}

/** 直方图中「和为素数」的子集总数 */
private fun countPrimeSums(hist: LongArray, isPrime: BooleanArray): Long {
    var total = 0L
    for (s in 2 until hist.size) if (isPrime[s]) total += hist[s]
    return total
}

fun main(args: Array<String>) {
    val n = if (args.isNotEmpty()) args[0].toInt() else 20
    require(n in 1..24) { "N 取 1..24（2^24 = 1677 万个子集已是单次枚举的上限附近）" }

    val primes = firstPrimes(n)
    var maxSum = 0
    for (p in primes) maxSum += p
    val isPrime = sieve(maxSum)
    println("brute-force：前 $n 个素数 ${primes.toList()}，元素和 $maxSum")

    // 逐前缀：暴力直方图 vs DP 直方图，逐项 + 汇总双重比对
    for (k in 1..n) {
        val brute = bruteHistogram(primes, k)
        val dp = dpHistogram(primes, k)
        check(brute.contentEquals(dp)) { "k=$k：暴力与 DP 直方图不一致" }
        val bruteCount = countPrimeSums(brute, isPrime)
        check(bruteCount == countPrimeSums(dp, isPrime)) { "k=$k：和为素数的子集数不一致" }
        println("k=$k：2^$k = ${1 shl k} 个子集，直方图逐项一致，和为素数的子集数 = $bruteCount")
    }

    // 计时：N 规模的完整暴力枚举（JIT 预热后 3 次取最优）
    bruteHistogram(primes, n)                       // 预热
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        val h = bruteHistogram(primes, n)
        val ms = (System.nanoTime() - t0) / 1e6
        check(h.size > 0)
        if (ms < best) best = ms
    }
    println("brute: ${"%.1f".format(best)} ms/次（N=$n，即 2^$n = ${1 shl n} 个子集的完整枚举）")
    println("注意：全量 2^669 个子集无法枚举；本题 DP 的实测耗时见 solution.kt 输出")
}
