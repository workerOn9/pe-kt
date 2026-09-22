package dev.pekt.problems

/**
 * Problem 189: Tri-colouring a Triangular Grid (Brute Force / HashMap DP Baseline)
 *
 * 基于 Map<List<Int>, Long> 的不可变状态转移实现，用于基准性能与正确性对照。
 */

fun solve0189BruteForce(): Long {
    var dp: Map<List<Int>, Long> = mapOf(
        listOf(0) to 1L,
        listOf(1) to 1L,
        listOf(2) to 1L
    )

    for (row in 2..8) {
        val nextDp = mutableMapOf<List<Int>, Long>()
        val k = row

        for ((prevU, ways) in dp) {
            var currentLayer = listOf(listOf(0) to ways, listOf(1) to ways, listOf(2) to ways)
            for (j in 0 until k - 1) {
                val p = prevU[j]
                val nextLayer = mutableListOf<Pair<List<Int>, Long>>()
                for ((uPref, w) in currentLayer) {
                    val uLeft = uPref.last()
                    for (uRight in 0..2) {
                        val set = mutableSetOf(p, uLeft, uRight)
                        val choices = 3 - set.size
                        if (choices > 0) {
                            nextLayer.add((uPref + uRight) to (w * choices))
                        }
                    }
                }
                currentLayer = nextLayer
            }
            for ((uList, w) in currentLayer) {
                nextDp[uList] = (nextDp[uList] ?: 0L) + w
            }
        }
        dp = nextDp
    }

    return dp.values.sum()
}

fun main() {
    println(solve0189BruteForce())
}
