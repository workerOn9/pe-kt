/**
 * Project Euler 056 — Powerful Digit Sum（暴力对照）
 *
 * 暴力解：对每一对 (a,b) 都从 1 开始连乘 b 次重新算出 a^b，再数字说和取最大。
 * 与优化解相比，重复计算了 a^1..a^(b-1) 这些中间幂，总乘法次数是优化解的约 50 倍。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

import java.math.BigInteger

fun solveBruteForce(): Long {
    var best = 0
    for (a in 2..99) {
        val base = BigInteger.valueOf(a.toLong())
        for (b in 1..99) {
            var power = BigInteger.ONE
            repeat(b) { power = power.multiply(base) }
            var s = 0
            for (c in power.toString()) s += c - '0'
            if (s > best) best = s
        }
    }
    return best.toLong()
}

fun main() {
    println(solveBruteForce())
}
