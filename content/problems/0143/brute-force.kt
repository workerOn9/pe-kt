/**
 * Project Euler 143 — Torricelli Triangles：独立对照解（刻意与 solution.kt 思路不同）
 *
 * 题面条件同样归结为：费马点到三顶点的距离 p、q、r 两两满足 x² + xy + y² 为完全平方数，
 * 且这样的三元组 (a<b<c) 在「120° 对」图上两两相邻，求不同的 a+b+c ≤ 120000 之和。
 * 差别在两处关键实现：
 *
 * 1) 120° 对的生成。solution.kt 用 (m, n, k) 参数化「造」出全部边；这里完全不碰参数化，
 *    而是**从定义 x² + xy + y² = z² 出发逐个固定 x 解出 y**：
 *
 *      (2y + x)² = 4z² − 3x²  ⇒  记 w = 2y + x，则 (2z − w)(2z + w) = 3x²。
 *
 *    于是枚举 N = 3x² 的因子对 A·B = N（A ≤ B），令 A = 2z − w、B = 2z + w，得
 *    z = (A + B)/4、w = (B − A)/2、y = (w − x)/2；每个候选都用定义式 x² + xy + y² = z² 复核。
 *    x 的最小素因子由筛法给出，3x² 的因子指数 = x 的指数翻倍（素因子 3 再多一次）。
 *    这条路完全不依赖「参数化是否取遍所有解」这一前提，正好用来验证它。
 *
 * 2) 三角形搜索。solution.kt 对每条边求两端升序上邻接表的有序交集（无哈希）；
 *    这里对每条边 (a, b) 直接遍历 b 的邻居 c > b（升序，和超限即停），再用哈希集合
 *    查 (a, c) 是否也是边。
 *
 * 复杂度：因子枚举总量 Σ_{x<L} d(3x²) = 10299946（实测最大 d(3x²) = 1782，x = 110880），
 * 三角形部分对 b 的每个候选邻居做一次哈希查询，实测 739451 次；空间 O(L + E)。
 * 两条路径的边数（147707）与答案（30758397）完全一致。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 最小素因子筛：spf[i]（i ≥ 2）为 i 的最小素因子。 */
fun smallestPrimeFactor(n: Int): IntArray {
    val spf = IntArray(n + 1)
    var i = 2
    while (i <= n) {
        if (spf[i] == 0) {
            var j = i
            while (j <= n) {
                if (spf[j] == 0) spf[j] = i
                j += i
            }
        }
        i++
    }
    return spf
}

/** x² + xy + y² 若为完全平方数则返回其平方根，否则返回 −1（定义式直接判定）。 */
fun sqrtIfSquare(x: Long, y: Long): Long {
    val v = x * x + x * y + y * y
    var r = Math.sqrt(v.toDouble()).toLong()
    while (r > 0 && r * r > v) r--
    while ((r + 1) * (r + 1) <= v) r++
    return if (r * r == v) r else -1L
}

/** 边键：把 (小, 大) 打包进一个 Long（小的占高 20 位，大的占低 20 位）。 */
fun edgeKey(x: Int, y: Int): Long =
    if (x < y) (x.toLong() shl 20) or y.toLong() else (y.toLong() shl 20) or x.toLong()

var bruteEdgeCount = 0
var bruteTriangleCount = 0
var bruteDivSteps = 0L
var bruteLookups = 0L

/** 3x² 的因子最大个数实测为 1782，这里留足余量。 */
const val DIV_BUF = 4096

/**
 * 用「固定 x、解二次方程」的方式生成全部 x + y ≤ limit 的 120° 对，
 * 返回 (每点双向邻接表, 规范化边键集合)。
 */
