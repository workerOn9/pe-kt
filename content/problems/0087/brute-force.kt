/**
 * Project Euler 087 — Prime Power Triples（暴力对照解）
 *
 * 思路：不预筛素数表，而是在枚举时用试除法当场判定素性，把所有可行的 p²+q³+r⁴ 丢进 HashSet 去重，
 * 最后看集合大小。与优化解的差别在于“记忆化方式”：哈希集合 vs 定长布尔数组，以及试除 vs 埃氏筛。
 * 复杂度：素数判定约 7000 次试除；三重枚举与优化解同阶，但 HashSet 的常数与内存开销明显更大。
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute.jar && java -jar brute.jar
 */

private const val p087Limit = 50_000_000

private fun p087IsPrimeByTrial(n: Int): Boolean {
    if (n < 2) return false
    var d = 2
    while (d.toLong() * d <= n) {
        if (n % d == 0) return false
        d++
    }
    return true
}

fun solveBruteForce(): Long {
    val primes = ArrayList<Int>()
    var n = 2
    while (n <= 7071) {
        if (p087IsPrimeByTrial(n)) primes.add(n)
        n++
    }
    val sums = HashSet<Int>()
    for (r in primes) {
        val r4 = r.toLong() * r * r * r
        if (r4 + 12 >= p087Limit) break
        for (q in primes) {
            val q3 = q.toLong() * q * q
            if (q3 + r4 + 4 >= p087Limit) break
            for (p in primes) {
                val p2 = p.toLong() * p
                val s = p2 + q3 + r4
                if (s >= p087Limit) break
                sums.add(s.toInt())
            }
        }
    }
    return sums.size.toLong()
}

fun main() {
    println(solveBruteForce())
}
