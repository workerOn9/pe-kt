#!/usr/bin/env kotlin
/**
 * PE 224 暴力对照 —— 按定义枚举边长对 (a, b)：
 *   c^2 = a^2 + b^2 + 1，c 为整数且 b <= c，周长 a + b + c <= N。
 * 复杂度 O(N^2/12)，仅小 N 可用；用于验证 solution.kt 的树 DFS。
 * 运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */
import kotlin.math.sqrt

private fun bruteCount(n: Long): Long {
    var cnt = 0L
    var a = 2L
    while (3 * a <= n) {
        var b = a
        while (a + 2 * b <= n) {
            val s = a * a + b * b + 1
            val c = sqrt(s.toDouble()).toLong()
            if (c >= b && c * c == s && a + b + c <= n) cnt++
            b++
        }
        a++
    }
    return cnt
}

fun main() {
    for (n in longArrayOf(1_000, 5_000, 20_000, 100_000)) {
        val t0 = System.nanoTime()
        val c = bruteCount(n)
        println("N=$n 暴力计数 = $c  (${(System.nanoTime() - t0) / 1_000_000} ms)")
    }
}
