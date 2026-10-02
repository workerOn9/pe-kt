#!/usr/bin/env kotlin
/**
 * Project Euler 303 — Multiples with Small Digits（小数字倍数）
 *
 * 题目：f(n) = n 的最小正整数倍数，其十进制写法只含数字 0、1、2。
 * 已知 Σ_{n=1}^{100} f(n)/n = 11363107，求 Σ_{n=1}^{10000} f(n)/n。
 *
 * 思路推导
 * ────────
 * 对固定的 n，目标是找「位数最少、同位数下字典序最小」的 {0,1,2} 数字串 x，使 x ≡ 0 (mod n)。
 * 这是余数图上的最短路：节点 = 模 n 的余数，边 = 在已有数字后追加 0/1/2（x → 10x + c）。
 * BFS 按 (位数, 字典序) 逐层扩展（队列初始为 1、2 两个一位数，层内先试 0 再 1 再 2），
 * 首次到达余数 0 的 x 即 f(n)。用 parent[nr]/digit[nr] 记录前驱用于重构，O(n) 状态。
 *
 * 正确性：同层 BFS 先到先得；不同层按位数排序——因此队列整体严格按
 * 「位数少优先、同位数字典序小优先」出队，首个余数为 0 的节点就是最小倍数。
 *
 * 复杂度：每个 n 的状态空间 O(n)，总 O(Σ n) = O(10000²/2) = 5×10^7 量级，毫秒级。
 * 注意 f(9999) 为 20 位数，超出 Long 上限，必须用 BigInteger；最终答案 16 位数仍在 Long 内。
 *
 * 验证
 * ────
 * 1. Σ_{n=1}^{100} f(n)/n = 11363107（题面样例）——程序先打印验证；
 * 2. 样例 f(2)=2、f(3)=12、f(7)=21、f(42)=210、f(89)=1121222 逐一核对。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 *       （本机无 kotlinc，用 AGENTS.md 的 compiler-embeddable shim 编译运行）
 */

import java.math.BigInteger
import kotlin.system.measureNanoTime

/** 求 f(n)：n 的最小只含 {0,1,2} 的倍数（BFS 余数图，返回十进制值）。f(9999) 有 20 位，须用 BigInteger。 */
private fun f(n: Int): BigInteger {
    if (n == 1) return BigInteger.ONE
    val parent = IntArray(n) { -1 }   // 前驱余数；-2 表示一位数根
    val digit = IntArray(n)
    val q = ArrayDeque<Int>()
    for (d in 1..2) {                 // 一位数不能以 0 开头
        val r = d % n
        if (parent[r] == -1) { parent[r] = -2; digit[r] = d; q.addLast(r) }
    }
    while (q.isNotEmpty()) {
        val r = q.removeFirst()
        if (r == 0) {
            val sb = StringBuilder()
            var cur = 0
            while (true) {
                sb.append(digit[cur])
                if (parent[cur] == -2) break
                cur = parent[cur]
            }
            return sb.reverse().toString().toBigInteger()
        }
        for (c in 0..2) {
            val nr = (r * 10 + c) % n
            if (parent[nr] == -1) { parent[nr] = r; digit[nr] = c; q.addLast(nr) }
        }
    }
    error("no multiple for n = $n")
}

fun main() {
    // 题面样例核对
    val samples = listOf(2 to BigInteger("2"), 3 to BigInteger("12"), 7 to BigInteger("21"), 42 to BigInteger("210"), 89 to BigInteger("1121222"))
    for ((n, expect) in samples) {
        val got = f(n)
        println("f($n) = $got ${if (got == expect) "✓" else "✗ 期望 $expect"}")
    }
    // 100 验证
    var sum100 = BigInteger.ZERO
    for (n in 1..100) sum100 += f(n) / n.toBigInteger()
    println("Σ_{n=1}^{100} f(n)/n = $sum100 ${if (sum100 == BigInteger("11363107")) "✓ 与题面一致" else "✗ 期望 11363107"}")

    // 10000 答案（预热后计时）
    val warm = f(9999); val warm2 = f(9998)
    var sum = BigInteger.ZERO
    val t0 = System.nanoTime()
    for (n in 1..10000) sum += f(n) / n.toBigInteger()
    val ms = (System.nanoTime() - t0) / 1_000_000.0
    println("f(9999) = $warm（预热校验，${warm.toString().length} 位）, f(9998) = $warm2")
    println("Σ_{n=1}^{10000} f(n)/n = $sum（实测 ${"%.2f".format(ms)} ms）")
    println("ANSWER: $sum")
}
