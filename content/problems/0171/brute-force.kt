/**
 * PE 171 — brute force：直接枚举 n 并逐位求平方和，只适合小上限（10^6 量级）。
 */
fun bruteForce(limit: Int): Long {
    var total = 0L
    for (n in 1 until limit) {
        var x = n
        var s = 0
        while (x > 0) { val d = x % 10; s += d * d; x /= 10 }
        val r = kotlin.math.sqrt(s.toDouble()).toInt()
        if (r * r == s) total = (total + n) % 1_000_000_000L
    }
    return total
}

fun main() {
    println("n<10^6 -> " + bruteForce(1_000_000))
}
