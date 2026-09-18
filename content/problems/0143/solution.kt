/**
 * Project Euler 143 — Torricelli Triangles（托里拆利三角形）
 *
 * 思路：设费马点 T 到三个顶点的距离为 p = TA、q = TC、r = TB。T 处三个夹角都是 120°，
 * 对三角形 ATB、BTC、CTA 用余弦定理（cos 120° = −1/2，平方项与交叉项同号相加）得
 *
 *   a² = q² + qr + r²,   b² = p² + pq + q²,   c² = p² + pr + r²,
 *
 * 其中 a = BC、b = CA、c = AB。也就是说每一对距离 (x, y) 都必须使 x² + xy + y² 为完全平方数
 * （下称「120° 对」）。反过来，p, q, r > 0 且三对关系都成立时，把三段以 120° 夹角拼起来
 * 恰好得到三角形 ABC，费马点 T 在其内部、各内角 < 120°，所以条件与题面完全等价。
 *
 * 于是问题化为图论：以正整数为顶点、120° 对为边建图，找所有三点两两相邻（三角形）的三元组
 * {p, q, r}，要求 p + q + r ≤ 120000，最后对**不同的和**去重再求和。
 *
 * 120° 对不必 O(L²) 枚举，它有完整参数化（m > n > 0）：
 *
 *   x = k(m² − n²),  y = k(2mn + n²),  x² + xy + y² = [k(m² + mn + n²)]²,  x + y = k·m(m + 2n)。
 *
 * 固定 (m, n) 后倍数只有 k ≤ L / (m(m+2n)) 个，故边数远小于 L²（实测去重后 147707 条边）。
 * 存图用序列化边数组排序去重 + CSR 上邻接表（每个顶点只存比它大的邻居），
 * 三角形则对每条边 (a,b) 求 adj(a) ∩ adj(b) 的有序交集，命中即累加不同的和。
 *
 * 复杂度：边数 E，生成 O(E log E)（排序，实测原始 277767 条候选、去重后 147707 条边），
 * 三角形枚举 O(Σ_{(a,b)∈E} (deg a + deg b))；空间 O(L + E)。
 *
 * 可独立运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */

/** x² + xy + y² 若为完全平方数则返回其平方根，否则返回 −1。 */
fun sqrtIfSquare(x: Long, y: Long): Long {
    val v = x * x + x * y + y * y
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return if (r * r == v) r else -1L
}

/** 把 {x, y}（x ≠ y，均 < 2²⁰）编码成一个 Long，编码序与 x < y 的字典序一致。 */
fun edgeKey(x: Int, y: Int): Long =
    if (x < y) (x.toLong() shl 20) or y.toLong() else (y.toLong() shl 20) or x.toLong()

/** 参数化枚举全部满足 x + y ≤ limit 的 120° 对，返回编码数组（含重复，未排序）。 */
fun encodeEdges(limit: Int): LongArray {
    var cap = 1 shl 18
    var arr = LongArray(cap)
    var n = 0
    var m = 2
    while (m.toLong() * m <= limit) {                    // x + y = m(m + 2n) ≥ m²
        var nn = 1
        while (nn < m && m.toLong() * m + 2L * m * nn <= limit) {
            val base = m * m + 2 * m * nn                 // (m, nn) 基元的 x + y
            val x0 = m * m - nn * nn
            val y0 = 2 * m * nn + nn * nn
            var k = 1
            while (k.toLong() * base <= limit) {
                if (n == cap) {
                    cap *= 2
                    arr = arr.copyOf(cap)
                }
                arr[n++] = edgeKey(k * x0, k * y0)
                k++
            }
            nn++
        }
        m++
    }
    return arr.copyOf(n)
}

/** 最近一次构图 / 搜索得到的边数、三角形个数与不同的和个数，仅用于诊断输出。 */
var lastEdgeCount = 0
var lastTriangleCount = 0
var lastSumCount = 0

/**
 * CSR 形式的**上邻接表**：只保留顶点 v → 比 v 大的邻居（升序）。
 * 返回长度 limit + 2 的前缀数组 start，adj 为邻居序列，顶点 v 的邻居是 adj[start[v]..<start[v+1])。
 * 只存上邻接是有意的：和 ≤ limit 的三元组里任意两数之和必 ≤ limit，截断不丢解；
 * 且三角形 (a<b<c) 的 c 必在 adj(a) 与 adj(b) 中同时出现，交集即候选。
 */
