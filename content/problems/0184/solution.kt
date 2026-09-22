/**
 * PE 184 — 顶点取自 I_105、内部包含原点的三角形个数。
 *
 * 原理（射线分解）：
 * I_r = {(x,y) : x² + y² < r²}。三角形包含原点 ⟺ 三个顶点方向不共线、
 * 两两不反向、且三个方向角把圆周分成的三段弧均 < 180°。
 * 1) 按本原方向（射线）分组：每个非零格点除以 gcd(|x|,|y|) 归一化，
 *    设第 i 条射线上有 m_i 个点，总点数 N = Σ m_i。
 * 2) 三射线组合总数（e3 = 初等对称多项式）：
 *      U = (N³ − 3N·p2 + 2·p3)/6,  p2 = Σ m_i², p3 = Σ m_i³。
 * 3) 坏三角形 = 最大弧 ≥ 180°：
 *    a) 以某射线为「枢轴」：另两个射线落在枢轴逆时针 180° 开弧内。
 *       计 Σ_i m_i·(S_i² − Q_i)/2，S_i = 该开弧内点数，Q_i = Σ m_j²（去同射线对）。
 *    b) 含一对反向射线：m_i·m_j·(N − m_i − m_j)。
 * 答案 = U − Σ枢轴 − Σ反向。
 *
 * 与精确叉积判定（全点三重枚举）在 r = 2, 3, 5, 6, 7 上逐一对拍一致
 * （含题面给出的 8、360、10600 三个锚点）。
 *
 * 运行：kotlinc solution.kt -include-runtime -d solution.jar && java -jar solution.jar
 */
private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

/** 极角半平面序：下半平面（含正 x 轴）在前 */
private fun half(x: Int, y: Int): Int = if (y > 0 || (y == 0 && x > 0)) 0 else 1

fun solve184(r: Int): Long {
    // 1) 按本原方向分组
    val rayMap = HashMap<Long, Long>()
    for (x in -r + 1 until r) {
        for (y in -r + 1 until r) {
            val d2 = x * x + y * y
            if (d2 == 0 || d2 >= r * r) continue
            val g = gcd(Math.abs(x), Math.abs(y))
            val key = ((x / g).toLong() shl 32) or ((y / g).toLong() and 0xFFFFFFFFL)
            rayMap[key] = (rayMap[key] ?: 0L) + 1L
        }
    }
    val dirs = ArrayList<Pair<Int, Int>>(rayMap.size)
    val mult = ArrayList<Long>(rayMap.size)
    for ((k, v) in rayMap) {
        dirs.add(((k shr 32).toInt()) to (k and 0xFFFFFFFFL).toInt())
        mult.add(v)
    }
    val m = dirs.size
    val order = (0 until m).sortedWith(Comparator { i, j ->
        val (x1, y1) = dirs[i]
        val (x2, y2) = dirs[j]
        val h1 = half(x1, y1); val h2 = half(x2, y2)
        if (h1 != h2) h1 - h2
        else {
            val cr = x1.toLong() * y2 - y1.toLong() * x2
            if (cr > 0) -1 else if (cr < 0) 1 else 0
        }
    })
    val sx = IntArray(m); val sy = IntArray(m); val sm = LongArray(m)
    for (i in 0 until m) {
        val o = order[i]
        sx[i] = dirs[o].first; sy[i] = dirs[o].second; sm[i] = mult[o]
    }
    var N = 0L
    for (v in sm) N += v

    // 2) 枢轴计数：双指针维护「逆时针 180° 开弧」内的点数 S 与平方和 Q
    var pivot = 0L
    var j = 0
    var S = 0L
    var Q = 0L
    for (i in 0 until m) {
        if (j <= i) { j = i + 1; S = 0; Q = 0 }
        while (j < i + m) {
            val k = if (j < m) j else j - m
            val cr = sx[i].toLong() * sy[k] - sy[i].toLong() * sx[k]
            if (cr <= 0) break
            S += sm[k]; Q += sm[k] * sm[k]; j++
        }
        pivot += sm[i] * (S * S - Q) / 2
        val knext = if (i + 1 < m) i + 1 else i + 1 - m
        if (i + 1 < j) { S -= sm[knext]; Q -= sm[knext] * sm[knext] }
    }

    // 3) 反向射线对
    var antip = 0L
    for (i in 0 until m) {
        for (k in i + 1 until m) {
            if (sx[i] == -sx[k] && sy[i] == -sy[k]) {
                antip += sm[i] * sm[k] * (N - sm[i] - sm[k])
            }
        }
    }

    // 4) e3 与最终结果
    var p2 = 0L
    var p3 = 0L
    for (v in sm) { p2 += v * v; p3 += v * v * v }
    val e3 = (N * N * N - 3 * N * p2 + 2 * p3) / 6
    return e3 - pivot - antip
}

fun main() {
    // 题面锚点
    check(solve184(2) == 8L) { "r=2 should be 8" }
    check(solve184(3) == 360L) { "r=3 should be 360" }
    check(solve184(5) == 10600L) { "r=5 should be 10600" }
    val answer = solve184(105)
    println(answer)
}
