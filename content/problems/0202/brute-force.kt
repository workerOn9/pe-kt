#!/usr/bin/env kotlin
// PE 202 暴力参照：直接枚举 m ∈ [1, S-1]，检查 m≡n (mod 3) 且 gcd(m,n)=1。
// O(S) 次 gcd —— 对 N=1000001（S≈5e5）毫秒级；对 N=12017639147（S≈6e9）需约一分钟级，
// 故本文件默认跑校验规模 N=1000001（期望 80840），真实 N 请用 solution.kt 的容斥版。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar [N]

fun main(args: Array<String>) {
    val n = if (args.isNotEmpty()) args[0].toLong() else 1000001L
    val s = (n + 3) / 2
    val target = (2 * (s % 3)) % 3
    var m = if (target >= 1) target else 3
    var cnt = 0L
    while (m <= s - 1) {
        if (gcd(m, s - m) == 1L) cnt++
        m += 3
    }
    println(cnt)
}

fun gcd(a: Long, b: Long): Long {
    var x = a; var y = b
    while (y != 0L) { val t = x % y; x = y; y = t }
    return x
}
