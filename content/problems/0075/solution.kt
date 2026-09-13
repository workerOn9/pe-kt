/**
 * Project Euler 075 — Singular Integer Right Triangles
 *
 * 优化解：周长由参数化 (m, n, k) 唯一刻画：
 *   a = k(m²−n²), b = 2kmn, c = k(m²+n²), L = 2k·m(m+n)   (m > n, m−n 奇, gcd(m,n)=1)
 * m−n 为奇数意味着 m、n 一奇一偶，于是 m+n 为奇数，周长 2m(m+n) 必为偶数，最小周长为 12。
 * 只枚举本原参数对，再在计数数组里按 k 的倍数打标记，最后数「恰好被标记一次」的 L。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val limit = 1_500_000
    val count = IntArray(limit + 1)
    var m = 2
    while (2L * m * (m + 1) <= limit) {
        for (n in 1 until m) {
            if (((m - n) and 1) == 1 && gcdNat75(m, n) == 1) {
                val base = 2 * m * (m + n)
                var p = base
                while (p <= limit) {
                    count[p]++
                    p += base
                }
            }
        }
        m++
    }
    var total = 0L
    for (l in 1..limit) if (count[l] == 1) total++
    return total
}

fun gcdNat75(a: Int, b: Int): Int {
    var x = a
    var y = b
    while (y != 0) {
        val t = x % y
        x = y
        y = t
    }
    return x
}

fun main() {
    println(solve())
}
