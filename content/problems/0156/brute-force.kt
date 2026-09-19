/**
 * Project Euler 156 — brute force reference (slow).
 * Enumerates all n in [0, 10^12] and checks f(n, d) == n.
 * Total work ~10^12 * 12 ≈ 10^13 ops: infeasible.
 * Kept for documentation only.
 */
fun bruteForce(d: Int): Long {
    fun countDigit(n: Long, d: Int): Long {
        if (n <= 0) return 0
        var count = 0L
        var pow10 = 1L
        while (pow10 <= n) {
            val high = n / (pow10 * 10)
            val cur = (n / pow10) % 10
            val low = n % pow10
            count += high * pow10
            if (cur > d) count += pow10
            else if (cur == d) count += low + 1
            pow10 *= 10
        }
        return count
    }
    var sum = 0L
    for (n in 0L..100000L) {  // truncated for sanity
        if (countDigit(n, d) == n) sum += n
    }
    return sum
}

fun main() {
    val s = bruteForce(1)
    println(s)
}