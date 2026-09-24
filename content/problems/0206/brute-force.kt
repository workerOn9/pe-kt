#!/usr/bin/env kotlin
// PE 206 暴力参照：同一区间、同一校验，但反向扫描（从上界往下），
// 用于验证解的唯一性位置与 solution.kt 互为对照。
// 构建：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar

fun main() {
    fun check19(s0: Long): Boolean {
        var s = s0
        var digit = 0L
        for (k in 0..9) {
            if (s % 10 != digit) return false
            s /= 100
            digit = if (digit == 0L) 9L else digit - 1
        }
        return true
    }
    var found = -1L
    var n = 1_414_213_562L
    val nMin = 1_000_000_000L
    var others = 0
    while (n >= nMin) {
        if (check19(n * n)) {
            if (found == -1L) found = n else others++
        }
        n--
    }
    println("$found extra=$others")
}
