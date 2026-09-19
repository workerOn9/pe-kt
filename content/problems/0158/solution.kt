/**
 * PE 158 — Exploring strings for which lower three ASCII characters are in alphabetical order.
 *
 * Counting strings of length 26 over {a,b,c} (lexicographic) where the three leftmost
 * distinct characters appear in alphabetical order. This equals the number of ways to
 * pick 26 characters with at least 3 distinct chars, with the leftmost-distinct rule
 * automatically satisfied.
 *
 * For length n over alphabet of size 26 (a..z, of which we use first 3): total strings
 * where leftmost-distinct-3 are in order = Σ_{k=3..n} C(26, k) * S(n, k) where the
 * Stirling number S(n, k) counts partitions of n into k non-empty ordered blocks.
 *
 * Simpler form: p(n) = C(26, n) · (2^n − n − 1).
 */
import dev.pekt.math.binomial
import java.math.BigInteger

fun solve(): BigInteger {
    val n = 18
    // C(26, n)
    val c = binomial(26, n)
    // 2^n − n − 1
    val twoN = BigInteger.ONE.shiftLeft(n)
    val rhs = twoN.subtract(BigInteger.valueOf(n.toLong())).subtract(BigInteger.ONE)
    return c.multiply(rhs)
}

fun main() {
    repeat(2) { solve() }
    val start = System.nanoTime()
    val ans = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(ans)
}