/**
 * PE 160 — Factorial trailing digits.
 *
 * Let f(N) be the last 5 non-zero digits of N!. Find sum_{n=1}^{10^9} f(n!).
 */
fun solve(): Long {
    var total = 0L
    for (n in 1L..1_000_000_000L) {
        total += trailingNonZeroDigits(n)
    }
    return total
}

/** Returns last 5 non-zero digits of n! as a Long (mod 10^5 after removing factors 2,5). */
fun trailingNonZeroDigits(n: Long): Long {
    var cnt2 = 0L
    var cnt5 = 0L
    var temp = n
    while (temp > 0) {
        cnt2 += temp / 2; temp /= 2
    }
    temp = n
    while (temp > 0) {
        cnt5 += temp / 5; temp /= 5
    }
    val extra2 = cnt2 - cnt5
    // Compute n! with all factors 2 and 5 removed, mod 10^5, then multiply 2^extra2
    val mod = 100000L
    var result = 1L
    for (i in 1L..n) {
        var x = i
        while (x % 2 == 0L) x /= 2
        while (x % 5 == 0L) x /= 5
        result = (result * (x % mod)) % mod
    }
    result = (result * powMod(2L, extra2, mod)) % mod
    return result
}

fun powMod(base: Long, exp: Long, mod: Long): Long {
    var result = 1L
    var b = base % mod
    var e = exp
    while (e > 0) {
        if (e and 1 == 1L) result = (result * b) % mod
        b = (b * b) % mod
        e /= 2
    }
    return result
}

fun main() {
    repeat(2) { solve() }
    val start = System.nanoTime()
    val ans = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(ans)
}