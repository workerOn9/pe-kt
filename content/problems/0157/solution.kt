/**
 * PE 157 — Base-10 Diophantine Reciprocal.
 *
 * Count (a, b, p, n) with 1 ≤ n ≤ 9, 1 ≤ a ≤ b, 1/a + 1/b = p/10^n.
 *
 * Method: (a+b)·10^n = p·a·b, p ≥ 1, so (a+b)·10^n ≥ a·b.
 * Since a ≤ b ≤ 2·10^n (from 1/a ≥ 1/(2·10^n) ⇒ a ≤ 2·10^n, and 1/b ≥ 1/a so b ≥ a).
 *
 * O(n · (2·10^n)²) ≈ 9·4·10^18 for n=9: too slow.
 * Better: iterate a, solve for b = a·10^n / (p·a - 10^n), enumerate valid (p, b).
 *
 * Tightest bound: a ≤ 10^n (since 1/a ≥ p/10^n ≥ 1/10^n).
 * For each a, p must satisfy p·a > 10^n (denominator positive) and b = a·10^n / (p·a - 10^n) ≥ a.
 */
fun solve(): Long {
    var total = 0L
    for (n in 1..9) {
        val tenPow = pow10(n)
        for (a in 1..tenPow) {
            // p·a - 10^n must divide a·10^n; p·a - 10^n > 0
            // d ranges over divisors of a·10^n that are < a (so b = a·10^n/d ≥ a; and p = (10^n + d)/a integer)
            val num = a.toLong() * tenPow.toLong()
            // Enumerate divisors d of num, d < a, d > 0; p = (10^n + d)/a must be integer.
            val divisors = divisorsOf(num)
            for (d in divisors) {
                if (d >= a) continue
                val ten = tenPow.toLong()
                if ((ten + d) % a.toLong() == 0L) total++
            }
        }
    }
    return total
}

fun pow10(n: Int): Int {
    var r = 1
    repeat(n) { r *= 10 }
    return r
}

fun divisorsOf(n: Long): List<Long> {
    val ds = mutableListOf<Long>()
    var i = 1L
    while (i * i <= n) {
        if (n % i == 0L) {
            ds.add(i)
            if (i != n / i) ds.add(n / i)
        }
        i++
    }
    return ds
}

fun main() {
    repeat(2) { solve() }
    val start = System.nanoTime()
    val ans = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(ans)
}