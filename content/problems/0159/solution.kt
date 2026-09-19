/**
 * PE 159 — Digital root sums of factorisations.
 *
 * Define mdr(n): product of digits of n (no leading zeros). Repeatedly apply until single digit → digital root.
 * mdrs(n) = final single digit.
 *
 * For n = p1·p2·...·pk (prime factorisation with repetition), compute mdrs of each factor.
 * Find all products of three such mdrs values that sum to n.
 * Sum all such n ≤ 250000.
 */
fun solve(): Long {
    // precompute mdr(n) for n ≤ 250000
    val maxN = 250_000
    val mdr = IntArray(maxN + 1)
    for (n in 1..maxN) {
        var x = n
        while (x >= 10) {
            var d = 1
            var y = x
            while (y > 0) {
                d *= y % 10
                y /= 10
            }
            x = d
        }
        mdr[n] = x
    }
    // Collect all triples (a,b,c) where a·b·c ≤ maxN
    var total = 0L
    for (a in 1..maxN) {
        val ma = mdr[a]
        for (b in a..maxN / a) {
            val mb = mdr[b]
            val prodAB = a * b
            for (c in b..maxN / prodAB) {
                val mc = mdr[c]
                val prod = prodAB * c
                if (mdr[prod] == ma + mb + mc) total += prod
            }
        }
    }
    return total
}

fun main() {
    repeat(2) { solve() }
    val start = System.nanoTime()
    val ans = solve()
    System.err.printf("optimized: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(ans)
}