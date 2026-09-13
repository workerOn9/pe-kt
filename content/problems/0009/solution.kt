/**
 * Project Euler 009 — Special Pythagorean Triplet
 *
 * 优化解：欧几里得公式参数化。
 * 任意本原勾股数可写成 a = m²−n², b = 2mn, c = m²+n²（m > n，互质且一奇一偶），
 * 非本原的再乘倍数 k。此时 a+b+c = 2km(m+n)，代入 1000 得 km(m+n) = 500，
 * 所以 m ≤ sqrt(500) ≈ 22，枚举量从 O(n²) 降到几十次。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun solve(sum: Int = 1000): Long {
    val half = sum / 2                       // km(m+n) = sum/2
    var m = 2
    while (m * (m + 1) <= half) {            // n ≥ 1 ⇒ m(m+n) ≥ m(m+1)
        for (n in 1 until m) {
            if (gcd(m, n) != 1 || (m - n) % 2 == 0) continue   // 本原条件
            val mmn = m * (m + n)
            if (half % mmn != 0) continue
            val k = half / mmn
            val a = k * (m * m - n * n)
            val b = k * 2 * m * n
            val c = k * (m * m + n * n)
            return a.toLong() * b * c
        }
        m++
    }
    error("无解")
}

fun main() {
    println(solve())
}
