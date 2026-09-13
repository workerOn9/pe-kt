/**
 * Project Euler 087 — Prime Power Triples
 *
 * 思路：p² < 5×10^7 给出 p ≤ 7071，q³ < 5×10^7 给出 q ≤ 367，r⁴ < 5×10^7 给出 r ≤ 84，
 * 对这三个范围内的素数做三重枚举求和，用定长布尔数组把和去重后统计个数。
 * 复杂度：O(π(7071)·π(367)·π(84)) ≈ 900×73×23 ≈ 1.5×10^6 次求和。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private const val p087Limit = 50_000_000

private fun p087PrimesUpTo(n: Int): List<Int> {
    val composite = BooleanArray(n + 1)
    val out = ArrayList<Int>()
    for (i in 2..n) {
        if (!composite[i]) {
            out.add(i)
            if (i.toLong() * i <= n) {
                var j = i * i
                while (j <= n) { composite[j] = true; j += i }
            }
        }
    }
    return out
}

fun solve(): Long {
    val primes = p087PrimesUpTo(7071)
    val seen = BooleanArray(p087Limit)
    var count = 0L
    for (p in primes) {
        val p2 = p * p
        if (p2 + 8 + 16 >= p087Limit) break
        for (q in primes) {
            val q3 = q * q * q
            if (p2 + q3 + 16 >= p087Limit) break
            for (r in primes) {
                val r4 = r * r * r * r
                if (p2 + q3 + r4 >= p087Limit) break
                val s = p2 + q3 + r4
                if (!seen[s]) { seen[s] = true; count++ }
            }
        }
    }
    return count
}

fun main() {
    println(solve())
}
