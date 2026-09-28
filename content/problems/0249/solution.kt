#!/usr/bin/env kotlin
/**
 * Project Euler 249 — Prime Subset Sums（素子集和）
 *
 * 思路：
 *   S = 5000 以下的全部素数（669 个），统计「元素之和为素数」的子集个数，输出后 16 位。
 *   把「子集」翻译成「子集和」后，问题就是典型的 0/1 背包**计数**：
 *
 *       dp[s] = 用当前处理过的素数拼出和 s 的子集个数，
 *
 *   逐个素数 p 更新（s 倒序，保证每个素数最多用一次）：
 *
 *       dp[s] ← dp[s] + dp[s − p]。
 *
 *   全部素数处理完后，对所有满足「s 是素数」的 s 累加 dp[s] 即得答案（空集的和 0
 *   不是素数，不会误入）。
 *
 *   **为什么可以全程取模：** 子集总数是 2^669 ≈ 10^201，而题目只要后 16 位；加法对
 *   取模封闭，每一步都模 10^16 不改变最终的低 16 位。值域证明：dp 的每一项都 < 10^16，
 *   一次加法 < 2×10^16 ≈ 2.0×10^16，远小于 Long 上限 9.2×10^18——所以整个 DP 用
 *   LongArray 加一步条件减法即可，既不需要 BigInteger 也不需要 `%`。
 *
 *   规模：素数 669 个，Σp = 1 548 136，dp 数组 1 548 137 个 Long（约 12 MB）。
 *   第 k 个素数只需扫到当前前缀和，总加法次数 Σ_k (p_1+…+p_{k-1}+1) = 325 967 779
 *   ≈ 3.26×10^8，只有「每次都扫满」（约 10.4 亿次）的三成。
 *   素数和判定复用同一份埃氏筛的 isPrime 表（筛到 1 548 136）。
 *
 * 旁证：
 *   1. 前 10 个素数：main 里用 2^10 = 1024 个子集暴力枚举出完整直方图，与 DP 逐项一致。
 *   2. 前 70 个素数：用 BigInteger 精确计数（不取模）与模 10^16 的 DP 逐项对比，此时
 *      最大计数已超过 10^16，取模真实发生——证明「取模」没有吃掉任何有效数字。
 *   3. brute-force.kt 对每个前缀 k = 1..20 独立枚举 2^k 个子集，与 DP 直方图逐项一致；
 *      全量 2^669 枚举不可行，只能做小规模互证（见该文件说明）。
 *   4. 全量质量守恒：Σ_s dp[s] 应等于 2^669，奇数和子集数应等于 2^668——668 个奇素数
 *      选奇数个/偶数个的方案各 2^667，再乘上「含不含 2」的 2 倍（2 不改变奇偶性）。
 *      这条恒等式直接检验 669 步 DP 的整体质量，而非只看最终那一项。
 *
 * 答案：9275262564250418（后 16 位，实跑输出）
 * 复杂度：O(Σ_k 前缀和_{k-1}) = 3.26×10^8 次加法，空间 O(Σp) ≈ 12 MB。
 * 构建：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 * 运行：java -jar solution.jar
 */

import java.math.BigInteger

private const val LIMIT = 5000
private const val MOD = 10_000_000_000_000_000L   // 10^16：题目只要后 16 位
private const val ANSWER = 9275262564250418L      // 实跑所得（仅作回归断言）

/** 埃氏筛：isPrime[i] = i 是否为素数（含 0、1 的边界） */
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

/** limit 以下（不含 limit）的全部素数，升序 */
private fun collectPrimes(limit: Int): IntArray {
    val isPrime = sieve(limit - 1)
    val out = ArrayList<Int>()
    for (i in 2 until limit) if (isPrime[i]) out.add(i)
    return out.toIntArray()
}

/**
 * 0/1 背包计数：返回 dp，dp[s] = 用 primes 拼出和 s 的子集个数（每步归约到 [0, mod)）。
 * 只扫 [p, 当前前缀和] 区间，避免对永远不可达的尾部做无用功。
 * 前提：mod ≤ Long.MAX_VALUE / 2，保证一次加法的中间值不溢出。
 */
private fun subsetSumCounts(primes: IntArray, mod: Long): LongArray {
    var maxSum = 0
    for (p in primes) maxSum += p
    val dp = LongArray(maxSum + 1)
    dp[0] = 1L                       // 空集
    var reach = 0                    // 已处理素数的元素和
    for (p in primes) {
        for (s in reach + p downTo p) {
            val v = dp[s] + dp[s - p]
            dp[s] = if (v >= mod) v - mod else v
        }
        reach += p
    }
    return dp
}

/** 同一递推的 BigInteger 精确版（不取模），只用于小规模旁证：检验取模不丢低位。 */
private fun subsetSumCountsExact(primes: IntArray): Array<BigInteger> {
    var maxSum = 0
    for (p in primes) maxSum += p
    val dp = Array(maxSum + 1) { BigInteger.ZERO }
    dp[0] = BigInteger.ONE
    var reach = 0
    for (p in primes) {
        for (s in reach + p downTo p) {
            dp[s] = dp[s] + dp[s - p]
        }
        reach += p
    }
    return dp
}

