package dev.pekt.engine

/**
 * PE 252 — Convex Holes（凸空穴）：在 500 个给定点中，求以给定点为顶点、内部不含任何
 * 给定点的最大面积凸多边形（边界上允许有给定点）。
 *
 * 推导（与 content/problems/0252/solution.kt 一致）：
 *   取凸空穴的「字典序最小顶点」p（先比 y、再比 x），其余顶点都在 p 上方；以 p 做扇形剖分，
 *   顶点按绕 p 的极角严格递增记 v1..vk，则双倍面积
 *     Σ cross(v_t − p, v_{t+1} − p)，
 *   空穴合法 ⟺ 每个扇三角形 (p,v_t,v_{t+1}) 内部无点、且每条对角线 (p,v_t)（2 ≤ t ≤ k−1）
 *   上无点（对角线严格在多边形内部；首尾两条边 (p,v1)、(p,vk) 上的点是允许的）。
 *
 *   DP：F[b][c] = 凸链 p→…→b→c 的最大双倍面积。
 *     · 种子（三角形 p,b,c）：F = cross(b−p,c−p) > 0，且三角形内部无点；
 *     · 转移：F[b][c] = F[a][b] + cross(b−p,c−p)，要求 orient(a,b,c) > 0（b 处左转）、
 *       线段 (p,b) 内部无点、三角形 (p,b,c) 内部无点；
 *     · 闭合：orient(b,c,p) > 0 时 F[b][c] 即一个完整凸空穴的双倍面积。
 *
 *   两处判空/取最大 O(1) 化：
 *     · 三角形 (p,b,c) 判空：夹在 θ_b、θ_c 之间、从 b 看方向最逆时针的点 m 满足
 *       cross(m−b,c−b) ≥ 0 即为空；与 p 同极角的点整组折叠（它们在射线 p→b 上，是边界点）。
 *     · 转移里 max over a（约束 orient(a,b,c) > 0）：固定 b 后按绕 b 方向排序，合法 a 是
 *       前缀；每个点作为中心的方向序只预处理一次（O(n² log n)），扫描时过滤候选 + 双指针。
 *
 * 复杂度：O(n³) 时间、O(n²) 空间；n = 500 本机 JIT 预热后约 0.6 s（10 s 熔断线内）。
 * 答案：面积 104924.0 → 双倍面积 209848，编码 round(面积×10⁸) = 双倍面积 × 5×10⁷
 *       = 10492400000000（与 freeCodeCamp 题库的公开断言值 104924 一致）。
 */
internal fun solve0252Impl(): Long {
    val (xs, ys) = pe252Points(500)
    val doubled = Pe252Solver(xs, ys).solve()
    check(doubled == 209_848L) { "PE252 期望双倍面积 209848，实得 $doubled" }
    return doubled * 50_000_000L
}

/** S₀=290797，Sₙ₊₁=Sₙ² mod 50515093，Tₙ=(Sₙ mod 2000)−1000；点 (T₂ₖ₋₁, T₂ₖ)。 */
private fun pe252Points(count: Int): Pair<IntArray, IntArray> {
    val xs = IntArray(count)
    val ys = IntArray(count)
    var s = 290797L
    for (k in 0 until count) {
        s = s * s % 50_515_093L
        xs[k] = (s % 2000L).toInt() - 1000
        s = s * s % 50_515_093L
        ys[k] = (s % 2000L).toInt() - 1000
    }
    // 题面说「点集」：同坐标只保留一个点（本数据集无重复，去重是防御性的）
    val seen = HashSet<Long>()
    var k = 0
    for (i in 0 until count) {
        val key = xs[i].toLong() shl 32 or (ys[i].toLong() and 0xFFFFFFFFL)
        if (seen.add(key)) {
            xs[k] = xs[i]
            ys[k] = ys[i]
            k++
        }
    }
    return Pair(xs.copyOf(k), ys.copyOf(k))
}

