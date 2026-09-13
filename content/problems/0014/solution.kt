/**
 * Project Euler 014 — Longest Collatz Sequence
 *
 * 优化解：记忆化搜索，均摊 O(n)。
 * 关键观察：不同起点的链大量重叠（13 → 40 → ... 的尾巴和 40 的链完全相同），
 * 所以缓存「到达 1 还需要的步数」，每条链只在第一次经过时真正计算。
 * 注意链中数值会超过一百万（题目允许），所以：
 *   - 链上当前值用 Long 防止 3n+1 溢出 Int；
 *   - 缓存只覆盖 [1, limit) 内的值，超出的不缓存（也不影响正确性）。
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(limit: Int = 1_000_000): Int {
    val cache = IntArray(limit)          // 0 = 未知；cache[n] = 从 n 到 1 的链长
    cache[1] = 1
    val path = ArrayList<Long>()         // 本次链上尚未知道长度的节点
    var bestStart = 1
    var bestLen = 1
    for (start in 2 until limit) {
        path.clear()
        var n = start.toLong()
        while (n >= limit || cache[n.toInt()] == 0) {
            path.add(n)
            n = if (n % 2 == 0L) n / 2 else 3 * n + 1
        }
        var len = cache[n.toInt()]       // 撞上了已知长度的节点
        for (i in path.size - 1 downTo 0) {
            len++
            val v = path[i]
            if (v < limit) cache[v.toInt()] = len
        }
        if (len > bestLen) {
            bestLen = len
            bestStart = start
        }
    }
    return bestStart
}

fun main() {
    println(solve())
}
