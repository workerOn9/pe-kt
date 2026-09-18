/**
 * Project Euler 128 — 暴力对照解（教学对比用）
 *
 * 与 solution.kt 的思路不同，本文件分两段：
 *
 * 【第一段·按定义朴素地扫】literalTerms(kMax)：老老实实走六边形螺旋，把第 1…kMax+1 环的
 * 「坐标 → 编号」全建出来，再对范围内的**每一块砖**取它的六个邻居、作差、用试除法判素、
 * 数出 PD。它不预设任何结构，作用是把「PD = 3 只可能出现在环起始砖与环结束砖」这一条
 * 经验事实（也是 solution.kt 的立论基础）在 48 万余块砖上实测一遍：扫出来的序列必须与
 * 第二段按结构算出的候选序列逐项相同，且 10 号砖必须是题面给的 271。
 *
 * 【第二段·足规模出答案】solveBruteForce()：素性一律用试除（不用筛法），不做模 5 剪枝，
 * 对每个 k 把环起始砖与环结束砖的六个差值原样列出（含两个 1），**按定义数其中素数的个数**，
 * 等于 3 才收进序列——不依赖「6k−6、6k、6k+6 必为合数」这类推理，与 solution.kt 的
 * 筛法查表 + 余数剪枝是两套机器。差距来源：优化解对 12k+5 以内做一次筛法（O(L log log L)），
 * 之后每环只查表；本解对每一环现算 12 个素性判断，其中素数要试除到 √(12k+5) ≈ 913
 * （6 步轮式约 150 轮、每轮两次除法），k 扫到 69563 时累计 83 万余次判断，因此慢一个多数量级。
 *
 * 复杂度：literalTerms 时间 O(kMax²·√(12·kMax))，空间 O(kMax²)；
 * solveBruteForce 时间 O(K√K)，空间 O(1)，K 为第 2000 项所在的环号。
 * 实测：本机 literalTerms(400) 覆盖环 1…400 共 481201 块砖，solveBruteForce 是本解计时的对象。
 *
 * 题面样例（PD(8) = 3、PD(17) = 2、第 10 块砖是 271）写成运行时断言。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

/** 试除法素性判断：只按 6m ± 1 的形式跳着除，够快又不依赖任何筛表。 */
fun isPrimeTrial(n: Long): Boolean {
    if (n < 2L) return false
    if (n % 2L == 0L) return n == 2L
    if (n % 3L == 0L) return n == 3L
    var d = 5L
    while (d * d <= n) {
        if (n % d == 0L) return false
        if (n % (d + 2L) == 0L) return false
        d += 6L
    }
    return true
}

/** 环 k 起始砖的编号。 */
fun ringStart(k: Int): Long = 3L * k * k - 3L * k + 2L

/** 环 k 结束砖的编号。 */
fun ringEnd(k: Int): Long = 3L * k * k + 3L * k + 1L

/** 环 k 起始砖与六个邻居的差值（k ≥ 2）。 */
fun startDiffs(k: Int): LongArray = longArrayOf(1L, 6L * k - 6, 6L * k - 1, 6L * k, 6L * k + 1, 12L * k + 5)

/** 环 k 结束砖与六个邻居的差值（k ≥ 2）。 */
fun endDiffs(k: Int): LongArray = longArrayOf(1L, 6L * k - 1, 6L * k, 6L * k + 5, 6L * k + 6, 12L * k - 7)

/** 按定义数一组差值里素数的个数——就是 PD 的定义本身，1 不是素数自然不计。 */
fun countPrimes(diffs: LongArray): Int {
    var count = 0
    for (d in diffs) if (isPrimeTrial(d)) count++
    return count
}

// ---------------------------------------------------------------------------
// 第一段：按定义走螺旋、逐块砖算 PD
// ---------------------------------------------------------------------------

private const val GRID_SHIFT = 1 shl 12                        // 坐标打包用的偏移（环数远小于 4096）

private fun pack(x: Int, y: Int): Long = (x + GRID_SHIFT).toLong() * 8192L + (y + GRID_SHIFT)

private fun unpackX(c: Long): Int = (c / 8192L).toInt() - GRID_SHIFT

private fun unpackY(c: Long): Int = (c % 8192L).toInt() - GRID_SHIFT

/** 六边形坐标系的六个方向，用于取邻居。 */
private val NEIGHBOUR_DIRS = intArrayOf(0, -1, -1, 0, -1, 1, 0, 1, 1, 0, 1, -1)

/**
 * 老老实实按题面走螺旋：返回 [打包坐标 → 砖号] 与 [砖号 n 的打包坐标存在下标 n − 1]。
 * 第 1…rings 环的编号与题面一致（12 点方向起逆时针），中心是 1。
 */
private fun spiralGrid(rings: Int): Pair<HashMap<Long, Long>, ArrayList<Long>> {
    // 每个角点 (cornerX, cornerY)，以及从该角点沿环逆时针出发的方向 step（前五个边长 k，最后一个 k − 1）
    val cornerX = intArrayOf(0, -1, -1, 0, 1, 1)
    val cornerY = intArrayOf(-1, 0, 1, 1, 0, -1)
    val stepX = intArrayOf(-1, 0, 1, 1, 0, -1)
    val stepY = intArrayOf(1, 1, 0, -1, -1, 0)
    val value = HashMap<Long, Long>()
    val coord = ArrayList<Long>()
    value[pack(0, 0)] = 1L
    coord.add(pack(0, 0))
    for (k in 1..rings) {
        for (q in 0..5) {
            for (r in 0 until k) {
                val x = cornerX[q] * k + r * stepX[q]
                val y = cornerY[q] * k + r * stepY[q]
                value[pack(x, y)] = ringStart(k) + q.toLong() * k + r
                coord.add(pack(x, y))
            }
        }
    }
    return value to coord
}

