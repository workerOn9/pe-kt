#!/usr/bin/env kotlin
/**
 * Project Euler 244 — brute-force：与 solution.kt 完全不同的实现路径（枚举，而非 DP 聚合）。
 *
 * solution.kt 的做法是：位打包成 Int + 定长 IntArray 当哈希表 + 在最短路 DAG 上做
 * 一次正向 DP，把「所有最短路的 checksum 之和」用线性递推聚合出来。
 *
 * 本文件刻意把每一处都换掉，用来当裁判：
 *   1. 局面不打包成 Int，而是用 16 个字符的 String（'K' 空位 / 'R' / 'B'）当 key，
 *      距离表用 HashMap<String, Integer>；
 *   2. 不做 DP 聚合，改成**递归 DFS 把所有最短路逐条走出来**，
 *      每找到一条完整路径就从头模拟一遍 checksum（不做任何模运算合并），
 *      最后把逐条结果加起来；
 *   3. 额外做一次**反向** BFS（S→u 的 distT 只算 u→T），
 *      用 distS[u] + distT[u] == distS[T] 过滤出「确实在最短路上」的状态，
 *      再独立数一遍最短路条数 —— 与正向 DP 的 paths 互证。
 *
 * 慢在 String 拼接 + 装箱 + 哈希，这正是耗时对比表里「优化前」那一行。
 *
 * 答案：96356848
 */

private const val MOD = 100000007
private val MOVE_CHARS = charArrayOf('L', 'R', 'U', 'D')

private val START = "KRBBRRBBRRBBRRBB"
private val TARGET = "KBRBBRBRRBRBBRBR"
private val EXAMPLE = "RRBBRBBBRKRBRRBB"

/** 把与空位相邻的滑块沿 m 方向推一格；推不动返回 null。字母指**滑块滑动方向**。 */
private fun push(board: String, m: Char): String? {
    val k = board.indexOf('K')
    val r = k / 4
    val c = k % 4
    val nr = when (m) { 'L', 'R' -> r; 'U' -> r + 1; else -> r - 1 }
    val nc = when (m) { 'L' -> c + 1; 'R' -> c - 1; else -> c }
    if (nr < 0 || nr > 3 || nc < 0 || nc > 3) return null
    val t = nr * 4 + nc
    val arr = board.toCharArray()
    arr[k] = arr[t]
    arr[t] = 'K'
    return String(arr)
}

private fun neighbours(board: String): List<Pair<Char, String>> {
    val out = ArrayList<Pair<Char, String>>(4)
    for (m in MOVE_CHARS) {
        val v = push(board, m) ?: continue
        out.add(Pair(m, v))
    }
    return out
}

/** 从 src 出发 BFS，返回 key → 最短步数，并保留 BFS 出队顺序。 */
private fun bfs(src: String, dst: String): Pair<HashMap<String, Int>, ArrayList<String>> {
    val dist = HashMap<String, Int>()
    val order = ArrayList<String>()
    dist[src] = 0
    order.add(src)
    var head = 0
    while (head < order.size) {
        val u = order[head++]
        val d = dist.getValue(u) + 1
        for ((_, v) in neighbours(u)) if (!dist.containsKey(v)) {
            dist[v] = d
            order.add(v)
        }
    }
    if (!dist.containsKey(dst)) error("目标不可达")
    return Pair(dist, order)
}

/** 核心：两次 BFS（正向 + 反向）筛出最短路上的状态，再逐条枚举最短路模拟 checksum。 */
private fun solve(verbose: Boolean = false): SolveResult {
    // --- 正向 BFS：distS[u] = S → u 的最少步数 ---
    val (distS, _) = bfs(START, TARGET)
    val best = distS.getValue(TARGET)

    // --- 反向 BFS：图是无向的，从 T 出发 BFS 得到的就是每个 u → T 的最少步数 ---
    val (distT, _) = bfs(TARGET, START)

    // 只保留真正落在最短路上的状态：distS[u] + distT[u] == distS[T]
    val onShortest = HashSet<String>()
    for (u in distS.keys) if (distS.getValue(u) + distT.getValue(u) == best) onShortest.add(u)

    // --- 独立数一遍最短路条数（只在 onShortest 上做，纯递归，不做 DP 聚合） ---
    fun countPaths(u: String): Long {
        if (u == TARGET) return 1L
        var total = 0L
        for ((_, v) in neighbours(u)) {
            if (v in onShortest && distS.getValue(v) == distS.getValue(u) + 1) total += countPaths(v)
        }
        return total
    }
    val pathCount = countPaths(START)

    // --- 逐条枚举所有最短路，每条单独模拟 checksum，不做任何模运算合并 ---
    var checksumSum = 0L
    var enumerated = 0
    var longest = 0
    fun walk(u: String, code: String, value: Long) {
        if (u == TARGET) {
            enumerated++
            if (verbose && code.length > longest) {
                longest = code.length
                println("最短路（共 $best 步）: $code")
                println("  checksum = $value")
            }
            checksumSum = (checksumSum + value) % MOD
            return
        }
        for ((m, v) in neighbours(u)) {
            if (v in onShortest && distS.getValue(v) == distS.getValue(u) + 1) {
                walk(v, code + m, (243 * value + m.code) % MOD)
            }
        }
    }
    walk(START, "", 0L)
    return SolveResult(best, pathCount, enumerated, checksumSum, distS.size)
}

private data class SolveResult(
    val best: Int,
    val pathCount: Long,
    val enumerated: Int,
    val checksumSum: Long,
    val size: Int,
)

fun main() {
    // --- 题面样例 ---
    var cur = START
    var cs = 0L
    for (ch in "LULUR") {
        val next = push(cur, ch) ?: error("样例路径走不通")
        cur = next
        cs = (243 * cs + ch.code) % MOD
    }
    println("LULUR == E : ${cur == EXAMPLE}, checksum = $cs (题面 19761398)")
    check(cur == EXAMPLE && cs == 19761398L) { "题面样例校验失败" }

    // --- 计时：先空跑一轮让 JIT 编译完，再取 3 轮的中位数 ---
    solve()
    val samples = LongArray(3) {
        val t = System.nanoTime()
        solve()
        (System.nanoTime() - t) / 1_000_000
    }.sorted()
    val res = solve(verbose = true)
    val best = res.best
    val pathCount = res.pathCount
    val enumerated = res.enumerated
    val checksumSum = res.checksumSum
    val size = res.size

    println("局面总数      = $size")
    println("最短步数      = $best")
    println("最短路条数    = $pathCount (枚举实得 $enumerated)")
    println("checksum 之和 = $checksumSum")
    println("耗时(JIT 预热后 3 轮中位数) = ${samples[1]} ms")
    check(checksumSum == 96356848L) { "答案与预期不符" }
}
