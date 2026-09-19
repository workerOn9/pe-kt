/**
 * PE 158 — brute force by enumeration of placements.
 * For each choice of "first-occurrence positions" (i < j < k) and assigned characters
 * (a < b < c), fill remaining n−3 positions with any of 3 chars.
 * Slow but correct.
 */
fun solveBrute(n: Int = 18): Long {
    var count = 0L
    for (i in 0 until n) {
        for (j in (i + 1) until n) {
            for (k in (j + 1) until n) {
                for (a in 0..25) for (b in (a + 1)..25) for (c in (b + 1)..25) {
                    // 3^(n-3) fillings
                    val fill = pow3(n - 3)
                    count += fill
                }
            }
        }
    }
    return count
}

fun pow3(k: Int): Long {
    var r = 1L
    repeat(k) { r *= 3 }
    return r
}

fun main() {
    println(solveBrute(5))  // small sanity
}