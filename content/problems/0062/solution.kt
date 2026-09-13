/**
 * Project Euler 062 — Cubic Permutations
 *
 * 思路：互为数字排列的立方体，其「数位多重集」完全相同。
 * 把数位计数向量压成指纹：立方数最多 12 位，每位数字出现次数 < 16，
 * 用 4 bit 存一个计数，key = Σ count[d]·2^(4d)（40 bit，装进 Long）。
 * 按 key 装箱，箱内的数两两互为排列；取大小为 5 的箱中最小值，再取全局最小。
 * 上界：最小的满足条件的立方体有 12 位，故枚举 n = 1..9999（n^3 < 10^12 覆盖全部 12 位及以下立方体），
 * 13 位立方体不可能与 12 位数互为排列。
 * 复杂度：O(N) 次取位运算 + 哈希（N = 10^4），指纹不用排序也不用分配字符串。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private fun digitKey(n: Long): Long {
    var x = n
    var key = 0L
    while (x > 0L) {
        key += 1L shl ((x % 10L).toInt() * 4)
        x /= 10L
    }
    return key
}

fun solve(): Long {
    val groups = HashMap<Long, MutableList<Long>>()
    var n = 1L
    while (n < 10000L) {
        val c = n * n * n
        groups.getOrPut(digitKey(c)) { ArrayList() }.add(c)
        n++
    }

    var best = Long.MAX_VALUE
    for (list in groups.values) {
        if (list.size != 5) continue
        for (c in list) if (c < best) best = c
    }
    return best
}

fun main() {
    println(solve())
}
