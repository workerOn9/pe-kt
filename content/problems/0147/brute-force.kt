/**
 * Project Euler 147 — 暴力解（几何枚举，思路与 solution.kt 相反）
 *
 * solution.kt 在变换坐标 (α,β) = (x+y, y−x) 里用闭式计数公式直接数"菱形内的整数矩形"，
 * 一个图形都不构造；本解反过来把交叉阴影网格当图来画，按定义一层层枚举：
 *
 *   1. 每个小方格贡献 4 条边 + 2 条对角线，共 6 条单位线段（坐标统一放大 2 倍，全程整数）；
 *   2. 按支撑直线分组，沿行进方向排序后把首尾相接的线段合并成"极长墨迹线段"
 *      （水平 j = 0..m、竖直 i = 0..n、斜率 ±1 各 m+n−1 条）；
 *   3. 枚举两条互相垂直方向上的两对极长线段（水平×竖直、斜率 +1 × 斜率 −1），
 *      算出四条支撑直线的四个交点，逐点检查是否同时落在相应线段跨度内 —— 四点全落上
 *      才算一个矩形，直接累加。
 *
 * 与优化解的差距：这里没有任何计数公式，矩形是一个个画出来再验证的。倾斜方向要枚举
 * C(m+n−1,2)² 组线段对（仅按跨度做了剪枝），单网格约 O((m+n)^4)；全题规模 47×43 在本机
 * 跑不完，因此实测放在缩减规模 rows ≤ BRUTE_ROWS、cols ≤ BRUTE_COLS（440 个网格）上，
 * 该规模下的结果同时也被 solution.kt 的公式解（solve(20,22)）核对过（见下方断言）。
 *
 * 复杂度：单网格最坏 O((m+n)^4)（缩减规模里最大的 20×22 网格要枚举 820² ≈ 6.7×10⁵ 组线段对），
 * 空间 O((m+n)²)。
 *
 * 可独立运行：kotlinc brute-force.kt -include-runtime -d brute-force.jar && java -jar brute-force.jar
 */

private const val BRUTE_ROWS = 20
private const val BRUTE_COLS = 22

/** 一族极长墨迹线段：key 为支撑直线标识，t 区间 [lo,hi] 为线段跨度（坐标放大 2 倍）。 */
private class Family(val key: IntArray, val lo: IntArray, val hi: IntArray)

/** 把同一支撑直线上的单位线段排序、合并成极长线段；要求每条直线只留一条（图是连通的）。 */
private fun merge(map: HashMap<Int, ArrayList<IntArray>>): Family {
    val keys = ArrayList<Int>()
    val los = ArrayList<Int>()
    val his = ArrayList<Int>()
    for ((k, list) in map) {
        val sorted = list.sortedWith(compareBy({ it[0] }, { it[1] }))
        var cl = sorted[0][0]
        var ch = sorted[0][1]
        for (t in 1 until sorted.size) {
            val s = sorted[t]
            if (s[0] <= ch) {
                if (s[1] > ch) ch = s[1]
            } else {
                keys.add(k); los.add(cl); his.add(ch)
                cl = s[0]; ch = s[1]
            }
        }
        keys.add(k); los.add(cl); his.add(ch)
    }
    return Family(keys.toIntArray(), los.toIntArray(), his.toIntArray())
}

/** 画图：由每个方格的 6 条线段出发，合并出四族墨迹直线；返回 [水平, 竖直, 斜率+1, 斜率−1]。 */
private fun inkFamilies(rows: Int, cols: Int): Array<Family> {
    val h = HashMap<Int, ArrayList<IntArray>>()      // 直线 y = j，跨度在 x 上
    val v = HashMap<Int, ArrayList<IntArray>>()      // 直线 x = i，跨度在 y 上
    val up = HashMap<Int, ArrayList<IntArray>>()     // 直线 y = x + c，c = j − i
    val dn = HashMap<Int, ArrayList<IntArray>>()     // 直线 y = −x + d，d = i + j + 1
    fun add(m: HashMap<Int, ArrayList<IntArray>>, k: Int, a: Int, b: Int) =
        m.getOrPut(k) { ArrayList() }.add(intArrayOf(a, b))
    for (j in 0 until rows) {
        for (i in 0 until cols) {
            val x0 = 2 * i; val x1 = 2 * i + 2; val y0 = 2 * j; val y1 = 2 * j + 2
            add(h, j, x0, x1); add(h, j + 1, x0, x1)                  // 上下边
            add(v, i, y0, y1); add(v, i + 1, y0, y1)                  // 左右边
            add(up, j - i, x0, x1)                                    // (i,j)–(i+1,j+1)
            add(dn, i + j + 1, x0, x1)                                // (i,j+1)–(i+1,j)
        }
    }
    return arrayOf(merge(h), merge(v), merge(up), merge(dn))
}

