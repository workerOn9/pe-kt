/**
 * Project Euler 021 — Amicable Numbers
 *
 * 优化解：筛法一次算出 1..limit 内所有数的真因子和 d(n)，O(n log n)，
 * 然后线性扫描找出所有亲和数对（a < b，d(a)=b 且 d(b)=a）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** 筛法求真因子和：枚举因子 f，把它累加到它的所有真倍数上 */
fun properDivisorSums(limit: Int): IntArray {
    val d = IntArray(limit)
    for (f in 1 until limit) {
        var m = 2 * f                       // 只加给 ≥2f 的倍数，保证 f 是真因子
        while (m < limit) {
            d[m] += f
            m += f
        }
    }
    return d
}

fun solve(limit: Int = 10000): Long {
    val d = properDivisorSums(limit)
    var sum = 0L
    for (a in 2 until limit) {
        val b = d[a]
        // b > a 去重（每对只计一次），同时天然排除 a == b 的完全数
        if (b > a && b < limit && d[b] == a) sum += a + b
    }
    return sum
}

fun main() {
    println(solve())
}
