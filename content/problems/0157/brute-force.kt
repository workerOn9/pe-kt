/**
 * PE 157 — brute force: iterate a, b directly over the bound, check (a+b)*10^n mod a*b == 0.
 * Infeasible for n=9 (~10^18 ops) — kept for small n sanity.
 */
fun bruteForce(n: Int): Long {
    val ten = pow10(n).toLong()
    var count = 0L
    for (a in 1..ten) {
        for (b in a..(2 * ten)) {
            if ((a + b) * ten % (a.toLong() * b) == 0L) count++
        }
    }
    return count
}

fun pow10(n: Int): Long {
    var r = 1L
    repeat(n) { r *= 10 }
    return r
}

fun main() {
    println(bruteForce(1))  // expect 20
    println(bruteForce(2))  // expect 108
}