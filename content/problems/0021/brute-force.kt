/**
 * Project Euler 021 — 暴力解（教学对比用）
 *
 * 每个数单独用试除法（试到 √n）求真因子和，n 个数共 O(n√n)。
 */

/** 试除法求 n 的真因子之和 */
fun d(n: Int): Int {
    if (n < 2) return 0
    var sum = 1                             // 1 总是真因子
    var f = 2
    while (f * f <= n) {
        if (n % f == 0) {
            sum += f
            val other = n / f
            if (other != f) sum += other    // 平方数只加一个因子
        }
        f++
    }
    return sum
}

fun solveBruteForce(limit: Int = 10000): Long {
    var sum = 0L
    for (a in 2 until limit) {
        val b = d(a)
        if (b > a && d(b) == a) sum += a + b
    }
    return sum
}

fun main() {
    println(solveBruteForce())
}
