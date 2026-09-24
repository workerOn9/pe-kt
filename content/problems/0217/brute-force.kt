#!/usr/bin/env kotlin
// PE 217 — Balanced Numbers（平衡数）暴力参照
// 思路：直接枚举所有小于 10^n 的正整数，按位取出十进制数字，比较「前 ⌈k/2⌉ 位」与
//       「后 ⌈k/2⌉ 位」的数字和（k 为奇数时中间位被两段各算一次，等价于抵消）。
//       只适用于 n ≤ 6 的量级，用于校验题面锚点 T(1) = 45、T(2) = 540、T(5) = 334795890，
//       并给 DP 解提供小规模对照值。数位全部用除法/取模取出，不用 toString 数位数。
// 构建：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
// 运行：java -jar brute-force.jar

/** 判断 x 是否为平衡数：高半段数字和 == 低半段数字和（奇数位时中间位两边各算一次）。 */
private fun isBalanced(x: Int): Boolean {
    val digits = IntArray(8)
    var len = 0
    var v = x
    while (v > 0) {
        digits[len++] = v % 10
        v /= 10
    }
    val h = (len + 1) / 2
    var hi = 0
    var lo = 0
    for (i in 0 until h) {
        lo += digits[i]              // 最低 h 位
        hi += digits[len - 1 - i]    // 最高 h 位
    }
    return hi == lo
}

fun main() {
    val answers = LongArray(7)
    val t0 = System.nanoTime()
    var limit = 1L
    for (n in 1..6) {
        limit *= 10
        var s = 0L
        var x = 1
        while (x < limit) {
            if (isBalanced(x)) s += x
            x++
        }
        answers[n] = s
        println("T($n) = $s")
    }
    val ms = (System.nanoTime() - t0) / 1_000_000
    System.err.println("brute force n<=6 wall = $ms ms")
}