private class Pe252Solver(private val xs: IntArray, private val ys: IntArray) {
    private val n = xs.size
    private val dirOrder = Array(n) { IntArray(if (n > 1) n - 1 else 0) }
    private val dirPos = Array(n) { IntArray(n) }
    private val rank = IntArray(n) { -1 }
    private val dp = LongArray(n * n)
    private val cand = IntArray(n)
    private val aList = IntArray(n)
    private val cList = IntArray(n)
    private val triOk = BooleanArray(n)
    private val segEmpty = BooleanArray(n)
    private val mergeBuf = IntArray(n)
    private var best = 0L

    init {
        for (c in 0 until n) {
            val ord = dirOrder[c]
            var k = 0
            for (x in 0 until n) if (x != c) ord[k++] = x
            sortByAngle(ord, k, xs[c], ys[c], false)
            dirPos[c][c] = -1
            for (i in 0 until k) dirPos[c][ord[i]] = i
        }
    }

    fun solve(): Long {
        best = 0L
        for (p in 0 until n) solveAnchor(p)
        return best
    }

    /** 绕 (cx,cy) 的极角升序（精确整数比较）；tieDist 时同角按距离升序。 */
    private fun lessIdx(a: Int, b: Int, cx: Int, cy: Int, tieDist: Boolean): Boolean {
        val ax = xs[a] - cx
        val ay = ys[a] - cy
        val bx = xs[b] - cx
        val by = ys[b] - cy
        val ha = if (ay > 0 || (ay == 0 && ax > 0)) 0 else 1
        val hb = if (by > 0 || (by == 0 && bx > 0)) 0 else 1
        if (ha != hb) return ha < hb
        val cr = ax.toLong() * by - ay.toLong() * bx
        if (cr != 0L) return cr > 0L
        if (!tieDist) return a < b
        val da = ax.toLong() * ax + ay.toLong() * ay
        val db = bx.toLong() * bx + by.toLong() * by
        if (da != db) return da < db
        return a < b
    }

    private fun sortByAngle(a: IntArray, count: Int, cx: Int, cy: Int, tieDist: Boolean) {
        if (count < 2) return
        mergeSort(a, 0, count, cx, cy, tieDist)
    }

    private fun mergeSort(a: IntArray, lo: Int, hi: Int, cx: Int, cy: Int, tieDist: Boolean) {
        if (hi - lo < 2) return
        val mid = (lo + hi) ushr 1
        mergeSort(a, lo, mid, cx, cy, tieDist)
        mergeSort(a, mid, hi, cx, cy, tieDist)
        var i = lo
        var j = mid
        var k = lo
        while (i < mid && j < hi) {
            mergeBuf[k++] = if (lessIdx(a[i], a[j], cx, cy, tieDist)) a[i++] else a[j++]
        }
        while (i < mid) mergeBuf[k++] = a[i++]
        while (j < hi) mergeBuf[k++] = a[j++]
        for (t in lo until hi) a[t] = mergeBuf[t]
    }

