#!/usr/bin/env kotlin
/**
 * Project Euler 301 — Nim（尼姆游戏）
 *
 * 题目：三堆石子普通尼姆，X(n1,n2,n3) 返回轮到行动方的胜负（零=必败）。
 * 求 1 ≤ n ≤ 2^30 中满足 X(n, 2n, 3n) = 0 的 n 的个数。
 *
 * 思路推导
 * ────────
 * 普通尼姆的必败判据是 Nim 和为零（Bouton 定理），即
 *   X(n1,n2,n3) = n1 ⊕ n2 ⊕ n3.
 * 于是 X(n,2n,3n)=0 ⟺ n ⊕ 2n ⊕ 3n = 0 ⟺ n ⊕ 2n = 3n。
 * 由于 3n = n + 2n，等式 n⊕2n = n+2n 成立当且仅当二进制加法不进位，
 * 即 n & (n<<1) == 0 ⟺ n 的二进制表示不含相邻的 1。
 * 问题化为：统计 1..2^30 中二进制无相邻 1 的整数个数。
 *
 * 数位 DP 直接实跑（不依赖闭式）：按二进制位从高位到低位扫描 N = 2^30，
 * 维护 (前缀与 N 相等 / 已小于 N) × (上一位 0 / 1) 四个状态计数，
 * 任何状态选 1 时若上一位为 1 则剪枝。最终计数含 n=0（全零串，无相邻 1），减去 1。
 *
 * 数学旁证：长度为 L 的无相邻 1 二进制串个数为斐波那契数 F_{L+2}
 * （以 F_1=F_2=1）。n ≤ 2^30 的计数 = (30 位内无相邻 1，含全零串 F_{32})
 *   + 2^30 本身（二进制 1000…0，无相邻 1）− 排除 n=0
 *   = F_{32} = 2178309。
 *
 * 验证
 * ────
 * 1. 数位 DP 输出 2178309，与斐波那契旁证 F_{32} 一致；
 * 2. n ≤ 2^16 时暴力枚举 (n,2n,3n) 异或检查，与同阈值 DP 结果一致。
 *
 * 复杂度：O(log N) 次转移（31 位），O(1) 空间；暴力对照 O(N)。
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

import kotlin.system.measureNanoTime

/** 统计 0..n 内二进制表示不含相邻 1 的整数个数（数位 DP，四状态）。 */
private fun countNoAdjacentUpTo(n: Long): Long {
    val bits = ArrayList<Int>()
    var m = n
    while (m > 0) { bits.add((m and 1L).toInt()); m = m shr 1 }
    if (bits.isEmpty()) bits.add(0)
    bits.reverse()
    // t0/t1: 前缀与 n 相等、上一位 0/1；f0/f1: 前缀已小于 n、上一位 0/1
    var t0 = 1L; var t1 = 0L; var f0 = 0L; var f1 = 0L
    for (b in bits) {
        var nt0: Long; var nt1: Long; var nf0: Long; var nf1: Long
        if (b == 0) {
            // tight 侧只能选 0（选 1 越界），0 == b 保持 tight
            nt0 = t0 + t1
            nt1 = 0L
            nf0 = f0 + f1
            nf1 = f0
        } else { // b == 1
            // tight 侧：(T,0) 选 0 → 已小于 → free0；(T,0) 选 1 → tight1；(T,1) 只能选 0 → free0
            nt0 = 0L
            nt1 = t0
            nf0 = t0 + t1
            nf1 = 0L
            // free 侧：上一位 0 可补 0/1，上一位 1 只能补 0
            nf0 += f0 + f1
            nf1 += f0
        }
        t0 = nt0; t1 = nt1; f0 = nf0; f1 = nf1
    }
    return t0 + t1 + f0 + f1
}

/** 暴力对照：统计 1..n 内 n ⊕ 2n ⊕ 3n == 0 的个数（小范围用）。 */
private fun bruteCount(limit: Int): Long {
    var c = 0L
    for (x in 1..limit) {
        val x2 = x.toLong() shl 1
        val x3 = x.toLong() * 3L
        if ((x.toLong() xor x2 xor x3) == 0L) c++
    }
    return c
}

fun main() {
    val limit = 1L shl 30

    val dpStart = System.nanoTime()
    val dpResult = countNoAdjacentUpTo(limit) - 1L  // 排除 n = 0
    val dpMs = (System.nanoTime() - dpStart) / 1_000_000.0

    // 数学旁证：F_{32}（无相邻 1 串数 = 斐波那契）
    var a = 1L; var b = 1L
    for (i in 3..32) { val c = a + b; a = b; b = c }
    val fib32 = b

    // 暴力对照（小范围）
    val bruteLimit = 1 shl 16
    val bruteStart = System.nanoTime()
    val brute = bruteCount(bruteLimit)
    val bruteMs = (System.nanoTime() - bruteStart) / 1_000_000.0
    val dpSmall = countNoAdjacentUpTo(bruteLimit.toLong()) - 1L

    println("DP result = $dpResult  (n ≤ 2^30, 排除 n=0)")
    println("Fibonacci 旁证 F_32 = $fib32  → ${if (dpResult == fib32) "一致" else "不一致!"}")
    println("暴力对照 n ≤ 2^16: brute=$brute, dp=$dpSmall → ${if (brute == dpSmall) "一致" else "不一致!"}")
    println("DP 耗时 ${"%.3f".format(dpMs)} ms；暴力(2^16) 耗时 ${"%.3f".format(bruteMs)} ms")
    println("ANSWER: $dpResult")
}
