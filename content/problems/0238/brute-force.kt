#!/usr/bin/env kotlin
/**
 * Project Euler 238 — brute-force：直接枚举 substring 的数位和与最早起始位置。
 *
 * 不用位集、不用周期、不用任何优化：对一个有限前缀 w_0 = s_0 s_1 ... s_K，
 * 逐起点 z 向前累加数位和，用一个 HashMap<和, 最早起点> 记录首次出现的位置。
 * 只能在小 K 上跑（本文件取 K = 2000，约 2.4 万位），用来和 solution.kt 的
 * 前 1000 个 p(k) 逐项对拍，并复核题面校验 Σ_{k=1..1000} p(k) = 4742。
 *
 * 注意：这个朴素定义对本题的完整规模是不可行的 —— w 有 10^{15} 量级的位；
 * 而且它不处理「加整周期」这件事，solution.kt 的周期性结论是独立的一层。
 */

private const val S0 = 14025256L
private const val MOD = 20300713L

private fun main() {
    val K = 2000
    val sb = StringBuilder()
    var s = S0
    for (i in 0 until K) {
        sb.append(s)
        s = (s * s) % MOD
    }
    val w = sb.toString()
    val n = w.length
    println("prefix digits = $n (K=$K)")

    // first[k] = 数位和恰为 k 的子串的最小起始下标（1-based）；未出现则不在表中
    val first = HashMap<Int, Int>()
    for (start in 0 until n) {
        var sum = 0
        for (end in start until n) {
            sum += w[end] - '0'
            if (sum > 1000) break          // 只关心 k <= 1000
            val prev = first[sum]
            if (prev == null || start + 1 < prev) first[sum] = start + 1
        }
    }

    var total = 0L
    val missing = ArrayList<Int>()
    for (k in 1..1000) {
        val p = first[k]
        if (p == null) missing += k else total += p
    }
    println("missing k in 1..1000: ${missing.size}${if (missing.isEmpty()) "" else " -> $missing"}")
    println("sum_{k=1..1000} p(k) = $total   (题面给出 4742)")
    check(total == 4742L) { "mismatch" }

    // 打印前 30 项，供与 solution.kt 的输出逐项比对
    print("p(1..30) = ")
    for (k in 1..30) print("${first[k]} ")
    println()

    // 抽样：k=1,5,7 应为 1；k=4,6,11 应为 2；k=2,9 应为 3（题面举例）
    check(first[1] == 1 && first[5] == 1 && first[7] == 1) { "pos-1 examples failed" }
    check(first[4] == 2 && first[6] == 2 && first[11] == 2) { "pos-2 examples failed" }
    check(first[2] == 3 && first[9] == 3) { "pos-3 examples failed" }
    println("brute-force OK")
}
