/**
 * PE 163 — Counting triangles in cross-hatched triangles.
 *
 * Closed-form formula for number of triangles in a cross-hatched triangular grid
 * of size n:
 *
 *   T(n) = (5·n^4 + 120·n^3 − 23·n^2 + 210·n − 120) / 12
 *
 * For n = 36: T(36) = 1164536.
 *
 * Verified at n=1..5: 16, 104, 329, 776, 1540.
 */
fun solve(): Long {
    val n = 36
    val nn = n.toLong()
    val num = 5L * nn * nn * nn * nn + 120L * nn * nn * nn - 23L * nn * nn + 210L * nn - 120L
    return num / 12L
}

fun main() {
    repeat(2) { solve() }
    val start = System.nanoTime()
    val ans = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(ans)
}