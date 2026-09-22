package dev.pekt.problems

/**
 * Problem 190: Maximising a Weighted Product (Double Precision Baseline)
 *
 * 使用原生 Double 浮点数与 Math.pow 进行快速评估的对照基准。
 */

fun solve0190BruteForce(): Long {
    fun computePmFloor(m: Int): Long {
        val denom = (m + 1).toDouble()
        var prod = 1.0
        for (i in 1..m) {
            val base = 2.0 * i / denom
            prod *= Math.pow(base, i.toDouble())
        }
        return prod.toLong()
    }

    var totalSum = 0L
    for (m in 2..15) {
        totalSum += computePmFloor(m)
    }

    return totalSum
}

fun main() {
    println(solve0190BruteForce())
}
