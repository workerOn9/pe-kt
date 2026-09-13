/**
 * Project Euler 095 — Amicable Chains
 *
 * 优化解：先用线性倍率筛（对每个 d 把 d 加到 d 的全部倍数上）在 O(N log N) 内求出
 * 每个 n ≤ 10^6 的真因数和 σ(n)。然后按起点从小到大的顺序走链，用 pos 数组记录
 * 每个数在当前路径中的位置：撞上路径内的数就得到一个环，取环长与环内最小值；
 * 撞上已处理过的数或走出 10^6 就直接剪枝。因为链一旦离开当前起点就不可能再产生
 * 未发现的环，每个数最多被访问常数次。
 *
 * 复杂度：时间 O(N log N + N·L)（N = 10^6，L 为链长），空间 O(N)
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

fun solve(): Long {
    val limit = 1_000_000
    val sigma = IntArray(limit + 1)
    for (d in 1..limit / 2) {
        var m = d + d
        while (m <= limit) {
            sigma[m] += d
            m += d
        }
    }

    val pos = IntArray(limit + 1) { -1 } // -1 未访问；≥0 在当前路径中的下标；-2 已处理完
    var bestLen = 0
    var bestMin = 0
    for (start in 1..limit) {
        if (pos[start] != -1) continue
        val path = ArrayList<Int>()
        var cur = start
        while (true) {
            if (cur < 1 || cur > limit) break
            if (pos[cur] == -2) break
            if (pos[cur] >= 0) {
                val idx = pos[cur]
                val cycleLen = path.size - idx
                var mn = Int.MAX_VALUE
                for (i in idx until path.size) if (path[i] < mn) mn = path[i]
                if (cycleLen > bestLen || (cycleLen == bestLen && mn < bestMin)) {
                    bestLen = cycleLen
                    bestMin = mn
                }
                break
            }
            pos[cur] = path.size
            path.add(cur)
            cur = sigma[cur]
        }
        for (v in path) pos[v] = -2
    }
    return bestMin.toLong()
}

fun main() {
    println(solve())
}