    private fun solveAnchor(p: Int) {
        val px = xs[p]
        val py = ys[p]
        rank[p] = -1
        var m = 0
        for (x in 0 until n) {
            if (x == p) continue
            val dy = ys[x] - py
            if (dy > 0 || (dy == 0 && xs[x] > px)) cand[m++] = x
        }
        if (m < 2) return
        sortByAngle(cand, m, px, py, true)
        for (i in 0 until m) rank[cand[i]] = i
        for (i in 0 until m) {
            val c = cand[i]
            segEmpty[c] = if (i == 0) true else {
                val b = cand[i - 1]
                (xs[b] - px).toLong() * (ys[c] - py) - (ys[b] - py).toLong() * (xs[c] - px) != 0L
            }
        }
        for (i in 0 until m) {
            val ci = cand[i]
            for (j in i + 1 until m) dp[ci * n + cand[j]] = -1L
        }
        for (bi in 0 until m) {
            val b = cand[bi]
            val bx = xs[b]
            val by = ys[b]
            // 第一遍：三角判空（同角点整组折叠）+ 种子
            var mPhi = -1
            var mPhiX = 0
            var mPhiY = 0
            var gi = bi + 1
            while (gi < m) {
                var gj = gi + 1
                val gx = xs[cand[gi]] - px
                val gy = ys[cand[gi]] - py
                while (gj < m) {
                    val dx = xs[cand[gj]] - px
                    val dy = ys[cand[gj]] - py
                    if (dx.toLong() * gy - dy.toLong() * gx != 0L) break
                    gj++
                }
                for (ci in gi until gj) {
                    val c = cand[ci]
                    val ok = if (mPhi < 0) true else {
                        (mPhiX - bx).toLong() * (ys[c] - by) - (mPhiY - by).toLong() * (xs[c] - bx) >= 0L
                    }
                    triOk[c] = ok
                    if (ok) {
                        val turn = (bx - px).toLong() * (ys[c] - py) - (by - py).toLong() * (xs[c] - px)
                        if (turn > 0L) dp[b * n + c] = turn
                    }
                }
                val isBGroup = gi == bi + 1 &&
                    (xs[cand[bi]] - px).toLong() * (ys[cand[gi]] - py) -
                    (ys[cand[bi]] - py).toLong() * (xs[cand[gi]] - px) == 0L
                if (!isBGroup) {
                    for (ci in gi until gj) {
                        val x = cand[ci]
                        val cr = if (mPhi < 0) Long.MAX_VALUE else
                            (mPhiX - bx).toLong() * (ys[x] - by) - (mPhiY - by).toLong() * (xs[x] - bx)
                        if (mPhi < 0 || cr > 0L) {
                            mPhi = x
                            mPhiX = xs[x]
                            mPhiY = ys[x]
                        }
                    }
                }
                gi = gj
            }
            // 第二遍：转移（绕 b 的方向序扫描 + 双指针前缀最大值）
            if (segEmpty[b]) {
                val ord = dirOrder[b]
                val posP = dirPos[b][p]
                val deg = n - 1
                var cntA = 0
                var cntC = 0
                var idx = posP
                for (s in 1 until n) {
                    idx++
                    if (idx >= deg) idx -= deg
                    val x = ord[idx]
                    val rx = rank[x]
                    if (rx < 0) continue
                    if (rx < bi) aList[cntA++] = x else if (rx > bi) cList[cntC++] = x
                }
                var ptr = 0
                var bestAVal = -1L
                for (t in 0 until cntC) {
                    val c = cList[t]
                    val cbx = (xs[c] - bx).toLong()
                    val cby = (ys[c] - by).toLong()
                    while (ptr < cntA) {
                        val a = aList[ptr]
                        if (cbx * (ys[a] - by) - cby * (xs[a] - bx) > 0L) {
                            val v = dp[a * n + b]
                            if (v > bestAVal) bestAVal = v
                            ptr++
                        } else {
                            break
                        }
                    }
                    if (bestAVal >= 0L && triOk[c]) {
                        val turn = (bx - px).toLong() * (ys[c] - py) - (by - py).toLong() * (xs[c] - px)
                        if (turn > 0L) {
                            val v = bestAVal + turn
                            if (v > dp[b * n + c]) dp[b * n + c] = v
                        }
                    }
                }
            }
            // 第三遍：闭合
            for (ci in bi + 1 until m) {
                val c = cand[ci]
                val v = dp[b * n + c]
                if (v > best) {
                    val cr = (xs[c] - bx).toLong() * (py - by) - (ys[c] - by).toLong() * (px - bx)
                    if (cr > 0L) best = v
                }
            }
        }
        for (i in 0 until m) rank[cand[i]] = -1
    }
}