/** 按定义算一块砖的 PD：在螺旋网格里取它的六个邻居编号作差，数其中素数的个数。 */
private fun pdOfTileIn(grid: Pair<HashMap<Long, Long>, ArrayList<Long>>, n: Long): Int {
    val (value, coord) = grid
    val c = coord[(n - 1).toInt()]
    val x = unpackX(c)
    val y = unpackY(c)
    var primes = 0
    var i = 0
    while (i < 12) {
        val other = value[pack(x + NEIGHBOUR_DIRS[i], y + NEIGHBOUR_DIRS[i + 1])]
        if (other != null) {
            val diff = Math.abs(other - n)
            if (diff >= 2L && isPrimeTrial(diff)) primes++
        }
        i += 2
    }
    return primes
}

/**
 * 按定义逐块砖扫描：返回第 1…kMax 环中所有 PD = 3 的砖号（升序）。
 * 网格多建一环，保证最外环的邻居也查得到。
 */
fun literalTerms(kMax: Int): List<Long> {
    val grid = spiralGrid(kMax + 1)
    val terms = ArrayList<Long>()
    for (n in 1L..ringEnd(kMax)) {
        if (pdOfTileIn(grid, n) == 3) terms.add(n)
    }
    return terms
}

// ---------------------------------------------------------------------------
// 第二段：足规模枚举，按定义数素数
// ---------------------------------------------------------------------------

/** 第 nth 块 PD = 3 的砖：逐环列出两类缝砖的六个差值，按定义数素数，不剪枝不筛法。 */
fun solveBruteForce(nth: Int = 2000): Long {
    if (nth <= 2) return if (nth == 1) 1L else 2L             // 中心砖 1 与第一环起始砖 2 是特例
    var count = 2
    var k = 2
    while (true) {
        if (countPrimes(startDiffs(k)) == 3) {
            count++
            if (count == nth) return ringStart(k)
        }
        if (countPrimes(endDiffs(k)) == 3) {
            count++
            if (count == nth) return ringEnd(k)
        }
        k++
    }
}

fun verifySample() {
    // 题面手工样例：绕砖 8 的差 12, 29, 11, 6, 1, 13 ⇒ PD(8) = 3；绕砖 17 的差 1, 17, 16, 1, 11, 10 ⇒ PD(17) = 2
    check(pdOfTile(8) == 3) { "PD(8) 应为 3，实得 ${pdOfTile(8)}" }
    check(pdOfTile(17) == 2) { "PD(17) 应为 2，实得 ${pdOfTile(17)}" }
    check(pdOfTile(1) == 3) { "PD(1) 应为 3，实得 ${pdOfTile(1)}" }
    check(pdOfTile(2) == 3) { "PD(2) 应为 3，实得 ${pdOfTile(2)}" }
    // 按定义逐块砖扫描：序列开头与题面锚点（第 10 块 271）
    val literal = literalTerms(9)
    check(literal.take(6) == listOf(1L, 2L, 8L, 19L, 20L, 37L)) {
        "按定义扫描的序列开头不符：${literal.take(6)}"
    }
    check(literal.size == 10 && literal[9] == 271L) {
        "环 1…9 内按定义应恰有 10 块 PD = 3 的砖、第 10 块为 271，实得 ${literal.size} 块、末块 ${literal.last()}"
    }
    // 结构对照：按「缝砖」判别式预测的候选序列必须与按定义逐块扫描的结果完全相同
    check(seamTerms(9) == literal) { "结构预测与按定义扫描不符：${seamTerms(9)} vs $literal" }
    check(seamTerms(300) == literalTerms(300)) { "环 1…300 内结构预测与按定义扫描不符" }
    check(seamTerms(400) == literalTerms(400)) { "环 1…400 内结构预测与按定义扫描不符" }
    // 足规模解在小规模上也要落到题面锚点
    check(solveBruteForce(10) == 271L) { "暴力解的第 10 块应为 271，实得 ${solveBruteForce(10)}" }
}

/** 按结构（缝砖判别）枚举到第 kMax 环，得到与 literalTerms 同范围的序列。 */
fun seamTerms(kMax: Int): List<Long> {
    val terms = ArrayList<Long>()
    terms.add(1L)
    terms.add(2L)
    for (k in 2..kMax) {
        if (countPrimes(startDiffs(k)) == 3) terms.add(ringStart(k))
        if (countPrimes(endDiffs(k)) == 3) terms.add(ringEnd(k))
    }
    return terms
}

/** 按定义算单块砖的 PD（网格建到 12 环，够覆盖砖 1…17 的邻居）。 */
fun pdOfTile(n: Long): Int = pdOfTileIn(spiralGrid(12), n)

/** 第一段按定义扫描的规模（环数）。 */
const val LITERAL_RINGS = 400

fun main() {
    verifySample()
    val scanStart = System.nanoTime()
    val literal = literalTerms(LITERAL_RINGS)
    System.err.printf(
        "literal scan (rings 1..%d, %d tiles): %.4f ms, terms=%d%n",
        LITERAL_RINGS, ringEnd(LITERAL_RINGS), (System.nanoTime() - scanStart) / 1e6, literal.size
    )
    repeat(3) { solveBruteForce() }                           // JIT 预热
    var best = Double.MAX_VALUE                               // 取多次最小值，压住机器负载抖动
    var answer = 0L
    repeat(5) {
        val start = System.nanoTime()
        answer = solveBruteForce()
        val ms = (System.nanoTime() - start) / 1e6
        if (ms < best) best = ms
    }
    System.err.printf("brute: %.4f ms%n", best)
    println(answer)
}
