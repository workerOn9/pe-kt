/**
 * Project Euler 037 — Truncatable Primes
 *
 * 优化解：从 11 起递增搜索，一次遍历同时做「从左截」与「从右截」两类判定，
 * 收满题目给定的 11 个即停；用试除法判定沿途所有截断值。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun isPrimeLong(n: Long): Boolean {
    if (n < 2) return false
    if (n < 4) return true
    if (n % 2L == 0L) return false
    var d = 3L
    while (d <= n / d) { if (n % d == 0L) return false; d += 2 }
    return true
}

fun solve(): Long {
    var sum = 0L; var count = 0; var n = 11
    while (count < 11) {
        val s = n.toString()
        var ok = true
        var left = 0L; var right = 0L
        for (i in s.indices) { left = left * 10 + (s[i] - '0'); if (!isPrimeLong(left)) { ok = false; break } }
        if (ok) {
            var p = 10L
            while (p <= n) { right = n % p; if (!isPrimeLong(right)) { ok = false; break }; p *= 10 }
        }
        if (ok) { sum += n; count++ }
        n++
    }
    return sum
}

fun main() {
    println(solve())
}
