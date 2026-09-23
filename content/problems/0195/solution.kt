package dev.pekt.problems

/**
 * Problem 195: 60-Degree Triangle Inscribed Circles
 *
 * 思路：利用 60° 三角形的参数化与 Dirichlet 除数问题
 *
 * 对于 60° 三角形，边长满足余弦定理 c^2 = a^2 + b^2 - ab。
 * 通过变换可证明：每个 60° 整数三角形对应于一对互素正整数 (p,q)，
 * 满足 p > q > 0 且 p ≢ q (mod 3)，以及
 *   a = k(2p-q), b = k(2q-p), c = k(p+q)
 * 其中 k 为正整数比例因子。
 *
 * 内切圆半径 r = (a+b-c)·tan(30°)/2 = ... 可推导得：
 *   r = k·(p^2-pq+q^2) / (2·√3)
 * 即 r ≤ n 当且仅当 k·(p^2-pq+q^2) ≤ 2n√3/... 经整理得：
 *   k ≤ n·2/√3 / (p^2-pq+q^2)
 *
 * 因此 T(n) = Σ_{p>q≥1, gcd(p,q)=1, p≢q(mod 3)} ⌊n·2/√3 / (p^2-pq+q^2)⌋
 *           + Σ_{p=q+3m, gcd(p,q)=1} ⌊n·6/√3 / (p^2-pq+q^2)⌋
 *
 * 这正是 Java 实现中的两个累加循环。
 *
 * 复杂度：O(n) 次除法，约 10^6 量级操作，可在毫秒内完成。
 *
 * kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve195(): Long {
    val n = 1053779L
    val maxr1 = (n * 2.0 / Math.sqrt(3.0)).toLong()
    val maxr2 = (n * 6.0 / Math.sqrt(3.0)).toLong()
    var result = 0L

    // 第一组：gcd(p,q)=1 且 p ≢ q (mod 3)
    var q = 1L
    while (q * q <= maxr1) {
        var p = q + 1L
        while (p * q <= maxr1) {
            if ((p - q) % 3 != 0L && gcd(p, q) == 1L) {
                result += maxr1 / (p * q)
            }
            p++
        }
        q++
    }

    // 第二组：p ≡ q (mod 3) 且 gcd(p,q)=1
    q = 1L
    while (q * q <= maxr2) {
        var p = q + 3L
        while (p * q <= maxr2) {
            if (gcd(p, q) == 1L) {
                result += maxr2 / (p * q)
            }
            p += 3L
        }
        q++
    }
    return result
}

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun main() { println(solve195()) }