/** 对「和为素数」的下标累加 dp，同样模 mod。返回值 < mod，故一次条件减法足够。 */
private fun countPrimeSums(dp: LongArray, isPrime: BooleanArray, mod: Long): Long {
    var total = 0L
    for (s in 2 until dp.size) {
        if (isPrime[s]) {
            total += dp[s]
            if (total >= mod) total -= mod
        }
    }
    return total
}

/** 2^k mod mod（mod ≤ Long.MAX_VALUE / 2）：逐次翻倍 + 条件减法。 */
private fun pow2Mod(k: Int, mod: Long): Long {
    var v = 1L
    repeat(k) {
        v += v
        if (v >= mod) v -= mod
    }
    return v
}

/** 暴力：枚举 primes（≤ 20 个）的全部 2^n 个子集，返回每个和的子集个数直方图。 */
private fun bruteHistogram(primes: IntArray): LongArray {
    val n = primes.size
    require(n in 0..20) { "暴力枚举只做 2^20 以内的规模" }
    var maxSum = 0
    for (p in primes) maxSum += p
    val hist = LongArray(maxSum + 1)
    for (mask in 0 until (1 shl n)) {
        var sum = 0
        for (i in 0 until n) if (mask shr i and 1 == 1) sum += primes[i]
        hist[sum]++
    }
    return hist
}

fun main() {
    val primes = collectPrimes(LIMIT)
    var maxSum = 0
    for (p in primes) maxSum += p
    val isPrime = sieve(maxSum)      // 覆盖到「全部素数之和」，用于判定任意子集和
    println("S：小于 $LIMIT 的素数 ${primes.size} 个，元素之和 = $maxSum")

    // ---- 自检 1：前 10 个素数，2^10 子集暴力枚举与 DP 直方图逐项一致 ----
    val head = primes.copyOfRange(0, 10)
    val headDp = subsetSumCounts(head, MOD)
    val headBrute = bruteHistogram(head)
    check(headDp.contentEquals(headBrute)) { "前 10 个素数：DP 与暴力枚举的直方图不一致" }
    println(
        "自检 1：前 10 个素数（元素和 ${head.sum()}）的全部 ${1 shl head.size} 个子集，" +
            "DP 与暴力直方图逐项一致；和为素数的子集数 = ${countPrimeSums(headDp, isPrime, MOD)}",
    )

    // ---- 自检 2：前 70 个素数，BigInteger 精确计数 vs 模 10^16 的 DP ----
    val mid = primes.copyOfRange(0, 70)
    val midExact = subsetSumCountsExact(mid)
    val midMod = subsetSumCounts(mid, MOD)
    val biMod = BigInteger.valueOf(MOD)
    var maxExact = BigInteger.ZERO
    for (s in midExact.indices) {
        if (midExact[s] > maxExact) maxExact = midExact[s]
        check(midExact[s].mod(biMod).toLong() == midMod[s]) {
            "前 70 个素数：s=$s 处精确值与取模值不一致"
        }
    }
    check(maxExact > biMod) { "前 70 个素数的计数尚未越过 10^16，这一对照没有真正检验取模" }
    println(
        "自检 2：前 70 个素数（2^70 量级子集）BigInteger 精确计数与模 10^16 DP 逐项一致；" +
            "最大计数 $maxExact > 10^16，取模确实在发生",
    )

    // ---- 本题：669 个素数 ----
    val dp = subsetSumCounts(primes, MOD)
    val answer = countPrimeSums(dp, isPrime, MOD)
    println("答案（全部子集中元素和为素数者，取后 16 位）= ${answer.toString().padStart(16, '0')}")

    // ---- 自检 3（全量质量守恒）：Σ_s dp[s] = 2^669；奇数和子集数 = 2^668 ----
    // S 里唯一的偶素数是 2：668 个奇素数取奇数个 / 偶数个的方案各 2^667，含不含 2 再各乘 2，
    // 于是偶数和、奇数和的子集**各** 2^668 = 2^669 / 2。这条恒等式覆盖 DP 的全部输出。
    var all = 0L
    var odd = 0L
    for (s in dp.indices) {
        all += dp[s]
        if (all >= MOD) all -= MOD
        if (s and 1 == 1) {
            odd += dp[s]
            if (odd >= MOD) odd -= MOD
        }
    }
    check(all == pow2Mod(669, MOD)) { "全部子集数不等于 2^669：$all" }
    check(odd == pow2Mod(668, MOD)) { "奇数和子集数不等于 2^668：$odd" }
    println("自检 3（全量）：Σ dp[s] ≡ 2^669、奇数和子集数 ≡ 2^668 (mod 10^16)，均吻合")
    check(answer == ANSWER) { "答案与预期不符：$answer" }

    // ---- 计时：JIT 预热后单次完整求解（DP + 素数和汇总）----
    subsetSumCounts(primes, MOD)                     // 预热
    var best = Double.MAX_VALUE
    repeat(3) {
        val t0 = System.nanoTime()
        val dpAgain = subsetSumCounts(primes, MOD)
        val answerAgain = countPrimeSums(dpAgain, isPrime, MOD)
        val ms = (System.nanoTime() - t0) / 1e6
        check(answerAgain == answer) { "计时循环结果漂移" }
        if (ms < best) best = ms
    }
    println("optimized: ${"%.0f".format(best)} ms/次（预热 1 次后 3 次取最优，含 DP + 素数和汇总）")
}
