#!/usr/bin/env kotlin
/**
 * Project Euler 234 — Semidivisible Numbers：暴力对照
 *
 * 思路：完全不使用「相邻素数对分块」的结论，直接按定义逐个数判定：
 *   对每个 n 求出 lps(n)（从 ⌊√n⌋ 往下找第一个素数）与 ups(n)（从 ⌈√n⌉ 往上找第一个素数），
 *   再看这两个素数是否恰有一个整除 n。复杂度 O(N·素数间隙)，只适合小范围对拍。
 *
 * 用途：与 solution.kt 的 O(√N) 分块求和对照（N = 10⁶ 时两者必须一致）。
 *
 * 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 * 运行：java -jar brute.jar
 */

fun main() {
    val t0 = System.currentTimeMillis()
    val sp = 1010
    val isPrime = BooleanArray(sp + 1) { true }
    isPrime[0] = false
    isPrime[1] = false
    var i = 2
    while (i * i <= sp) {
        if (isPrime[i]) { var j = i * i; while (j <= sp) { isPrime[j] = false; j += i } }
        i++
    }
    for (n in longArrayOf(15L, 1000L, 1_000_000L)) {
        val spN = Math.sqrt(n.toDouble()).toInt() + 60
        val isP = BooleanArray(spN + 1) { true }
        isP[0] = false; isP[1] = false
        var q = 2
        while (q * q <= spN) { if (isP[q]) { var jj = q * q; while (jj <= spN) { isP[jj] = false; jj += q } }; q++ }
        var sum = 0L
        var cnt = 0
        for (x in 4..n) {
            var r = Math.sqrt(x.toDouble()).toLong()
            while (r * r > x) r--
            while ((r + 1) * (r + 1) <= x) r++
            var down = r
            while (down >= 2 && !isP[down.toInt()]) down--
            if (down < 2) continue
            var u = r
            if (u * u < x) u++
            while (!isP[u.toInt()]) u++
            val bad = (x % down == 0L)
            val high = (x % u == 0L)
            if (bad != high) { sum += x; cnt++ }
        }
        println("N=$n -> sum=$sum count=$cnt (${System.currentTimeMillis() - t0} ms)")
    }
}
