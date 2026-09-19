/**
 * PE 160 — brute force (too slow for 10^9, kept for small n sanity).
 */
fun bruteForce(limit: Int = 1000): Long {
    var total = 0L
    for (n in 1..limit) {
        var fact = 1L
        for (i in 1..n) {
            fact *= i
            while (fact % 10 == 0L) fact /= 10
            fact %= 100000L  // keep only last 5 non-zero digits
        }
        total += fact
    }
    return total
}

fun main() {
    println(bruteForce(10))  // should be 36288 for 10!
}