fun buildUpperAdjacency(limit: Int): Pair<IntArray, IntArray> {
    val edges = encodeEdges(limit)
    java.util.Arrays.sort(edges)                          // 按 (小, 大) 字典序，顺带完成去重
    var e = 0
    for (i in edges.indices) {
        if (i == 0 || edges[i] != edges[i - 1]) edges[e++] = edges[i]
    }
    lastEdgeCount = e
    val deg = IntArray(limit + 1)
    for (i in 0 until e) deg[(edges[i] ushr 20).toInt()]++
    val start = IntArray(limit + 2)
    for (v in 1..limit) start[v + 1] = start[v] + deg[v]
    val fill = start.copyOf()
    val adj = IntArray(e)
    for (i in 0 until e) {
        val lo = (edges[i] ushr 20).toInt()
        val hi = (edges[i] and 0xFFFFFL).toInt()
        adj[fill[lo]++] = hi                              // edges 有序 ⇒ 每个 lo 段升序
    }
    return Pair(start, adj)
}

/** 所有满足 p + q + r ≤ limit 的托里拆利三角形的**不同**和。 */
fun triangleSums(limit: Int): HashSet<Int> {
    val (start, adj) = buildUpperAdjacency(limit)
    val sums = HashSet<Int>()
    var found = 0
    for (a in 1 until limit) {
        val as0 = start[a]
        val ae = start[a + 1]
        if (as0 == ae) continue
        for (ia in as0 until ae) {
            val b = adj[ia]
            var i = as0
            var j = start[b]
            val be = start[b + 1]
            while (i < ae && j < be) {                    // 两条升序链求交
                val u = adj[i]
                val v = adj[j]
                when {
                    u < v -> i++
                    u > v -> j++
                    else -> {                             // u 是 a、b 的公共邻居，且 u > b > a
                        if (a + b + u > limit) break      // u 随 i 单调不减
                        sums.add(a + b + u)
                        found++
                        i++
                        j++
                    }
                }
            }
        }
    }
    lastTriangleCount = found
    lastSumCount = sums.size
    return sums
}

fun solve(limit: Int = 120_000): Long {
    var total = 0L
    for (s in triangleSums(limit)) total += s
    return total
}

fun verifySample() {
    // 题面样例：a = 399、b = 455、c = 511，对应的费马点距离是 p = 195、q = 264、r = 325。
    check(sqrtIfSquare(195, 264) == 399L) { "195/264 应给出 399" }
    check(sqrtIfSquare(195, 325) == 455L) { "195/325 应给出 455" }
    check(sqrtIfSquare(264, 325) == 511L) { "264/325 应给出 511" }
    check(195L + 264 + 325 == 784L) { "样例三距离之和应为 784" }
    // 该三元组能被搜索到；784 是本题最小和，故把上限压到 783 时应无解
    check(triangleSums(784).contains(784)) { "上限 784 时应含 784" }
    check(triangleSums(783).isEmpty()) { "上限 783 时应无解" }
    // 边界：x = y 时 3x² 不是平方；(1,2) 不是 120° 对；(3,5) 是（3² + 15 + 5² = 7²）
    check(sqrtIfSquare(1, 1) == -1L)
    check(sqrtIfSquare(1, 2) == -1L)
    check(sqrtIfSquare(3, 5) == 7L)
}

fun main() {
    verifySample()
    repeat(10) { solve() }                                // JIT 预热
    val times = DoubleArray(9)
    var answer = 0L
    for (i in times.indices) {                            // 本机并发负载极重，取多轮最小值作基准
        val t0 = System.nanoTime()
        answer = solve()
        times[i] = (System.nanoTime() - t0) / 1e6
    }
    times.sort()
    System.err.printf(
        "optimized: %.4f ms (9 轮最小值；中位数 %.4f，最大 %.4f；edges=%d, triangles=%d, distinctSums=%d)%n",
        times[0], times[4], times[8], lastEdgeCount, lastTriangleCount, lastSumCount
    )
    println(answer)
}