/** 一个 rows×cols（m 行 n 列）交叉阴影网格里的矩形总数，四个交点全在墨迹上才计数。 */
fun countIn(rows: Int, cols: Int): Long {
    val fam = inkFamilies(rows, cols)
    val h = fam[0]; val v = fam[1]; val up = fam[2]; val dn = fam[3]
    var count = 0L

    // 轴对齐：两对水平/竖直线的四个交点都要落在相应跨度内
    for (a in h.key.indices) {
        for (b in a + 1 until h.key.size) {
            val yA = 2 * h.key[a]
            val yB = 2 * h.key[b]
            for (p in v.key.indices) {
                val xP = 2 * v.key[p]
                if (xP < h.lo[a] || xP > h.hi[a] || xP < h.lo[b] || xP > h.hi[b]) continue
                if (yA < v.lo[p] || yA > v.hi[p] || yB < v.lo[p] || yB > v.hi[p]) continue
                for (q in p + 1 until v.key.size) {
                    val xQ = 2 * v.key[q]
                    if (xQ < h.lo[a] || xQ > h.hi[a] || xQ < h.lo[b] || xQ > h.hi[b]) continue
                    if (yA < v.lo[q] || yA > v.hi[q] || yB < v.lo[q] || yB > v.hi[q]) continue
                    count++
                }
            }
        }
    }

    // 倾斜：斜率 +1 的两条线与斜率 −1 的两条线，交点 x = (d − c)/2（放大 2 倍即 d − c）
    for (a in up.key.indices) {
        for (b in a + 1 until up.key.size) {
            val ca = up.key[a]; val cb = up.key[b]
            // 必要条件：两条斜率 +1 线上的四个交点都要落在各自跨度内，由此给出 d 的可行区间。
            // 注意不能取两条 +1 线跨度的交集 —— 两条线的墨迹可以完全不重叠（例如 2×3 网格里
            // c = −2 的跨度是 [4,6]、c = 1 的是 [0,2]，却仍能与 d = 2,3 围出矩形）。
            val dLo = maxOf(ca + up.lo[a], cb + up.lo[b])
            val dHi = minOf(ca + up.hi[a], cb + up.hi[b])
            if (dLo > dHi) continue
            val cand = ArrayList<Int>()
            for (p in dn.key.indices) if (dn.key[p] in dLo..dHi) cand.add(p)
            for (u in cand.indices) {
                val p = cand[u]; val da = dn.key[p]
                if (da - ca !in dn.lo[p]..dn.hi[p] || da - cb !in dn.lo[p]..dn.hi[p]) continue
                for (w in u + 1 until cand.size) {
                    val q = cand[w]; val db = dn.key[q]
                    if (db - ca !in dn.lo[q]..dn.hi[q] || db - cb !in dn.lo[q]..dn.hi[q]) continue
                    // 四个交点 (X = d − c) 必须同时落在对应 +1 线与 −1 线的墨迹跨度上
                    if (da - ca in up.lo[a]..up.hi[a] && db - ca in up.lo[a]..up.hi[a] &&
                        da - cb in up.lo[b]..up.hi[b] && db - cb in up.lo[b]..up.hi[b]) count++
                }
            }
        }
    }
    return count
}

/** 所有 rows ≤ maxRows、cols ≤ maxCols 的网格里的矩形总数。 */
fun countAll(maxRows: Int = BRUTE_ROWS, maxCols: Int = BRUTE_COLS): Long {
    var total = 0L
    for (rows in 1..maxRows) for (cols in 1..maxCols) total += countIn(rows, cols)
    return total
}

fun verifySample() {
    val table = listOf(1 to 1 to 1L, 1 to 2 to 4L, 1 to 3 to 8L, 2 to 1 to 4L, 2 to 2 to 18L, 2 to 3 to 37L)
    for ((dim, want) in table) {
        val got = countIn(dim.first, dim.second)
        check(got == want) { "${dim.first}×${dim.second} 应为 $want，实得 $got" }
    }
    var cum = 0L
    for (rows in 1..2) for (cols in 1..3) cum += countIn(rows, cols)
    check(cum == 72L) { "3×2 及更小网格应累计 72，实得 $cum" }
    // 缩减规模上与公式解对齐（该常数来自 solution.kt 的 solve(20,22) 实跑输出）
    check(countAll() == 9611448L) { "rows≤20, cols≤22 应为 9611448，实得 ${countAll()}" }
}

fun main() {
    verifySample()
    repeat(3) { countAll() }                      // JIT 预热
    val start = System.nanoTime()
    val answer = countAll()
    System.err.printf("brute: %.4f ms%n", (System.nanoTime() - start) / 1e6)
    println(answer)
}