fun generatePairs(limit: Int): Pair<HashMap<Int, MutableList<Int>>, HashSet<Long>> {
    val spf = smallestPrimeFactor(limit)
    val buf = LongArray(DIV_BUF)
    val primes = IntArray(8)
    val exps = IntArray(8)
    val adj = HashMap<Int, MutableList<Int>>()
    val keys = HashSet<Long>()
    bruteDivSteps = 0L
    for (x in 1 until limit) {
        // 把 x 分解成素因子列表，指数翻倍得到 x²；素因子 3 的指数再 +1 ⇒ 3x² 的指数
        var np = 0
        var t = x
        var idx3 = -1
        while (t > 1) {
            val p = spf[t]
            var e = 0
            while (t % p == 0) {
                t /= p
                e++
            }
            exps[np] = 2 * e
            if (p == 3) idx3 = np
            primes[np] = p
            np++
        }
        if (idx3 >= 0) exps[idx3]++ else {
            primes[np] = 3
            exps[np] = 1
            np++
        }
        // 展开全部因子
        buf[0] = 1L
        var cnt = 1
        for (i in 0 until np) {
            val p = primes[i].toLong()
            val base = cnt
            var mul = 1L
            for (j in 1..exps[i]) {
                mul *= p
                for (q in 0 until base) buf[cnt++] = buf[q] * mul
            }
        }
        check(cnt < DIV_BUF) { "x = $x 的因子数 $cnt 超出缓冲" }
        bruteDivSteps += cnt
        val n3 = 3L * x * x
        for (i in 0 until cnt) {
            val a = buf[i]
            val b = n3 / a
            if (b < a) continue                      // 只取 A = 2z − w ≤ B = 2z + w（用除法避免 A² 溢出）
            if ((a + b) % 4L != 0L) continue         // z = (A + B)/4 必须为整数
            val w = (b - a) / 2
            if ((w - x) % 2L != 0L) continue         // y = (w − x)/2 必须为整数
            val y = (w - x) / 2
            if (y < 1L || x + y > limit) continue
            val z = (a + b) / 4
            check(x.toLong() * x + x * y + y * y == z * z) { "($x, $y, $z) 不满足定义式" }
            val xi = x
            val yi = y.toInt()
            if (keys.add(edgeKey(xi, yi))) {
                adj.getOrPut(xi) { ArrayList() }.add(yi)
                adj.getOrPut(yi) { ArrayList() }.add(xi)
            }
        }
    }
    for (l in adj.values) l.sort()
    bruteEdgeCount = keys.size
    return Pair(adj, keys)
}

/**
 * 三角形搜索：对每条边 (a, b)（a < b）遍历 b 的邻居 c > b，再哈希查 (a, c)；
 * 由于邻居升序且需 a + b + c ≤ limit，和一旦超限即可停止。
 */
fun bruteTriangleSums(limit: Int): HashSet<Int> {
    val (adj, keys) = generatePairs(limit)
    val sums = HashSet<Int>()
    var tri = 0
    var lookups = 0L
    for ((a, na) in adj) {
        for (b in na) {
            if (b <= a) continue
            val nb = adj[b] ?: continue
            for (c in nb) {
                if (c <= b) continue
                if (a + b + c > limit) break
                lookups++
                if (keys.contains(edgeKey(a, c))) {
                    sums.add(a + b + c)
                    tri++
                }
            }
        }
    }
    bruteTriangleCount = tri
    bruteLookups = lookups
    return sums
}

fun solveBrute(limit: Int = 120_000): Long {
    var total = 0L
    for (s in bruteTriangleSums(limit)) total += s
    return total
}

fun verifySample() {
    // 题面样例：a = 399、b = 455、c = 511 对应 p = 195、q = 264、r = 325，p + q + r = 784
    check(sqrtIfSquare(195, 264) == 399L)
    check(sqrtIfSquare(195, 325) == 455L)
    check(sqrtIfSquare(264, 325) == 511L)
    check(195L + 264 + 325 == 784L)
    check(bruteTriangleSums(784).contains(784))
    check(bruteTriangleSums(783).isEmpty())
    check(sqrtIfSquare(1, 1) == -1L)
    check(sqrtIfSquare(1, 2) == -1L)
    check(sqrtIfSquare(3, 5) == 7L)
}

fun main() {
    verifySample()
    repeat(3) { solveBrute() }                            // JIT 预热
    val times = DoubleArray(5)
    var answer = 0L
    for (i in times.indices) {                            // 本机并发负载极重，取多轮最小值作基准
        val t0 = System.nanoTime()
        answer = solveBrute()
        times[i] = (System.nanoTime() - t0) / 1e6
    }
    times.sort()
    System.err.printf(
        "brute: %.4f ms (5 轮最小值；中位数 %.4f，最大 %.4f；edges=%d, triangles=%d, divSteps=%d, lookups=%d)%n",
        times[0], times[2], times[4], bruteEdgeCount, bruteTriangleCount, bruteDivSteps, bruteLookups
    )
    println(answer)
}
