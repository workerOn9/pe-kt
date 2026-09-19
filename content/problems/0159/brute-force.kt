/**
 * PE 159 — brute force via mdr computation (O(n log n) for n up to 250000).
 */
fun bruteForce(): Long {
    val maxN = 250_000
    // Compute mdr for each number
    val mdr = IntArray(maxN + 1)
    for (n in 1..maxN) {
        var x = n
        while (x >= 10) {
            var d = 1
            var y = x
            while (y > 0) { d *= y % 10; y /= 10 }
            x = d
        }
        mdr[n] = x
    }
    // Compute mdrs via dr
    fun dr(x: Int): Int {
        return if (x <= 9) x else dr(x.toString().map { it.digitToInt() }.reduce { a, b -> a + b })
    }
    val mdrs = IntArray(maxN + 1) { i -> dr(mdr[i]) }
    // Find factorizations
    var total = 0L
    for (n in 3..maxN) {
        var sum = 0
        var temp = n
        while (temp > 1) {
            // find smallest prime factor
            var spf = 2
            while (spf <= temp / spf && temp % spf != 0) spf++
            val p = if (spf <= temp / spf) spf else temp
            sum += mdrs[p]
            temp /= p
        }
        // Check condition: need exactly 3 factors sum to n
        // Simplified: factorise n into exactly 3 primes (with repetition)
        val factors = mutableListOf<Int>()
        temp = n
        while (temp > 1) {
            var spf = 2
            while (spf <= temp / spf && temp % spf != 0) spf++
            val p = if (spf <= temp / spf) spf else temp
            factors.add(p)
            temp /= p
        }
        if (factors.size == 3) {
            val rhs = factors.map { mdrs[it] }.sum()
            if (mdrs[n] == rhs) total += n
        }
    }
    return total
}

fun main() {
    println(bruteForce())
}