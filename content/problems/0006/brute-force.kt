/**
 * Project Euler 006 — 暴力解（教学对比用）
 *
 * 逐项枚举 1..n，累加平方与和的平方，O(n)。
 */

fun solveBruteForce(n: Long = 100): Long {
    var sum = 0L
    var sumSq = 0L
    for (i in 1..n) {
        sum += i
        sumSq += i * i
    }
    return sum * sum - sumSq
}

fun main() {
    println(solveBruteForce())
}
