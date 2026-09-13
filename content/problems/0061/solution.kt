/**
 * Project Euler 061 — Cyclical Figurate Numbers
 *
 * 思路：每个数只需记住「前两位」与「后两位」，循环条件即后一个数的前两位 = 前一个数的后两位，
 * 且六个数首尾相接。把 3..8 阶的全部四位多边形数按前两位建索引，
 * 再做 DFS：从任意一个数出发，转移时只能取「前两位 = 当前后两位」且阶数未被用过、
 * 数值未出现过的数，走到六个后检查末尾后两位是否等于起点的前两位。
 * 索引把每层分支从数百降到个位数。
 * 复杂度：DFS 深度 6，每层分支 ~3，实测毫秒级。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

private fun polygonal(s: Int, n: Int): Int = when (s) {
    3 -> n * (n + 1) / 2
    4 -> n * n
    5 -> n * (3 * n - 1) / 2
    6 -> n * (2 * n - 1)
    7 -> n * (5 * n - 3) / 2
    8 -> n * (3 * n - 2)
    else -> 0
}

fun solve(): Long {
    val byPrefix = HashMap<Int, MutableList<IntArray>>()
    for (s in 3..8) {
        var n = 1
        while (true) {
            val v = polygonal(s, n)
            if (v >= 10000) break
            if (v >= 1000) byPrefix.getOrPut(v / 100) { ArrayList() }.add(intArrayOf(v, s))
            n++
        }
    }

    val chain = ArrayList<Int>()
    var answer = 0L
    fun dfs(usedTypes: Int, firstPrefix: Int, prevSuffix: Int): Boolean {
        if (chain.size == 6) {
            if (prevSuffix == firstPrefix) {
                answer = chain.sumOf { it.toLong() }
                return true
            }
            return false
        }
        val next = byPrefix[prevSuffix] ?: return false
        for (e in next) {
            val v = e[0]
            val s = e[1]
            if (usedTypes and (1 shl s) != 0) continue
            if (chain.contains(v)) continue
            chain.add(v)
            if (dfs(usedTypes or (1 shl s), firstPrefix, v % 100)) return true
            chain.removeAt(chain.size - 1)
        }
        return false
    }

    for (list in byPrefix.values) {
        for (e in list) {
            chain.clear()
            chain.add(e[0])
            if (dfs(1 shl e[1], e[0] / 100, e[0] % 100)) return answer
        }
    }
    return answer
}

fun main() {
    println(solve())
}
