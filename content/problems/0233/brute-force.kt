#!/usr/bin/env kotlin
/**
 * Project Euler 233 — 暴力对照：逐点枚举圆上的整点
 *
 * 完全按定义数点：对每个 N，x 从 −2N 扫到 2N（圆的直径 ≈ 1.41N，留足余量），
 * 解 y² − N·y + (x² − N·x) = 0，判判别式 D = N² + 4Nx − 4x² 是否为完全平方数。
 * 复杂度 O(N) 每个 N，只用于小范围验证闭式
 *    f(N) = 4·∏_{p ≡ 1 (4), p^e ‖ N} (2e+1)
 * 以及题面样例 f(10000) = 36。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar
 */

fun pointsOnCircle(n: Long): Long {
    var cnt = 0L
    var x = -2 * n
    while (x <= 2 * n) {
        val dd = n * n - 4 * x * x + 4 * n * x
        if (dd >= 0) {
            val s = Math.sqrt(dd.toDouble()).toLong()
            if (s * s == dd && (n + s) % 2 == 0L) {
                val y1 = (n + s) / 2
                val y2 = (n - s) / 2
                if (y1 * y1 - n * y1 + x * x - n * x == 0L) cnt++
                if (y2 != y1 && y2 * y2 - n * y2 + x * x - n * x == 0L) cnt++
            }
        }
        x++
    }
    return cnt
}

fun closedForm(n0: Long): Long {
    var n = n0
    while (n % 2 == 0L) n /= 2
    var res = 1L
    var p = 3L
    while (p * p <= n) {
        if (n % p == 0L) {
            var e = 0
            while (n % p == 0L) { n /= p; e++ }
            if (p % 4 == 1L) res *= (2 * e + 1)
        }
        p += 2
    }
    if (n > 1 && n % 4 == 1L) res *= 3
    return 4 * res
}

fun main() {
    val t0 = System.currentTimeMillis()
    var ok = true
    for (n in longArrayOf(5, 6, 15, 25, 45, 75, 105, 169, 225, 325, 455)) {
        val a = pointsOnCircle(n); val b = closedForm(n)
        if (a != b) ok = false
        println("N=$n brute=$a closed=$b")
    }
    println("f(10000) brute = " + pointsOnCircle(10000) + " (官方样例 36)")
    println("f(10000) closed = " + closedForm(10000))
    println("all small consistent: $ok  (${System.currentTimeMillis() - t0} ms)")
}
