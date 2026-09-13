/**
 * Project Euler 023 — 暴力解（教学对比用）
 *
 * 试除法判盈数；然后对每个 n 顺序扫描盈数列表，
 * 查 n - a 是否也是盈数，O(limit × 盈数个数) 次集合查询。
 */

/** 试除法求 n 的真因子之和 */
fun d(n: Int): Int {
    if (n < 2) return 0
    var sum = 1
    var f = 2
    while (f * f <= n) {
        if (n % f == 0) {
            sum += f
            val other = n / f
            if (other != f) sum += other
        }
        f++
    }
    return sum
}

fun solveBruteForce(limit: Int = 28123): Long {
    val abundants = (12..limit).filter { d(it) > it }
    val abundantSet = abundants.toHashSet()
    var sum = 0L
    for (n in 1..limit) {
        var canExpress = false
        for (a in abundants) {
            if (a > n / 2) break
            if (n - a in abundantSet) {
                canExpress = true
                break
            }
        }
        if (!canExpress) sum += n
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
